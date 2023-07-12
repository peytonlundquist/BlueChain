package node.blockchain.PRISM;

import node.communication.Address;

public class MinerData {
    
    private Address address;
    public Address getAddress() {
        return address;
    }


    public void setAddress(Address address) {
        this.address = address;
    }

    private long timestamp; // assuming this is a Unix timestamp
    private int accuracy; // either -1 or 1
    private String outputHash;

    public MinerData(Address address, long timestamp, int accuracy, String outputHash) {
        this.address = address;
        this.timestamp = timestamp;
        this.accuracy = accuracy;
        this.outputHash = outputHash;
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