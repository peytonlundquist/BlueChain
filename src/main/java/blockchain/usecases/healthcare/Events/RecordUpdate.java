package blockchain.usecases.healthcare.Events;

import java.util.Date;

import blockchain.usecases.healthcare.Event;

/**
 * This class is an event that will be stored in a transaction in the blockchain. It
 * represents a update of a patients record. For example, A patient's height, weight,
 * blood pressure, etc.
 */
public class RecordUpdate extends Event{
    
    private Date date;
    private String key;
    private String value;

    /**
     * This constructor sets the date, key, and value of the record update.
     * @param date The date of the record update.
     * @param record The record to be updated.
     * @param value The new value of the record.
     */
    public RecordUpdate(Date date, String record, String value) {
        super(Action.Record_Update);
        this.date = date;
        this.key = record;
        this.value = value;
    }
    
    /**
     * This method returns the date of the record update.
     * @return The date of the record update.
     */
    public Date getDate() {
        return date;
    }

    /**
     * This method returns the key of the record update.
     * @return The key of the record update.
     */
    public String getKey() {
        return key;
    }

    /**
     * This method returns the value of the record update.
     * @return The value of the record update.
     */
    public String getValue() {
        return value;
    }

    public String toString(){
        return "Record Update: " + date.toString() + " | " + key + " | " + value;
    }
}
