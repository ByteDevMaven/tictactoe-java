
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocket {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        ServerSocket server = new ServerSocket(80); // Use port 8080 for non-admin access
        System.out.println("WebSocket server started on ws://127.0.0.1:80");

        while (true) { // Keep the server running to handle multiple clients
            Socket client = server.accept();
            System.out.println("Client connected.");

            new Thread(() -> handleClient(client)).start(); // Handle each client in a separate thread
        }
    }

    private static void handleClient(Socket client) {
        try (InputStream in = client.getInputStream(); OutputStream out = client.getOutputStream(); Scanner scanner = new Scanner(in, "UTF-8")) {

            // Step 1: Perform WebSocket Handshake
            String data = scanner.useDelimiter("\\r\\n\\r\\n").next();
            Matcher get = Pattern.compile("^GET").matcher(data);

            if (get.find()) {
                Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                if (match.find()) {
                    String key = match.group(1).trim();
                    String acceptKey = Base64.getEncoder().encodeToString(
                            MessageDigest.getInstance("SHA-1")
                                    .digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8"))
                    );

                    String response = """
                                      HTTP/1.1 101 Switching Protocols\r
                                      Connection: Upgrade\r
                                      Upgrade: websocket\r
                                      Sec-WebSocket-Accept: """ + acceptKey + "\r\n\r\n";
                    out.write(response.getBytes("UTF-8"));
                    System.out.println("Handshake completed.");
                }
            }

            // Step 2: Enter a loop to listen for client messages
            while (true) {
                byte[] buffer = new byte[1024];
                int bytesRead = in.read(buffer);

                if (bytesRead == -1) {
                    System.out.println("Client disconnected.");
                    break;
                }

                // Decode WebSocket frames (basic implementation, assumes text frames)
                byte[] decoded = decodeWebSocketFrame(buffer, bytesRead);
                String message = new String(decoded, "UTF-8");
                System.out.println("Received: " + message);

                // Echo the message back to the client
                sendMessage(out, "Echo: " + message);
            }
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    // Decode a WebSocket frame
    private static byte[] decodeWebSocketFrame(byte[] buffer, int length) {
        int payloadStart = 2;
        int payloadLength = buffer[1] & 0x7F;

        if (payloadLength == 126) {
            payloadStart = 4;
        } else if (payloadLength == 127) {
            payloadStart = 10;
        }

        byte[] mask = new byte[4];
        System.arraycopy(buffer, payloadStart, mask, 0, 4);

        int dataStart = payloadStart + 4;
        byte[] decoded = new byte[length - dataStart];
        for (int i = dataStart; i < length; i++) {
            decoded[i - dataStart] = (byte) (buffer[i] ^ mask[(i - dataStart) % 4]);
        }

        return decoded;
    }

    // Send a WebSocket message
    private static void sendMessage(OutputStream out, String message) throws IOException {
        byte[] messageBytes = message.getBytes("UTF-8");
        int frameSize = 2 + messageBytes.length;

        byte[] frame = new byte[frameSize];
        frame[0] = (byte) 0x81; // Text frame
        frame[1] = (byte) messageBytes.length;
        System.arraycopy(messageBytes, 0, frame, 2, messageBytes.length);

        out.write(frame);
    }
}
