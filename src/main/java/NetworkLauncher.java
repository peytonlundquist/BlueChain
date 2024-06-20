import node.Node;
import utils.Address;
import utils.Config;

import java.io.File;
import java.util.ArrayList;
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
            Config configValues = new utils.Config();

            /* List of node objects for the launcher to start*/
            ArrayList<Node> nodes = new ArrayList<Node>();

            int timedWaitDelay = 0;

            if (args.length > 0 && args[0].equals("-t")) {
                timedWaitDelay = Integer.parseInt(args[1]);
            }

            for (int i = configValues.getStartingPort(); i < configValues.getStartingPort() + configValues.getNumNodes(); i++) {
                nodes.add(new Node(configValues, i));
            }

            try {
                Thread.sleep(timedWaitDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            StringTokenizer st;
            String path = "./src/main/java/node/nodeRegistry/";
            File folder = new File(path);
            File[] registeredNodes = folder.listFiles();

            for (int i = 0; i < registeredNodes.length; i++) {
                String name = registeredNodes[i].getName();

                if(!name.contains("keep")){
                    st = new StringTokenizer(name, "_");
                    String host = st.nextToken();
                    int port = Integer.parseInt(st.nextToken().replaceFirst(".txt", ""));
                    globalPeers.add(new Address(port, host));
                }
            }       

            NetworkLauncher n = new NetworkLauncher();
            n.startNetworkClients(globalPeers, nodes); // Begins network connections

        } catch (NumberFormatException e){
            System.out.println("Error: args formatted incorrect" + e);
            System.out.println(usage);
        }
    }

    /* Gives each node a thread to start node connections */
    public void startNetworkClients(ArrayList<Address> globalPeers, ArrayList<Node> nodes){
        for(int i = 0; i < nodes.size(); i++){
            //Collections.shuffle(globalPeers);
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
