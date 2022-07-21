import java.util.ArrayList;

public class SuperNode {
    public static void main(String args[]) {
        Node n1 = new Node(Integer.parseInt(args[0]), 4);
        ArrayList<Address> globalPeers = new ArrayList<Address>();
        globalPeers.add(new Address(8000, "localhost"));
        globalPeers.add(new Address(8001, "localhost"));
        globalPeers.add(new Address(8002, "localhost"));
        globalPeers.add(new Address(8003, "localhost"));
        globalPeers.add(new Address(8004, "localhost"));
        globalPeers.add(new Address(8005, "localhost"));
        globalPeers.add(new Address(8006, "localhost"));
        globalPeers.add(new Address(8007, "localhost"));
        globalPeers.add(new Address(8008, "localhost"));
        n1.runNode(globalPeers);
    }
}
