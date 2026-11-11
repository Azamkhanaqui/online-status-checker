import java.io.*;
import java.net.*;

public class HeartbeatClient {
    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected, sending heartbeats...");
            while (true) {
                out.println("HEARTBEAT");
                Thread.sleep(2000);
            }
        }
    }
}