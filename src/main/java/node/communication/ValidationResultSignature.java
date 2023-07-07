package node.communication;
import java.io.Serializable;

import node.blockchain.prescription.ValidationResult;

public class ValidationResultSignature implements Serializable {
    private byte[] signature;
    private ValidationResult vr;
    private Address address;
    
    public ValidationResultSignature(byte[] signature, Address address, ValidationResult vr) {
        this.signature = signature;
        this.vr = vr;
        this.address = address;
    }

    public byte[] getSignature() {
        return signature;
    }

    public Address getAddress() {
        return address;
    }

    public ValidationResult getVr() {
        return vr;
    }

    public String toString(){
        return vr.isValid() + ", " + vr.getAlgorithmSeed() +  ", " + vr.getPtTransactionHash() +", " + address.toString();
    }
}