
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/hospitality_db?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "Saurabh@2005";

        try (Connection con =
                     DriverManager.getConnection(url, user, password)) {

            System.out.println("Database connected successfully!");
            System.out.println("Connection valid: " + con.isValid(2));

        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            System.out.println(e.getMessage());
        }
    }
}

