# Parsatron Parser Combinator

*Parsatron* is a port of Nate Young's 
Clojure [parser combinator library](https://github.com/youngnh/parsatron) 
project. 

See [A Guide to the Parsatron](https://github.com/sjl/parsatron/blob/docs/docs/guide.markdown)


## Example

Parsatron expression evaluator example

The expression evaluator evaluates expressions like `"(3 + 4) * 5"`. It supports the math operators `+`, `-`, `*`, and `/`, `long` and `double` numbers, and the parenthesis `(` and `)`.

The evaluator uses two Parsatron parsers. The up-front tokenizing parser operates on a string (stream of characters) and returns a list of tokens. The expression parser operates on a stream of tokens and returns a number.


**Usage**

[1] Load the expression parser below in a REPL

[2] Test the tokenizer:

```clojure
 (tokenize "3 + 4.2")  ; => [[:int "3" (1,1)] [:op "+" (1,3)] [:float "4.2" (1,5)]]
```

[3] Test the expression parser:

```clojure
(evaluate "3 + 4.1 - 5 * 3.2")     ; => -8.9
(evaluate "3 + (4.1 - 5) * 3.2")   ; => 0.11999999999999877
```

**Expression Parser**

```clojure
(do
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
  ;;; Integer             = Digit { Digit } ;
  ;;; Float               = Integer "." { Digit };
  ;;;
  ;;; Token               = Whitespace | Operation | ParOpen | ParClose | Integer | Float ;
  ;;; Tokens              = { Token } EOI ;
  ;;;
  ;;;
  ;;; [2] Expression Parser
  ;;; ----------------------------------------------------------------------------
  ;;;
  ;;; Literal             = Integer | Float ;
  ;;;
  ;;; Main                = Expression EOI ;
  ;;; Expression          = AddExpression ;
  ;;; AddExpression       = MulExpression { ( "+" | "-" ) MulExpression } ;
  ;;; MulExpression       = UnaryExpression { ( "*" | "/" ) UnaryExpression } ;
  ;;; UnaryExpression     = ( "+" | "-" ) UnaryExpression | ParExpression | Literal ;
  ;;; ParExpression       = "(" Expression ")" ;


  (load-module :parsatron)
  (ns-alias 'p 'parsatron)


  ;;; ----------------------------------------------------------------------------
  ;;; Token
  ;;; ----------------------------------------------------------------------------

  (deftype :Token [type :keyword, val :string, line :long, column :long]
    Object
      (toString [this] (str/format "[%s %s (%d,%d)]"
                                   (pr-str (:type this))
                                   (pr-str (:val this))
                                   (:line this)
                                   (:column this)))
    p/SourcePosition
      (line [this] (:line this))
      (column [this] (:column this)))

  (defn token-type? [token type]
    (= type (:type token)))

  (defn token-value? [token value]
    (= value (:val token)))

  (defn token? [token type value]
    (and (= type (:type token)) (= value (:val token))))


  ;;; ----------------------------------------------------------------------------
  ;;; Tokenizer
  ;;; ----------------------------------------------------------------------------

  (p/defparser ws []
    (p/let->> [[l c] (p/pos)
               t     (p/many1 (p/any-char-of " \t\n"))]
       (p/always (Token. :whitespace (apply str t) l c))))

  (p/defparser operator []
    (p/let->> [[l c] (p/pos)
               t     (p/any-char-of "+-*/")]
       (p/always (Token. :op (str t) l c))))

  (p/defparser lparen []
    (p/let->> [[l c] (p/pos)
               t     (p/choice (p/char #\lparen))]
       (p/always (Token. :lparen (str t) l c))))

  (p/defparser rparen []
    (p/let->> [[l c] (p/pos)
               t     (p/choice (p/char #\rparen))]
       (p/always (Token. :rparen (str t) l c))))

  (p/defparser integer []
    (p/let->> [[l c] (p/pos)
               i     (p/many1 (p/digit))]
       (p/always (Token. :int (apply str i) l c))))

  (p/defparser float []
    (p/attempt (p/let->> [[l c] (p/pos)
                          i     (p/many1 (p/digit))
                          d     (p/char #\.)
                          f     (p/many1 (p/digit))]
                  (p/always (Token. :float
                                    (apply str (flatten (list i d f)))
                                    l c)))))

  (p/defparser token []
    (p/let->> [_  (p/many (ws)) ; skip whitespace token
               t  (p/choice (operator) (lparen) (rparen) (float) (integer))]
       (p/always t)))

  (p/defparser tokens []
    (p/let->> [t (p/many (token))
               _ (p/eof)]
       (p/always t)))

  (defn tokenize [e]
    (p/run (tokens) (str/trim-right e)))



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
    (p/token #(token? % :op sym)))

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

  (defn evaluate [e]
    (p/run (expr) (tokenize e)))
)
```


