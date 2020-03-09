# DPAS
High Dependable Systems Project: Dependable Public Announcement Server


## setup

```bash
mvn archetype:generate -DgroupId=SEC -DartifactId=DPAS -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
cd DPAS
mvn package
```

## to run
```bash
java -cp target/DPAS-1.0-SNAPSHOT.jar SEC.App
```
or
```bash
mvn compile
mvn exec:java -DmainClass=SEC.App
```
or (if main class is defined at pom.xml)
```bash
mvn compile
mvn exec:java 
```
