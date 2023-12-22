package blockchain;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The Block class is an abstract class representing a generic block in a blockchain.
 * It provides common attributes and methods that are shared among different types of blocks.
 */
public abstract class Block implements Serializable {
    
    /** The unique identifier for the block. */
    protected int blockId;

    /** The list of transactions contained in the block. */
    protected HashMap<String, Transaction> txList;

    /** The hash of the previous block in the blockchain. */
    protected String prevBlockHash;

    /** The hash of the Merkle root of the transactions in the block. */
    protected String merkleRootHash;


    /**
     * Retrieves the Merkle root hash of the transactions in the block.
     *
     * @return The Merkle root hash of the transactions.
     */
    public String getMerkleRootHash() {
        return merkleRootHash;
    }

    /**
     * Retrieves the unique identifier of the block.
     *
     * @return The block identifier.
     */
    public int getBlockId() {
        return blockId;
    }

    /**
     * Retrieves the list of transactions in the block.
     *
     * @return The list of transactions.
     */
    public HashMap<String, Transaction> getTxList() {
        return txList;
    }

    /**
     * Retrieves the hash of the previous block in the blockchain.
     *
     * @return The hash of the previous block.
     */
    public String getPrevBlockHash() {
        return prevBlockHash;
    }

    /**
     * Sets the Merkle root hash for the block.
     *
     * @param rootHash The Merkle root hash to set.
     */
    public void setMerkleRootHash(String rootHash){
        this.merkleRootHash = rootHash;
    }
}
