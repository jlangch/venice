# Parsifal Parser Combinator

*Parsifal* is a port of Nate Young's Parsatron
Clojure [parser combinator library](https://github.com/youngnh/parsatron) 
project. 

See [A Guide to Parsifal](ext-parsifal-guide.md)


## Simple Examples

### Parse a float number

```clojure
(do
  (load-module :parsifal ['parsifal :as 'p])
  
  (p/defparser float []
    (p/let->>* [i  (p/many1 (p/digit))
                d  (p/char ".")
                f  (p/many1 (p/digit))]
      (p/always (apply str (flatten (list i d f))))))
  
  (defn evaluate [expression]
    (p/run (p/let->> [f (float) 
                      _ (p/eof)]
             (p/always f)) 
           expression)))
```

```clojure
(evaluate "1.0")       ; => "1.0"
(evaluate "120.468")   ; => "120.468"
(evaluate "1.2---")    ; => ParseError: Expected end of input at line: 1 column: 4
(evaluate "abc")       ; => ParseError: Unexpected token 'a' at line: 1 column: 1
```

### Parse a float number in scientific notation

```clojure
(do
  (load-module :parsifal ['parsifal :as 'p])

  (p/defparser integer []
    (p/let->>* [i  (p/either (p/char "0") (p/any-char-of "123456789"))
                d  (p/many (p/digit))]
      (p/always (apply str (flatten (list i d))))))

  (p/defparser signed-integer []
    (p/either (p/let->>* [s  (p/any-char-of "-+")
                          i  (integer)]
                (p/always (str s i)))
               (integer)))
  
  (p/defparser mantissa []
    (p/let->>* [i  (signed-integer)
                d  (p/char ".")
                f  (p/many1 (p/digit))]
      (p/always (apply str (flatten (list i d f))))))
 
  (p/defparser exponent []
    (p/either (p/let->>* [s  (p/any-char-of "-+")
                          i  (p/many1 (p/digit))]
                (p/always (apply str (flatten (list s i)))))
              (p/let->>* [i  (p/many1 (p/digit))]
                (p/always (apply str i)))))

  (p/defparser float []
    (p/either (p/let->>* [m  (mantissa)
                          p  (p/any-char-of "Ee")
                          e  (exponent)]
                (p/always (str m p e)))
              (mantissa)))

  (defn evaluate [expression]
    (p/run (p/let->> [f (float) 
                      _ (p/eof)]
             (p/always f)) 
           expression)))
```

```clojure
(evaluate "1.0")          ; => "1.0"
(evaluate "-1.0")         ; => "-1.0"
(evaluate "+1.0")         ; => "+1.0"
(evaluate "120.468")      ; => "120.468"
(evaluate "120.468E3")    ; => "120.468E3"
(evaluate "-120.468E-3")  ; => "-120.468E-3"
(evaluate "-120.468E+3")  ; => "-120.468E+3"
```

### Parse a quoted string with escaped chars

```clojure
(do
  (load-module :parsifal ['parsifal :as 'p])

  (p/defparser escaped []
    (p/let->>* [e  (p/char #\backslash)
                c  (p/any-char-of "\\0nrvtbf\"")]
      (p/always (char-escaped c))))

  (p/defparser not-escaped []
    (p/none-char-of "\\\""))

  (p/defparser quoted-string []
    (p/between (p/char #\")
               (p/char #\")
               (p/many (p/either (escaped) (not-escaped)))))

  ;; (evaluate (apply str [#\" #\1 #\\ #\n #\2 #\"]))  -  "1\n2"
  ;; (evaluate (apply str [#\" #\1 #\\ #\f #\2 #\"]))  -  "1\f2"
  ;; (evaluate (apply str [#\" #\1 #\\ #\" #\2 #\"]))  -  "1\"2"
  (defn evaluate [expression]
    (p/run (quoted-string) expression)))
```

```clojure
(evaluate (apply str [#\" #\1 #\\ #\n #\2 #\"]))  ; => [#\1 #\newline #\2]
(evaluate (apply str [#\" #\1 #\\ #\f #\2 #\"]))  ; => [#\1 #\formfeed #\2]
(evaluate (apply str [#\" #\1 #\\ #\" #\2 #\"]))  ; => [#\1 #\" #\2]
```

## Expression evaluator 1

Parsifal expression evaluator example

The expression evaluator evaluates expressions like `"(3 + 4) * 5"`. It supports the math operators `+`, `-`, `*`, and `/`, `long` and `double` numbers, and the parenthesis `(` and `)`.

**Usage**

[1] Load the expression parser below in a REPL

[2] Test the expression parser:

```clojure
(evaluate "1")                    ; => 1
(evaluate "1 + 2")                ; => 3
(evaluate "1 + 2 * 3 + 4")        ; => 11
(evaluate "(1 + 2) * (3 + 4)")    ; => 21
(evaluate "3 + 4.1 - 5 * 3.2")    ; => -8.9
(evaluate "3 + (4.1 - 5) * 3.2")  ; => 0.11999999999999877
```

**Expression Parser**

```clojure
(do
  ;;; ----------------------------------------------------------------------------
  ;;; EBNF
  ;;; ----------------------------------------------------------------------------
  ;;;
  ;;; Whitespace      = " " | "\t" | "\r" | "\n" ;
  ;;; LParen          = "(" ;
  ;;; RParen          = ")" ;
  ;;; Digit           = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
  ;;; Integer         = Digit { Digit } ;
  ;;; Float           = Digit { Digit } "." Digit { Digit };
  ;;;
  ;;; Expr            = Term ( "+" | "-" ) Expr | Term ;
  ;;; Term            = Factor ( "*" | "/" ) Term | Factor ;
  ;;; Factor          = "(" Expr ")" | Float | Integer ;
  ;;;
  ;;; Main            = Expr EOI ;


  (load-module :parsifal ['parsifal :as 'p])


  (defn eval-op [op-name x y]
    ((resolve (symbol op-name)) x y))

  (p/defparser ws []
    (p/many (p/any-char-of " \t\r\n")))

  (p/defparser lparen []
    (p/>>* (ws) (p/char "(")))

  (p/defparser rparen []
    (p/>>* (ws) (p/char ")")))

  (p/defparser op [ch]
    (p/>>* (ws) (p/char ch)))

  (p/defparser int []
    (p/let->>* [_  (ws)
                i  (p/many1 (p/digit))]
      (p/always (long (apply str i)))))

  (p/defparser float []
    (p/let->>* [_  (ws)
                i  (p/many1 (p/digit))
                d  (p/char ".")
                f  (p/many1 (p/digit))]
      (p/always (double (apply str (flatten (list i d f)))))))

  (p/defparser expr []
    (p/either (p/let->>* [x   (term)
                          op  (p/either (op "+") (op "-"))
                          y   (expr)]
                (p/always (eval-op (str op) x y)))
              (term)))

  (p/defparser term []
    (p/either (p/let->>* [x   (factor)
                          op  (p/either (op "*") (op "/"))
                          y   (term)]
                (p/always (eval-op (str op) x y)))
              (factor)))

  (p/defparser factor []
    (p/choice (p/between (lparen) (rparen) (expr))
              (float)
              (int)))

  (p/defparser main []
    ;; 1) parse empty expressions:    ""           => OK, value => nil
    ;; 2) parse valid expressions:    "3 + 4"      => OK, value => 7
    ;; 3) parse left over tokens:     "(3 + 4) 9"  => ERR, Unexpected token '9'
    (p/either (p/eof)
              (p/let->> [e  (expr)
                         _  (ws)
                         t  (p/either (p/eof) (p/any))]
                 (if (nil? t)
                   (p/always e)
                   (p/never (str "Unexpected token '" t "'"))))))

  (defn evaluate [expression]
    (p/run (main) expression))
)
```


## Expression evaluator 2

Parsifal expression evaluator example with tokenizer and unary expressions

The expression evaluator evaluates expressions like `"(3 + 4) * 5"`. It supports unary expression like `-4`, the math operators `+`, `-`, `*`, `/`,  the number types `long` and `double`, and the parenthesis `(` and `)`.

The evaluator uses two Parsifal parsers. The up-front tokenizing parser operates on a string (stream of characters) and returns a list of tokens. The expression parser operates on a stream of tokens and returns a number.


**Usage**

[1] Load the expression parser below in a REPL

[2] Test the tokenizer:

```clojure
(tokenize "3 + 4.2")  ; => [[:int "3" (1,1)] [:op "+" (1,3)] [:float "4.2" (1,5)]]
```

[3] Test the expression parser:

```clojure
(evaluate "1")                    ; => 1
(evaluate "1 + 2")                ; => 3
(evaluate "1 + 2 * 3 + 4")        ; => 11
(evaluate "(1 + 2) * (3 + 4)")    ; => 21
(evaluate "3 + 4.1 - 5 * 3.2")    ; => -8.9
(evaluate "3 + (4.1 - 5) * 3.2")  ; => 0.11999999999999877
;; unary
(evaluate "-1")                   ; => -1
(evaluate "1 + -2")               ; => -1
(evaluate "1 + 2 * -3 + 4")       ; => -1
(evaluate "-(-1 + 2) * (-3 + 4)") ; => -1
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
  ;;; Whitespace      = " " | "\t" | "\r" | "\n" ;
  ;;; Operator        = "+" | "-" | "*" | "/" ;
  ;;; LParen          = "(" ;
  ;;; RParen          = ")" ;
  ;;; Digit           = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
  ;;; Integer         = Digit { Digit } ;
  ;;; Float           = Digit { Digit } "." Digit { Digit };
  ;;;
  ;;; Token           = Whitespace | Operator | LParen | RParen | Float | Integer ;
  ;;; Tokens          = { Token } EOI ;
  ;;;
  ;;;
  ;;; [2] Expression Parser
  ;;; ----------------------------------------------------------------------------
  ;;;
  ;;; Main            = Expression EOI;
  ;;; Expression      = AddExpression ;
  ;;; AddExpression   = MulExpression { ( "+" | "-" ) MulExpression } ;
  ;;; MulExpression   = UnaryExpression { ( "*" | "/" ) UnaryExpression } ;
  ;;; UnaryExpression = ( "+" | "-" ) UnaryExpression | ParExpression | Literal ;
  ;;; ParExpression   = "(" Expression ")" ;
  ;;; Literal         = Integer | Float ;


  (load-module :parsifal ['parsifal :as 'p])


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



  ;;; ----------------------------------------------------------------------------
  ;;; Tokenizer
  ;;; ----------------------------------------------------------------------------

  (p/defparser ws-tok []
    (p/let->> [[l c] (p/pos)
               t     (p/many1 (p/any-char-of " \t\r\n"))]
      (p/always (Token. :ws (apply str t) l c))))

  (p/defparser op-tok []
    (p/let->> [[l c] (p/pos)
               t     (p/any-char-of "+-*/")]
      (p/always (Token. :op (str t) l c))))

  (p/defparser lparen-tok []
    (p/let->> [[l c] (p/pos)
               t     (p/char #\lparen)]
      (p/always (Token. :lparen (str t) l c))))

  (p/defparser rparen-tok []
    (p/let->> [[l c] (p/pos)
               t     (p/char #\rparen)]
      (p/always (Token. :rparen (str t) l c))))

  (p/defparser int-tok []
    (p/let->> [[l c] (p/pos)
               i     (p/many1 (p/digit))]
      (p/always (Token. :int (apply str i) l c))))

  (p/defparser float-tok []
    (p/attempt (p/let->> [[l c] (p/pos)
                          i     (p/many1 (p/digit))
                          d     (p/char #\.)
                          f     (p/many1 (p/digit))]
                  (p/always (Token. :float
                                    (apply str (flatten (list i d f)))
                                    l c)))))

  (p/defparser unknown-tok []
    (p/let->> [[l c] (p/pos)
               s     (p/many1 (p/none-char-of " \t\r\n"))]
      (p/always (Token. :unknown (apply str s) l c))))

  (p/defparser token []
    (p/many (ws-tok))
    (p/choice (op-tok) 
              (lparen-tok) 
              (rparen-tok) 
              (float-tok) 
              (int-tok) 
              (unknown-tok)))

  (p/defparser tokens []
    (p/let->> [t (p/many (p/attempt (token)))
               _ (p/many (p/attempt (ws-tok)))
               _ (p/eof)]
      (p/always t)))

  (defn tokenize [expression]
    (p/run (tokens) expression))



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
    (p/token #(and (= :op (:type %)) (= sym (:val %)))))

  (defn lparen []
    (p/token #(= :lparen (:type %))))

  (defn rparen []
    (p/token #(= :rparen (:type %))))

  (p/defparser int []
    (p/let->> [i (p/token #(= :int (:type %)))]
       (p/always (long (:val i)))))

  (p/defparser float []
   (p/let->> [i (p/token #(= :float (:type %)))]
      (p/always (double (:val i)))))

  (p/defparser expr []
    ; no EOF handling in this parser! It's recursively called.
    (add-expr))

  (p/defparser add-expr []
    (p/let->> [seed   (mul-expr)
               tuples (p/many (p/let->> [opc (p/either (op "+") (op "-"))
                                         val (mul-expr)]
                                (p/always [(:val opc) val])))]
       (p/always (chained-math seed tuples))))

  (p/defparser mul-expr []
    (p/let->> [seed   (unary-expr)
               tuples (p/many (p/let->> [opc (p/either (op "*") (op "/"))
                                         val (unary-expr)]
                                (p/always [(:val opc) val])))]
       (p/always (chained-math seed tuples))))

  (p/defparser unary-expr []
    (p/choice (p/let->> [opc (p/either (op "+") (op "-"))
                         val (unary-expr)]
                 (p/always (if (= "+" (:val opc)) val (negate val))))
              (paren-expr)
              (float)
              (int)))

  (p/defparser paren-expr []
    (p/between (lparen) (rparen) (expr)))

  (p/defparser main []
    ;; 1) parse empty expressions:    ""           => OK, value => nil
    ;; 2) parse valid expressions:    "3 + 4"      => OK, value => 7
    ;; 3) parse left over tokens:     "(3 + 4) 9"  => ERR, Unexpected token '9'
    (p/either (p/eof)
              (p/let->> [e (expr)
                         t (p/either (p/eof) (p/any))]
                 (if (nil? t)
                   (p/always e)
                   (p/never (str "Unexpected token '" (:val t) "'"))))))

  (defn evaluate [expression]
    (p/run (main) (tokenize expression)))
)
```


