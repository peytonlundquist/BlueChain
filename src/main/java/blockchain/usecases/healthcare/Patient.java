package blockchain.usecases.healthcare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * This class represents a patient in the healthcare system. This is where
 * all the patient's information is stored including events and records.
 */
public class Patient implements Serializable{

    /* static fields */
    private String UID;
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

        /* Generates a random ID for patient based on Java's UUID */
        this.UID = UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * This method returns the UID of the patient
     * @return The UID of the patient
     */
    public String getUID(){
        return UID;
    }

    /**
     * This method returns the first name of the patient
     * @return The first name of the patient
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * In case the patient changes their first name, this method
     * allows the first name to be updated.
     * @param firstName The new first name of the patient
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * This method returns the last name of the patient
     * @return The last name of the patient
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * In case the patient changes their last name, this method
     * allows the last name to be updated.
     * @param lastName The new last name of the patient
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * This method returns the date of birth of the patient
     * @return The date of birth of the patient
     */
    public Date getDob() {
        return dob;
    }

    /**
     * When the patients record gets updated, this method adds or
     * modifies a field in the patients record
     * @param key The record name to be updated/added
     * @param value The new value of the record
     */
    public void addField(String key, String value){
        fields.put(key, value);
    }

    /**
     * This method adds an event to the patient's history.
     * @param event The event to be added to the patient's history
     */
    public void addEvent(Event event){
        events.add(event);
    }

    /**
     * This method returns all the events in the patient's history
     * @return The events in the patient's history
     */
    public ArrayList<Event> getEvents(){
        return events;
    }

    /**
     * This method returns all the records of the patient
     * @return The records of the patient
     */
    public HashMap<String, String> getFields(){
        return fields;
    }

    @Override
    public String toString() {
        return "Name: " + this.firstName + " " + this.lastName + " | DOB: " + this.dob.toString() + " | UID: " + this.UID;
        // Also return the fields entered by doctors
    }
}