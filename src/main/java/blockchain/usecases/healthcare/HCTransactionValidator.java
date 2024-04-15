/**
 * This class is used to validate the transaction before it is added to the blockchain. It does so
 * by checking the event of the transaction and making sure all the data is not null. If the data is
 * null, then the transaction is not valid and will not be added to the blockchain.
 * 
 * @date 03-20-2021
 */

package blockchain.usecases.healthcare;

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

public class HCTransactionValidator extends TransactionValidator {

    private LockManager lockManager;
    private ArrayList<Address> clientsToAlert;

    public HCTransactionValidator() {
        this.lockManager = new LockManager();
        lockManager.addLock("eventsLock");

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

            // Checks to see if any data is null
            if (appointment.getProvider() == null) { return false; }
            if (appointment.getLocation() == null) { return false; }
            if (appointment.getDate() == null) { return false; }

        } else if (transaction.getEvent().getAction().name().equals("Prescription")){ // If the event is a prescription
            Prescription prescription = (Prescription) transaction.getEvent();

            // Checks to see if any data is null
            if (prescription.getDate() == null) { return false; }
            if (prescription.getMedication() == null) { return false; }
            if (prescription.getProvider() == null) { return false; }
            if (prescription.getAddress() == null) { return false; }
            if (prescription.getPerscribedCount() == 0) { return false; }


        } else if (transaction.getEvent().getAction().name().equals("Record_Update")) { // If the event is a record update
            RecordUpdate recordUpdate = (RecordUpdate) transaction.getEvent();

            // Checks to see if any data is null
            if (recordUpdate.getDate() == null) { return false; }
            if (recordUpdate.getKey() == null) { return false; }
            if (recordUpdate.getValue() == null) { return false; }

        }
              
        return true;
    }

    public static void updateEvents(HashMap<String, HCTransaction> blockTxList){
        HashSet<String> keys = new HashSet<>(blockTxList.keySet());

        // For each hash of a transaction
        for(String key : keys){
            HCTransaction transaction = blockTxList.get(key); // Grabbing the first transaction from our list of tx using hash

            Event event = transaction.getEvent();
            String uid = transaction.getPatientUID();

            /* Update our accounts based on this transaction */
        }
    }

    public void alertWallet(HashMap<String, Transaction> txMap, MerkleTree mt, Address myAddress){
        HashMap<String, HCTransaction> hcTxMap = new HashMap<>();
        HashSet<String> keys = new HashSet<>(txMap.keySet());

        for(String key : keys){
            HCTransaction transactionInList = (HCTransaction) txMap.get(key);
            hcTxMap.put(key, transactionInList);
        }

        synchronized(lockManager.getLock("eventsLock")){
            HCTransactionValidator.updateEvents(hcTxMap);

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
