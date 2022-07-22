import node.Node;
import node.communication.Address;

import java.util.ArrayList;

public class NetworkLauncher {
    final private static int MIN_PORT = 8000;
    final private static int MAX_PORT = 8009;

    public static void main(String args[]) {
        ArrayList<Address> globalPeers = new ArrayList<Address>();
        for(int i = MIN_PORT; i < MAX_PORT + 1; i++){
            globalPeers.add(new Address(i, "localhost"));
        }

        Node n0 = new Node(8000, 5, 3);
        Node n1 = new Node(8001, 4, 3);
        Node n2 = new Node(8002, 5, 3);
        Node n3 = new Node(8003, 4, 3);
        Node n4 = new Node(8004, 5, 3);
        Node n5 = new Node(8005, 4, 3);

        NetworkLauncher nl0 = new NetworkLauncher(globalPeers, n0);
        NetworkLauncher nl1 = new NetworkLauncher(globalPeers, n1);
        NetworkLauncher nl2 = new NetworkLauncher(globalPeers, n2);
        NetworkLauncher nl3 = new NetworkLauncher(globalPeers, n3);
        NetworkLauncher nl4 = new NetworkLauncher(globalPeers, n4);
        NetworkLauncher nl5 = new NetworkLauncher(globalPeers, n5);

    }
    public NetworkLauncher(ArrayList<Address> globalPeers, Node node){
        NodeLauncher nodeLauncher = new NodeLauncher(node, globalPeers);
        nodeLauncher.start();
    }

    class NodeLauncher extends Thread {
        Node node;
        ArrayList<Address> globalPeers;

        NodeLauncher(Node node, ArrayList<Address> globalPeers){
            this.node = node;
            this.globalPeers = globalPeers;
        }

        public void run() {
            node.runNode(globalPeers);
        }
    }
}
