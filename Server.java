import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final AtomicInteger activeConnections = new AtomicInteger(0);
    private static final List<PrintWriter> clientWriters = new ArrayList<>();
    private static final List<String> clients = new ArrayList<>(); // List to store connected clients' usernames

    public static void main(String[] args) throws IOException {
        try (ServerSocket listener = new ServerSocket(7000)) {
            System.out.println("The chat server is running...");
            while (true) {
                Socket socket = listener.accept();
                activeConnections.incrementAndGet();
                System.out.println("Current active connections: " + activeConnections.get());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                clientWriters.add(out); // Add the client's output stream to the list

                // Create a new thread and pass the socket and output stream
                new Thread(new ClientHandler(socket, out)).start();
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter clientWriter;
        private String clientID;

        public ClientHandler(Socket socket, PrintWriter clientWriter) {
            this.socket = socket;
            this.clientWriter = clientWriter;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                System.out.println("A client has connected.");

                String clientID = in.readLine();
                clients.add(clientID); // Add the client's username to the list of connected clients

                // Print the list of connected clients (just for testing)
                System.out.println("Connected clients: " + clients);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if ("exit".equalsIgnoreCase(inputLine)) {
                        System.out.println("Client requested to close the connection.");
                        break;
                    } else if ("users".equalsIgnoreCase(inputLine)) {
                        // Respond with the list of connected clients
                        sendConnectedClientsList();
                    } else {
                        // Broadcast the received message along with the sender's ID
                        broadcastMessage(clientID + ": " + inputLine);
                    }
                }
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port 7000 or listening for a connection");
                System.out.println(e.getMessage());
            } finally {
                try {
                    socket.close();
                    activeConnections.decrementAndGet();
                    System.out.println("Current active connections: " + activeConnections.get());
                    // Remove the client's output stream from the list
                    clientWriters.remove(clientWriter);
                    clients.remove(clientID); // Remove the username from the list when the client disconnects
                } catch (IOException e) {
                    System.out.println("Error closing the client socket.");
                }
            }
        }
    }

    // Method to broadcast a message to all connected clients
    private static void broadcastMessage(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }

    // Method to send the list of connected clients to the client who requested it
    private static void sendConnectedClientsList() {
        StringBuilder userList = new StringBuilder("Connected Clients: ");
        for (String client : clients) {
            userList.append(client).append(", ");
        }
        // Remove the trailing ", " and send the list to the client
        userList.setLength(userList.length() - 2);
        for (PrintWriter writer : clientWriters) {
            writer.println(userList.toString());
        }
    }
}
