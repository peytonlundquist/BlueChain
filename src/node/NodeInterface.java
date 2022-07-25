package node;

import node.communication.Address;

import java.util.ArrayList;

public interface NodeInterface {
    void requestConnections();

    void requestConnections(ArrayList<Address> globalPeers);

    void addBlock();
    boolean validateBlock();

    void establishConnection(Address address);

    void searchForPeers();
}
