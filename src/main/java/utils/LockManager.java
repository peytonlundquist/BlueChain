package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LockManager {
    private final Map<String, Object> locks = new HashMap<>();

    public Object getLock(String lockName) {
        return locks.get(lockName);
    }

    public void addLock(String lock){
        this.locks.put(lock, new Object());
    }

    public void addLocks(Set<String> locks){
        for(String lock : locks){
            this.locks.put(lock, new Object());
        }
    }

    public LockManager(Set<String> locks){
        addLocks(locks);
    }

    public LockManager(){};
}
