/**
 * Class to represent the event of a record update. This event updates a existing
 * or new record of a patient. It contains the date, key, and value of the record
 * 
 * @date 03-20-2021
 */

package blockchain.usecases.healthcare.Events;

import java.util.Date;

import blockchain.usecases.healthcare.Event;

public class RecordUpdate extends Event{

    private Date date;
    private String key;
    private String value;


    /**
     * This method returns the date of the record update.
     * @param date The date of the record update.
     * @param key The key of the record update.
     * @param value The value of the record update.
     */
    public RecordUpdate(Date date, String key, String value) {
        super(Action.Record_Update);
        this.date = date;
        this.key = key;
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
}
