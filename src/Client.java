import node.communication.Message;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * One shot client that queries the network's nodes for each nodes' amount of connections
 */
public class Client {
    private final static int MIN_PORT = 8000;
    public static void main(String[] args) {

        int port;

        if(args.length > 0){
            try{
                port = Integer.parseInt(args[0]);
                queryPeer(port);
            }catch (NumberFormatException e){
                System.out.println("Expected integer or no arguments");
                System.out.println("Usage: [Node port]");
            }
        }else{
            for(int i = 0; i < 100; i++){
                port = MIN_PORT + i;
                queryPeer(port);
            }
        }
    }

    private static void queryPeer(int port){
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
            System.out.println("Node " + port + " has " + localPeers.size() + " local peer connections.");
            s.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error occurred");
        }
    }

}
