package node.blockchain.prescription.Events;

import java.util.Date;

import node.blockchain.prescription.Event;

public class Prescription extends Event {

    private Date date;
    private int amount;
    private String medication;
    private String doctorName;
    
    public int getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getMedication() {
        return medication;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public Prescription(String patientUID, String doctorName, String medication, Date date, int amount) {
        super(patientUID, Action.Prescription);
        this.doctorName = doctorName;
        this.date = date;
        this.amount = amount;
        this.medication = medication;
    }
    
}
