package node.blockchain;

import java.util.HashMap;

public abstract class TransactionValidator {
    /**
     * Validates a transaction throughout blockchain and mempool
     * @param transaction
     * @return
     */
    abstract public boolean validate(Transaction transaction, HashMap<String, Integer> accounts, HashMap<String, Transaction> assumedTransactions);
}
