package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.FileParameter;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.FileFolderType;

import javax.annotation.Nonnull;
import javax.swing.*;

public class FilePathP extends FileFolderType {
    public FilePathP(String name, Module module) {
        super(name, module);
    }

    public FilePathP(String name, Module module, @Nonnull String filePath) {
        super(name, module, filePath);
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new FileParameter(this,FileParameter.FileTypes.FILE_TYPE);
    }
}
