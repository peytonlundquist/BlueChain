package node.communication;

import node.blockchain.BlockSkeleton;

import java.io.Serializable;

public class BlockSignature implements Serializable {
    private byte[] signature;
    private String hash;

    private Address address;
    public BlockSignature(byte[] signature, String hash, Address address) {
        this.signature = signature;
        this.hash = hash;
        this.address = address;
    }

    public byte[] getSignature() {
        return signature;
    }

    public String getHash() {
        return hash;
    }

    public Address getAddress() {
        return address;
    }

    public String toString(){
        return hash.substring(0, 4) + ", " + address.toString();
    }
}