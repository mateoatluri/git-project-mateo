import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.nio.file.Files;
import java.nio.file.Path;

public class Git {

    public static void main(String[] args) {

        createGit();

        try {
            System.out.println(hashFile("Hello.txt"));
        } catch (IOException e) {
            System.out.println("SHA-1 is not available " + e);
        }

        

    }

    public static void createGit() {
        try {

            int counter = 0;
             File git = new File("git/");
            if (!git.mkdir()) {
                counter++;
            }
   

            File objects = new File(git, "objects/");
            if (!objects.mkdir()) {
                counter++;
            }

            File index = new File(git, "index");
            if (!index.createNewFile()) {
                counter++;
            }


            File head = new File(git, "head");
            if (!head.createNewFile()) {
                counter++;
            }

            if (counter >= 4) {
                System.out.println("Git Repository Already Exists");
            } else {
                System.out.println("Git Repository Created");
            }

        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());


        }
    
    }

    public static String hashFile(String filePath) throws IOException {
        // TODO (FH-4): read the whole file, digest it, convert the bytes to hex

        Path path = Path.of(filePath);
        if (!Files.isRegularFile(path)) {
            throw new IOException("No such file: " + filePath);
        }

        byte[] fileBytes = Files.readAllBytes(path);

        MessageDigest digest;
        
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available", e);
        }

        byte[] hash = digest.digest(fileBytes);

        return HexFormat.of().formatHex(hash);

   
    }

}