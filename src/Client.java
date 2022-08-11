import graphing.Graph;
import graphing.GraphNode;
import node.communication.Address;
import node.communication.Message;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * One shot client that queries the network's nodes
 */
public class Client {
    private final static int MIN_PORT = 8000;
    private final static int NUM_NODES = 100;

    public static void main(String[] args) {
        int port;

        if(args.length > 0) {
            if (args[0].equals("graph")) {
                LinkedList<GraphNode> graphNodes = new LinkedList<>();
                for (int i = 0; i < NUM_NODES; i++) {
                    port = MIN_PORT + i;
                    ArrayList<Address> localPeers = queryPeer(port);
                    if (localPeers != null) {
                        System.out.println("Node " + port + " has " + localPeers.size() + " local peer connections.");
                        graphNodes.add(new GraphNode(port, localPeers));
                    }
                }
                new Graph(graphNodes);
            } else if (args[0].equals("query")) {
                try {
                    port = Integer.parseInt(args[1]);
                    ArrayList<Address> localPeers = queryPeer(port);
                    if (localPeers != null) {
                        System.out.print("Node " + port + " has " + localPeers.size() + " local peer connections. Peers: ");
                        for(Address address : localPeers){
                            System.out.print(address.getPort() + " ");
                        }
                        System.out.print("\n");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Expected integer or no arguments");
                    System.out.println("Usage: [graph] [query <portNum>]");
                } catch (Exception e){
                    System.out.println(e);
                }
            }else{
                System.out.println("Usage: [graph] [query <portNum>]");
            }
        }else{
            System.out.println("Usage: [graph] [query <portNum>]");
        }
    }

    /**
     * Queries a specified node given its port, assuming localhost
     * @param port
     * @return ArrayList<Address> node's connection list
     */
    private static ArrayList<Address> queryPeer(int port){
        try {
            Socket s = new Socket("localhost", port);
            InputStream in = s.getInputStream();
            ObjectInputStream oin = new ObjectInputStream(in);
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            Message message = new Message(Message.Request.QUERY_PEERS);
            oout.writeObject(message);
            oout.flush();
            Message messageReceived = (Message) oin.readObject();
            ArrayList<?> localPeers = (ArrayList<?>) messageReceived.getMetadata();
            s.close();
            return (ArrayList<Address>) localPeers;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error occurred");
        }
        return null;
    }
}
