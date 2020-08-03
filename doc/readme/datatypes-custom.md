# Custom Datatypes

Venice is dynamically typed and provides a rich set of datatypes. But with domain
modeling primitive types like longs, strings, and maps are not as expressive as 
we are used to with languages that allow you to define your own types.

Even though Venice will never progress into a statically typed language, the ability
to define custom types will make a domain model more expressive and type safe. 

Venice custom types are composed from 'smaller' types:

- Composing types with "AND" (records)
- Composing types with "OR" (choices)
- Wrapping types (constraints)

**Validation**

Custom types can be defined with an optional validation function that validates
the instantiation of a type. With value immutability the instantiated values are 
guaranteed to be valid for their lifetime.

Due to the dynamic nature of Venice, types cannot not be checked at Reader time and
function arguments are not type checked implicitly, if required so function
preconditions can be added for runtime checking. 




## Composing types with "AND"

A custom "AND" type defines a type composed of one or multiple simpler types.


### Define a custom type 

A custom "AND" type has a name for reference and at least one field. The type's name 
is defined as a keyword and the fields are tuples of name and type.


```clojure
(deftype :complex [real      :long
                   imaginary :long])
 ```
 
Venice implicitly creates a builder function suffixed with a '.'
  
```clojure
(complex. 200 300)
```

... and a type check function 

```clojure
(complex? (complex. 200 300))  ; => true
```

Custom "AND" types are implemented in terms of maps, so all map functions
can be applied:

 ```clojure
(def x (complex. 200 300))

(println (str/format "(re: %d, im: %d)" (:real x) (:imaginary x)))
```
 

The field type :any is representing any type:

```clojure
(do
  (deftype :named [name :string, value :any]) 
  
  (def x (named. "count" 200))
  (def y (named. "seq" [1 2])))
```


Modify fields with `assoc`:

```clojure
(do
  (deftype :complex [real :long, imaginary :long])
  (def x (complex. 100 200))
  (def y (assoc x :real 110))
  y)
```


Remove fields with `dissoc`:

```clojure
(do
  (deftype :complex [real :long, imaginary :long])
  (def x (complex. 100 200))
  (def y (dissoc x :real 110))
  y)
```

_Note:_ `dissoc` on custom types will turn the custom type back into a standard map because the 
resulting value will not comply with the custom type's rules anymore.


## Composing types with "OR"

A custom "OR" type defines a set of values for the type. The set of values 
can be composed of individual values and/or all values defined by a simpler 
type.


### Define a custom type 

```clojure

; individual keyword values 
(deftype-or :color :red :green :blue)

; individual string values 
(deftype-or :fruit "apple" "peach" "banana")

; individual integer values
(deftype-or :small-number 0 1 2 3 4 5 6 7 8 9)

; optional string type (all string values and nil)
(deftype-or :middle-name :string nil)

; all numbers defined by the primitive number types
(deftype-or :number :long :integer :double :decimal)

 ```

Venice implicitly creates a builder function suffixed with a dot
  
```clojure
(color. :blue)

(fruit. "apple")

(small-number. 6)

(middle-name. "John")
(middle-name. nil)

(number. 10)
(number. 10.4567M)
```

... and a type check function 

```clojure
(color? (color. :blue))  ; => true

(fruit? (fruit. "apple"))  ; => true
(string? (fruit. "apple"))  ; => true
```




## Wrapper types

Wrapper types give simpler types a name and can add a constraint 
on them. An eMail address is not just a string it's represented by
a string and has well defined constraints.


```clojure
(do
  ; the type :first-name is a string (values cannot be nil)
  (deftype-of :first-name :string)
  
  ; if a first name must not be empty a constraint can be added
  (deftype-of :first-name2 :string not-empty?)
  
  ; the constraint (validation) can also be written as
  (deftype-of :first-name3 :string #(assert (not-empty? %)))
  
  (def name (first-name. "John"))
  
  ; the value 'name' is of type :first-name and of type :string, so
  ; all string functions can be applied to it
  ;   (first-name? name) => true
  ;   (string? name) => true 
  (println (str/format "%s: length=%d" name (count name))))
```


```clojure
(do
  (deftype-of :email-address :string str/valid-email-addr?)
              
  (def email (email-address. "foo@foo.org"))
  
  (println email))
```



## Sample 'Payment' Domain Model

```clojure
(do
  (ns foo)

  ; ---------------------------------------------------------
  ; define the payment domain model
  ; ---------------------------------------------------------
  (deftype-of :check-number :integer)

  (deftype-of :card-number :string)

  (deftype-or :card-type :mastercard :visa)

  (deftype :credit-card [type    :card-type 
                         number  :card-number])

  (deftype :check [number :check-number])

  (deftype-of :payment-amount :decimal)

  (deftype-or :payment-currency :CHF :EUR)

  (deftype-or :payment-method :cash
                              :check
                              :credit-card)

  (deftype :payment [amount    :payment-amount
                     currency  :payment-currency
                     method    :payment-method ])


  ; ---------------------------------------------------------
  ; build a credit card payment
  ; ---------------------------------------------------------
  (def payment (payment.
                    (payment-amount. 2000.00M)
                    (payment-currency. :CHF)
                    (payment-method.
                         (credit-card.
                              (card-type. :mastercard)
                              (card-number. "0800-0000-0000-0000")))))

  ; ---------------------------------------------------------
  ; print the payment
  ; ---------------------------------------------------------
  (println (str/format "Payment: %s %s by %s '%s'"
                       (:amount payment)
                       (:currency payment)
                       (-> payment :method :type)
                       (-> payment :method :number)))
            
  ; => "Payment: 2000.00 CHF by mastercard '0800-0000-0000-0000'"  
)
```

