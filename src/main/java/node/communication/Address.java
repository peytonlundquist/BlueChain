package node.communication;

import java.io.Serializable;

import node.NodeType;

public class Address implements Serializable {
    private final int port;
    private final String host;
    private final NodeType nt;

    public NodeType getNodeType() {
        return nt;
    }

    public Address(int port, String host, NodeType nt){
        this.port = port;
        this.host = host;
        this.nt = nt;
    }

    public int getPort(){
        return port;
    }

    public String getHost(){
        return host;
    }

    public boolean equals(Address address){
        return this.port == address.getPort() && this.host.equals(address.getHost());
    }

    @Override
    public String toString() {
        return String.valueOf(port).concat("_" + host);
    }
}
