package node.blockchain.prescription;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// Ensure that Hashing is defined or imported
import node.communication.utils.Hashing;

public class Patient {


    /*
     * These are the static fields that shouldnt change.
     */
    private String name; //fields need to show that they aren't used so data isn't retrievable in security breach.
    private Date dob;
    private String UID;

    /*
     * non-static fields in hashmap for updating ex/ weight.
     */


    private LinkedHashMap<String, String> fields;

    public Patient(String name, Date dob) {
        this.name = name;
        this.dob = dob;
        this.fields = new LinkedHashMap<>(); //use linked hashmap to allow to get latest note or entry. Ask peyton if we would need to make new or reference how it was previously.
        this.UID = Hashing.getSHAString(name + dob.toString());
    }

    public String getUID() {
        return UID;
    }

    // Adds a note to the patient record with a key, for example, the date and time as the key
    public void addNote(String key, String note) {
        fields.put(key, note);
    }

    // Retrieves the latest note
    public String getLatestNote() {
        if (fields.isEmpty()) {
            return "No notes available";
        }
        String lastKey = null;
        for (String key : fields.keySet()) {
            lastKey = key;
        }
        return fields.get(lastKey);
    }

    // Adds multiple notes to the patient record
    public void addMultipleNotes(Map<String, String> notes) {
        fields.putAll(notes);
    }

    //search for note key field. O(1) avg case
    public String getNoteByKey(String key) {
    String note = fields.get(key);
    if (note == null) {
        return "No note found for the given key";
    } else {
        return note;
    }
}
/*test
// public static void main(String[] args) {
Map<String, String> notes = new HashMap<>();
notes.put("Note1", "This is note 1");
notes.put("Note2", "This is note 2");

Patient patient = new Patient("John Doe", new Date());
patient.addMultipleNotes(notes);
// }*/

    
}