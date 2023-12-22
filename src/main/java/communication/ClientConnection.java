package communication;

import node.Node;
import utils.*;
import java.net.SocketException;
import java.util.ArrayList;
import communication.messaging.Message;
import communication.messaging.Messager;
import communication.messaging.Message.Request;

/**
 * Attempts to establish bidirectional connections to a specified number of peers.
 * This thread iterates through the global peers, attempting to connect to them
 * based on eligibility criteria defined by the node. It establishes connections to
 * fulfill the minimum required connections while avoiding exceeding the maximum.
 */
public class ClientConnection extends Thread {
    private final Node node;
    private final ArrayList<Address> globalPeers;

    /**
     * Constructs a new ClientConnection instance.
     *
     * @param node The Node instance associated with this client connection.
     * @param globalPeers The list of global peers available for connection attempts.
     * @throws SocketException If an error occurs while creating the thread.
     */
    public ClientConnection(Node node, ArrayList<Address> globalPeers) throws SocketException {
        this.node = node;
        this.globalPeers = globalPeers;
        setPriority(NORM_PRIORITY - 1);
    }

    /**
     * Runs the ClientConnection thread. Iterates through global peers, attempts
     * to establish connections, and fulfills the minimum required connections.
     */
    public void run() {
        if (node.getLocalPeers().size() < node.getMaxPeers()) {
            for (Address address : globalPeers) {
                if (node.getLocalPeers().size() >= node.getMaxPeers()){
                    break;
                }
                if (Utils.eligibleConnection(node, address, false)) {                        
                    Message messageReceived = Messager.sendTwoWayMessage(address, new Message(Request.REQUEST_CONNECTION, node.getAddress()), node.getAddress());
                    if (messageReceived.getRequest().equals(Message.Request.ACCEPT_CONNECTION)) {
                        Utils.establishConnection(node, address);
                        if (node.getLocalPeers().size() == node.getMinConnections()) {
                            return;
                        }
                    }
                }
            }
        }
    }
}
