package blockchain;
import java.io.Serializable;

import utils.Address;

/**
 * The BlockSignature class represents the digital signature associated with a block in a blockchain.
 */
public class BlockSignature implements Serializable {
    private byte[] signature;
    private String hash;
    private Address address;

    /**
     * Constructs a BlockSignature instance with the specified parameters.
     *
     * @param signature The digital signature associated with the block.
     * @param hash      The hash value of the block.
     * @param address   The address of the signer.
     */
    public BlockSignature(byte[] signature, String hash, Address address) {
        this.signature = signature;
        this.hash = hash;
        this.address = address;
    }

    /**
     * Gets the digital signature associated with the block.
     *
     * @return The signature as a byte array.
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Gets the hash value of the block.
     *
     * @return The hash value as a string.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Gets the address of the signer.
     *
     * @return An Address object representing the signer's address.
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Returns a string representation of the BlockSignature, showing the first 4 characters
     * of the hash and the signer's address.
     *
     * @return A string representation of the BlockSignature.
     */
    public String toString(){
        return hash.substring(0, 4) + ", " + address.toString();
    }
}