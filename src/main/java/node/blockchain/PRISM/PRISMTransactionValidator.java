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

    private float alpha = 1;
    private float beta = 1;
    private float gamma = 1;

    /**
     * Called everytime a block is added
     * 
     * @param repData
     * @param globalPeers
     * @param alpha
     * @param beta
     * @param gamma
     * @return
     */
    public HashMap<Address, RepData> calculateReputationsData(Block block, HashMap<Address, RepData> repData) {

        for (String txHash : block.getTxList().keySet()) { // For each transaction in that block
            PRISMTransaction PRISMtx = (PRISMTransaction) block.getTxList().get(txHash); // Initialize PRISMTransaction

            if (PRISMtx.getRecord() instanceof ProvenanceRecord) { // If that PRISMtx contains a ProvenanceRecord (which
                                                                   // is should since it's a WorkFlowTaskBlock)
                ProvenanceRecord pr = (ProvenanceRecord) PRISMtx.getRecord();

                for (MinerData minerData : pr.getMinerData()) { // Get the miner data in that ProvenanceRecord
                    RepData minerRepData = repData.get(minerData.getAddress()); // Get the existing reputation data for
                                                                                // the miner

                    minerRepData.addTimeSummation(minerData.getTimestamp() - pr.getMinimumCorrectTime()); // Update time
                                                                                                          // summation
                    minerRepData.addBlocksParticipated(); // Increment the block participation count (Phi)
                    minerRepData.addAccuracySummation(minerData.getAccuracy()); // Update accuracy summation (A)

                    if (minerData.getAccuracy() == 1)
                        minerRepData.addAccuracyCount(); // Increment the accuracy count (T) if accuracy is 1

                    minerRepData.setCurrentReputation(calculateReputation(minerRepData)); // Calculate current
                                                                                          // reputation
                    repData.put(minerData.getAddress(), minerRepData); // Update the reputation data for the miner
                }
            }
        }
        return repData; // Return the modified reputation data
    }

    public Map<Address, RepData> calculateReputationData(Block block, Address targetAddress, Map<Address, RepData> repData) {
        for (String txHash : block.getTxList().keySet()) { // For each transaction in that block
            PRISMTransaction PRISMtx = (PRISMTransaction) block.getTxList().get(txHash); // Initialize PRISMTransaction

            if (PRISMtx.getRecord() instanceof ProvenanceRecord) { // If that PRISMtx contains a ProvenanceRecord (which
                                                                   // it should since it's a WorkFlowTaskBlock)
                ProvenanceRecord pr = (ProvenanceRecord) PRISMtx.getRecord();

                for (MinerData minerData : pr.getMinerData()) { // Get the miner data in that ProvenanceRecord
                    if (minerData.getAddress().equals(targetAddress)) { // Only update data for the target miner
                        RepData minerRepData = repData.get(minerData.getAddress()); // Get the existing reputation data
                                                                                    // for the miner

                        minerRepData.addTimeSummation(minerData.getTimestamp() - pr.getMinimumCorrectTime()); // Update
                                                                                                              // time
                                                                                                              // summation
                        minerRepData.addBlocksParticipated(); // Increment the block participation count (Phi)
                        minerRepData.addAccuracySummation(minerData.getAccuracy()); // Update accuracy summation (A)

                        if (minerData.getAccuracy() == 1)
                            minerRepData.addAccuracyCount(); // Increment the accuracy count (T) if accuracy is 1

                        minerRepData.setCurrentReputation(calculateReputation(minerRepData)); // Calculate current
                                                                                              // reputation
                        repData.put(minerData.getAddress(), minerRepData); // Update the reputation data for the miner
                    }
                }
            }
        }
        return repData; // Return the modified reputation data
    }

    public float calculateReputation(RepData repData) {
        return (((alpha * repData.getAccurarySummation())
                + (beta * repData.getTimeSummation()))
                + (gamma * ((float) repData.getAccuracyCount() / repData.blocksParticipated)))
                / repData.blocksParticipated; // Calculate the reputation score and return it
    }

    public boolean validate(Object[] objects, HashMap<Address, RepData> repData) {
        // TODO Auto-generated method stub
        // Here we can check what the RecordType is and validate it this way.
        PRISMTransaction transaction = (PRISMTransaction) objects[0];
        if (transaction.getRecord().getRecordType().equals(RecordType.ProvenanceRecord)) {
            return true;// Eventually, we want to check if a node has enough of a reputation to propose
                        // a transaction.
        } else if (transaction.getRecord().getRecordType().equals(RecordType.Project)) {
            Project project = (Project) transaction.getRecord();

            if (repData.get(project.getAuthors()[0]).currentReputation > 0.5) {
                return true;
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
