public interface NodeInterface {
    void requestConnections();

    void addBlock();
    boolean validateBlock();

    void establishConnection(Address address);

    void searchForPeers();
}
