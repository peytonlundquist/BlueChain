package communication;

import node.Node;
import utils.Address;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import communication.*;
import communication.messaging.Message;

/**
 * Attempts to establish bidirectional connection to specified amount of peers
 */
public class ClientConnection extends Thread {
    private final Node node;
    private final ArrayList<Address> globalPeers;

    public ClientConnection(Node node, ArrayList<Address> globalPeers) throws SocketException {
        this.node = node;
        this.globalPeers = globalPeers;
        setPriority(NORM_PRIORITY - 1);
    }

    public void run() {
        if (node.getLocalPeers().size() < node.getMaxPeers()) {
            for (Address address : globalPeers) {
                if (node.getLocalPeers().size() >= node.getMaxPeers()){
                    break;
                }
                try {
                    if (node.eligibleConnection(address, false)) {
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
                            if (node.getLocalPeers().size() == node.getMinConnections()) {
                                return;
                            }
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
