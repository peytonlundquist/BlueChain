import node.Node;
import node.communication.Address;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Launches a single node instance for debugging or development
 */
public class SuperNode {

    static final private String host = "localhost";
    static final private int MAX_PORT = 8020;
    static final private int MIN_PORT = 8000;


    public static void main(String[] args) {

        Node n1 = new Node(Integer.parseInt(args[0]), 5, 3, 1000, 10, 8000);
        ArrayList<Address> globalPeers = new ArrayList<Address>();

        System.out.println("==== Global Peer List ====");
        for(int i = MIN_PORT; i < MAX_PORT + 1; i++){
            globalPeers.add(new Address(i, host));
            System.out.println("Host: " + host + " Port: " + i);
        }

        System.out.println("==========================");
        Collections.shuffle(globalPeers);
        n1.requestConnections(globalPeers);
    }
}
