package blockchain.usecases.healthcare.Events;

import blockchain.usecases.healthcare.Event;
import blockchain.usecases.healthcare.Patient;

/**
 * This class is an event that will be stored in a transaction in the blockchain. It
 * represents that a new patient was created.
 */
public class CreatePatient extends Event {
    private Patient patient;
    
    /**
     * This constructor instantiates a new patient to be stored.
     * @param patient The patient to be stored.
     */
    public CreatePatient(Patient patient){
        super(Action.Create_Patient);
        this.patient = patient;
    }

    /**
     * This method returns the patient in this event.
     * @return The patient in this event.
     */
    public Patient getPatient(){
        return patient;
    }

    @Override
    public String toString() {
        return "Create Patient: " + patient.getFirstName() + " | " + patient.getLastName() + " | " + patient.getUID();
    }
}
