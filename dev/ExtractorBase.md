
# ExtractorBase #

These are errors and warnings that may be logged by the ExtractorBase class.
All reports append the stableId of the object being processed when the warning/error was issued.
 
### Warnings

1. Unknown PhysicalEntity type *className* 

A PhysicalEntity that did not exist in the GraphCore database  at time of writing has been used. Code needs to be adapted in the ExtractorBase::addComponentId function.


### Errors

1. No database object set

The class has been instantiated without setting a database object from which to extract data. Bad BUG !


-----
This file was last updated in July 2018. 