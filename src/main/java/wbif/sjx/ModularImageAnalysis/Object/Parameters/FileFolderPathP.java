package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.FileParameter;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.FileFolderType;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.io.File;

public class FileFolderPathP extends FileFolderType {
    public FileFolderPathP(String name, Module module, @Nonnull String fileFolderPath) {
        super(name,module,fileFolderPath);
    }

    @Override
    public boolean isDirectory() {
        String fileFolderPath = getPath();
        if (fileFolderPath == null) return false;
        return new File(fileFolderPath).isDirectory();
    }

    @Override
    public String getValueAsString() {
        return getPath();
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new FileParameter(this,FileParameter.FileTypes.EITHER_TYPE);
    }
}
