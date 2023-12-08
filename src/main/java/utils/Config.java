package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    
    private int numNodes ;
    private int maxConnections;
    private int minConnections;
    private int startingPort;
    private int quorumSize; 
    private int minimumTransactions;
    private int debugLevel;
    private String use;

    public int getNumNodes() {
        return numNodes;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getMinConnections() {
        return minConnections;
    }

    public int getStartingPort() {
        return startingPort;
    }

    public int getQuorumSize() {
        return quorumSize;
    }

    public int getMinimumTransactions() {
        return minimumTransactions;
    }

    public int getDebugLevel() {
        return debugLevel;
    }

    public String getUse() {
        return use;
    }

    public Config(){

        String configFilePath = "src/main/java/config.properties";
        Properties prop = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(configFilePath);
            prop = new Properties();
            prop.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        numNodes = Integer.parseInt(prop.getProperty("NUM_NODES"));
        maxConnections = Integer.parseInt(prop.getProperty("MAX_CONNECTIONS"));
        minConnections = Integer.parseInt(prop.getProperty("MIN_CONNECTIONS"));
        startingPort = Integer.parseInt(prop.getProperty("STARTING_PORT"));
        quorumSize = Integer.parseInt(prop.getProperty("QUORUM"));
        minimumTransactions = Integer.parseInt(prop.getProperty("MINIMUM_TRANSACTIONS"));
        debugLevel = Integer.parseInt(prop.getProperty("DEBUG_LEVEL"));
        use = prop.getProperty("USE");
    }
}
