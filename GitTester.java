import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitTester {
    public static void main(String[] args) throws IOException {
        GitWrapper.init();
        GitWrapper.add("Hello.txt");
        GitWrapper.add("file1.txt");
        String hash = GitWrapper.commit("John Doe", "Initial commit");
        System.out.println("Commit: " + hash);
    }
}