import node.Node;
import node.communication.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
    public static void main(String args[]) {
        try {
            Socket s = new Socket(args[0], Integer.parseInt(args[1]));
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
            System.out.println("Node has " + localPeers.size() + " local peer connections.");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
