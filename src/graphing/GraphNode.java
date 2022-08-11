package graphing;

import node.communication.Address;

import java.util.ArrayList;

public class GraphNode {
    private int port;
    private ArrayList<Address> localPeers;
    private int x;
    private int y;

    public GraphNode(int port, ArrayList<Address> localPeers){
        this.port = port;
        this.localPeers = localPeers;
    }

    public ArrayList<Address> getLocalPeers() {
        return localPeers;
    }

    public int getPort() {
        return port;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

}
