package blockchain.usecases.healthcare;

public abstract class Event {

    public enum Action {
        Appointment,
        Prescription,
        Record_Update
    }

    private String patientUID;
    private Action action;

    public String getPatientUID() {
        return patientUID;
    }

    public Action getAction() {
        return action;
    }

    public Event(String patientUID, Action action){
        this.patientUID = patientUID;
        this.action = action;
    }
}
