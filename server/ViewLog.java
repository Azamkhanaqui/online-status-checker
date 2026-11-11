import java.sql.*;

public class ViewLog {
    public static void main(String[] args) throws SQLException {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:status.db");
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM status_log")) {
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | "
                        + rs.getString("status") + " | "
                        + rs.getString("changed_at"));
            }
        }
    }
}