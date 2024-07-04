package client;

import java.io.BufferedReader;
import java.util.ArrayList;
import utils.Address;

public abstract class Client {
    protected Object updateLock;
    protected BufferedReader reader;
    protected Address myAddress;
    protected ArrayList<Address> fullNodes;
    protected boolean test;

    public Client(Object updateLock, BufferedReader reader, Address myAddress, ArrayList<Address> fullNodes) {
        this.updateLock = updateLock;
        this.reader = reader;
        this.myAddress = myAddress;
        this.fullNodes = fullNodes;
    }

    public abstract void testNetwork(int numOfTests);
    public abstract void printUsage();
}
