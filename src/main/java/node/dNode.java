package node;
/**
 * A dNode is a deviation of the block node for the doctor stakeholder in prescription tracking
 * , a cooperating member within the PT network following this Quorum-based blockchain protocol
 * as implemented here.
 */

public class dNode extends Node {

    public dNode(String use, int port, int maxPeers, int initialConnections, int numNodes, int quorumSize, int minimumTransaction, int debugLevel) {
        super(use, port, maxPeers, initialConnections, numNodes, quorumSize, minimumTransaction, debugLevel);
        // Custom initialization for doctors' nodes
    }
    
    // Custom methods for doctors (e.g., createPrescription)
}
