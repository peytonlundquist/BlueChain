package node.blockchain.prescription;

import java.io.Serializable;

public class ValidationResult implements Serializable{
    private boolean isValid;
    private int algorithmSeed;
    private String ptTransactionHash;

    public String getPtTransactionHash() {
        return ptTransactionHash;
    }

    public ValidationResult(boolean isValid, int algorithmSeed, String ptTransactionHash) {
        this.isValid = isValid;
        this.algorithmSeed = algorithmSeed;
        this.ptTransactionHash = ptTransactionHash;
    }

    public boolean isValid() {
        return isValid;
    }

    public int getAlgorithmSeed() {
        return algorithmSeed;
    }

    public String getStringForSig(){
        return toString();
    }

    public String toString(){
        return isValid() + ", " + getAlgorithmSeed() +  ", " + getPtTransactionHash();
    }
}