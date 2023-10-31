package io.github.mianalysis.mia.process.system;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Stephen on 16/10/2016.
 */
public class Folder {
    private final File folder;
    private final Folder parent;
    private File[] files;
    private File[] folders;
    private int file_count = 0; // Current file count
    private int folder_count = 0; // Current folder count
    private int n_folders = 0; // Number of children folders
    private int n_files = 0; // Number of children files

    public Folder(File folder, Folder parent) {
        this.folder = folder;
        this.parent = parent;

        File[] file_list = folder.listFiles();

        // Counting the number of files and folders
        if (file_list != null) {
            for (int i = 0; i < file_list.length; i++) {
                if (file_list[i].isDirectory()) {
                    n_folders++;
                } else {
                    n_files++;
                }
            }

            // Creating arrays for files and folders
            files = new File[n_files];
            folders = new File[n_folders];
            for (int i = 0; i < file_list.length; i++) {
                if (file_list[i].isDirectory()) {
                    folders[folder_count] = file_list[i];
                    folder_count++;
                } else {
                    files[file_count] = file_list[i];
                    file_count++;
                }
            }
        }

        // Resetting the counters
        file_count = 0;
        folder_count = 0;

        // Sorting alphabetically
        if (files != null)
            Arrays.sort(files);

        if (folders != null)
            Arrays.sort(folders);

    }

    public File getFolderAsFile() {
        return folder;

    }

    public Folder getParent() {
        return parent;

    }

    public boolean hasMoreFiles() {
        if (file_count < n_files) {
            return true;

        } else {
            return false;

        }
    }

    public File getNextFile() {
        if (hasMoreFiles()) {
            return files[file_count++];

        } else {
            return null;

        }

    }

    public boolean hasMoreFolders() {
        if (folder_count < n_folders) {
            return true;

        } else {
            return false;

        }
    }

    public Folder getNextFolder() {
        if (hasMoreFolders()) {
            return new Folder(folders[folder_count++], this);

        } else {
            return null;

        }
    }

    public File[] getFolders() {
        return folders;

    }

    public File[] getFiles() {
        return files;

    }

    public void printContents() {
        String name = folder.getName();
        Folder parent = this.parent;

        while (parent != null) {
            name = parent.getFolderAsFile().getName() + "/" + name;
            parent = parent.getParent();
        }
        System.out.println(name);

        if (hasMoreFolders()) {
            for (int i = 0; i < folders.length; i++) {
                System.out.println("    Folders: " + folders[i].getName());

            }
        }

        if (hasMoreFiles()) {
            for (int i = 0; i < files.length; i++) {
                System.out.println("    Files: " + files[i].getName());

            }
        }

        System.out.println(" ");
    }

    public void printFiles() {
        String name = folder.getName();
        Folder parent = this.parent;

        while (parent != null) {
            name = parent.getFolderAsFile().getName() + "/" + name;
            parent = parent.getParent();
        }
        System.out.println(name);

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                System.out.println("    " + files[i].getName());

            }
        }

        System.out.println(" ");
    }

    public void printFolders() {
        String name = folder.getName();
        Folder parent = this.parent;

        while (parent != null) {
            name = parent.getFolderAsFile().getName() + "/" + name;
            parent = parent.getParent();
        }
        System.out.println(name);

        if (folders != null) {
            for (int i = 0; i < folders.length; i++) {
                System.out.println("    " + folders[i].getName());

            }
        }

        System.out.println(" ");
    }

    public int getNumberOfFiles() {
        return n_files;

    }

    public int getNumberOfFolders() {
        return n_folders;

    }

    public int getCurrentFileNumber() {
        return file_count;

    }

    public int getCurrentFolderNumber() {
        return folder_count;

    }

    public void resetCurrentFileNumber() {
        file_count = 0;

    }

    public void resetCurrentFolderNumber() {
        folder_count = 0;

    }
}
