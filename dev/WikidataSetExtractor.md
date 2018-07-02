
# WikidataSetExtractor #

These are errors and warnings that may be logged by the WikidataSetExtractor class.
All reports append the stableId of the object being processed when the warning/error was issued.
 
### Warnings

1. Unexpected type of set encountered *className* 

A Set that did not exist in the GraphCore database  at time of writing has been used. Code needs to be adapted in the WikidataSetExtractor::getSetType function.


### Errors

1. Invalid Set: 

This means an object that does not inherit from the GraphCore EntitySet class has been passed to this code. This would be a BUG.


-----
This file was last updated in July 2018. 