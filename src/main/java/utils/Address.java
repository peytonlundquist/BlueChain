package utils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a network address with a port and host.
 */
public class Address implements Serializable {
    private final int port;
    private final String host;

    /**
     * Constructs an Address with the specified port and host.
     * @param port The port number.
     * @param host The host address.
     */
    public Address(int port, String host){
        this.port = port;
        this.host = host;
    }

    /**
     * Gets the port number of the address.
     * @return The port number.
     */
    public int getPort(){
        return port;
    }

    /**
     * Gets the host address of the address.
     * @return The host address.
     */
    public String getHost(){
        return host;
    }

    /**
     * Checks if this Address is equal to another Address.
     * @param address The Address to compare.
     * @return true if the Addresses are equal, false otherwise.
     */
    public boolean equals(Address address){
        return this.port == address.getPort() && this.host.equals(address.getHost());
    }

    /**
     * Returns a string representation of the Address.
     * The format is "port_host".
     * @return The string representation of the Address.
     */
    @Override
    public String toString() {
        return String.valueOf(port).concat("_" + host);
    }
}
