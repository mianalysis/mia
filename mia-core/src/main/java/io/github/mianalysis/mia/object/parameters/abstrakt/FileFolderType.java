package io.github.mianalysis.mia.object.parameters.abstrakt;

import java.io.File;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;

public abstract class FileFolderType extends TextSwitchableParameter {
    protected String path = "";

    public FileFolderType(String name, Module module) {
        super(name, module);
    }

    public FileFolderType(String name, Module module, @NotNull String path) {
        super(name, module);
        this.path = path;
    }

    public FileFolderType(String name, Module module, @NotNull String path, String description) {
        super(name, module, description);
        this.path = path;
    }

    public String getPath() {
        return GlobalVariables.convertString(path, module.getModules());
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDirectory() {
        if (path == null)
            return false;
            
        String fileFolderPath = getPath();
        
        return new File(fileFolderPath).isDirectory();
    }

    @Override
    public <T> T getValue(WorkspaceI workspace) {
        return (T) GlobalVariables.convertString(path, module.getModules());
    }

    @Override
    public <T> void setValue(T value) {
        setPath((String) value);
    }

    @Override
    public String getRawStringValue() {
        return path;
    }

    @Override
    public void setValueFromString(String string) {
        setPath(string);
    }

    @Override
    public boolean verify() {
        // Checking a file has been specified
        if (path == null || path.equals(""))
            return false;

        // Checking the file exists
        String converted = GlobalVariables.convertString(path, module.getModules());

        return new File(converted).exists();

    }
}
