import node.communication.Address;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import javax.swing.JFrame;

public class Graph extends Canvas{
    LinkedList<GraphNode> graphNodes;
    public Graph(LinkedList<GraphNode> graphNodes){
        this.graphNodes = graphNodes;
        JFrame f=new JFrame();
        f.add(this);
        f.setSize(1000,1000);
        //f.setLayout(null);
        f.setVisible(true);
    }

    public void paint(Graphics g) {
        int scalar = 80;
        int offset = 50;

        Iterator<GraphNode> iterator = graphNodes.iterator();
        LinkedHashMap<Integer, Coordinates> nodeMap = new LinkedHashMap<>();
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++) {
                GraphNode node = iterator.next();
                g.setColor(Color.black);
                g.drawOval(j * scalar, i * scalar + offset, 25, 25);
                g.fillOval(j * scalar, i * scalar + offset, 25, 25);
                g.setColor(Color.blue);
                g.drawOval(j * scalar, i * scalar + offset, 20, 20);
                g.fillOval(j * scalar, i * scalar + offset, 20, 20);
                g.drawString(String.valueOf(node.getPort()),j * scalar,(i * scalar) + offset);
                nodeMap.put(node.getPort(), new Coordinates(j * scalar, i * scalar + offset));
            }
        }

        g.setColor(Color.black);
        g.drawOval(11 * scalar, 11 * scalar + offset, 25, 25);
        g.fillOval(11 * scalar, 11 * scalar + offset, 25, 25);
        g.setColor(Color.blue);
        g.drawOval(11 * scalar, 11 * scalar + offset, 20, 20);
        g.fillOval(11 * scalar, 11 * scalar + offset, 20, 20);
        g.drawString("8100", 11 * scalar,(11 * scalar) + offset);
        nodeMap.put(8100, new Coordinates(11 * scalar, 11 * scalar + offset));

        for(GraphNode node : graphNodes){
            ArrayList<Address> localPeers = node.getLocalPeers();
            for(Address address : localPeers){
                g.drawLine(nodeMap.get(node.getPort()).getX(),
                        nodeMap.get(node.getPort()).getY(),
                        nodeMap.get(address.getPort()).getX(),
                        nodeMap.get(address.getPort()).getY());
            }
        }

        int i = 0;
    }
}

class Coordinates{
    private final int x;
    private final int y;
    public Coordinates(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}