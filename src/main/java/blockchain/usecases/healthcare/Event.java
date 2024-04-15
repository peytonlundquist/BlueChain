/**
 * This abstract class is the base class for all events that can be tracked in the healthcare blockchain.
 * Any class that extends this one is used to store in the blockchain. Refer to the Events file for examples.
 */

package blockchain.usecases.healthcare;

import java.io.Serializable;

public abstract class Event implements Serializable{

    public enum Action {
        Appointment,
        Prescription,
        Record_Update,
        Create_Patient
    }

    private Action action;

    /**
     * This method returns the action that the event represents.
     * @return The action that the event represents.
     */
    public Action getAction() {
        return action;
    }

    /**
     * This constructor sets the action that the event represents.
     * @param action The action that the event represents.
     */
    public Event(Action action){
        this.action = action;
    }

    public String toString(){
        return "";
     };
}
