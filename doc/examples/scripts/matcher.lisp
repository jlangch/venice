;; https://www.jdoodle.com/execute-clisp-online/
;; https://www.tutorialspoint.com/execute_lisp_online.php

(defun variable-p (expr)
  (and (symbolp expr) (not (eq expr 'd))
    (let ((name (symbol-name expr)))
      (and (= (length name) 1) (alpha-char-p (char name 0))))))


(defun match-variable (var input bindings)
  (let ((binding (assoc var bindings)))
    (cond ((null binding) (cons (cons var input) bindings))
      ((equal input (cdr binding)) bindings))))


(defun match* (pattern input &optional (bindings '((dummy . dummy))))
  (cond ((null bindings) nil)
    ((variable-p pattern) (match-variable pattern input bindings))
    ((eql pattern input) bindings)
    ((and (consp pattern) (consp input))
    (match* (rest pattern) (rest input)
      (match* (first pattern) (first input) bindings)))))


(defun match (pattern input)
  (let ((result (match* pattern input)))
    (and result (or (butlast result) t))))


(defun apply-rule (input rules)
  (loop for (pattern replacement) in rules
    for bindings = (match* pattern input)
      thereis (and bindings (sublis bindings replacement))))


(defparameter *rules* nil)


(defun transform (expr &optional (*rules* *rules*))
  (transform* expr))


(defun transform* (expr)
  (if (atom expr) expr (transform-expr (mapcar #'transform* expr))))


(defun transform-expr (expr)
  (cond ((transform* (apply-rule expr *rules*)))
    ((evaluable expr) (eval expr))
    (t expr)))


(defun evaluable (expr)
  (and (every #'numberp (rest expr))
    (or (member (first expr) '(+ - * /))
      (and (eq (first expr) 'expt) (integerp (third expr))))))


(defparameter *simple-diff-rules*
  '(((D x x) 1)
    ((D (+ u v) x) (+ (D u x) (D v x)))
    ((D (* u v) x) (+ (* (D u x) v) (* u (D v x))))
    ((D (/ v) x) (- (/ (D v x) (* v v))))
    ((D u x) 0)
    ;;((D (sin u) x) (* (cos u) (D u x)))
    ;;((D (exp u) x) (* (exp u) (D u x)))
    ;;((D (log u) x) (* (/ u) (D u x)))
    ))


(defparameter *chain-diff-rules*
  (loop for (in out) in '(((exp u) (exp u)) ((log u) (/ u))
    ((sin u) (cos u)) ((cos u) (- (sin u))))
    collect `((D ,in x) (* ,out (D u x)))))


(defparameter *diff-rules*
  (append (butlast *simple-diff-rules*)
          *chain-diff-rules*
          (last *simple-diff-rules*)))


(defparameter *input-rules*
  '(((+ x y z . w) (+ x (+ y z . w)))
    ((* x y z . w) (* x (* y z . w)))
    ((- x y) (+ x (* -1 y)))
    ((/ x y) (* x (/ y)))
    ((^ x y) (expt x y))
    ((expt x y) (exp (* y (log x))))
    ((log a b) (/ (log a) (log b)))
    ((sqrt x) (^ x (/ 1 2)))
    ((tan x) (/ (sin x) (cos x)))))


(defparameter *simplification-rules*
  '(((+ 0 x) x) ((+ x 0) x)
    ((* x 1) x) ((* 1 x) x)
    ((* 1 x . w) (* x . w))
    ((* x 0) 0) ((* 0 x) 0)
    ((* (/ x) x . w) (* 1 . w))
    ((* x (* y z . w)) (* x y z . w))
    ((exp (* a (log b))) (expt b a))))


(defun doit (expr)
  (transform (transform (transform expr *input-rules*)
    *diff-rules*) *simplification-rules*))

(print (doit '(+ 0 x)))
