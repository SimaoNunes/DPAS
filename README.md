# DPAS
High Dependable Systems Project: Dependable Public Announcement Server


## setup
>>> mvn archetype:generate -DgroupId=SEC -DartifactId=DPAS -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
>>> cd DPAS
>>> mvn package


>>> java -cp target/DPAS-1.0-SNAPSHOT.jar SEC.App
or 
>>> mvn compile
>>> mvn exec:java -DmainClass=SEC.App
