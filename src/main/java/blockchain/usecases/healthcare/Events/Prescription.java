package blockchain.usecases.healthcare.Events;

import java.util.Date;

import blockchain.usecases.healthcare.Event;

/**
 * This class is an event that will be stored in a transaction in the blockchain. It
 * represents a prescription for a medication issued from a healthcare provider.
 */
public class Prescription extends Event {

    private Date date;
    private int perscribedCount;
    private String medication;
    private String provider;
    private String address;

    /**
     * This constructor sets the medication, provider, address, date, and perscribed count of the prescription.
     * @param medication The medication name.
     * @param provider The provider of the prescription.
     * @param address The address of where the medication was perscribed.
     * @param date The date the prescription was issued.
     * @param perscribedCount The perscribed count of the medication.
     */
    public Prescription(String medication, String provider, String address, Date date, int perscribedCount) {
        super(Action.Prescription);
        this.date = date;
        this.perscribedCount = perscribedCount;
        this.provider = provider;
        this.address = address;
        this.medication = medication;
    }

    /**
     * This method returns the perscribed count of the medication.
     * @return The perscribed count of the medication.
     */
    public int getPerscribedCount() {
        return perscribedCount;
    }

    /**
     * This method returns the provider of the prescription.
     * @return The provider of the prescription.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * This method returns the date the perscription was issued.
     * @return The date of the prescription.
     */
    public Date getDate() {
        return date;
    }

    /**
     * This method returns the medication name.
     * @return The medication of the prescription.
     */
    public String getMedication() {
        return medication;
    }

    /**
     * This method returns where the medication was perscribed.
     * @return The address of the prescription.
     */
    public String getAddress() {
        return address;
    }
    
    public String toString() {
        return "Prescription: " + perscribedCount + "ct of " + medication + " prescribed by " + provider + " on " + date + " at " + address;
    }
}
