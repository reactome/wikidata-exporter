
# WikidataExport #

These are errors and warnings that may be logged by the main WikidataExport class.
 
### Warnings

1. No unique label established for modified protein 

Every entry in Wikidata must have a unique label. Sometimes with the modified proteins it can be hard to find a unique label that captures the protein and its modifications. Altering the label in the modprot_data.csv file will solve the issue.


### Errors

1. Caught IOException: with error message

Files could not be read/created/written etc.

2. Unexpected type  + typeToWrite +  encountered

The 'typeToWrite' variable comes from a restricted list and sends output to the appropriate file. It should never be anything else.

     * @note typeToWrite values are
     *        "P" pathway
     *        "R" reaction
     *        "E" entity
     *        "MP" modified protein


-----
This file was last updated in July 2018. 