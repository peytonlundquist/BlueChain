import java.io.Serializable;

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
}
