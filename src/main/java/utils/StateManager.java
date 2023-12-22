package utils;

/**
 * The StateManager class is responsible for managing the state transitions
 * within the system. It provides methods to change the current state and
 * wait for a specific state to be reached.
 */
public class StateManager {
    private int state;
    private LockManager lockManager;

    /**
     * Constructs a new StateManager with an initial state of 0 and initializes
     * the required lock for state synchronization.
     */
    public StateManager(){
        state = 0;
        lockManager = new LockManager();
        lockManager.addLock("state_lock");
    }

    /**
     * Requests a change in the system state. The method is synchronized to ensure
     * thread-safe state updates.
     *
     * @param newState The state to transition to.
     */
    public void stateChangeRequest(int statetoChange){
        synchronized(lockManager.getLock("state_lock")){
            state = statetoChange;
        }
    }

    /**
     * Waits for the system to reach a specific state. This method continuously
     * checks the current state and sleeps for 1000 milliseconds between checks
     * until the desired state is reached.
     *
     * @param desiredState The state to wait for.
     */
    public void waitForState(int desiredState){
        while(state != desiredState){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
