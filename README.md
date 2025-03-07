[![Java CI with Maven](https://github.com/kayyagari/coconut/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/kayyagari/coconut/actions/workflows/maven.yml)
# Coconut
Coconut is a plugin for MirthConnect offering below functionalties:
1. Load CSV files using DuckDb
2. Compile Java models at runtime and use them for fetching data
3. Query loaded CSV data (with pagination)
4. Generate a Java model source for any loaded CSV file

# Usage
```javascript
// access the global Coconut instance
var $cn = globalMap.get("$cn");
var storeName = "petstore";

// create/open a store
var petStore = $cn.openStore(storeName);
// CSV content
var csvData = "id,name\n" +
              "1,Rabbit\n" +
              "2,Squirrel\n" +
              "3,Panda\n" +
              "4,Lion\n";
// write the file /tmp/animals.csv
Packages.org.apache.commons.io.FileUtils.write(new Packages.java.io.File("/tmp/animals.csv"), csvData, Packages.java.nio.charset.StandardCharsets.UTF_8);

// load a CSV into a table with the given name (the table will be created automatically)
petStore.loadCsv("/tmp/animals.csv", "animals");

// define a new java model to work with the data present in the table
var animalJavaSrc = "public class Animal { public long id; public String name;}";

// compile the model on the fly, no need to add a jar resource
var animalClass = $cn.compileJavaModel("Animal", animalJavaSrc);

// API
// 1. Get one record
var squirrel = petStore.getOne("select * from animals where id = 2", animalClass);
logger.warn(squirrel.id + " " + squirrel.name);

// 2. Get a list of records
var animals = petStore.getList("select * from animals", animalClass); // fetch data
for(var i=0; i < animals.size(); i++) {
	var a = animals.get(i);
	logger.warn(a.id + " " + a.name);
}

// 3. Page through the records
var pageSize = 2;
var offset = 0;
var firstPage = petStore.getPagedList(animalClass, "animals", offset, pageSize);
logger.warn("First record -> " + firstPage.get(0).id + " " + firstPage.get(0).name);

var lastPage = petStore.getPagedList(animalClass, "animals", offset+2, pageSize);
logger.warn("Last record -> " + lastPage.get(1).id + " " + lastPage.get(1).name);

// 4. Generate a Java model from an existing table
// generate a Java model from the existing table, e.g, when there are too many columns and it is laborious to create a model
// note that the generated model may not be perfect and requires manual editing
var generatedJavaModelSource = petStore.generateJavaModelFrom("animals");
// the generated source can be modified if needed before passing it to $cn.compileJavaModel() to generate the corresponding class
logger.warn(generatedJavaModelSource);
```
## Loading a CSV file using raw-SQL
The DuckDb connection underlying a CoconutStore instance can be used to execute SQL statement directly.
This is helpful in cases where a given CSV file needs custom config options to load.

For example, to load NPI data:
```javascript
var $cn = globalMap.get("$cn");
var storeName = "npidata";
var npiStore = $cn.openStore(storeName);
npiStore.loadCsvWithRawSql("create table npi as select * from read_csv('/tmp/npidata_pfile_20050523-20250209.csv', all_varchar=1)");
var npiModelSrc = npiStore.generateJavaModelFrom("npi");
logger.warn(npiModelSrc);
```