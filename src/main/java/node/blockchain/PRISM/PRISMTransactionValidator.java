package node.blockchain.PRISM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import node.blockchain.Block;
import node.blockchain.TransactionValidator;
import node.blockchain.PRISM.RecordTypes.Project;
import node.blockchain.PRISM.RecordTypes.ProvenanceRecord;
import node.blockchain.PRISM.RecordTypes.Record.RecordType;
import node.communication.Address;

public class PRISMTransactionValidator extends TransactionValidator {

    public Map<Address, Float> calculateReputations(LinkedList<Block> blockchain, ArrayList<Address> globalPeers,
            float alpha, float beta, float gamma) {

        Map<Address, Float> reputations = new HashMap<>();
        for (Address address : globalPeers) {
            int blocksParticipated = 0;
            float accuracy = 0.0f;
            float time = 0.0f;
            int accuracyCount = 0;
            float minimumCorrectTime = Float.MAX_VALUE;

            for (Block block : blockchain) {
                
                block = (WorkflowTaskBlock ) block;
                
                if (prismTransaction.getSignatures().containsKey(address)) { // Assuming getSignatures() returns a
                                                                             // Map<Address, Signature>
                    blocksParticipated++;

                    MinerData minerData = PRISMTransaction.getMinerData(address); // Assuming getMinerData() returns
                                                                                  // the MinerData for an address
                    accuracy += minerData.getAccuracy(); // Assuming getCorrectness() returns the correctness value

                    if (minerData.getAccuracy() == 1) {
                        accuracyCount++;
                    }

                    if (minerData.getAccuracy() == 1 && minerData.getTimestamp() < minimumCorrectTime) {
                        minimumCorrectTime = minerData.getTimestamp();
                    }

                    time += minerData.getTimestamp() - minimumCorrectTime;
                }
            }
        }

        if (blocksParticipated > 0) {
            float rep = (alpha * accuracy + beta * time + gamma * ((float) accuracyCount / blocksParticipated))
                    / blocksParticipated;
            reputations.put(address, rep);
        }
    }

    return reputations;

    }

    public Float calculateReputation(Address address, float alpha, float beta, float gamma) {
        return null;
    }

    @Override
    public boolean validate(Object[] objects) {
        // TODO Auto-generated method stub
        // Here we can check what the RecordType is and validate it this way.
        PRISMTransaction transaction = (PRISMTransaction) objects[0];
        if (transaction.getRecord().getRecordType().equals(RecordType.ProvenanceRecord)) {
            return true;// Eventually, we want to check if a node has enough of a reputation to propose
                        // a transaction.
        } else if (transaction.getRecord().getRecordType().equals(RecordType.Project)) {
            Project project = (Project) transaction.getRecord();

            if (calculateReputation(project.getAuthors()[0], 0, 0, 0) > 0.75) {
                return true; // Same is true here
            }

            return false;

        }

        return false;

    }

}
