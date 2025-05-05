
import java.sql.*;
import java.util.Scanner;

public class VotingApp {
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        while (true) {
            System.out.println("\nVoting App Menu:");
            System.out.println("1. Add Candidate");
            System.out.println("2. Cast Vote");
            System.out.println("3. Show Results");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1 -> addCandidate();
                case 2 -> castVote();
                case 3 -> showResults();
                case 4 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void addCandidate() {
        System.out.print("Enter candidate name: ");
        String name = scanner.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO candidates (name, votes) VALUES (?, 0)");
            stmt.setString(1, name);
            stmt.executeUpdate();
            System.out.println("Candidate added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding candidate: " + e.getMessage());
        }
    }

    private void castVote() {
        System.out.print("Enter voter name: ");
        String voter = scanner.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM voters WHERE name = ?");
            checkStmt.setString(1, voter);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("You have already voted.");
                return;
            }

            Statement stmt = conn.createStatement();
            ResultSet candidates = stmt.executeQuery("SELECT * FROM candidates");
            System.out.println("Candidates:");
            while (candidates.next()) {
                System.out.println(candidates.getInt("id") + ". " + candidates.getString("name"));
            }

            System.out.print("Enter candidate ID to vote: ");
            int candidateId = scanner.nextInt();
            scanner.nextLine();

            PreparedStatement voteStmt = conn.prepareStatement("UPDATE candidates SET votes = votes + 1 WHERE id = ?");
            voteStmt.setInt(1, candidateId);
            voteStmt.executeUpdate();

            PreparedStatement insertVoter = conn.prepareStatement("INSERT INTO voters (name) VALUES (?)");
            insertVoter.setString(1, voter);
            insertVoter.executeUpdate();

            System.out.println("Vote cast successfully.");
        } catch (SQLException e) {
            System.out.println("Error casting vote: " + e.getMessage());
        }
    }

    private void showResults() {
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM candidates");
            System.out.println("\nElection Results:");
            while (rs.next()) {
                System.out.println(rs.getString("name") + ": " + rs.getInt("votes") + " votes");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving results: " + e.getMessage());
        }
    }
}
