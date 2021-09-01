package io.github.mianalysis.MIA.Object.Parameters;

import io.github.mianalysis.MIA.GUI.ParameterControls.ParameterControl;
import io.github.mianalysis.MIA.GUI.ParameterControls.FileParameter;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.FileFolderType;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.Parameter;

import com.drew.lang.annotations.NotNull;
import java.io.File;

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
        return new FileParameter(this,FileParameter.FileTypes.FOLDER_TYPE);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        FolderPathP newParameter = new FolderPathP(name,newModule,getPath(),getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }

    @Override
    public boolean verify() {
        // Check file is specified
        if (!super.verify()) return false;

        // Check file exists
        if (!new File(path).exists()) return false;

        // Finally, check file is a file (not a folder)
        return new File(path).isDirectory();

    }
}
