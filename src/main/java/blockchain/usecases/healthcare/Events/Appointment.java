/**
 * Class to represent an appointment event. This event tracks the appointment date,
 * location, and provider. It also has helper methods for getting the date, location,
 * and provider.
 * 
 * @date 03-20-2021
 */

package blockchain.usecases.healthcare.Events;

import java.util.Date;

import blockchain.usecases.healthcare.Event;

public class Appointment extends Event {

    private Date date;
    private String location;
    private String provider;
    
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

    /**
     * This constructor sets the date, location, and provider of the appointment.
     * @param date The date of the appointment.
     * @param location The location of the appointment.
     * @param provider The provider of the appointment.
     */
    public Appointment(Date date, String location, String provider) {
        super(Action.Appointment);
        this.date = date;
        this.location = location;
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "Appointment: " + date + " at " + location + " with " + provider;
    }
}