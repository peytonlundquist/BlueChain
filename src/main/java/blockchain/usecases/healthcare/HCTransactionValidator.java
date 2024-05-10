package blockchain.usecases.healthcare;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import blockchain.Transaction;
import blockchain.TransactionValidator;
import blockchain.usecases.healthcare.Events.*;
import communication.messaging.Message;
import communication.messaging.Messager;
import utils.Address;
import utils.LockManager;
import utils.merkletree.MerkleTree;

/**
 * This class is used to validate the transaction before it is added to the blockchain. It does so
 * by checking the event of the transaction and making sure all the data is not null. If the data is
 * null, then the transaction is not valid and will not be added to the blockchain.
 */
public class HCTransactionValidator extends TransactionValidator {

    private LockManager lockManager;
    private ArrayList<Address> clientsToAlert;
    private ArrayList<String> patientUIDs;

    public HCTransactionValidator() {
        this.lockManager = new LockManager();
        lockManager.addLock("eventsLock");
        this.patientUIDs = new ArrayList<String>();

        this.clientsToAlert = new ArrayList<Address>();
    }

    /**
     * This method validates the transaction before it is added to the blockchain. It does so by
     * checking the event of the transaction and making sure all the data is not null. If the data is
     * null, then the transaction is not valid and will not be added to the blockchain.
     * @param objects The objects to validate.
     * @return True if the transaction is valid, false otherwise.
     */
    @Override
    public boolean validate(Object[] objects) {
        HCTransaction transaction = (HCTransaction) objects[0];

        if(transaction.getEvent().getAction().name().equals("Appointment")){ // If the event is an appointment
            Appointment appointment = (Appointment) transaction.getEvent();

            // Checks to see if patient UID is within the list of patient UIDs
            if (!patientUIDs.contains(transaction.getPatientUID())) { return false; }

            // Checks to see if any data is null
            if (appointment.getDate() == null) { return false; }
            if (appointment.getProvider() == null) { return false; }
            if (appointment.getLocation() == null) { return false; }

            // Checks to see if the date is within the last 5 years
            String dateParts[] = appointment.getDate().toString().split(" ");
            String year = dateParts[5];
            if (Integer.parseInt(year) < LocalDate.now().getYear() - 5) { return false; }
            else if (Integer.parseInt(year) > LocalDate.now().getYear() + 5) { return false; }
            
        } else if (transaction.getEvent().getAction().name().equals("Prescription")){ // If the event is a prescription
            Prescription prescription = (Prescription) transaction.getEvent();

            if (!patientUIDs.contains(transaction.getPatientUID())) { return false; }

            // Checks to see if any data is null
            if (prescription.getDate() == null) { return false; }
            if (prescription.getMedication() == null) { return false; }
            if (prescription.getProvider() == null) { return false; }
            if (prescription.getAddress() == null) { return false; }

            // Checks to see if the date is within the last 5 years
            String dateParts[] = prescription.getDate().toString().split(" ");
            String year = dateParts[5];
            if (Integer.parseInt(year) < LocalDate.now().getYear() - 5) { return false; }
            else if (Integer.parseInt(year) > LocalDate.now().getYear() + 5) { return false; }

            // Checks to see if the prescribed count is less than or equal to 0
            if (prescription.getPerscribedCount() <= 0) { return false; }


        } else if (transaction.getEvent().getAction().name().equals("Record_Update")) { // If the event is a record update
            RecordUpdate recordUpdate = (RecordUpdate) transaction.getEvent();

            // Checks to see if any data is null
            if (!patientUIDs.contains(transaction.getPatientUID())) { return false; }

            // Checks to see if the record update is null
            if (recordUpdate.getKey() == null) { return false; }
            if (recordUpdate.getValue() == null) { return false; }

        } else if (transaction.getEvent().getAction().name().equals("Create_Patient")) {
            CreatePatient createPatient = (CreatePatient) transaction.getEvent();

            //Checks to see if any data is null
            if (createPatient.getPatient().getFirstName() == null) { return false; }
            if (createPatient.getPatient().getLastName() == null) { return false; }
            if (createPatient.getPatient().getDob() == null) { return false; }
            
            //Checks the date of birth to ensure that it is within the last 200 years
            String dateParts[] = createPatient.getPatient().getDob().toString().split(" ");
            String year = dateParts[5];
            if (Integer.parseInt(year) < 1900) { return false; }
            if (Integer.parseInt(year) > LocalDate.now().getYear() + 1) { return false; }

            // If all the patient information is valid, add the patient uid to the list of patient uids
            patientUIDs.add(transaction.getPatientUID());
        }
              
        return true;
    }

    public void alertWallet(HashMap<String, Transaction> txMap, MerkleTree mt, Address myAddress){
        HashMap<String, HCTransaction> hcTxMap = new HashMap<>();
        HashSet<String> keys = new HashSet<>(txMap.keySet());

        for(String key : keys){
            HCTransaction transactionInList = (HCTransaction) txMap.get(key);
            hcTxMap.put(key, transactionInList);
        }

        synchronized(lockManager.getLock("eventsLock")){
            for (Address address : clientsToAlert) {
                for(String transHash : txMap.keySet()) {
                    Messager.sendOneWayMessage(address, 
                    new Message(Message.Request.ALERT_WALLET, mt.getProof(txMap.get(transHash))), myAddress);
                }
            }
        }
    }

    public void addClientsToAlert(Address address){
        synchronized(lockManager.getLock("eventsLock")){
            clientsToAlert.add(address);
        }
    }
}
