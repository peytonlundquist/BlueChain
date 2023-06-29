import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JavaHTTPServer extends NanoHTTPD {

    public JavaHTTPServer() throws IOException {
        super(5005); 
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Server started on port 5005");
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if (uri.equals("/")) {
            uri = "/index.html";
        }

        try {
            String filePath = "src/main/resources" + uri;
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            return newFixedLengthResponse(new String(fileBytes));
        } catch (IOException e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "404 - Not Found");
        }
    }

    public static void main(String[] args) throws IOException {
        new JavaHTTPServer();
    }
}