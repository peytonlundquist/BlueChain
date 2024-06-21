package client;

import java.io.BufferedReader;
import java.util.ArrayList;
import utils.Address;

public class Client {
    protected Object updateLock;
    protected BufferedReader reader;
    protected Address myAddress;
    protected ArrayList<Address> fullNodes;
    protected boolean test;
}
