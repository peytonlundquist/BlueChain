package node.blockchain.PRISM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import node.blockchain.Block;
import node.blockchain.Transaction;
import node.blockchain.TransactionValidator;
import node.blockchain.PRISM.RecordTypes.Project;
import node.blockchain.PRISM.RecordTypes.ProvenanceRecord;
import node.blockchain.PRISM.RecordTypes.Record.RecordType;
import node.communication.Address;

public class PRISMTransactionValidator extends TransactionValidator {

    /**
     * Called everytime a block is added
     * @param repData
     * @param globalPeers
     * @param alpha
     * @param beta
     * @param gamma
     * @return
     */

    public HashMap<Address, Float> calculateReputations(Block block, HashMap<Address, ArrayList<Float>> repData, ArrayList<Address> globalPeers,
            float alpha, float beta, float gamma) {

        HashMap<Address, Float> reputations = new HashMap<>();

        for (String txHash : block.getTxList().keySet()) { // For each transaction in that block
            Float myTime = 0f;
            PRISMTransaction PRISMtx = (PRISMTransaction) block.getTxList().get(txHash); // Initialize
                                                                                            // PRISMTransaction
            if (PRISMtx.getRecord() instanceof ProvenanceRecord) { // If that PRISMtx contains a
                                                                    // ProvenanceRecord (which is should
                                                                    // since its a WorkFlowTaskBlock)
                ProvenanceRecord pr = (ProvenanceRecord) PRISMtx.getRecord();
                for (MinerData minerData : pr.getMinerData()) { // Get the miner data in that
                    int blocksParticipated = 0;
                    float accuracy = 0.0f;
                    float time = 0.0f;
                    int accuracyCount = 0;
                    time = myTime - pr.getMinimumCorrectTime();

                    blocksParticipated++; // For Phi
                    accuracy += minerData.getAccuracy(); // Summation of A
                    if (accuracy == 1) accuracyCount++;
                    myTime += minerData.getTimestamp(); // Summation of T

                    if (blocksParticipated > 0) {
                    float rep = (((alpha * accuracy) + (beta * time))
                            + (gamma * ((float) accuracyCount / blocksParticipated)))
                            / blocksParticipated;
                    reputations.put(minerData.getAddress(), rep);
                    repData.put(minerData.getAddress(), new ArrayList<>(){blocksParticipated})
                    }

                }
            }

        }
        return reputations;
    }

    public Float calculateReputation(Address address, LinkedList<Block> blockchain, float alpha, float beta,
            float gamma) {

        int blocksParticipated = 0;
        float accuracy = 0.0f;
        float time = 0.0f;
        int accuracyCount = 0;

        for (Block block : blockchain) { // For each block
            if (block instanceof WorkflowTaskBlock) { // If that block is a WTB
                for (String txHash : block.getTxList().keySet()) { // For each transaction in that block
                    Float myTime = 0f;
                    PRISMTransaction PRISMtx = (PRISMTransaction) block.getTxList().get(txHash); // Initialize
                                                                                                 // PRISMTransaction
                    if (PRISMtx.getRecord() instanceof ProvenanceRecord) { // If that PRISMtx contains a
                                                                           // ProvenanceRecord (which is should
                                                                           // since its a WorkFlowTaskBlock)
                        ProvenanceRecord pr = (ProvenanceRecord) PRISMtx.getRecord();
                        for (MinerData minerData : pr.getMinerData()) { // Get the miner data in that
                                                                        // provenanceRecord
                            if (minerData.getAddress() == address) {
                                blocksParticipated++; // For Phi
                                accuracy += minerData.getAccuracy(); // Summation of A
                                if (accuracy == 1)
                                    accuracyCount++;
                                myTime += minerData.getTimestamp(); // Summation of T
                            }

                        }
                        time = myTime - pr.getMinimumCorrectTime();
                    }

                }
            }

        }
        if (blocksParticipated > 0) {
            float rep = (((alpha * accuracy) + (beta * time)) + (gamma * ((float) accuracyCount / blocksParticipated)))
                    / blocksParticipated;
            return rep;
        }
        return 0f;
    }

    
    public boolean validate(Object[] objects, LinkedList<Block> blockchain) {
        // TODO Auto-generated method stub
        // Here we can check what the RecordType is and validate it this way.
        PRISMTransaction transaction = (PRISMTransaction) objects[0];
        if (transaction.getRecord().getRecordType().equals(RecordType.ProvenanceRecord)) {
            return true;// Eventually, we want to check if a node has enough of a reputation to propose
                        // a transaction.
        } else if (transaction.getRecord().getRecordType().equals(RecordType.Project)) {
            Project project = (Project) transaction.getRecord();

            if (calculateReputation(project.getAuthors()[0], blockchain, 0, 0, 0) > 0.75) {
                return true; // Same is true here
            }

            return false;

        }

        return false;

    }

    @Override
    public boolean validate(Object[] objects) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validate'");
    }

}
