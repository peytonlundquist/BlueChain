package blockchain;

import java.io.Serializable;

/**
 * The Transaction class is an abstract class representing a generic transaction in a blockchain.
 * It provides common attributes and methods that are shared among different types of transactions.
 */
public abstract class Transaction implements Serializable{

    /** The timestamp indicating when the transaction was created. */
    protected String timestamp;

    /** The unique identifier (UID) for the transaction. */
    protected String UID;

    /** The digital signature associated with the transaction's UID. */
    protected byte[] sigUID;

    /**
     * Sets the digital signature for the transaction's UID.
     *
     * @param sig The digital signature to set.
     */
    public void setSigUID(byte[] sig){
        sigUID = sig;
    }

    /**
     * Retrieves the digital signature associated with the transaction's UID.
     *
     * @return The digital signature.
     */
    public byte[] getSigUID(){
        return sigUID;
    }

    /**
     * Retrieves the unique identifier (UID) for the transaction.
     *
     * @return The transaction's UID.
     */
    public String getUID(){
        return timestamp + toString();
    }

    /**
     * Retrieves the timestamp indicating when the transaction was created.
     *
     * @return The transaction timestamp.
     */
    public String getTimestamp(){
        return timestamp;
    }

    /**
     * Checks if two transactions are equal by comparing their UIDs.
     *
     * @param transaction The transaction to compare.
     * @return true if the UIDs are equal, false otherwise.
     */
    public boolean equals(Transaction transaction){
        if(transaction.getUID().equals(this.getUID())){
            return true;
        }
        return false;
    }

    /**
     * Abstract method to be implemented by concrete transaction classes.
     * Converts the transaction to a string representation.
     *
     * @return A string representation of the transaction.
     */
    abstract public String toString();
}
