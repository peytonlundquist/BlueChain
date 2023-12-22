package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The LockManager class provides a mechanism for managing locks associated
 * with different entities in the system. It allows the creation, retrieval,
 * and initialization of locks for synchronization purposes.
 */
public class LockManager {
    private final Map<String, Object> locks = new HashMap<>();

    /**
     * Retrieves the lock associated with the specified name.
     *
     * @param lockName The name of the lock to retrieve.
     * @return The lock object associated with the given name.
     */
    public Object getLock(String lockName) {
        return locks.get(lockName);
    }

    /**
     * Adds a new lock with the specified name to the lock manager.
     *
     * @param lock The name of the lock to add.
     */
    public void addLock(String lock){
        this.locks.put(lock, new Object());
    }

    /**
     * Adds multiple locks to the lock manager.
     *
     * @param locks The set of lock names to add.
     */
    public void addLocks(Set<String> locks){
        for(String lock : locks){
            this.locks.put(lock, new Object());
        }
    }

    /**
     * Constructs a new LockManager with the specified set of locks.
     *
     * @param locks The set of locks to initialize the manager with.
     */
    public LockManager(Set<String> locks){
        addLocks(locks);
    }

    /**
     * Default constructor for the LockManager.
     */
    public LockManager(){};
}
