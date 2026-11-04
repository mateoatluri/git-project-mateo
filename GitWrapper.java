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

public class GitWrapper {

    private static Git repo;

    /**
     * Initializes a new Git repository.
     * Creates git/, git/objects/, git/index, and git/HEAD.
     */
    public static void init() {
        repo = new Git();
    }

    /**
     * Stages a file for the next commit.
     * Throws IOException if the path does not exist or is a directory.
     * Does nothing if the file's content is already staged for that path.
     *
     * @param filePath The path to the file to stage.
     */
    public static void add(String filePath) throws IOException {
        String hash = Git.hashFile(filePath);
        String entry = hash + " " + filePath;
        Path indexPath = Path.of(repo.getIndex().getPath());

        if (Files.readAllLines(indexPath).contains(entry)) {
            return;
        }

        repo.createBlob(filePath);
        repo.addFileEntry(filePath);
    }

    /**
     * Creates a commit capturing the current state of the index.
     * Builds trees, writes the commit file, updates HEAD.
     *
     * Commit format:
     * tree: <tree_sha>
     * parent: <parent_sha> (omit line for initial commit)
     * author: <author>
     * date: <date>
     * message: <message>
     *
     * @param author  The author name.
     * @param message The commit message.
     * @return The SHA-1 hash of the new commit.
     */
    public static String commit(String author, String message) throws IOException {
        try {
            return repo.commit(author, message);
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

    /**
     * EXTRA CREDIT: Restores the working directory to the state of a specific
     * commit.
     * Traces the commit's root tree and recreates all files and directories.
     *
     * @param commitHash The SHA-1 hash of the commit to check out.
     */
    public static void checkout(String commitHash) {
        // implement here
    }
}