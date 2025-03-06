import java.sql.*;

public class Database {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=chatdb;encrypt=false;";
    private static final String USER = "sa";
    private static final String PASSWORD = "1211";
    private static Connection connection = null;

    static {
        try {
            // Đăng ký driver đúng cho SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("SQL Server JDBC Driver không tìm thấy!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveMessage(String sender, String message) {
        String sql = "INSERT INTO messages (sender, message, timestamp) VALUES (?, ?, GETDATE())";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sender);
            pstmt.setString(2, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet getMessages() {
        String sql = "SELECT TOP 100 * FROM messages ORDER BY timestamp DESC";
        try {
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Thêm phương thức main để kiểm tra kết nối
    public static void main(String[] args) {
        if (testConnection()) {
            System.out.println("Kết nối thành công!");
        } else {
            System.out.println("Kết nối thất bại!");
        }
    }
}
