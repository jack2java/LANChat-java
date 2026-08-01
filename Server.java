import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static final int PORT = 5000;
    public static final int MAX_CLIENTS = 5;

    private volatile boolean running = true;

    private ServerSocket serverSocket;

    private final ExecutorService pool =
            Executors.newFixedThreadPool(MAX_CLIENTS);

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {

        try {

            serverSocket = new ServerSocket(PORT);

            System.out.println("==================================");
            System.out.println(" LANChat-Java Server");
            System.out.println(" Port : " + PORT);
            System.out.println(" Type 'exit' to stop.");
            System.out.println("==================================");

            startConsole();

            while (running) {

                try {

                    Socket socket = serverSocket.accept();

                    System.out.println(
                            "[CONNECTED] "
                            + socket.getInetAddress().getHostAddress());

                    pool.execute(new ClientHandler(socket));

                } catch (IOException e) {

                    if (running)
                        e.printStackTrace();

                }

            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            shutdown();

        }

    }

    private void startConsole() {

        Thread console = new Thread(() -> {

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(System.in));

            while (running) {

                try {

                    String cmd = reader.readLine();

                    if (cmd == null)
                        continue;

                    cmd = cmd.trim();

                    if (cmd.equalsIgnoreCase("exit")) {

                        running = false;

                        serverSocket.close();

                    }

                } catch (IOException ignored) {
                }

            }

        });

        console.setDaemon(true);
        console.start();

    }

    private void shutdown() {

        System.out.println();
        System.out.println("Stopping server...");

        pool.shutdownNow();

        System.out.println("Server stopped.");

    }

}