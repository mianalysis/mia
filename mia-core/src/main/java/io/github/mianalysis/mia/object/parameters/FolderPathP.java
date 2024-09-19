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

public class FolderPathP extends FileFolderType {
    public FolderPathP(String name, Module module) {
        super(name, module);
    }

    public FolderPathP(String name, Module module, @NotNull String filePath) {
        super(name, module, filePath);
    }

    public FolderPathP(String name, Module module, @NotNull String filePath, String description) {
        super(name, module, filePath, description);
    }

    @Override
    protected ParameterControl initialiseControl() {
        return ParameterControlFactory.getFileFolderSelectionControl(this, FileTypes.FOLDER_TYPE);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        FolderPathP newParameter = new FolderPathP(name, newModule, getPath(), getDescription());

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

        // Check file exists
        if (!new File(converted).exists())
            return false;

        // Finally, check file is a file (not a folder)
        return new File(converted).isDirectory();

    }
}
