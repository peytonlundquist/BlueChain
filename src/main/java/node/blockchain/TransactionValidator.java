package node.blockchain;

public abstract class TransactionValidator {
    /**
     * Validates a transaction throughout blockchain and mempool
     * @param transaction
     * @return
     */
    abstract public boolean validate(Object[] objects);
}
