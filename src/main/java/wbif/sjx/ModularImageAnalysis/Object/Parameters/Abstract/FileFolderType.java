package wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract;

import wbif.sjx.ModularImageAnalysis.Module.Module;

public abstract class FileFolderType extends Parameter {
    private String path;

    public FileFolderType(String name, Module module, String path) {
        super(name, module);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path= path;
    }

    public abstract boolean isDirectory();

}
