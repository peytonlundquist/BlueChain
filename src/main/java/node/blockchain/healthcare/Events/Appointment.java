package node.blockchain.healthcare.Events;

import java.util.Date;

import node.blockchain.healthcare.Event;

public class Appointment extends Event {

    private Date date;
    private String time;
    private String location;
    private String provider;
    
    public String getTime() {
        return time;
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

    public String getLocation() {
        return location;
    }

    public Appointment(String patientUID, Date date, String time, String location) {
        super(patientUID, Action.Appointment);
        this.date = date;
        this.time = time;
        this.location = location;
    }
}