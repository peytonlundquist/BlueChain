/**
 * This abstract class is the base class for all events that can be tracked in the healthcare blockchain.
 * It contains an enum for the action that the event represents and a constructor to set the action.
 * This class is extended by the Appointment, Prescription, Record_Update, and Create_Patient classes.
 * 
 * @date 03-20-2021
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
}
