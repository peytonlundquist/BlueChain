import node.Node;
import node.communication.Address;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Launches a network given specified configurations
 */
public class NetworkLauncher {

    /* Make a list of the entirety of each node's address */
    private static final ArrayList<Address> globalPeers = new ArrayList<Address>();

    public static void main(String[] args) {
        String usage = "Usage: NetworkLauncher " +
                "[-o <see options>] [-t <TimedWaitDelayMilliseconds>]" +
                "\n Options:" +
                "\n -o <myNodesStartingPort> <myNodesEndingPort> <otherSubNetStartingPort> <otherSubNetEndingPort> <otherSubNetHostName> ..." +
                "\n     Specifies information regarding other subnets of nodes. " +
                "\n     First we specify our range of port for localhost, then list other subnets." +
                "\n     Total number of nodes must be under the specified amount in config.properties" +
                "\n     No limit for number of subnets one can reasonably specify" +
                "\n\n -t <TimedWaitDelayMilliseconds>" +
                "\n     Specifies the time for a subnet to wait before seeking out connections. " +
                "\n     Useful to allow all subnets to bind to their ports before connecting" +
                "\n\n Default: NetworkLauncher will launch number of nodes specified in config.properties " +
                "\n on localhost with no other scope of nodes";
        try {
            /* Grab values from config file */
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
            int debugLevel = Integer.parseInt(prop.getProperty("DEBUG_LEVEL"));


            /* List of node objects for the launcher to start*/
            ArrayList<Node> nodes = new ArrayList<Node>();

            int timedWaitDelay = 0;
            int myNodesStartingPort;
            int myNodesEndingPort;
            boolean oFlag = false;

            /* Allow specification for subnets */
            if(args.length > 0){
                if(args.length == 1 && !args[0].equals("-a")){
                    System.out.println(usage);
                    return;
                }

                if(args[0].equals("-a")){

                    for(int i = startingPort; i < startingPort + numNodes; i++){
                        nodes.add(new Node(i, maxConnections, minConnections, numNodes, quorumSize, startingPort,debugLevel));
                    }

                    StringTokenizer st;
                    String path = ".\\src\\main\\java\\node\\nodeRegistry\\";
                    File folder = new File(path);
                    File[] registeredNodes = folder.listFiles();



                    for(int i = 0; i < registeredNodes.length; i++){
                        String name = registeredNodes[i].getName();
                        st = new StringTokenizer(name, "_");
                        String host = st.nextToken();
                        int port = Integer.parseInt(st.nextToken().substring(0, 4));
                        System.out.println("Port: " + port + ", host: " + host);
                        globalPeers.add(new Address(port, host));
                    }

                    NetworkLauncher n = new NetworkLauncher();
                    n.startNetworkClients(globalPeers, nodes); // Begins network connections

                }else{
                    if(args[0].equals("-t")){
                        timedWaitDelay = Integer.parseInt(args[1]);
                    }

                    if (args[0].equals("-o") || (args.length > 3 && args[2].equals("-o"))) {
                        if(oFlag){
                            System.out.println("Error. Too many -o flags");
                            System.out.println(usage);
                            return;
                        }
                        oFlag = true;
                        int currentArg = 0;
                        if(args[2].equals("-o")){
                            currentArg = 2;
                        }
                        myNodesStartingPort =Integer.parseInt(args[currentArg + 1]);
                        myNodesEndingPort = Integer.parseInt(args[currentArg + 2]);

                        for(int i = myNodesStartingPort; i < myNodesEndingPort; i++){
                            nodes.add(new Node(i, maxConnections, minConnections, numNodes, quorumSize, startingPort, debugLevel));
                            globalPeers.add(new Address(i, "localhost"));
                        }

                        for(int i = currentArg + 3; i < args.length; i = i + 3){
                            if(args[i].equals("-t")){
                                timedWaitDelay = Integer.parseInt(args[i+1]);
                                break;
                            }

                            for(int j = Integer.parseInt(args[i]); j < Integer.parseInt(args[i + 1]); j++){
                                globalPeers.add(new Address(j, args[i + 2]));
                                if(globalPeers.size() > numNodes){
                                    System.out.println("Error: Network total nodes is greater than number of nodes specified in config.properties");
                                    System.out.println(usage);
                                    return;
                                }
                            }
                        }
                    }

                    if(!oFlag){
                        for(int i = startingPort; i < startingPort + numNodes; i++){
                            globalPeers.add(new Address(i, "localhost"));
                            nodes.add(new Node(i, maxConnections, minConnections, numNodes, quorumSize, startingPort,debugLevel));
                        }
                    }

                    try {
                        Thread.sleep(timedWaitDelay);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    NetworkLauncher n = new NetworkLauncher();
                    n.startNetworkClients(globalPeers, nodes); // Begins network connections
                }
            }else{
                for(int i = startingPort; i < startingPort + numNodes; i++){
                    globalPeers.add(new Address(i, "localhost"));
                    nodes.add(new Node(i, maxConnections, minConnections, numNodes, quorumSize, startingPort,debugLevel));
                }
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e){
            System.out.println("Error: args formatted incorrect" + e);
            System.out.println(usage);
        }
    }

    /* Gives each node a thread to start node connections */
    public void startNetworkClients(ArrayList<Address> globalPeers, ArrayList<Node> nodes){
        for(int i = 0; i < nodes.size(); i++){
            Collections.shuffle(globalPeers);
            new NodeLauncher(nodes.get(i), globalPeers).start();
        }
    }

    /**
     * Thread which is assigned to start a single node within the NetworkLaunchers managed nodes
     */
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
