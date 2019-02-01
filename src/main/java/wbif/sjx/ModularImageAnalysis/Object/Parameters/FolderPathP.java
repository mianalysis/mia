package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.FileParameter;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.FileFolderType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;
import javax.swing.*;

public class FolderPathP extends FileFolderType {
    public FolderPathP(String name, Module module) {
        super(name, module);
    }

    public FolderPathP(String name, Module module, @Nonnull String filePath) {
        super(name, module, filePath);
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new FileParameter(this,FileParameter.FileTypes.FOLDER_TYPE);
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new FilePathP(name,module,getPath());
    }
}
