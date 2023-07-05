
package node.blockchain.prescription.Events;

import java.util.Date;

import node.blockchain.prescription.Event;

public class FillScript extends Event {

    private Date date;
    private int quantity;
    private String medication;
    private int dosage;
    private String doctorName;

    public Date getDate() {
        return this.date;
    }

    public int getQuantity() {
        return this.quantity;
    }


    public String getMedication() {
        return this.medication;
    }


    public int getDosage() {
        return this.dosage;
    }


    public String getDoctorName() {
        return this.doctorName;
    }

    public FillScript(Date date, String patientUID, String doctorName, String medication, int dosage, int quantity) {
        super(patientUID, Action.FillScript);
        this.date = date;
        this.doctorName = doctorName;
        this.quantity = quantity;
        this.medication = medication;
    }
}
