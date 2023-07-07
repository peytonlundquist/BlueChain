package node.blockchain.healthcare;

import java.util.Date;
import java.util.HashMap;

import node.communication.utils.Hashing;

public class Patient {

    private String UID;

    /* Static Fields */
    private String name;
    private Date dob;

    /* Non-static *fields in the form of a map, with the key being something
     * like weight. We want a map because there may be many fields / records
     * a doctor may want to update the patients file with that we can't
     * predict
    */
    HashMap<String, String> fields;

    public Patient(String name, Date dob){
        this.name = name;
        this.dob = dob;
        UID = Hashing.getSHAString(name + dob.toString());
    }

    public String getUID(){
        return UID;
    }
}