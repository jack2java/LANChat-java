// Client.java v0.3

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;

public class Client {

    private String host;
    private int port;
    private String username;

    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;

    private volatile boolean running = true;

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {

        if (!loadConfiguration()) {
            return;
        }

        printBanner();

        try {

            socket = new Socket(host, port);

            serverIn = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            serverOut = new PrintWriter(
                    socket.getOutputStream(), true);

            System.out.println("Connected to server.");

            // Login automatically using client.properties
            serverOut.println("/login " + username);

            Thread receiver = new Thread(() -> {

                try {

                    String line;

                    while (running &&
                           (line = serverIn.readLine()) != null) {

                        System.out.println(line);

                    }

                } catch (IOException ignored) {

                } finally {

                    running = false;

                    System.out.println();
                    System.out.println("Disconnected from server.");

                }

            });

            receiver.setDaemon(true);
            receiver.start();

            BufferedReader keyboard =
                    new BufferedReader(
                            new InputStreamReader(System.in));

            while (running) {

                String line = keyboard.readLine();

                if (line == null) {
                    continue;
                }

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.equalsIgnoreCase("/quit")) {

                    serverOut.println("/quit");
                    break;

                }

                serverOut.println(line);

            }

        } catch (IOException e) {

            System.out.println("Unable to connect to server.");

        } finally {

            close();

        }

    }

    private boolean loadConfiguration() {

        Properties properties = new Properties();

        try (FileInputStream in =
                     new FileInputStream("client.properties")) {

            properties.load(in);

            host = properties.getProperty(
                    "server.host",
                    "127.0.0.1");

            port = Integer.parseInt(
                    properties.getProperty(
                            "server.port",
                            "5000"));

            username = properties.getProperty(
                    "username",
                    "Guest");

            return true;

        } catch (Exception e) {

            System.out.println("Cannot read client.properties");
            return false;

        }

    }

    private void close() {

        running = false;

        try {
            if (serverIn != null)
                serverIn.close();
        } catch (IOException ignored) {
        }

        if (serverOut != null)
            serverOut.close();

        try {
            if (socket != null)
                socket.close();
        } catch (IOException ignored) {
        }

    }

    private void printBanner() {

        System.out.println("--------------------------------");
        System.out.println("LANChat-Java Client");
        System.out.println("--------------------------------");
        System.out.println("Server : " + host);
        System.out.println("Port   : " + port);
        System.out.println("User   : " + username);
        System.out.println();
        System.out.println("Type /quit to exit.");
        System.out.println();

    }

}