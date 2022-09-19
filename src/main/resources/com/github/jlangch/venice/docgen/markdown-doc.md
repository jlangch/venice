# Venice Markdown

¶


## Headings

To create a heading, add one to four `#` symbols before the heading text. The number of `#` will determine the size of the heading.

```
# The largest heading
## The second largest heading
### The third largest heading
#### The fourth largest heading
```

¶
¶


## Paragraphs and Line Breaks

```
A paragraph is simply one or more consecutive lines of text, separated by 
one or more blank lines (a line containing nothing but spaces or tabs).

Within a paragraph line breaks can be added by placing a `pilcrow`

Line 1¶Line 2¶
Line 3
```

A paragraph is simply one or more consecutive lines of text, separated by 
one or more blank lines (a line containing nothing but spaces or tabs).

Within a paragraph line breaks can be added by placing a `pilcrow`

Line 1¶Line 2¶
Line 3

¶
¶


## Styling

Venice markdown supports *italic*, **bold**, and ***bold-italic*** styling

```
This is *italic*, **bold**, and ***bold-italic*** styled text.
```

This is *italic*, **bold**, and ***bold-italic*** styled text.

¶
¶


## Lists

Unordered List

```
* item 1
* item 2
* item 3
```

* item 1
* item 2
* item 3

¶


Ordered List

```
1. item 1
2. item 2
3. item 3
```

1. item 1
2. item 2
3. item 3

¶


Mulitiline list items with explicit line breaks:

```
* item 1
* item 2¶
  next line¶
  next line 
* item 3
```

* item 1
* item 2¶
  next line¶
  next line 
* item 3


Mulitiline list items with auto line breaks:

```
* item 1
* Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod 
  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim 
  veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex 
  ea commodo consequat. Duis aute irure dolor in reprehenderit in 
  voluptate velit esse cillum dolore eu fugiat nulla pariatur. 
* item 3
```

* item 1
* Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod 
  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim 
  veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex 
  ea commodo consequat. Duis aute irure dolor in reprehenderit in 
  voluptate velit esse cillum dolore eu fugiat nulla pariatur. 
* item 3

¶
¶


## Links

Links are created by wrapping link text in brackets `[ ]`, and then wrapping the URL in parentheses `( )`. 

```
[Venice](https://github.com/jlangch/venice)
```

[Venice](https://github.com/jlangch/venice)

¶
¶


## Tables

A simple table

```
| JAN | 1   |
| FEB | 20  |
| MAR | 300 |
```

¶

Column alignment

```
| :---  | :---: | ----: |
| 1     |   1   |     1 |  
| 200   |  200  |   200 |
| 30000 | 30000 | 30000 |
```

¶

Width header

```
| Col 1 | Col 2 | Col 3 |
| :---  | :---: | ----: |
| 1     |   1   |     1 |  
| 200   |  200  |   200 |
| 30000 | 30000 | 30000 |
```

¶

Line breaks in cells

```
| JAN | 1¶ 2¶ 3 |
| FEB | 20      |
| MAR | 300     |
```

¶
¶


**Column format using CSS styles**

The Venice markdown supports a few CSS styles

Text alignment: 

* `text-align: left`
* `text-align: center`
* `text-align: right`

Column width:

* `width: 15%`
* `width: 15pm`
* `width: 15em`
* `width: auto`

```
| Col 1 | Col 2 | 
| [![text-align: left; width: 6em]] | [![text-align: left; width: 6em]] |
| 1     | 1     | 
| 200   | 200   |
| 30000 | 30000 |
```

| Col 1 | Col 2 | 
| [![text-align: left; width: 6em]] | [![text-align: left; width: 6em]] |
| 1     | 1     | 
| 200   | 200   |
| 30000 | 30000 |

¶
¶


## Code

Code can be called out within a text by enclosing it with single backticks.

```
To open a namespace use `(ns name)`.
```

To open a namespace use `(ns name)`.

¶


Code block are enclosed with three backticks:

```
 ```
(defn hello [] 
   (println "Hello stranger"))
   
(hello)
 ```
```

producing

```
(defn hello [] 
   (println "Hello stranger"))
   
(hello)
```
 