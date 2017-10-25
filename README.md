[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

# WikidataExport

This is the sister code of the [R_WikidataBot](https://github.com/skeating/R_WikidataBot) and creates the required csv file from the Reactome Graph Database.

## Code

The code is written in Java and uses the [Graph Database](http://www.reactome.org/pages/documentation/developer-guide/graph-database/) and corresponding API. 


### Usage

### Arguments

The following arguments are required

- -h "host" 			The neo4j host for the ReactomeDB
- -b "port"				The neo4j port
- -u "user" 			The neoj4 username
- -p "password" 		The neo4j password
- -o "outfilename"		The full path for the output file to be written
 
Zero or one of the following "pathway" arguments are also expected to identify which Pathway(s) are to be exported

- -t "toplevelpath"	    A single integer argument that is the databaseIdentifier for a Pathway
- -s "species"          A single integer argument that is the databaseIdentifier for a Species
- -m "multiple"         A comma-separated list of integers that are the databaseIdentifiers of several Pathways

### Output depending on "pathway" argument

- no specific pathway argument

The output when no specific path way argument is specified will be the csv file containing an entry for every pathway in the Reactome DB.

- -t dbid

The output for the argument -t will be the csv file with a single entry corresponding to the pathway specified.

- -s dbid

The output for the argument -s will be the csv file with a one entry corresponding to each pathway in the species specified.

- -m dbid1,dbid2,dbid3

The output for the argument -m will be the csv file with a one entry corresponding to each pathway specified.

#### Output file

The code generates a comma separated value (.csv) file where each line refers to a Pathway and therefore a single Wikisdata entry. The entries expected are:

**Species\_code,Reactome\_Id,Name,Description,[publication1;publication2;..],goterm,None**

where

- Species\_code is the three letter abbreviation of the species used by Reactome e.g. HSA
- Reactome\_Id is the Reactome Stable Indentifier for the pathway
- Name the Reactome Display Name
- Description a sentence based on Name stating that this is an instance of this pathway in the given species
- [publication1;..] a semi-colon separated list of the pmid URL of each referenced publication
- goterm the relevant term as GO:nnnn
- None - to indicate the end of the entry
  

### Restricting output

Before outputting data the code checks that the Pathway meets any restrictions that have been placed in the    ***private static boolean is_appropriate(Pathway path)*** function. This allows the user to tailor the output.

## TO DO

Currently the code only supports the Homo sapiens species as it hard codes the 'HSA' entry in the output.

1. Support all species
2. Support a reaction that is not a Pathway
3. Add partOf and hasPart entries to link pathways




