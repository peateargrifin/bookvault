import java.sql.*;
public class CheckAuthors {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/bookvault?stringtype=unspecified";
        try (Connection conn = DriverManager.getConnection(url, "postgres", "123456")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM authors");
            if(rs.next()) {
                System.out.println("TOTAL AUTHORS: " + rs.getInt(1));
            }
        }
    }
}
