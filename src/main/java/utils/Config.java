package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The Config class represents a configuration utility for a distributed system.
 * It loads configuration properties from a specified file and provides methods
 * to access the configuration parameters.
 */
public class Config {
    
    private int numNodes ;
    private int maxConnections;
    private int minConnections;
    private int startingPort;
    private int quorumSize; 
    private int minimumTransactions;
    private int debugLevel;
    private String use;

    /**
     * Gets the number of nodes in the distributed system.
     *
     * @return The number of nodes.
     */
    public int getNumNodes() {
        return numNodes;
    }

    /**
     * Gets the maximum number of connections allowed.
     *
     * @return The maximum number of connections.
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Gets the minimum number of connections required.
     *
     * @return The minimum number of connections.
     */
    public int getMinConnections() {
        return minConnections;
    }

    /**
     * Gets the starting port for communication in the distributed system.
     *
     * @return The starting port.
     */
    public int getStartingPort() {
        return startingPort;
    }

    /**
     * Gets the quorum size required for decision-making in the system.
     *
     * @return The quorum size.
     */
    public int getQuorumSize() {
        return quorumSize;
    }

    /**
     * Gets the minimum number of transactions required for system operation.
     *
     * @return The minimum number of transactions.
     */
    public int getMinimumTransactions() {
        return minimumTransactions;
    }

    /**
     * Gets the debug level for logging and debugging purposes.
     *
     * @return The debug level.
     */
    public int getDebugLevel() {
        return debugLevel;
    }

    /**
     * Gets the specified use case for the distributed system.
     *
     * @return The use case.
     */
    public String getUse() {
        return use;
    }


    /**
     * Constructs a Config object by loading configuration properties from the specified file.
     * The file path is set to "src/main/java/config.properties" by default.
     */
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
