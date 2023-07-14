mvn clean install ; rm src/main/java/node/nodeRegistry/*.txt ; rm src/main/resources/*.json; rm src/main/resources/*.ndjson ; touch src/main/resources/network.ndjson; java -cp target/network-1.0-SNAPSHOT.jar:target/javax.json-1.1.4.jar NetworkLauncher;


