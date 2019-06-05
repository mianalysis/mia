package wbif.sjx.MIA.Object.Parameters.Abstract;

import wbif.sjx.MIA.Module.Module;

import javax.annotation.Nonnull;
import java.io.File;

public abstract class FileFolderType extends Parameter {
    protected String path = "";

    public FileFolderType(String name, Module module) {
        super(name, module);
    }

    public FileFolderType(String name, Module module, String description) {
        super(name, module);
    }

    public FileFolderType(String name, Module module, @Nonnull String path, String description) {
        super(name, module, description);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path= path;
    }

    public abstract boolean isDirectory();

    @Override
    public <T> T getValue() {
        return (T) path;
    }

    @Override
    public <T> void setValue(T value) {
        path = (String) value;
    }

    @Override
    public String getRawStringValue() {
        return path;
    }

    @Override
    public boolean verify() {
        // Checking a file has been specified
        if (path == null || path.equals("")) return false;

        // Checking the file exists
        return new File(path).exists();
    }
}
