package blockchain.usecases.healthcare.Events;

import blockchain.usecases.healthcare.Event;
import blockchain.usecases.healthcare.Patient;

public class CreatePatient extends Event {
    private Patient patient;
    
    public CreatePatient(Patient patient){
        super(Action.Create_Patient);
        this.patient = patient;
    }

    public Patient getPatient(){
        return patient;
    }
}
