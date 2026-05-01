import org.mindrot.jbcrypt.BCrypt;

public class GeneratePasswords {
    public static void main(String[] args) {
        String[] passwords = {
            "SecurePass123!",  // john.smith
            "Welcome2024!",    // jane.doe
            "Admin@2024",      // admin.user
            "Disabled123!",    // disabled.user
            "BobSecure99!"     // bob.wilson
        };
        
        String[] usernames = {
            "john.smith",
            "jane.doe",
            "admin.user",
            "disabled.user",
            "bob.wilson"
        };
        
        System.out.println("Generating BCrypt hashes for test users...\n");
        
        for (int i = 0; i < passwords.length; i++) {
            String hash = BCrypt.hashpw(passwords[i], BCrypt.gensalt(12));
            System.out.println(usernames[i] + " / " + passwords[i]);
            System.out.println("Hash: " + hash);
            System.out.println();
        }
    }
}

// Made with Bob
