# Venice project stats

## 12. Juli 2026



```text
# note: the cloc tool is installed via homebrew: brew install cloc

% cd /Users/juerg
% cloc --timeout 20 \
       --exclude-lang=JavaScript,Smalltalk \
       --match-d='/(main|org)/' \
       --read-lang-def=Documents/workspace-omni/venice/cloc-venice.txt \
       Documents/workspace-omni/venice/src 

     916 text files.
     905 unique files.
      31 files ignored.

github.com/AlDanial/cloc v 1.98  T=8.74 s (103.6 files/s, 26857.2 lines/s)
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                           766          24336          25631         128756
Venice                         114           9230           4644          38683
HTML                             2             47             27            847
Text                             9            157              0            833
Markdown                         5            296              0            700
Bourne Shell                     3             39             64            118
DOS Batch                        2             32             50             80
JSON                             2              6              0             48
XML                              1              4              0             39
Maven                            1              7              0             36
-------------------------------------------------------------------------------
SUM:                           905          34154          30416         170140
-------------------------------------------------------------------------------
```
