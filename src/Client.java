import node.Node;
import node.communication.Message;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * One shot client that queries the network's nodes for each nodes' amount of connections
 */
public class Client {
    private final static int MIN_PORT = 8000;
    public static void main(String args[]) {
        int peer;
        for(int i = 0; i < 100; i++){
            peer = MIN_PORT + i;
            try {
                Socket s = new Socket("localhost", peer);
                InputStream in = null;
                in = s.getInputStream();
                ObjectInputStream oin = new ObjectInputStream(in);
                OutputStream out = s.getOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(out);
                Message message = new Message(Message.Request.QUERY_PEERS);
                oout.writeObject(message);
                oout.flush();
                Message messageReceived = (Message) oin.readObject();
                ArrayList<Node> localPeers = (ArrayList<Node>) messageReceived.getMetadata();
                System.out.println("Node " + peer + " has " + localPeers.size() + " local peer connections.");
                s.close();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        
    }

}
