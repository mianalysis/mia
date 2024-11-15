//TODO: Verbose option on System.out.println

package io.github.mianalysis.mia.process.system;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;


/**
 * Created by Stephen on 16/10/2016.
 */
public class FileCrawler {
    public Folder rootFolder = null; //Root folder
    public Folder folder = null; //Current folder
    private HashSet<FileCondition> fileConditions = new HashSet<FileCondition>(); //List of file conditions
    private HashSet<FileCondition> folderConditions = new HashSet<FileCondition>(); //List of folder conditions
    private boolean includeSubFolders = true;
    private boolean ignoreCase = false;

    public FileCrawler(File root) {
        folder = new Folder(root,null);
        rootFolder = folder;

    }

    public void setRootFolder(File root) {
        folder = new Folder(root,null);
        rootFolder = folder;

    }

    public Folder getRootFolderAsFolder() {
        return rootFolder;
    }

    public File getRootFolderAsFile() {
        return rootFolder.getFolderAsFile();

    }

    public Folder getCurrentFolderAsFolder() {
        return folder;

    }

    public File getCurrentFolderAsFile() {
        return folder.getFolderAsFile();

    }

    /**
     * Runs through all folders, listing files and folder
     */
    public void scanFolders() {
        while(folder.getParent() != null | folder.hasMoreFolders()) {
            Folder next_folder = folder.getNextFolder();

            if (next_folder != null) {
                next_folder.printFiles();
                folder = next_folder;

            } else { //Reached deepest point, so go to current folder's parent
                folder = folder.getParent();

            }
        }
    }

    public boolean hasMoreFilesInFolder() {
        return folder.hasMoreFiles();

    }

    public boolean hasMoreFoldersInFolder() {
        return folder.hasMoreFolders();

    }

    public File getNextValidFileInFolder() {
        if (folder == null) return null;

        File file = folder.getNextFile();

        // While the current file doesn't match the conditions, but isn't null go to the next file
        while ((!testFileConditions(file)) & (file != null)) {
            file = folder.getNextFile();

        }

        return file;
    }

    public File getNextFileInFolder() {
        if (folder == null) return null;

        return folder.getNextFile();

    }

    public ArrayList<File> getAllValidFilesInFolder() {
        ArrayList<File> files = new ArrayList<File>();

        // Resetting to the start of the current folder
        if (folder == null) return files;
        folder.resetCurrentFileNumber();

        // Adding all valid files to the ArrayList
        File file = getNextValidFileInFolder();

        while (file != null) {
            files.add(file);
            file = getNextValidFileInFolder();
        }

        return files;
    }

    public File getNextValidFileInStructure() {
        // First, attempt to return the next file in the current folder
        File next_file = getNextValidFileInFolder();
        if (next_file != null) {
            return next_file;

        }

        // Now, try moving to the next folder
        if (includeSubFolders) {
            while (goToNextValidFolder()) {
                next_file = getNextValidFileInFolder();
                if (next_file != null) {
                    return next_file;

                }
            }
        }

        // Failing this, there are no files left, so return null
        return null;

    }

    public int getNumberOfValidFilesInStructure() {
        Folder folder_temp = folder;
        folder = rootFolder;

        int count = 0;

        // First, attempt to return the next file in the current folder

        File next_file = getNextValidFileInStructure();
        while (next_file != null) {
            count++;
            next_file = getNextValidFileInStructure();
        }

        folder = folder_temp;

        return count;

    }

    public int getNumberOfValidFoldersInStructure() {
        Folder folder_temp = folder;
        folder = rootFolder;

        int count = 0;

        // First, attempt to return the next file in the current folder

        boolean validFolder = goToNextValidFolder();
        while (validFolder) {
            count++;
            validFolder = goToNextValidFolder();
        }

        folder = folder_temp;

        return count;

    }

    /**
     * Depth of the folder relative to the root folder.  The root folder has a depth of 0
     * @return depth of the current folder
     */
    public int getCurrentDepth() {
        int depth = 0;

        Folder parent = folder.getParent();
        while (parent != null) {
            depth++;
            parent = parent.getParent();
        }

        return depth;

    }

    public boolean goToNextValidFolder() {
        boolean hasmore = goToNextFolder();

        while (hasmore) {
            if (testFolderConditions(folder.getFolderAsFile())) {
                return true;
            }

            hasmore = goToNextFolder();

        }

        return false;

    }

    /**
     * Sets the current folder as the next folder in the structure.
     * @return true if there was a next folder and false if the end of the structure has been reached
     */
    public boolean goToNextFolder() {
        if (folder == null) return false;

        // Returns a condition stating if there are more folders to go
        boolean hasmore = true;

        boolean foundNext = false;

        while (!foundNext) {
            Folder next_folder = folder.getNextFolder();

            if (next_folder != null) {
                folder = next_folder;
                foundNext = true;
//            System.out.println(">>> "+folder.getFolderAsFile().getAbsolutePath());

            } else { //Reached deepest point, so go to current folder's parent
                folder = folder.getParent();
//            if (folder != null) System.out.println("<<< "+folder.getFolderAsFile().getAbsolutePath());
            }

            if (folder == null) {
                hasmore = false;
                foundNext = true;
            }
        }

        return hasmore;

    }

    public void addFileCondition(FileCondition file_condition) {
        fileConditions.add(file_condition);

    }

    public void addFolderCondition(FileCondition folder_condition) {
        folderConditions.add(folder_condition);

    }

    public boolean testFileConditions(File test_file) {
        boolean cnd = true;

        if (fileConditions != null) {
            Iterator<FileCondition> iterator = fileConditions.iterator();
            while(iterator.hasNext()) {
                //If any condition fails, the output is false
                if (!iterator.next().test(test_file,ignoreCase)) cnd = false;

            }
        }

        return cnd;

    }

    public boolean testFolderConditions(File test_folder) {
        boolean cnd = true;

        if (folderConditions != null) {
            Iterator<FileCondition> iterator = folderConditions.iterator();
            while (iterator.hasNext()) {
                //If any condition fails, the output is false
                if (!iterator.next().test(test_folder,ignoreCase)) cnd = false;

            }
        }

        return cnd;

    }

    public boolean testCurrentFolderIsValid() {
        if (folder == null) {
            return false;
        } else {
            return testFolderConditions(folder.getFolderAsFile());

        }
    }

    public void resetIterator() {
        folder = rootFolder;
        Folder next_folder = folder.getNextFolder();

        if (next_folder != null) {
            folder = next_folder;

        } else { //Reached deepest point, so go to current folder's parent
            folder.resetCurrentFileNumber();
            folder.resetCurrentFolderNumber();
            folder = folder.getParent();

        }
    }

    public void setIncludeSubFolders(boolean includeSubFolders) {
        this.includeSubFolders = includeSubFolders;
    }

    public boolean getIncludeSubFolders() {
        return includeSubFolders;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    public static String checkPath(String path) {
        File file = new File(path);

        // Check if the full path exists
        if (file.exists())
            return path;

        // If this file doesn't exist, test to see if its parent does
        String parentPath = file.getParent();

        // If there's no parent (i.e. we're at the path root) return an empty string
        if (parentPath == null)
            return "";

        return checkPath(parentPath);

    }
}