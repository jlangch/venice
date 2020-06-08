# Custom Datatypes

Venice is dynamically typed and provides a rich set of datatypes. But with domain
modelling primitive types like longs, strings, and maps are not as expressive as 
we're used with languages that have a type system.

Even though Venice will never progess into a statically typed language, the ability
to define custom types will make a domain model more expressive. 

Venice is validating the instantiation of the custom types by checking the values 
for type compliance. With value immutability the instantiated values are guaranteed
to be valid for their lifetime.

Venice custom types are composed from 'smaller' types:

- Composing types with "AND" (records)
- Composing types with "OR" (choices)
- Wrapper types (constraints)

Custom types can be defined with an optional validation function that validates
the instantiation of the type.



## Composing types with "AND"


signature: `(deftype name fields)` 

```clojure
(do
  (ns foo)
  
  (deftype :complex [real      :long
                     imaginary :long]) 

  ; Venice implicitly creates a builder function suffixed with a '.'
  (def y (complex. 200 300))

  ; ... and a type check function 
  (complex? y)
  
  y  ;; => #:foo/complex{:real 200 :imaginary 300}
)
```

```clojure
do
  (ns foo)
  
  (deftype :named [name :string, value :any]) 
  
  (def x (named. "count" 200))
  (def y (named. "seq" [1 2]))
  
  [x y]
  
  ;;=> [#:foo/named{:name "count" :value 200} #:foo/named{:name "seq" :value [1 2]}]
)
```


signature: `(deftype name fields validator)`

```clojure
(do
  (ns foo)
  
  (deftype :complex
           [real :long, imaginary :long]
           (fn [t]
              (assert (pos? (:real t)) "real must be positive")
              (assert (pos? (:imaginary t)) "imaginary must be positive")))

  (def x (complex. 100 200))
  
  [(:real x) (:imaginary x)]
  
  ;;=> [100 200]
)
```

## Composing types with "OR"


## Wrapper types




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

