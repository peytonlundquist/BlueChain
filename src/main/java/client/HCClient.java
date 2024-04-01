/**
 * This class represents the client for the health care use case. It is responsible for prompting the user for
 * intput and then creating the appropriate event and submitting it to the blockchain via transaction. Each event's
 * parameters needs to be filled in or it won't validate and refuse to be added to the blockchain. After the user
 * creates an event, the client will represent the event back to the user verifying that it was added.
 * 
 * @date 03-20-2024
 */

package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;

import blockchain.Transaction;
import blockchain.usecases.healthcare.Event;
import blockchain.usecases.healthcare.HCTransaction;
import blockchain.usecases.healthcare.Patient;
import blockchain.usecases.healthcare.Events.*;
import communication.messaging.Message;
import communication.messaging.Messager;
import me.tongfei.progressbar.ProgressBar;
import utils.Address;
import utils.merkletree.MerkleTreeProof;

public class HCClient {
    
    private Object updateLock;
    private BufferedReader reader;
    private Address myAddress;
    private ArrayList<Address> fullNodes;
    protected boolean test;

    HashSet<HCTransaction> seenTransactions;
    ArrayList<Patient> patients;

    private SimpleDateFormat formatter;


    /**
     * Constructs a HCClient instance. Initializes the client with the given parameters.
     * @param updateLock The lock for multithreading.
     * @param reader The reader for user input.
     * @param myAddress The address of the client.
     * @param fullNodes The address list of full nodes to use.
     */
    public HCClient(Object updateLock, BufferedReader reader, Address myAddress, ArrayList<Address> fullNodes){
        this.reader = reader;
        this.updateLock = updateLock;
        this.myAddress = myAddress;
        this.fullNodes = fullNodes;

        this.seenTransactions = new HashSet<>();
        this.patients = new ArrayList<Patient>();

        this.formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm a");

        // Messages the wallet to add this client on the list of clients to alert.
        Object data = new Object();
        data = myAddress;
        Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost()),
        new Message(Message.Request.ALERT_HC_CLIENTS, data), myAddress);

        // Messages the wallet to request ledger to update client.
        Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost()),
        new Message(Message.Request.REQUEST_LEDGER, data), myAddress);
    }

    public void initializeClient(HashMap<String, Transaction> ledger) {
        /*
         * This method is meant to be called when the client is initalized. It will
         * update the client's list of patients with the data from the ledger. In 
         * order for this method to work, we need to be able to pull all the data from
         * the ledger. Below is the psuedocode for how this method should work.
         */

        for (Transaction transaction : ledger.values()) {
            HCTransaction hcTransaction = (HCTransaction) transaction;
            if (hcTransaction.getEvent() instanceof CreatePatient) {
                CreatePatient createPatient = (CreatePatient) hcTransaction.getEvent();
                Patient patient = createPatient.getPatient();
                patients.add(patient);
            } else if (hcTransaction.getEvent() instanceof RecordUpdate) {
                RecordUpdate recordUpdate = (RecordUpdate) hcTransaction.getEvent();
                for (Patient patient : patients) {
                    if (patient.getUID().equals(hcTransaction.getPatientUID())) {
                        patient.addField(recordUpdate.getKey(), recordUpdate.getValue());
                    }
                }
            } else if (hcTransaction.getEvent() instanceof Prescription) {
                Prescription prescription = (Prescription) hcTransaction.getEvent();
                for (Patient patient : patients) {
                    if (patient.getUID().equals(hcTransaction.getPatientUID())) {
                        patient.addEvent(prescription);
                    }
                }
            } else if (hcTransaction.getEvent() instanceof Appointment) {
                Appointment appointment = (Appointment) hcTransaction.getEvent();
                for (Patient patient : patients) {
                    if (patient.getUID().equals(hcTransaction.getPatientUID())) {
                        patient.addEvent(appointment);
                    }
                }
            }
        }
    }

    /**
     * Creates a new appointment. Prompts the user for the patient's UID, the appointment's date, 
     * location, and provider. It then creates a transaction containing the appointment event and
     * submits it to the full nodes.
     * @throws IOException Thrown if there is an error reading user input.
     * @throws ParseException Thrown if there is an error parsing the date.
     */
    public void createAppointment() throws IOException, ParseException {
        formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm a");
        
        System.out.println("Creating a new appointment");
        System.out.println("Enter the patient's UID:");
        String patientUID = reader.readLine();
        System.out.println("Enter the appointment's date (dd-MM-yyyy HH:mm am/pm):");
        String strDate = reader.readLine();
        Date date = formatter.parse(strDate);
        System.out.println("Enter the appointment's location:");
        String location = reader.readLine();
        System.out.println("Enter the appointment's provider:");
        String provider = reader.readLine();
        System.out.println();

        Appointment appointment = new Appointment(date, location, provider);
        HCTransaction newTransaction = new HCTransaction(appointment, patientUID);
        // byte[] signedUID = patientUID.getBytes();
        // newTransaction.setSigUID(signedUID);

        submitToNodes(newTransaction);

        System.out.println("\n--APPOINTMENT CREATED--");
        System.out.println("Appointment info:");
        System.out.println("Patient UID: " + patientUID);
        System.out.println("Appointment date: " + date);
        System.out.println("Location: " + location);
        System.out.println("Provider: " + provider);
        System.out.println();
    }

    /**
     * Creates a new perscription. Prompts the user for the patient's UID, the perscription's date,
     * medication, perscribed count, provider, and address. It then creates a transaction containing
     * the perscription event and submits it to the full nodes.
     * @throws IOException Thrown if there is an error reading user input.
     * @throws ParseException Thrown if there is an error parsing the date.
     */
    public void createPerscription() throws IOException, ParseException{
        formatter = new SimpleDateFormat("dd-MM-yyyy");

        System.out.println("Creating a new perscription");
        System.out.println("Enter the patient's UID:");
        String patientUID = reader.readLine();
        System.out.println("Enter the perscription date (dd-MM-yyyy):");
        String strDate = reader.readLine();
        Date date = formatter.parse(strDate);
        System.out.println("Enter the perscription's medication:");
        String medication = reader.readLine();
        System.out.println("Enter the perscription's perscribed count:");
        int count = Integer.valueOf(reader.readLine());
        System.out.println("Enter the perscription's provider:");
        String provider = reader.readLine();
        System.out.println("Enter address of the issued perscription:");
        String address = reader.readLine();
        System.out.println();

        Prescription prescription = new Prescription(medication, provider, address, date, count);
        HCTransaction newTransaction = new HCTransaction(prescription, patientUID);
        byte[] signedUID = patientUID.getBytes();
        newTransaction.setSigUID(signedUID);

        submitToNodes(newTransaction);

        System.out.println("\n--PERSCRIPTION CREATED--");
        System.out.println("Patient UID: " + patientUID);
        System.out.println("Perscription date: " + date);
        System.out.println("Medication: " + medication);
        System.out.println("Perscribed count: " + count);
        System.out.println("Provider: " + provider);
        System.out.println("Address: " + address);
        System.out.println();
    }

    /**
     * Updates a patient's record. Prompts the user for the patient's UID, the record to update, and
     * the new value of the record. It then creates a transaction containing the record update event
     * and submits it to the full nodes.
     * @throws IOException Thrown if there is an error reading user input.
     */
    public void updateRecord() throws IOException {
        System.out.println("Updating a patient's record");
        System.out.println("Enter the patient's UID:");
        String patientUID = reader.readLine();
        System.out.println("Enter the record to update:");
        String key = reader.readLine();
        System.out.println("Enter the new value of the record:");
        String value = reader.readLine();
        System.out.println();

        // Date is the current date that the record is updated
        RecordUpdate recordUpdate = new RecordUpdate(new Date(), key, value);
        HCTransaction newTransaction = new HCTransaction(recordUpdate, patientUID);
        byte[] signedUID = patientUID.getBytes();
        newTransaction.setSigUID(signedUID);

        submitToNodes(newTransaction);

        System.out.println("\n--RECORD UPDATED--");
        System.out.println("Patient UID: " + patientUID);
        System.out.println("Record to Update: " + key);
        System.out.println("New value: " + value);
        System.out.println();
    }

    // Might not be necessary, requires consultation.
    public void createNewPatient() throws IOException, ParseException {
        formatter = new SimpleDateFormat("dd-MM-yyyy");

        System.out.println("Creating a new patient");
        System.out.println("Enter the patient's first name:");
        String fname = reader.readLine();
        System.out.println("Enter the patient's last name:");
        String lname = reader.readLine();
        System.out.println("Enter the patient's date of birth (dd-MM-yyyy):");
        String dob = reader.readLine();
        Date date = formatter.parse(dob);

        Patient patient = new Patient(fname, lname, date);
        CreatePatient createPatient = new CreatePatient(patient);

        HCTransaction newTransaction = new HCTransaction(createPatient, patient.getUID());

        submitToNodes(newTransaction);

        System.out.println("\nPatient successfully created. Patient UID: " + patient.getUID());
    }

    public void showPatientDetails() throws IOException {
        System.out.println("Enter the patient's UID:");
        String patientUID = reader.readLine();

        for(Patient patient : patients){
            if(patient.getUID().equals(patientUID)){
                HashMap<String, String> fields = patient.getFields();
                ArrayList<Event> patientEvents = patient.getEvents();

                System.out.println("\n--PATIENT DETAILS--");
                System.out.println("First name: " + patient.getFirstName());
                System.out.println("Last name: " + patient.getLastName());
                System.out.println("Date of birth: " + patient.getDob());

                for(String key : fields.keySet()){
                    System.out.println(key + ": " + fields.get(key));
                }

                System.out.println("\nEVENTS:");

                for (Event event : patientEvents) {
                    System.out.println(event.toString());
                }

                System.out.println();

                return;
            }
        }

        System.out.println("Patient not found.");
    }

    public void showAllPatients() {
        System.out.println("\n--ALL PATIENTS--");
        for(Patient patient : patients){
            System.out.println(patient.toString());
        }
    }

    public void updatePatientDetails(MerkleTreeProof mtp) throws IOException {
        synchronized(updateLock){

            HCTransaction transaction = (HCTransaction) mtp.getTransaction();

            for(HCTransaction existingTransaction : seenTransactions){
                if(existingTransaction.equals(transaction)){
                    return;
                }
            }
            
            seenTransactions.add(transaction);

            if(!mtp.confirmMembership()){
                System.out.println("Could not validate tx in MerkleTreeProof" );
                return;
            }

            if (!this.test) System.out.println("Updating patient details...");

            if (transaction.getEvent() instanceof CreatePatient) {
                CreatePatient createPatient = (CreatePatient) transaction.getEvent();
                patients.add(createPatient.getPatient());
            } else {
                for(Patient patient : patients){ 
                    if (patient.getUID().equals(transaction.getPatientUID())) {
                        if (transaction.getEvent() instanceof RecordUpdate) {
                            RecordUpdate recordUpdate = (RecordUpdate) transaction.getEvent();
                            patient.addField(recordUpdate.getKey(), recordUpdate.getValue());
                        } else if (transaction.getEvent() instanceof Prescription){
                            Prescription prescription = (Prescription) transaction.getEvent();
                            patient.addEvent(prescription);
                        } else if (transaction.getEvent() instanceof Appointment){
                            Appointment appointment = (Appointment) transaction.getEvent();
                            patient.addEvent(appointment);
                        }
                    }
                }
            }
        }

            if(!this.test) {
                System.out.println("\nFull node has update. Updating patients...");
                System.out.print(">");
            }
    }

    /**
     * Updates the list of full nodes we are communicating with in the network.
     * @throws IOException If an I/O error occurs.
     */
    public void submitToNodes(HCTransaction transaction){
        System.out.println("Submitting transaction to nodes: ");
        for(Address address : fullNodes){
            submitTransaction(transaction, address);
        }

        Object data = new Object();
        data = myAddress;
        Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost()),
        new Message(Message.Request.ALERT_HC_CLIENTS, data), myAddress);
    }

    /**
     * Submits a transaction to the given address.
     * @param transaction The transaction to submit.
     * @param address The address to submit the transaction to.
     */
    public void submitTransaction(HCTransaction transaction, Address address){
        try {
            Socket s = new Socket(address.getHost(), address.getPort());
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            Message message = new Message(Message.Request.ADD_TRANSACTION, transaction);
            oout.writeObject(message);
            oout.flush();
            Thread.sleep(1000);
            s.close();
            if(!this.test) System.out.println("Full node: " + address);
        } catch (IOException e) {
            System.out.println("Full node at " + address + " appears down.");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * TEST METHOD. Adds an appointment to the events list.
     * @param provider The name of the appointment provider
     */
    public Appointment testAddAppointment(String provider) {
        synchronized(updateLock) {
            
            Appointment newAppointment = new Appointment(new Date(), "TEST", provider);

            // Object data = new Object();
            // data = myAddress;
            // Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost()),
            // new Message(Message.Request.ALERT_HC_CLIENTS, data), myAddress);

            return newAppointment;
        }
    }

    
    /**
     * TEST METHOD. Submits a transaction to the network.
     * @param event The event to submit.
     * @param patientUID The UID of the patient.
     */
    protected void testSubmitTransaction(Event event, String patientUID) {

        HCTransaction newTransaction = new HCTransaction(event, patientUID);
        String signedUID = newTransaction.getUID() + event.toString();
        newTransaction.setSigUID(signedUID.getBytes());

        for(Address address : fullNodes){
            submitTransaction(newTransaction, address);
        }
    }

    /**
     * TEST METHOD. Tests the network by adding a number of appointments, perscriptions, and record updates
     * to the events list and then checking if they were added to the blockchain.
     * @param j The number of events to add to the list.
     */
    void testNetwork(int j){
        System.out.println("Beginning Test");
        try {     
            Patient patient = new Patient("John", "Doe", new Date());
            patients.add(patient);
            
            ProgressBar pb = new ProgressBar("Test", j);
            pb.start(); // the progress bar starts timing
            pb.setExtraMessage("Testing..."); // Set extra message to display at the end of the bar
            
            for(int i = 0; i < j; i++){
                    String provider = "Provider " + i;
                    Appointment apt = testAddAppointment(provider);
                    testSubmitTransaction(apt, patient.getUID());
                    Thread.sleep(2000);
                    pb.step();
            }

            pb.stop(); // stops the progress bar
            System.out.println("Sleeping wallet for last minute updates...");
            Thread.sleep(100000);

            // Make sure that the ledger matches the added events.
            if(patient.getEvents().size() == j) {
                System.out.println("\n*********************Test passed.*********************");
            }else{
                System.out.println("\n*********************Test Failed*********************");
            }

            System.out.println("Expected events added: " + j);
            System.out.println("Actual events added: " + patient.getEvents().size());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints the user menu to the client.
     */
    protected void printUsage(){
        System.out.println("BlueChain Health Care Usage:");
        System.out.println("a: Create a new appointment");
        System.out.println("p: Create a new perscription");
        System.out.println("r: Update a patient's record");
        System.out.println("c: create a new patient");
        System.out.println("s: Show patient details");
        System.out.println("d: Show all patients");
        System.out.println("u: Update full nodes");
    }
}
