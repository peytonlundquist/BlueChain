import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Handles one connection in a separate thread.
 */
class ClientConnection extends Thread {

    private Node node;
    private ArrayList<Address> globalPeers;
    private int maxPeers;

    ClientConnection(Node node, ArrayList<Address> globalPeers) throws SocketException {
        this.node = node;
        this.globalPeers = globalPeers;
        setPriority(NORM_PRIORITY - 1);
        System.out.println("Created client thread " + this.getName());

    }

    public void run() {
        maxPeers = node.getMaxPeers();
        ArrayList<Address> potentialPeers = globalPeers;
        while(true) {
            if (node.getLocalPeers().size() < node.getMaxPeers()) {
                for (Address address : potentialPeers) {
                    try {
                        if (!address.equals(node.getAddress()) && !node.getLocalPeers().contains(address)) {
                            Socket s = new Socket(address.getHost(), address.getPort());
                            InputStream in = s.getInputStream();
                            ObjectInputStream oin = new ObjectInputStream(in);
                            OutputStream out = s.getOutputStream();
                            ObjectOutputStream oout = new ObjectOutputStream(out);

                            Message message = new Message(Message.Request.REQUEST_CONNECTION, node.getAddress());
                            oout.writeObject(message);
                            oout.flush();
                            Message messageReceived = (Message) oin.readObject();


                            if (messageReceived.getRequest().equals(Message.Request.ACCEPT_CONNECTION)) {
                                node.establishConnection(address);
                                System.out.println("client estab");
                            } else if (messageReceived.getRequest().equals(Message.Request.REJECT_CONNECTION)) {
                                potentialPeers.remove(address);
                            }
                            s.close();
                        }
                    } catch (ConnectException e0) {

                    } catch (IOException e1) {
                        System.out.println(e1);
                    } catch (ClassNotFoundException e2) {
                        System.out.println(e2);
                    }
                }
            }
        }
    }
}
