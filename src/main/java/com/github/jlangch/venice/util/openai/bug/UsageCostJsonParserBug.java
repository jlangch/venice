/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.util.openai.bug;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.admin.organization.usage.UsageCostsParams;
import com.openai.models.admin.organization.usage.UsageCostsResponse;
import com.openai.models.admin.organization.usage.UsageCostsResponse.Data.Result.OrganizationCostsResult;


/**
 * Demonstrates the openai-java <code>OrganizationCostsResult</code> value parser bug
 *
 * <p>Depends on OpenAI:  com.openai:openai-java:4.42.0
 *
 * <p>Call from Venice REPL:
 *
 * <pre>
 * ;; workaround: false
 * (. :com.github.jlangch.venice.util.openai.bug.UsageCostJsonParserBug :run false)
 * </pre>
 */
public class UsageCostJsonParserBug {

    private UsageCostJsonParserBug() {}

    public static void main(String[] args) {
        run(true);
    }

    public static void run(final boolean workaround) {
        final OpenAIClient client = OpenAIOkHttpClient
                                        .builder()
                                        .adminApiKey(System.getenv("OPENAI_ADMIN_KEY"))
                                        .build();

        final long startTime = LocalDate
                                .now(ZoneOffset.UTC)
                                .minusDays(10)
                                .atStartOfDay(ZoneOffset.UTC)
                                .toEpochSecond();

        final long endTime = Instant.now().getEpochSecond();

        final UsageCostsParams params = UsageCostsParams
                                            .builder()
                                            .startTime(startTime)
                                            .endTime(endTime)
                                            .bucketWidth(UsageCostsParams.BucketWidth._1D)
                                            .addGroupBy(UsageCostsParams.GroupBy.PROJECT_ID)
                                            .addGroupBy(UsageCostsParams.GroupBy.API_KEY_ID)
                                            .addGroupBy(UsageCostsParams.GroupBy.LINE_ITEM)
                                            .limit(180)
                                            .build();

        final UsageCostsResponse response = client
                                                .admin()
                                                .organization()
                                                .usage()
                                                .costs(params);

        response.data().forEach(bucket -> {
            final LocalDateTime bucketStart = toLocalDateTime(bucket.startTime());
            final LocalDateTime bucketEnd = toLocalDateTime(bucket.endTime());

            bucket.results().forEach(result -> {
                if (!result.isOrganizationCosts()) {
                    return;
                }

                final OrganizationCostsResult costs = result.asOrganizationCosts();

                try {
                    final double value;

                    if (!workaround) {
                        // The OpenAI value Json number parser throws an exception
                        value = costs.amount()
                                     .flatMap(amount -> amount.value())  // <== exception
                                     .orElse(0.0);
                    }
                    else {
                        // This custom JsonField number parser works fine
                        value = Double.parseDouble(
                                    costs.amount().isPresent()
                                        ? costs.amount().get()._value().asString().orElse("0.0")
                                        : "0.0");
                    }

                    final String currency = costs.amount()
                                                .flatMap(amount -> amount.currency())
                                                .orElse("unknown");

                    final String lineItem = costs.lineItem().orElse("-");

                    System.out.println(String.format(
                            "%s – %s | %.6f %s | %s",
                            bucketStart,
                            bucketEnd,
                            value,
                            currency,
                            lineItem));
                }
                catch(Exception ex) {
                    /*
                       Exception:

                       com.openai.errors.OpenAIInvalidDataException: `value` is invalid, received 0E-6176
                          at com.openai.core.JsonField.getOptional$openai_java_core(Values.kt:191)
                          at com.openai.models.admin.organization.usage.UsageCostsResponse$Data$Result$OrganizationCostsResult$Amount.value(UsageCostsResponse.kt:6680)

                       Some exception messages:

                          `value` is invalid, received 0E-6176
                          `value` is invalid, received 0.003627500000000000000000000000000000
                          `value` is invalid, received 0.007605000000000000000000000000000000


                        Exception in thread "main" com.openai.errors.OpenAIInvalidDataException: `value` is invalid, received 0E-6176
                            at com.openai.core.JsonField.getOptional$openai_java_core(Values.kt:191)
                            at com.openai.models.admin.organization.usage.UsageCostsResponse$Data$Result$OrganizationCostsResult$Amount.value(UsageCostsResponse.kt:6680)
                            at com.github.jlangch.venice.util.openai.bugs.UsageCostJsonParserBug.lambda$2(UsageCostJsonParserBug.java:72)
                            at java.util.Optional.flatMap(Optional.java:241)
                            at com.github.jlangch.venice.util.openai.bugs.UsageCostJsonParserBug.lambda$1(UsageCostJsonParserBug.java:72)
                            at java.util.ArrayList.forEach(ArrayList.java:1259)
                            at com.github.jlangch.venice.util.openai.bugs.UsageCostJsonParserBug.lambda$0(UsageCostJsonParserBug.java:59)
                            at java.util.ArrayList.forEach(ArrayList.java:1259)
                            at com.github.jlangch.venice.util.openai.bugs.UsageCostJsonParserBug.main(UsageCostJsonParserBug.java:55)
                     */
                    System.out.println("Exception: " + ex.getMessage());

                    throw ex;
                }
            });
        });
    }


    private static LocalDateTime toLocalDateTime(final long epochSeconds) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(epochSeconds),
                ZoneOffset.UTC);
    }

}
