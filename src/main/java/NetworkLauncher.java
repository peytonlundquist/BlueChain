import node.Node;
import node.communication.Address;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

/**
 * Launches a network given specified configurations
 */
public class NetworkLauncher {

    private static final ArrayList<Address> globalPeers = new ArrayList<Address>();

    public static void main(String[] args) {
        try {
            String configFilePath = "src/main/java/config.properties";
            FileInputStream fileInputStream = new FileInputStream(configFilePath);
            Properties prop = new Properties();
            prop.load(fileInputStream);

            int numNodes = Integer.parseInt(prop.getProperty("NUM_NODES"));
            int maxConnections = Integer.parseInt(prop.getProperty("MAX_CONNECTIONS"));
            int minConnections = Integer.parseInt(prop.getProperty("MIN_CONNECTIONS"));
            int startingPort = Integer.parseInt(prop.getProperty("STARTING_PORT"));
            int quorumSize = Integer.parseInt(prop.getProperty("QUORUM"));
            int minTransactionsPerBlock = Integer.parseInt(prop.getProperty("MIN_TRANSACTIONS_PER_BLOCK"));


            ArrayList<Node> nodes = new ArrayList<Node>();

            if(args.length > 0){
                int myNodesStartingPort = Integer.parseInt(args[0]);
                int myNodesEndingPort = Integer.parseInt(args[1]);

                for(int i = startingPort; i < startingPort + numNodes; i++){
                    globalPeers.add(new Address(i, "localhost"));
                }
                for(int i = myNodesStartingPort; i < myNodesEndingPort; i++){
                    nodes.add(new Node(i, maxConnections, minConnections, numNodes, quorumSize, startingPort, minTransactionsPerBlock));
                }
            }else{
                for(int i = startingPort; i < startingPort + numNodes; i++){
                    globalPeers.add(new Address(i, "localhost"));
                    nodes.add(new Node(i, maxConnections, minConnections, numNodes, quorumSize, startingPort, minTransactionsPerBlock));
                }
            }
            NetworkLauncher n = new NetworkLauncher();
            n.startNetworkClients(globalPeers, nodes);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startNetworkClients(ArrayList<Address> globalPeers, ArrayList<Node> nodes){
        for(int i = 0; i < nodes.size(); i++){
            Collections.shuffle(globalPeers);
            new NodeLauncher(nodes.get(i), globalPeers).start();
        }
    }

    class NodeLauncher extends Thread {
        Node node;
        ArrayList<Address> globalPeers;

        NodeLauncher(Node node, ArrayList<Address> globalPeers){
            this.node = node;
            this.globalPeers = globalPeers;
        }

        public void run() {
            node.requestConnections(globalPeers);
        }
    }
}
