# Parsatron Parser Combinator

*Parsatron* is a port of Nate Young's 
Clojure [parser combinator library](https://github.com/youngnh/parsatron) 
project. 

See [A Guide to the Parsatron](https://github.com/sjl/parsatron/blob/docs/docs/guide.markdown)


## Example

Parsatron expression evaluator example

The expression evaluator evaluates expressions like `"3 + (4 * 5)"`. It
supports the math operators `+`, `-`, `*`, and `/`, integer and float
numbers, and parenthesis.

The evaluator uses two Parsatron parsers. A tokenzing parsers splits the
expression strings into tokens that are feeded into the expression parser.

**Usage**

[1] Start a REPL and load the expression parser script:

```clojure
(load-file "path-to-venice/doc/examples/scripts/expr-parser.venice")
```

[2] Test the tokenizer:

```clojure
 (p/run (tokens) "3 + 4.2")
 ; => ([:int "3"] [:operator "+"] [:float "4.2"] )
```

[3] Test the expression parser:

```clojure
(->> (p/run (tokens) "3 + 4.1 - 5 * 3.2")
     (p/run (expr))
; => -8.9
```

```clojure
(->> (p/run (tokens) "3 + (4.1 - 5) * 3.2")
     (p/run (expr))
; => 0.11999999999999877
```

**Expression Parser**

```clojure
;;; ----------------------------------------------------------------------------
;;; EBNF
;;; ----------------------------------------------------------------------------
;;;
;;; [1] Tokenizer
;;; ----------------------------------------------------------------------------
;;; Whitespace          = " " | "\t" | "\n" ;
;;; Operation           = "+" | "-" | "*" | "/" ;
;;; LParen              = "(" ;
;;; RParen              = ")" ;
;;; Digit               = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
;;; Digit_1to9          = "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
;;; Integer             = "0" | Digit_1to9 [ Digit ]* ;
;;; Float               = Integer "." { Digit };
;;;
;;; Token               = Whitespace | Operation | ParOpen | ParClose | Integer | Float ;
;;; Tokens              = { Token } EOI ;
;;;
;;;
;;; [2] Expression Parser
;;; ----------------------------------------------------------------------------
;;;
;;; Literal             =  Integer | Float ;
;;;
;;; Main                =  Expression EOI ;
;;; Expression          =  AddExpression ;
;;; AddExpression       =  MulExpression { ( "+" | "-" ) MulExpression } ;
;;; MulExpression       =  UnaryExpression { ( "*" | "/" ) UnaryExpression } ;
;;; UnaryExpression     =  ( "+" | "-" ) UnaryExpression | ParExpression | Literal ;
;;; ParExpression       =  "(" Expression ")" ;


(load-module :parsatron)
(ns-alias 'p 'parsatron)


;;; ----------------------------------------------------------------------------
;;; Token
;;; ----------------------------------------------------------------------------

(deftype :Token [type :keyword, val :string]
  Object
    (toString [this] (str/format "[%s %s]"
                                 (pr-str (:type this))
                                 (pr-str (:val this)))))

(defn token-type? [token type]
  (= type (:type token)))

(defn token-value? [token value]
  (= value (:val token)))

(defn token? [token type value]
  (and (= type (:type token)) (= value (:val token))))

(defn remove-whitespaces [tokens]
  (filter #(not (token-type? % :whitespace)) tokens))


;;; ----------------------------------------------------------------------------
;;; Tokenizer
;;; ----------------------------------------------------------------------------

(p/defparser ws []
  (p/let->> [t (p/choice (p/char #\space) (p/char #\newline) (p/char #\tab))]
     (p/always (Token. :whitespace (str t)))))

(p/defparser operator []
  (p/let->> [t (p/choice (p/char #\+) (p/char #\-) (p/char #\*) (p/char #\/))]
     (p/always (Token. :operator (str t)))))

(p/defparser lparen []
  (p/let->> [t (p/choice (p/char #\lparen))]
     (p/always (Token. :lparen (str t)))))

(p/defparser rparen []
  (p/let->> [t (p/choice (p/char #\rparen))]
     (p/always (Token. :rparen (str t)))))

(p/defparser digit-1to9 []
  (p/choice (p/char #\1) (p/char #\2) (p/char #\3)
            (p/char #\4) (p/char #\5) (p/char #\6)
            (p/char #\7) (p/char #\8) (p/char #\9)))

(p/defparser integer []
  (p/attempt (p/either (p/let->> [_ (p/char #\0)]
                          (p/always (Token. :int "0")))
                       (p/let->> [f (digit-1to9)
                                  r (p/many (p/digit))]
                          (p/always (Token. :int (apply str (list* f r))))))))

(p/defparser float []
  (p/attempt (p/let->> [i-tok (integer)
                        _     (p/char #\.)
                        f     (p/many1 (p/digit))]
                (let [i (:val i-tok)]
                  (p/always (Token. :float
                                    (apply str (flatten (list* i #\. f)))))))))

(p/defparser token []
  (p/choice (ws) (operator) (lparen) (rparen) (float) (integer)))

(p/defparser tokens []
  (p/let->> [toks (p/many (token))
             _    (p/eof)]
     (p/always (remove-whitespaces (list* toks)))))


;;; ----------------------------------------------------------------------------
;;; Expression Parser
;;; ----------------------------------------------------------------------------

(defn chained-math [seed-val tuples]
  ;; (chained-math 1 [["+" 6] ["-" 4]]) ; => (1 + 6 - 4) => 3
  (reduce (fn [acc t] (let [op (resolve (symbol (first t)))]
                        (op acc (second t))))
          seed-val
          tuples))

(defn op [sym]
  (p/token #(token? % :operator sym)))

(defn l-paren []
  (p/token #(token-type? % :lparen)))

(defn r-paren []
  (p/token #(token-type? % :rparen)))

(p/defparser nint []
  (p/let->> [i (p/token #(token-type? % :int))]
     (p/always (long (:val i)))))

(p/defparser nfloat []
 (p/let->> [i (p/token #(token-type? % :float))]
    (p/always (double (:val i)))))

(p/defparser number []
  (p/either (nint) (nfloat)))


(p/defparser expr [] (add-expr))

(p/defparser add-expr []
  (p/let->> [seed   (mul-expr)
             tuples (p/many (p/let->> [op_ (p/either (op "+") (op "-"))
                                       val (mul-expr)]
                               (p/always [(:val op_) val])))]
     (p/always (chained-math seed tuples))))

(p/defparser mul-expr []
  ;; "3 * 4", "-3 * 4", "3 * 4 * 5"
  (p/let->> [seed   (unary-expr)
             tuples (p/many (p/let->> [op_ (p/either (op "*") (op "/"))
                                       val (unary-expr)]
                               (p/always [(:val op_) val])))]
     (p/always (chained-math seed tuples))))

(p/defparser unary-expr []
  ;; "3", "+3", "-3"
  (p/choice (p/let->> [op_ (p/either (op "+") (op "-"))
                       val (unary-expr)]
               (p/always (if (token-value? op_ "+") val (negate val))))
            (par-expr)
            (number)))

(p/defparser par-expr []
  (p/between (l-paren) (r-paren) (expr)))
```


