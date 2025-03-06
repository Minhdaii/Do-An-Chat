import java.io.*;
import java.net.*;
import java.util.HashSet;

public class ChatServer {
    private static final int PORT = 9999;
    private static HashSet<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat Server is running...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Yêu cầu client gửi tên
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null || name.trim().isEmpty()) {
                        return;
                    }

                    synchronized (writers) {
                        if (!writers.contains(out)) {
                            writers.add(out);
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED " + name);
                System.out.println(name + " has joined the chat.");
                
                // Nhận và gửi tin nhắn
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        break;
                    }

                    synchronized (writers) {
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    }
                }
            } catch (SocketException e) {
                System.out.println("Client disconnected: " + name);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                cleanup();
            }
        }

        private void cleanup() {
            if (out != null) {
                synchronized (writers) {
                    writers.remove(out);
                }
            }
            System.out.println(name + " has left the chat.");
            
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
