import org.mindrot.jbcrypt.BCrypt;

public class GenerateDemoUsers {
    public static void main(String[] args) {
        String[] passwords = {
            "Demo@123",      // demo.user1
            "Demo@123",      // demo.user2
            "Demo@123"       // demo.user3
        };
        
        String[] usernames = {
            "demo.user1",
            "demo.user2",
            "demo.user3"
        };
        
        String[] fullNames = {
            "Demo User 1",
            "Demo User 2",
            "Demo User 3"
        };
        
        String[] emails = {
            "demo.user1@prolifics.com",
            "demo.user2@prolifics.com",
            "demo.user3@prolifics.com"
        };
        
        System.out.println("==============================================");
        System.out.println("Generating 3 Demo Users for Live Presentation");
        System.out.println("==============================================\n");
        
        System.out.println("CSV Format Lines (add to users.csv):\n");
        
        for (int i = 0; i < passwords.length; i++) {
            String hash = BCrypt.hashpw(passwords[i], BCrypt.gensalt(12));
            
            // Print in CSV format
            System.out.println(usernames[i] + "," + hash + "," + fullNames[i] + "," + 
                             emails[i] + ",ACTIVE,USER,");
        }
        
        System.out.println("\n==============================================");
        System.out.println("Login Credentials for Demo:");
        System.out.println("==============================================\n");
        
        for (int i = 0; i < passwords.length; i++) {
            System.out.println("Username: " + usernames[i]);
            System.out.println("Password: " + passwords[i]);
            System.out.println("Full Name: " + fullNames[i]);
            System.out.println("Status: ACTIVE (No MFA configured)");
            System.out.println();
        }
        
        System.out.println("==============================================");
        System.out.println("All users have password: Demo@123");
        System.out.println("All users have NO MFA configured (will show QR code)");
        System.out.println("==============================================");
    }
}

// Made with Bob
