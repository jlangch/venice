# Source Code as PDF

For documentation purposes Venice source code can be rendered 
to HTML or PDF document.


### Example

The renderer can be called from the REPL

To create a HTML and PDF representation from the 'sudoku.venice' script run:

```clojure
venice> !source-pdf /Users/foo/scripts/sudoku.venice /Users/foo/scripts/ /Users/foo/fonts
```

[Sudoku PDF](https://github.com/jlangch/venice/blob/master/doc/assets/source-to-pdf/sudoku.venice.pdf)

[Sudoku HTML](https://htmlpreview.github.io/?https://github.com/jlangch/venice/blob/master/doc/assets/source-to-pdf/sudoku.venice.html)


### Prerequisites

The PDF renderer requires two specific fonts in the passed font directory '/Users/foo/fonts':

 - [OpenSans-Regular.ttf](https://fonts.google.com/specimen/Open+Sans)
 - [SourceCodePro-Regular.ttf](https://fonts.google.com/specimen/Source+Sans+Pro)
 
