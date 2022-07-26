import node.Node;
import node.communication.Address;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Launches a network given specified configurations
 */
public class NetworkLauncher {
    final private static int MIN_PORT = 8000;
    final private static int MAX_PORT = 8100;
    final private static int MAX_PEERS = 10;
    final private static int INITIAL_CONNECTIONS = 3;
    private static ArrayList<Address> globalPeers = new ArrayList<Address>();

    public static void main(String[] args) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        for(int i = MIN_PORT; i < MAX_PORT + 1; i++){
            globalPeers.add(new Address(i, "localhost"));
            nodes.add(new Node(i, MAX_PEERS, INITIAL_CONNECTIONS));
        }
        NetworkLauncher n = new NetworkLauncher();
        n.startNetworkClients(globalPeers, nodes);
    }

    public void startNetworkClients(ArrayList<Address> globalPeers, ArrayList<Node> nodes){
        for(int i = 0; i < (MAX_PORT + 1) - MIN_PORT; i++){
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
