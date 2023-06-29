package node.blockchain.healthcare.Events;

import java.sql.Date;

import node.blockchain.healthcare.Event;

public class Prescription extends Event{

    private Date date;
    private int amount;
    private String medication;
    private String provider;
    
    public int getAmount() {
        return amount;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Date getDate() {
        return date;
    }

    public String getMedication() {
        return medication;
    }

    public Prescription(String patientUID, String medication, Date date, int amount) {
        super(patientUID, Action.Prescription);
        this.date = date;
        this.amount = amount;
        this.medication = medication;
    }
    
}
