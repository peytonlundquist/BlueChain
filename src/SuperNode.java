import java.util.ArrayList;

public class SuperNode {
    public static void main(String args[]) {
        Node n1 = new Node(Integer.parseInt(args[0]), 4);
        ArrayList<Address> globalPeers = new ArrayList<Address>();
        globalPeers.add(new Address(Integer.parseInt(args[1]), "localhost"));
        globalPeers.add(new Address(Integer.parseInt(args[2]), "localhost"));
        globalPeers.add(new Address(Integer.parseInt(args[3]), "localhost"));
        globalPeers.add(new Address(Integer.parseInt(args[4]), "localhost"));
        n1.runNode(globalPeers);
    }
}
