package blockchain.usecases.healthcare.Events;

import java.util.Date;

import blockchain.usecases.healthcare.Event;

/**
 * This class is an event that will be stored in a transaction in the blockchain. It
 * represents an appointment with a healthcare provider.
 */
public class Appointment extends Event {

    private Date date;
    private String location;
    private String provider;

    /**
     * This constructor sets the date, location, and provider of the appointment.
     * @param date The date and time of the appointment.
     * @param location The location of the appointment.
     * @param provider The provider overseeing appointment.
     */
    public Appointment(Date date, String location, String provider) {
        super(Action.Appointment);
        this.date = date;
        this.location = location;
        this.provider = provider;
    }
    
    /**
     * This method returns the provider of the appointment.
     * @return The provider of the appointment.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * This method returns the date of the appointment.
     * @return The date of the appointment.
     */
    public Date getDate() {
        return date;
    }

    /**
     * This method returns the location of the appointment.
     * @return The location of the appointment.
     */
    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "Appointment: " + date + " at " + location + " with " + provider;
    }
}