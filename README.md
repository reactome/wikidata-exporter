[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

## WikidataExport

This is the sister code of the [r-wikidata-bot](https://github.com/reactome/r-wikidata-bot) and creates the required json files from the Reactome Graph Database.

## Code

The code is written in Java and uses the [Graph Database](http://www.reactome.org/pages/documentation/developer-guide/graph-database/) and corresponding API. 


### Arguments

The following arguments are required

```console
-h "host"      The neo4j host for the ReactomeDB
-b "port"      The neo4j port
-u "user"      The neoj4 username
-p "password"  The neo4j password
-o "outputdirectory"    The directory where output files will be written
```

### Output

Five json files are written:
- pathway.json : has all the pathways
- reaction.json : has all the reactions
- physicalEntity.json : has all the physical entities
- modifiedProtein.json : has all the modified proteins
- parent.json : has all the child-parent links


These files are read in by the sister code r-wikidata-bot and used to populate/update Reactome data in Wikidata.


### Prerequisites

- [Reactome Graph Database](http://www.reactome.org/dev/graph-database/) must be up and running before executing 
the wikidata-exporter


#### Export data from Reactome

1. Cloning and packaging the wikidata-exporter project

```
git clone https://github.com/reactome/wikidata-exporter.git
cd wikidata-exporter
mvn clean package
```

2. Generating .json files

```
mkdir data
java -jar target/wikidata-exporter-jar-with-dependencies.jar -h localhost -b 7474 -u user -p not4share -o data
```

This step may take up to 10 minutes.

3. Verification

The specified outputdirectory should have the 5 json files mentioned above.

The wikidata-exporter/WikidataExporter.log file has the logs for the project. 
 
