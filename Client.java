import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }

        String serverIP = args[0];
        Socket socket = new Socket(serverIP, 7000);

        // Create a thread to continuously send user input to the server
        Thread sendMessageThread = new Thread(() -> {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner userInput = new Scanner(System.in);

                // Prompt the user for their ID
                System.out.print("Enter your ID: ");
                String userID = userInput.nextLine();
                out.println(userID);  // Send the ID to the server

                while (true) {
                    String message = userInput.nextLine();
                    out.println(message);  // Send the message to the server
                    out.flush();  // Flush the stream to send the message immediately

                    if ("exit".equalsIgnoreCase(message)) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        sendMessageThread.start();

        // Listen for messages from the server
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                System.out.println(serverResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
