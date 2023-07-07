package node;

public class fNode extends Node {

    public fNode(String use, int port, int maxPeers, int initialConnections, int numNodes, int quorumSize, int minimumTransaction, int debugLevel) {
        super(use, port, maxPeers, initialConnections, numNodes, quorumSize, minimumTransaction, debugLevel);
        // Custom initialization for pharmacy nodes
    }
    
    // Custom methods for pharmacies (e.g., fulfillPrescription)
}
