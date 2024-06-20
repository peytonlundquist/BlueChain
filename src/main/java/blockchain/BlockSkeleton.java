package blockchain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The BlockSkeleton class represents a simplified version of a block containing essential
 * information. This class is used during the process of propagating block information within 
 * a blockchain network.
 */
public class BlockSkeleton implements Serializable{
    private final int blockId;
    private String hash;
    private final ArrayList<String> keys;
    private ArrayList<BlockSignature> signatures;

    /**
     * Constructs a BlockSkeleton instance with the specified parameters.
     *
     * @param blockId    The unique identifier of the block.
     * @param keys       The list of transaction keys included in the block.
     * @param signatures The list of signatures associated with the block.
     * @param hash       The hash value of the block.
     */
    public BlockSkeleton (int blockId, ArrayList<String> keys, ArrayList<BlockSignature> signatures, String hash){
        this.keys = keys;
        this.blockId = blockId;
        this.signatures = signatures;
        this.hash = hash;
    }

    /**
     * Gets the list of signatures associated with the block.
     *
     * @return An ArrayList of BlockSignature objects.
     */
    public ArrayList<BlockSignature> getSignatures() {return signatures;}

    /**
     * Gets the unique identifier of the block.
     *
     * @return The block ID.
     */
    public int getBlockId(){return blockId;}

    /**
     * Gets the list of transaction keys included in the block.
     *
     * @return An ArrayList of transaction keys.
     */
    public ArrayList<String> getKeys() {return keys;}

    /**
     * Gets the hash value of the block.
     *
     * @return The hash value.
     */
    public String getHash(){return hash;}
}

