// ClientHandler.java v0.3

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;

    private BufferedReader in;
    private PrintWriter out;

    private String username = "Unknown";

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        System.out.println("[CONNECTED] "
                + socket.getInetAddress().getHostAddress());

        try {

            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(
                    socket.getOutputStream(), true);

            String message;

            while ((message = in.readLine()) != null) {

                message = message.trim();

                if (message.isEmpty()) {
                    continue;
                }

                // First command from client
                if (message.startsWith("/login ")) {

                    username = message.substring(7).trim();

                    if (username.isEmpty()) {
                        username = "Guest";
                    }

                    System.out.println(username + " logged in.");

                    out.println("Welcome " + username);

                    continue;
                }

                // Disconnect
                if (message.equalsIgnoreCase("/quit")) {

                    out.println("Goodbye.");

                    break;
                }

                // Echo (temporary until broadcast is implemented)
                System.out.println(username + ": " + message);

                out.println(username + ": " + message);

            }

        } catch (IOException e) {

            System.out.println("Connection lost.");

        } finally {

            try {
                socket.close();
            } catch (IOException ignored) {
            }

            System.out.println("[DISCONNECTED] " + username);

        }

    }

}