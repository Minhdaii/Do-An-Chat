import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClient extends JFrame {
    private JTextArea messageArea;
    private JTextField messageInput;
    private JButton sendButton;
    private JPanel inputPanel;
    private JPanel headerPanel;
    private JLabel statusLabel;
    private JLabel welcomeLabel;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    
    private Color primaryColor = new Color(64, 81, 181);
    private Color accentColor = new Color(255, 64, 129);
    private Color lightGray = new Color(240, 240, 240);
    private Font defaultFont = new Font("Segoe UI", Font.PLAIN, 14);

    public ChatClient() {
        super("Ung Dung Chat");
        setLayout(new BorderLayout(0, 0));
        
        // Header Panel with Welcome Label
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(primaryColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        welcomeLabel = new JLabel("Xin chao!");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        statusLabel = new JLabel("Dang ket noi...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Message Area
        messageArea = new JTextArea(20, 40);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(defaultFont);
        messageArea.setMargin(new Insets(10, 10, 10, 10));
        messageArea.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Input Panel
        inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(lightGray);
        
        messageInput = new JTextField();
        messageInput.setFont(defaultFont);
        messageInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        sendButton = new JButton("Gui");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(accentColor);
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        sendButton.setFocusPainted(false);
        
        ActionListener sendAction = e -> guiTinNhan();
        messageInput.addActionListener(sendAction);
        sendButton.addActionListener(sendAction);
        
        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        messageInput.setEditable(false);
    }
    
    private void guiTinNhan() {
        String message = messageInput.getText();
        if (!message.trim().isEmpty()) {
            out.println(message);
            messageInput.setText("");
        }
    }
    
    private void themTinNhan(String sender, String message, String time) {
        String formattedMessage = String.format("%s - %s: %s\n", time, sender, message);
        messageArea.append(formattedMessage);
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }
    
    private void themTinNhanHeThong(String message) {
        messageArea.append("He thong: " + message + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }
    
    private String layTenNguoiDung() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel("Chon ten nguoi dung:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JTextField usernameField = new JTextField();
        usernameField.setFont(defaultFont);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(usernameField, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Dang Ky Nguoi Dung", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        if (result == JOptionPane.OK_OPTION && !usernameField.getText().trim().isEmpty()) {
            return usernameField.getText().trim();
        } else {
            return "Khach" + System.currentTimeMillis() % 1000;
        }
    }
    
    private void chay() throws IOException {
        try {
            String serverAddress = "localhost";
            statusLabel.setText("Dang ket noi den may chu...");
            
            Socket socket = new Socket(serverAddress, 9999);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            while (true) {
                String line = in.readLine();
                if (line.startsWith("SUBMITNAME")) {
                    username = layTenNguoiDung();
                    out.println(username);
                    statusLabel.setText("Dang dang nhap...");
                } else if (line.startsWith("NAMEACCEPTED")) {
                    messageInput.setEditable(true);
                    messageInput.requestFocus();
                    statusLabel.setText("Da ket noi: " + username);
                    welcomeLabel.setText("Xin chao " + username); // Update welcome label with username
                    themTinNhanHeThong("Ban da tham gia cuoc tro chuyen voi ten " + username);
                } else if (line.startsWith("MESSAGE")) {
                    String[] parts = line.substring(8).split(": ", 2);
                    if (parts.length == 2) {
                        themTinNhan(parts[0], parts[1], timeFormat.format(new Date()));
                    }
                } else if (line.startsWith("USERJOINED")) {
                    String joinedUser = line.substring(10);
                    themTinNhanHeThong(joinedUser + " da tham gia cuoc tro chuyen");
                } else if (line.startsWith("USERLEFT")) {
                    String leftUser = line.substring(8);
                    themTinNhanHeThong(leftUser + " da roi khoi cuoc tro chuyen");
                } else if (line.startsWith("USERLIST")) {
                    // No need to process user list
                    themTinNhanHeThong("Da nhan danh sach nguoi dung tu may chu");
                }
            }
        } catch (Exception e) {
            statusLabel.setText("Mat ket noi");
            themTinNhanHeThong("Loi ket noi: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClient client = new ChatClient();
            client.setVisible(true);
            new Thread(() -> {
                try {
                    client.chay();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
}
