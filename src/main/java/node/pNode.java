package node;

public class pNode extends Node {

    public pNode(String use, int port, int maxPeers, int initialConnections, int numNodes, int quorumSize, int minimumTransaction, int debugLevel) {
        super(use, port, maxPeers, initialConnections, numNodes, quorumSize, minimumTransaction, debugLevel);
        // Custom initialization for patient nodes
    }
    
    // Custom methods for patients (e.g., viewPrescriptionHistory)
}

