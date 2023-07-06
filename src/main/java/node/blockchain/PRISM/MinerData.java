package node.blockchain.PRISM;

public class MinerData {
    
    private byte[] signature;
    private long timestamp; // assuming this is a Unix timestamp
    private int accuracy; // either -1 or 1
    private String outputHash;

    public MinerData(byte[] signature, long timestamp, int accuracy, String outputHash) {
        this.signature = signature;
        this.timestamp = timestamp;
        this.accuracy = accuracy;
        this.outputHash = outputHash;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[]  signature) {
        this.signature = signature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int correctness) {
        this.accuracy = correctness;
    }
}
