/**
 * This class represents the event of a prescription. This event tracks the date, medication, 
 * provider, and address of the prescription.
 * 
 * @date 03-20-2021
 */

package blockchain.usecases.healthcare.Events;

import java.util.Date;

import blockchain.usecases.healthcare.Event;

public class Prescription extends Event {

    private Date date;
    private int perscribedCount;
    private String medication;
    private String provider;
    private String address;

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
     * This method returns the date of the prescription.
     * @return The date of the prescription.
     */
    public Date getDate() {
        return date;
    }

    /**
     * This method returns the medication of the prescription.
     * @return The medication of the prescription.
     */
    public String getMedication() {
        return medication;
    }

    /**
     * This method returns the address of the prescription.
     * @return The address of the prescription.
     */
    public String getAddress() {
        return address;
    }

    /**
     * This constructor sets the medication, provider, address, date, and perscribed count of the prescription.
     * @param medication The medication of the prescription.
     * @param provider The provider of the prescription.
     * @param address The address of the prescription.
     * @param date The date of the prescription.
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
    
    public String toString() {
        return "Prescription: " + perscribedCount + "ct of " + medication + " prescribed by " + provider + " on " + date + " at " + address;
    }
}
