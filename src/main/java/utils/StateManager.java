package utils;

public class StateManager {
    private int state;
    private LockManager lockManager;

    public StateManager(){
        state = 0;
        lockManager = new LockManager();
        lockManager.addLock("state_lock");
    }

    public void stateChangeRequest(int statetoChange){
        synchronized(lockManager.getLock("state_lock")){
            state = statetoChange;
        }
    }

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
