package blockchain.usecases.healthcare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class Patient implements Serializable{

    private String UID;

    /* Static Fields */
    private String firstName;
    private String lastName;
    private Date dob;

    /* Non-static *fields in the form of a map, with the key being something
     * like weight. We want a map because there may be many fields / records
     * a doctor may want to update the patients file with that we can't
     * predict
    */
    HashMap<String, String> fields;
    ArrayList<Event> events;

    public Patient(String fName, String lName, Date dob){
        this.firstName = fName;
        this.lastName = lName;
        this.dob = dob;
        this.fields = new HashMap<String, String>();
        this.events = new ArrayList<Event>();
        this.UID = UUID.randomUUID().toString().replace("-", "");
    }

    public String getUID(){
        return UID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public void addField(String key, String value){
        fields.put(key, value);
    }

    public void addEvent(Event event){
        events.add(event);
    }

    public ArrayList<Event> getEvents(){
        return events;
    }

    public HashMap<String, String> getFields(){
        return fields;
    }

    @Override
    public String toString() {
        return "Name: " + this.firstName + " " + this.lastName + " | DOB: " + this.dob.toString() + " | UID: " + this.UID;
        // Also return the fields entered by doctors
    }
}