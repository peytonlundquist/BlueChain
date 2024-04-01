/**
 * This class contains a transaction for the healthcare blockchain. It's contents consist of the
 * event and the UID of the patient. The toString represents the transaction stored in a block back
 * to the user.
 * 
 * @date 03-20-2024
 */

package blockchain.usecases.healthcare;

import blockchain.Transaction;
import utils.Hashing;

public class HCTransaction extends Transaction {

    private Event event;
    private String patientUID;


    /**
     * Constructs a HCTransaction instance. Assigns the UID as a hash of the patient UID,
     * the event action, and the current time in milliseconds (to make sure theres no repeats). 
     * @param event The event of the transaction.
     * @param patientUID The UID of the patient.
     */
    public HCTransaction(Event event, String patientUID){
        this.event = event;
        this.patientUID = patientUID;

        // Create unique identifier for the transaction assigned to UID
        UID = Hashing.getSHAString(patientUID + event.hashCode() + timestamp);
    }

    /**
     * This method returns the event of the transaction.
     * @return The event of the transaction.
     */
    public Event getEvent() {
        return event;
    }

    /**
     * This method returns the UID of the patient.
     * @return The UID of the patient.
     */
    public String getPatientUID() {
        return patientUID;
    }

    /**
     * This method returns a string representation of the transaction.
     * @return The string representation of the transaction.
     */
    @Override
    public String toString() {
        // Set the string representation of the transaction
        return event.getAction().name() + " ID: " + UID;
    }    
}