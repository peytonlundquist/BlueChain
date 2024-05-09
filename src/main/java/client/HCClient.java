package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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

/**
 * This class represents the client for the health care use case. It is responsible for prompting the user for
 * intput and then creating the appropriate event and submitting it to the blockchain via transaction. Each event's
 * parameters needs to be filled in or it won't validate and refuse to be added to the blockchain. After the user
 * creates an event, the client will represent the event back to the user verifying that it was added.
 */

public class HCClient {
    
    private Object updateLock;
    private BufferedReader reader;
    private Address myAddress;
    private ArrayList<Address> fullNodes;
    protected boolean test;
    private boolean patientClient;
    private Patient currentPatient;

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
        this.patientClient = false;
        this.currentPatient = null;

        this.seenTransactions = new HashSet<>();
        this.patients = new ArrayList<Patient>();

        this.formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm a");

        // Messages the wallet to add this client on the list of clients to alert.
        Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost()),
        new Message(Message.Request.ALERT_WALLET, myAddress), myAddress);

        // Messages the wallet to request ledger to update client.
        Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost()),
        new Message(Message.Request.REQUEST_TX, myAddress), myAddress);
    }

    /**
     * Initializes the client with the Block chain ledger. It will add all the patients and their
     * events to the client. The ledger is accessed by the client sending the message:
     * REQUEST_LEDGER to the nodes. The nodes will then send the ledger to the client. Each transaction
     * in the leger is added to the list of patients as well as their events.
     * 
     * @param ledger The BlockChain ledger to initialize the client with.
     */
    public void initializeClient(ArrayList<Transaction> ledger) {
        for (Transaction transaction : ledger) {
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
                        patient.addEvent(recordUpdate);
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

    public void setPatientClient(boolean patientClient) {
        this.patientClient = patientClient;
    }

    public boolean isPatient(String uid) {
        for (Patient patient : patients) {
            if (patient.getUID().equals(uid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new appointment. Prompts the user for the patient's UID, the appointment's date, 
     * location, and provider. It then creates a transaction containing the appointment event and
     * submits it to the full nodes.
     * 
     * @throws IOException Thrown if there is an error reading user input.
     * @throws ParseException Thrown if there is an error parsing the date.
     */
    public void createAppointment() throws IOException, ParseException {
        formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm a");
        
        System.out.println("Creating a new appointment");

        // Prompt for user UID, continues to prompt until an existing UID is entered
        System.out.println("Enter the patient's UID:");
        String patientUID = reader.readLine();
        while(!isPatient(patientUID)) {
            System.out.println("Patient not found. Please enter a valid patient UID or [N/n] to quit:");
            patientUID = reader.readLine();
            if (patientUID.charAt(0) == 'n' || patientUID.charAt(0) == 'N') {
                return;
            }
        }

        // Prompts the user for the appointment date, continues until a valid date is entered.
        Date date = null;
        while(date == null) {
            try {
                System.out.println("Enter the appointment's date (MM-dd-yyyy HH:mm am/pm):");
                String strDate = reader.readLine();
                date = formatter.parse(strDate);
            } catch (ParseException e) {
                System.out.println("Error, please enter the date in the correct format.");
            }
        }

        // Prompts the user for the appointment location and provider
        System.out.println("Enter the appointment's location:");
        String location = reader.readLine();
        System.out.println("Enter the appointment's provider:");
        String provider = reader.readLine();

        Appointment appointment = new Appointment(date, location, provider);
        HCTransaction newTransaction = new HCTransaction(appointment, patientUID);

        if (checkDate(date, 5, 5)) {
            submitToNodes(newTransaction);

            System.out.println("\n\n--APPOINTMENT CREATED--");
            System.out.println("Appointment info:");
            System.out.println("Patient UID: " + patientUID);
            System.out.println("Appointment date: " + date);
            System.out.println("Location: " + location);
            System.out.println("Provider: " + provider + "\n");
        } else {
            System.out.println("Error: Date must be within 5 years of the current date.");
        }
    }

    /**
     * Creates a new perscription. Prompts the user for the patient's UID, the perscription's date,
     * medication, perscribed count, provider, and address. It then creates a transaction containing
     * the perscription event and submits it to the full nodes.
     * @throws IOException Thrown if there is an error reading user input.
     * @throws ParseException Thrown if there is an error parsing the date.
     */
    public void createPerscription() throws IOException, ParseException{
        formatter = new SimpleDateFormat("MM-dd-yyyy");

        // Prompts user for perscription information
        System.out.println("Creating a new perscription");

        // Prompts the user for the patient's UID, continues until a valid UID is entered
        System.out.println("Enter the patient's UID:");
        String patientUID = reader.readLine();
        while(!isPatient(patientUID)) {
            System.out.println("Patient not found. Please enter a valid patient UID or [N/n] to quit:");
            patientUID = reader.readLine();
            if (patientUID.charAt(0) == 'n' || patientUID.charAt(0) == 'N') {
                return;
            }
        }

        // Prompts the user for the perscription date, continues until a valid date is entered
        Date date = null;
        while(date == null) {
            try {
                System.out.println("Enter the perscription's date (MM-dd-yyyy):");
                String strDate = reader.readLine();
                date = formatter.parse(strDate);
            } catch (ParseException e) {
                System.out.println("Error, please enter the date in the correct format.");
            }
        }

        System.out.println("Enter the perscription's medication:");
        String medication = reader.readLine();

        // Prompts the user for the perscribed count, continues until a valid count is entered
        int count = 0;
        while (count <= 0) {
            try {
                System.out.println("Enter the perscription's perscribed count:");
                count = Integer.valueOf(reader.readLine());
                if (count <= 0) {
                    System.out.println("Error, please enter a number greater than 0.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error, please enter the a number for the medication count.");
            }
        }

        // Prompts the user for the perscription provider and address
        System.out.println("Enter the perscription's provider:");
        String provider = reader.readLine();
        System.out.println("Enter address of the issued perscription:");
        String address = reader.readLine();

        // Creates a new perscription event and transaction. Transaction is sent to nodes
        Prescription prescription = new Prescription(medication, provider, address, date, count);
        HCTransaction newTransaction = new HCTransaction(prescription, patientUID);

        if (checkDate(date, 5, 5)) {
            submitToNodes(newTransaction);

            // Prints back the perscription information to the user
            System.out.println("\n\n--PERSCRIPTION CREATED--");
            System.out.println("Patient UID: " + patientUID);
            System.out.println("Perscription date: " + date);
            System.out.println("Medication: " + medication);
            System.out.println("Perscribed count: " + count);
            System.out.println("Provider: " + provider);
            System.out.println("Address: " + address + "\n");
        } else {
            System.out.println("Error: date must be within 5 years of the current date.");
        }
    }

    /**
     * Updates a patient's record. Prompts the user for the patient's UID, the record to update, and
     * the new value of the record. It then creates a transaction containing the record update event
     * and submits it to the full nodes.
     * @throws IOException Thrown if there is an error reading user input.
     */
    public void updateRecord() throws IOException {
        // Prompts user for patient record information
        System.out.println("Updating a patient's record");
        System.out.println("Enter the patient's UID:");
        String patientUID = reader.readLine();
        while(!isPatient(patientUID)) {
            System.out.println("Patient not found. Please enter a valid patient UID or [N/n] to quit:");
            patientUID = reader.readLine();
            if (patientUID.charAt(0) == 'n' || patientUID.charAt(0) == 'N') {
                return;
            }
        }

        System.out.println("Enter the record to update:");
        String key = reader.readLine();
        System.out.println("Enter the new value of the record:");
        String value = reader.readLine();

        // Creates a new record update event and transaction. Transaction is sent to nodes
        RecordUpdate recordUpdate = new RecordUpdate(new Date(), key, value);
        HCTransaction newTransaction = new HCTransaction(recordUpdate, patientUID);

        submitToNodes(newTransaction);

        // Prints back the updated record information to the user
        System.out.println("\n\n--RECORD UPDATED--");
        System.out.println("Patient UID: " + patientUID);
        System.out.println("Record to Update: " + key);
        System.out.println("New value: " + value + "\n");
    }

    // Might not be necessary, requires consultation.
    public void createNewPatient() throws IOException, ParseException {
        // If patient client, check if patient already exists
        if (patientClient && currentPatient != null) {
            System.out.println("You are already a patient. Please log out to create a new account.");
            return;
        }

        formatter = new SimpleDateFormat("MM-dd-yyyy");

        // Prompts user for patient information
        System.out.println("Creating a new patient");
        System.out.println("Enter the patient's first name:");
        String fname = reader.readLine();
        System.out.println("Enter the patient's last name:");
        String lname = reader.readLine();

        Date date = null;
        while(date == null) {
            try {
                System.out.println("Enter the patient's date of birth (MM-dd-yyyy):");
                String strDate = reader.readLine();
                date = formatter.parse(strDate);
            } catch (ParseException e) {
                System.out.println("Error, please enter the date in the correct format.");
            }
        }

        // Creates a new patient. 
        Patient patient = new Patient(fname, lname, date);
        CreatePatient createPatient = new CreatePatient(patient);

        // If patient client, set current patient to the new patient
        if (patientClient) {
            currentPatient = patient;
            patients.add(currentPatient);
        }

        // Creates a new transaction and submits it to the nodes
        HCTransaction newTransaction = new HCTransaction(createPatient, patient.getUID());

        submitToNodes(newTransaction);

        if (checkDate(date, 200, 1)) {
            System.out.println("\nCreating new patient...");
        } else {
            System.out.println("Error: New patient's age cannot exist.");
        }
    }

    /**
     * Shows the patient details after prompting for patient UID. If the client is a patient 
     * client, it will show the current patient's details.
     * @throws IOException Thrown if there is an error reading user input.
     */
    public void showPatientDetails() throws IOException {
        if (patientClient) { // If patient client, show current patient details
            if (currentPatient != null) {
                printPatientDetails(currentPatient);
            } else {
                System.out.println("You are not a patient. Please create a patient account to view details.");
            }
            return;
        } 

        // If not patient client, prompt user for patient UID
        System.out.println("Enter the patient's UID:");
        String patientUID = reader.readLine();
        while(!isPatient(patientUID)) {
            System.out.println("Patient not found. Please enter a valid patient UID or [N/n] to quit:");
            patientUID = reader.readLine();
            if (patientUID.charAt(0) == 'n' || patientUID.charAt(0) == 'N') {
                return;
            }
        }

        // Search for patient in list of patients, if found, print patient details
        for(Patient patient : patients){
            if(patient.getUID().equals(patientUID)){
                printPatientDetails(patient);
                return;
            }
        }

    }

    /**
     * Prints the patient detials using a patient as a parameter
     *
     * @param patient The patient object to be printed
     */
    private void printPatientDetails(Patient patient) {
        HashMap<String, String> fields = patient.getFields();
        ArrayList<Event> patientEvents = patient.getEvents();

        System.out.println("\n--PATIENT DETAILS--");
        System.out.println("First name: " + patient.getFirstName());
        System.out.println("Last name: " + patient.getLastName());
        System.out.println("Date of birth: " + patient.getDob());
        System.out.println("Patient UID: " + patient.getUID());

        for(String key : fields.keySet()){
            System.out.println(key + ": " + fields.get(key));
        }

        System.out.println("\nEVENTS:");

        for (Event event : patientEvents) {
            System.out.println(event.toString());
        }

        System.out.println();
    }

    /**
     * Shows all patients in the client's list of patients.
     */
    public void showAllPatients() {
        System.out.println("\n--ALL PATIENTS--");
        for(Patient patient : patients){
            System.out.println(patient.toString());
        }
    }

    /**
     * Updates the list of full nodes we are communicating with in the network. Anytime a
     * event is added, the full nodes are alerted and updates the rest of the clients. This
     * method is used to update the list of clients with the most recent transaction added
     * to the ledger
     * 
     * @param mtp The MerkleTreeProof containing the transaction to update the client with.
     * @throws IOException If an I/O error occurs.
     */
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

            if (transaction.getEvent() instanceof CreatePatient) {
                CreatePatient createPatient = (CreatePatient) transaction.getEvent();
                patients.add(createPatient.getPatient());
            } else {
                for(Patient patient : patients){ 
                    if (patient.getUID().equals(transaction.getPatientUID())) {
                        if (transaction.getEvent() instanceof RecordUpdate) {
                            RecordUpdate recordUpdate = (RecordUpdate) transaction.getEvent();
                            patient.addField(recordUpdate.getKey(), recordUpdate.getValue());
                            patient.addEvent(recordUpdate);
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

        if(!this.test && !patientClient) {
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

        Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost()),
        new Message(Message.Request.ALERT_WALLET, myAddress), myAddress);
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
            e.printStackTrace();
        }
    }

    /**
     * TEST METHOD. Submits a transaction to the network.
     * @param event The event to submit.
     * @param patientUID The UID of the patient.
     */
    protected void testSubmitToNodes(HCTransaction transaction) {
        for(Address address : fullNodes){
            submitTransaction(transaction, address);
        }

        Messager.sendOneWayMessage(new Address(fullNodes.get(0).getPort(), fullNodes.get(0).getHost()),
        new Message(Message.Request.ALERT_WALLET, myAddress), myAddress);
    }

    /**
     * TEST METHOD. Tests the network by adding a number of appointments, perscriptions, and record updates
     * to the events list and then checking if they were added to the blockchain. Test count (j) must be a
     * number divisible by 4. Otherwise the test will fail.
     * @param j The number of events to add to the list.
     */
    void testNetwork(int j){
        System.out.println("Beginning Test");

        if (j % 4 != 0) {
            System.out.println("Error: Test count must be divisible by 4.");
            return;
        }

        try {     
            Patient patient = new Patient("John", "Doe", new Date());
            CreatePatient createMasterPatient = new CreatePatient(patient);
            HCTransaction createPatientTransaction = new HCTransaction(createMasterPatient, patient.getUID());
            testSubmitToNodes(createPatientTransaction);
            Thread.sleep(500);
            
            ProgressBar pb = new ProgressBar("Test", j);
            pb.start(); // the progress bar starts timing
            pb.setExtraMessage("Testing..."); // Set extra message to display at the end of the bar
            
            for(int i = 0; i < j; i++){
                String provider = "Provider " + i;
                HCTransaction transaction;
                
                if(i % 4 == 0) {
                    Appointment apt = new Appointment(new Date(), "123 st", provider);
                    transaction = new HCTransaction(apt, patient.getUID());
                } else if(i % 4 == 1) {
                    RecordUpdate ru = new RecordUpdate(new Date(), provider, "Value");
                    transaction = new HCTransaction(ru, patient.getUID());
                } else if (i % 4 == 2) {
                    Prescription rx = new Prescription("Medication", provider, "123 st", new Date(), 1);
                    transaction = new HCTransaction(rx, patient.getUID());
                } else {
                    Patient tempPatient = new Patient(provider, provider, new Date());
                    CreatePatient createPatient = new CreatePatient(tempPatient);
                    transaction = new HCTransaction(createPatient, tempPatient.getUID());
                }

                testSubmitToNodes(transaction);
                Thread.sleep(1000);
                pb.step();
            }

            pb.stop(); // stops the progress bar
            System.out.println("Sleeping wallet for last minute updates...");
            Thread.sleep(100000);

            int aptActual = 0, aptExpected = j / 4; 
            int rxActual = 0, rxExpected = j / 4;
            int ruActual = 0, ruExpected = j / 4;
            int patientsActual = patients.size() - 1; // -1 because we added a patient at the start
            int patientsExpected = j / 4;
            boolean passed = true;

            for (Event e : patients.get(0).getEvents()) {
                if (e instanceof Appointment) {
                    aptActual++;
                } else if (e instanceof Prescription) {
                    rxActual++;
                } else if (e instanceof RecordUpdate) {
                    ruActual++;
                }
            }

            if (aptActual != aptExpected) {
                passed = false;
            }
            if (rxActual != rxExpected) {
                passed = false;
            }
            if (ruActual != ruExpected) {
                passed = false;
            }
            if (patientsActual != patientsExpected) {
                passed = false;
            }

            if(passed) {
                System.out.println("\n*********************Test passed.*********************");
            }else{
                System.out.println("\n*********************Test Failed*********************");
            }

            System.out.println("Tests completed: " + j);
            System.out.println("Expected appointments: " + aptExpected + " | Appointments Added: " + aptActual);
            System.out.println("Expected perscriptions: " + rxExpected + " | Perscriptions Added: " + rxActual);
            System.out.println("Expected record updates: " + ruExpected + " | Record Updates Added: " + ruActual);
            System.out.println("Expected patients: " + patientsExpected + " | Patients Added: " + (patientsActual));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean checkDate(Date date, int yearsBefore, int yearsAfter) {
        String dateParts[] = date.toString().split(" ");
        String year = dateParts[5];
        if (Integer.parseInt(year) < LocalDate.now().getYear() - yearsBefore) { 
            return false; 
        } else if (Integer.parseInt(year) > LocalDate.now().getYear() + yearsAfter) { 
            return false; 
        }
        return true;
    }

    /**
     * Prints the user menu to the client.
     */
    protected void printUsage(){
        System.out.println("BlueChain Health Care Usage:");
        System.out.println("a: Create a new appointment");
        System.out.println("p: Create a new perscription");
        System.out.println("r: Update a patient's record");
        System.out.println("c: Create a new patient");
        System.out.println("s: Show patient details");
        System.out.println("d: Show all patients");
        System.out.println("u: Update full nodes");
    }

    /**
     * Prints the patient menu to the client.
     */
    protected void printPatientUsage(){
        System.out.println("BlueChain Patient Health Care Usage:");
        System.out.println("c: Create a new account");
        System.out.println("s: Show account details");
    }
}
