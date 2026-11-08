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

public class Git {

    private File git;
    private File objects;
    private File index;
    private File head;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        Git repo = new Git();

        repo.createBlob("Hello.txt");
        repo.addFileEntry("Hello.txt");
        repo.createBlob("file1.txt");
        repo.addFileEntry("file1.txt");

        repo.createBlob("testerFolder/fileTest.txt");
        repo.addFileEntry("testerFolder/fileTest.txt");


        String root = repo.createTreeUsingIndex();
        System.out.println("Root tree: " + root);

        // testing commit
        System.out.println("\n" + "------------TESTING COMMIT-----------" + "\n");
        String testCommit = repo.commit("mateo", "please let this work");
        System.out.println("First Commit Hash: " + testCommit);
        System.out.println("First Commit contents:\n" + Files.readString(Path.of("git/objects/" + testCommit)));

        String testCommitTwo = repo.commit("mateo", "please let this work again");
        System.out.println("Second Commit Hash: " + testCommitTwo);
        System.out.println("Second Commit contents:\n" + Files.readString(Path.of("git/objects/" + testCommitTwo)));

        //System.out.println("-----Checking out----");
        //checkout("0f0efceac74ca33c736075ac5e9a34b7d5267264");


    }

    public Git() {
        createGit();
    }

    /**
     * @return the objects
     */
    public File getObjects() {
        return objects;
    }

    /**
     * @param objects the objects to set
     */
    public void setObjects(File objects) {
        this.objects = objects;
    }

    /**
     * @return the index
     */
    public File getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(File index) {
        this.index = index;
    }

    /**
     * @return the head
     */
    public File getHead() {
        return head;
    }

    /**
     * @param head the head to set
     */
    public void setHead(File head) {
        this.head = head;
    }

    public void createGit() {
        try {

            int counter = 0;
            git = new File("git/");
            if (!git.mkdir()) {
                counter++;
            }

            objects = new File(git, "objects/");
            if (!objects.mkdir()) {
                counter++;
            }

            index = new File(git, "index");
            if (!index.createNewFile()) {
                counter++;
            }

            head = new File(git, "HEAD");
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

    public void createBlob(String takenFile) throws IOException {

        String fileHash = hashFile(takenFile);

        File newFile = new File(objects, fileHash);
        newFile.createNewFile();

        Files.write(Path.of(newFile.getPath()), Files.readAllBytes(Path.of(takenFile)));

        // BufferedReader fileReader = new BufferedReader(new FileReader(takenFile));
        // FileWriter writeFile = new FileWriter(newFile.toPath().toString());
        // String fileContents = "";
        // while ((fileContents = fileReader.readLine()) != null) {
        // writeFile.write(fileContents + "\n");
        // }
        // fileReader.close();
        // writeFile.close();

    }

    public void addFileEntry(String takenFile) throws IOException {

        String fileHash = hashFile(takenFile);

        BufferedReader fileReader = new BufferedReader(new FileReader(index));

        String firstLine = fileReader.readLine();
        String line = firstLine;
        String newEntry = fileHash + " " + takenFile;
        StringBuilder contents = new StringBuilder();
        boolean found = false;

        while (line != null) {
            int firstSpace = line.indexOf(' ');
            String path = line.substring(firstSpace + 1);
            if (path.equals(takenFile)) {
                line = newEntry;
                found = true;
            }
            contents.append(line);
            line = fileReader.readLine();
            if (line != null) {
                contents.append("\n");
            }
        }
        fileReader.close();

        FileWriter writeFile = new FileWriter(index);
        writeFile.write(contents.toString());

        if (!found) {
            if (firstLine == null) {
                writeFile.write(newEntry);
            } else {
                writeFile.write("\n" + newEntry);
            }
        }

        writeFile.close();

    }

    public String createTree(String[] workingList, String dirPath) throws IOException {
        String start = dirPath;
        if (!dirPath.isEmpty()) {
            start += "/";
        }
        String content = "";
        for (int i = 0; i < workingList.length; i++) {
            if (workingList[i] == null) {
                i++;
            }
            String path = workingList[i].substring(46);
            if (path.startsWith(start)) {
                String hash = workingList[i].substring(0, 46);
                String name = path.substring(start.length());
                if (!name.contains("/")) {
                    content += hash + name + "\n";
                }
            }
        }
        File tempFile = new File("cur");
        tempFile.createNewFile();
        FileWriter curFile = new FileWriter(tempFile);
        curFile.write(content);
        curFile.close();
        createBlob(tempFile.getPath());
        String treeHash = hashFile(tempFile.getPath());
        return treeHash;

    }

    // chekc / and then alphebetical
    public static String[] redoList(String[] workingList) {
        for (int i = 0; i < workingList.length; i++) {
            if (!workingList[i].substring(0, 5).equals("blob ") && !workingList[i].substring(0, 5).equals("tree ")) {
                workingList[i] = "blob " + workingList[i];
            }
        }
        for (int i = 0; i < workingList.length; i++) {
            for (int j = i + 1; j < workingList.length; j++) {
                String first = workingList[i].substring(46);
                String second = workingList[j].substring(46);
                if (countSlashes(first) < countSlashes(second)) {
                    String temp = workingList[i];
                    workingList[i] = workingList[j];
                    workingList[j] = temp;
                } else if (countSlashes(first) == countSlashes(second)) {
                    if (first.compareTo(second) > 0) {
                        String temp = workingList[i];
                        workingList[i] = workingList[j];
                        workingList[j] = temp;
                    }
                }
            }
        }
        return workingList;
    }

    public static int countSlashes(String path) {
        int count = 0;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                count++;
            }
        }
        return count;
    }

    public String[] getLeafRoot(String[] workingList) throws IOException {
        workingList = redoList(workingList);
        String path = workingList[0].substring(46);
        int count = countSlashes(path);
        int other = 0;
        String directory = "";
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                other++;
                if (count == other) {
                    directory = path.substring(0, i);
                }
            }

        }
        String treeHash = createTree(workingList, directory);
        return collapseWorkingList(workingList, directory, treeHash);
    }

    public static String[] collapseWorkingList(String[] workingList, String directory, String treeHash) {
        String prefix = directory + '/';
        int alrExist = 0;
        for (int i = 0; i < workingList.length; i++) {
            String path = workingList[i].substring(46);
            if (path.startsWith(prefix)) {
                alrExist++;
            }
        }
        String[] newList = new String[workingList.length - alrExist + 1];
        int cur = 0;
        for (int i = 0; i < workingList.length; i++) {
            String path = workingList[i].substring(46);
            if (!path.startsWith(prefix)) {
                newList[cur] = workingList[i];
                cur++;
            }
        }
        newList[cur] = "tree " + treeHash + " " + directory;
        return redoList(newList);
    }

    public String createFinalTree(String[] workingList) throws IOException {
        workingList = redoList(workingList);
        while (workingList.length > 0 && countSlashes(workingList[0].substring(46)) > 0) {
            workingList = getLeafRoot(workingList);
        }
        return createTree(workingList, "");
    }

    public String createTreeUsingIndex() throws IOException {
        List<String> indexLines = Files.readAllLines(index.toPath());
        String[] workingList = new String[indexLines.size()];
        workingList = indexLines.toArray(workingList);
        return createFinalTree(workingList);

    }

    public String commit(String author, String message) throws IOException, NoSuchAlgorithmException{
        List<String> entries = Files.readAllLines(Path.of(index.getPath()));
        String treeHash = createFinalTree(entries.toArray(new String [0]));
        Path headPath = Path.of(head.getPath());
        String parentHash = new String(Files.readAllBytes(headPath)).trim();

        String content = "tree: " + treeHash + "\n";
        if(!parentHash.isEmpty()){
            content += "parent: " + parentHash + "\n";
        }
        content += "author: " + author + "\n";
        content += "date: " + java.time.Instant.now() + "\n";
        content += "message: " + message + "\n";

        byte[] commitBytes = content.getBytes();
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(commitBytes);
        String commitHash = HexFormat.of().formatHex(hash);

        File commitFile = new File(objects, commitHash);
        Files.write(Path.of(commitFile.getPath()), commitBytes);
        Files.write(headPath, commitHash.getBytes());
        return commitHash;
    }


    ///////////////////////
    /// the checkout code below (3 methods below this) I pasted from what I (mateo) wrote in 
    /// git-project-Margaux - mr jloppppez said it was okay :)
    
        public static void checkout(String commitHash) throws IOException {
        // implement here

        
        BufferedReader dirReader = new BufferedReader(new FileReader("git/objects/" + commitHash));
        String line = dirReader.readLine();
        dirReader.close();

        int firstSpace = line.indexOf(" ");
        String rootHash = line.substring(firstSpace + 1);

        checkoutRecursive("tree", rootHash, ".");
        

    }

    public static void checkoutRecursive(String objectType, String objectHash, String dirPath) throws IOException {
        //base case
        if (objectType.equals("blob")) {
            createFile(objectHash, dirPath);
            return;
        }

        // it must be tree
        File folder = new File(dirPath);
        folder.mkdir();

        //make array list w/ all the contents
        ArrayList<String> dirChildren = new ArrayList<String>();
        BufferedReader dirReader = new BufferedReader(new FileReader("git/objects/" + objectHash));
        String line;

        while ((line = dirReader.readLine()) != null) {
                dirChildren.add(line);
            }
        dirReader.close();

        for (int i = 0; i < dirChildren.size(); i++) {
            int firstSpace = dirChildren.get(i).indexOf(" ");
            int lastSpace = dirChildren.get(i).lastIndexOf(" ");
    
            String childType = dirChildren.get(i).substring(0, firstSpace);
            String childHash = dirChildren.get(i).split(" ")[1];
            String childName = dirChildren.get(i).substring(lastSpace + 1);
            String childPath = dirPath + "/" + childName;

            checkoutRecursive(childType, childHash, childPath);
        }

    }

    public static String createFile(String dirHash, String dirPath) throws IOException {

        //copied code from createBlob, but make it so it now creates a real file, not a blob in objects
        Path blobPath = Path.of("git/objects", dirHash);
        byte[] blobContents = Files.readAllBytes(blobPath);
        Path childPath = Path.of(dirPath);

        Files.write(childPath, blobContents);

        return dirPath;
        
    }

}