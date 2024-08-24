/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.IPreCompiled;
import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.javainterop.SandboxRules;


public class Embed_15_RuleEngine {

    public static void main(final String[] args) {
        try {
            run();
            System.exit(0);
        }
        catch(VncException ex) {
            ex.printVeniceStackTrace();
            System.exit(1);
        }
        catch(RuntimeException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void run() {
        // Setup services
        final Configuration config = new Configuration();
        config.setValue(
                // id
                "rules.cart.discount",
                // script
                "(do                                                            \n" +
                "  (defn calculate [cart coupon]                                \n" +
                "    (case coupon                                               \n" +
                "      ;; \"10% Off Coupon\"                                    \n" +
                "      \"SUMMER10\"    { :discount  10.0, :freeship  false }    \n" +
                "                                                               \n" +
                "      ;; \"Free Shipping Coupon\"                              \n" +
                "      \"FREESHIP\"    { :discount  0.0, :freeship  true }      \n" +
                "                                                               \n" +
                "      ;; \"Buy Many Get 30% Off\"                              \n" +
                "      \"BMGOF\"       (if (>= (. cart :getItemCount) 2)        \n" +
                "                        { :discount 30.0, :freeship false }    \n" +
                "                        { :discount 0.0, :freeship false })    \n" +
                "                                                               \n" +
                "      { :discount  0.0, :freeship  false } ))                  \n" +
                "                                                               \n" +
                "  (calculate cart coupon))                                     ");

        // create a cart with a few items
        final Cart cart = new Cart();
        cart.addIem(new CartItem(new Product("Bottle of water", 1.4), 1));

        // rules
        final DiscountRules rules = new DiscountRules(config);

        // Check
        System.out.println("Discount #1: " + rules.calculate(cart, "SUMMER10"));
        System.out.println("Discount #2: " + rules.calculate(cart, "FREESHIP"));
        System.out.println("Discount #3: " + rules.calculate(cart, "BMGOF"));
        System.out.println("Discount #4: " + rules.calculate(cart, null));
    }



    public static class Configuration {
        public Configuration() {
        }

        public String getValue(final String key) {
            return config.get(key);
        }
        public void setValue(final String key, final String value) {
            config.put(key, value);
        }

        private final HashMap<String,String> config = new HashMap<>();
    }

    public static class Product {
        public Product(final String name, final double price) {
            this.name = name;
            this.price = price;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }

        private final String name;
        private final double price;
    }

    public static class CartItem {
        public CartItem(final Product product, final int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }

        private final Product product;
        private final int quantity;
    }

    public static class Cart {
        public Cart() {
        }

        public void addIem(final CartItem item) {
            items.add(item);
        }

        public int getItemCount() {
            return items.size();
        }

        public List<CartItem> getItems() {
            return Collections.unmodifiableList(items);
        }

        private final List<CartItem> items = new ArrayList<>();
    }

    public static class Discount {
        public Discount(final double discounPercentage, final boolean freeshipping) {
            this.discounPercentage = discounPercentage;
            this.freeshipping = freeshipping;
        }

        public double getDiscounPercentage() { return discounPercentage; }
        public boolean isFreeshipping() { return freeshipping; }

        @Override
        public String toString() {
            return String.format(
                    "discount: %.1f, free shipping: %b",
                    discounPercentage,
                    freeshipping);
        }

        private final double discounPercentage;
        private final boolean freeshipping;
    }

    public static class DiscountRules {
        public DiscountRules(final Configuration config) {
            // depending on the security requirements, it might by necessary
            // to add a sandbox (SandboxInterceptor) to limit what the
            // extension point script is allowed to do!
            this.venice = new Venice(new SandboxRules()
                                            .rejectAllUnsafeFunctions()
                                            .withClasses("com.github.jlangch.venice.examples.*:*")
                                            .whitelistVeniceFunctions(".")
                                            .sandbox());
            this.rule = compileRule(this.venice, config);
        }

        public Discount calculate(final Cart cart, final String coupon) {
            @SuppressWarnings("unchecked")
            final Map<String,Object> event = (Map<String,Object>)venice.eval(
                                                rule,
                                                Parameters.of("cart", cart,
                                                              "coupon", coupon));

            return new Discount(
                         (double)event.get("discount"),
                         (boolean)event.get("freeship"));
        }

        private static IPreCompiled compileRule(
                final Venice venice,
                final Configuration config
        ) {
            return venice.precompile(
                      "rule",
                      config.getValue("rules.cart.discount"),
                      true);
        }

        private final Venice venice;
        private final IPreCompiled rule;
    }
}
