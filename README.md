[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

# WikidataExport

This is the sister code of the [R_WikidataBot](https://github.com/skeating/R_WikidataBot) and creates the required csv files from the Reactome Graph Database.

## Code

The code is written in Java and uses the [Graph Database](http://www.reactome.org/pages/documentation/developer-guide/graph-database/) and corresponding API. 


### Arguments

The following arguments are required

```console
-h "host"      The neo4j host for the ReactomeDB
-b "port"      The neo4j port
-u "user"      The neoj4 username
-p "password"  The neo4j password
-o "outdir"    The directory where output files will be written
```


### Output


## TO DO

Currently the code only supports the Homo sapiens species. This is hard coded at present but commented out code will facilitate adding other species in the future.



