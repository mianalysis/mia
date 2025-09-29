package io.github.mianalysis.mia.object.parameters;

import java.io.File;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.parameters.abstrakt.FileFolderType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.process.ParameterControlFactory;
import io.github.mianalysis.mia.process.system.FileTools.FileTypes;

public class FilePathP extends FileFolderType {
    public FilePathP(String name, Module module) {
        super(name, module);
    }

    public FilePathP(String name, Module module, @NotNull String filePath) {
        super(name, module, filePath);
    }

    public FilePathP(String name, Module module, @NotNull String filePath, String description) {
        super(name, module, filePath, description);
    }

    @Override
    public void setPath(String path) {
        if (path == null)
            return;

        this.path = path;

    }

    @Override
    protected ParameterControl initialiseControl() {
        return ParameterControlFactory.getFileFolderSelectionControl(this, FileTypes.FILE_TYPE);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        FilePathP newParameter = new FilePathP(name, newModule, getPath(), getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }

    @Override
    public boolean verify() {
        // Check file is specified
        if (!super.verify())
            return false;

        // Checking the file exists
        String converted = GlobalVariables.convertString(path, module.getModules());

        // Finally, check file is a file (not a folder)
        return new File(converted).isFile();

    }
}
