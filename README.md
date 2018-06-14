[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

## WikidataExport

This is the sister code of the [r-wikidata-bot](https://github.com/reactome/r-wikidata-bot) and creates the required csv files from the Reactome Graph Database.

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

Four csv files are written:
- hsa\_pathway\_data.csv
- hsa\_reaction\_data.csv
- hsa\_entity\_data.csv
- hsa\_modprot\_data.csv


These files are read in by the sister code r-wikidata-bot and used to populate/update wikidata entries.


## Limitations

Currently the code only supports the Homo sapiens species. This is hard coded at present but commented out code will facilitate adding other species in the future.


---

## RELEASE

### PREREQUISITES


1. You must have [Reactome Graph Database](http://www.reactome.org/dev/graph-database/) up and running before executing the wikidata-exporter
2. You must have Python v3 with [WikidataIntegrator](https://github.com/SuLab/WikidataIntegrator) installed before executing the r-wikidata-bot

**STEP ONE: Export from Reactome**

1. Cloning and packaging the project

```console
git clone https://github.com/reactome/wikidata-exporter.git
cd wikidata-exporter
mvn clean package
```

2. Generating .csv files

```console
mkdir outputdir
java -jar target/wikidata-exporter-jar-with-dependencies.jar -h localhost -b 7474 -u user -p not4share -output outputdir
```

3. Change directory

```console
cd ..
```

STEP TWO: Import to Wikidata

1. Clone the project

```console
git clone https://github.com/reactome/r-wikidata-bot.git
cd wikidata-exporter
```

2. Move the .csv files

```console
mkdir input
cp ../wikidata-exporter/outputdir/*.csv ./input/*
```

3. Run the import

```console
python update_wikidata.py Pathwaybot password input ReactomeReleaseNo ReactomeNewsNumberForRelease
```

The ReactomeNewsNumberForRelease is part of teh URL that identifies the release: e.g. in https://reactome.org/about/news/103-version-65-released it would be '103'

Note: This bot can only be run under the 'Pathwaybot' wikidata account.
