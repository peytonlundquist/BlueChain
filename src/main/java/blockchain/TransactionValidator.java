package blockchain;

/**
 * The TransactionValidator class is an abstract class representing a validator for transactions
 * throughout the blockchain and mempool. Concrete implementations of this class are responsible
 * for defining the validation logic for specific types of transactions.
 */
public abstract class TransactionValidator {
    /**
     * Validates a transaction throughout the blockchain and mempool.
     *
     * @param objects An array of objects containing information needed for validation.
     *                The contents of the array depend on the specific implementation.
     * @return true if the transaction is valid, false otherwise.
     */
    abstract public boolean validate(Object[] objects);
}
