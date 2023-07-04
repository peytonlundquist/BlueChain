package node.blockchain.PRISM;

public class MinerData {
    
    private String signature;
    private long timestamp; // assuming this is a Unix timestamp
    private int correctness; // either -1 or 1

    public MinerData(String signature, long timestamp, int correctness) {
        this.signature = signature;
        this.timestamp = timestamp;
        this.correctness = correctness;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCorrectness() {
        return correctness;
    }

    public void setCorrectness(int correctness) {
        this.correctness = correctness;
    }
}
