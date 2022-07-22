package node.communication;

import java.io.Serializable;
import java.util.Objects;

public class Address implements Serializable {
    private int port;
    private String host;

    public Address(int port, String host){
        this.port = port;
        this.host = host;
    }

    public int getPort(){
        return port;
    }

    public String getHost(){
        return host;
    }

    public boolean equals(Address address){
        return this.port == address.getPort() && Objects.equals(this.host, address.getHost());
    }
}
