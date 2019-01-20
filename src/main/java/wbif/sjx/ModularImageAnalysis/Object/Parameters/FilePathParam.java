package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.FileParameter;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.FileFolderType;

import javax.swing.*;

public class FilePathParam extends FileFolderType {
    public FilePathParam(String name, Module module, String filePath) {
        super(name, module, filePath);
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public String getValueAsString() {
        return getPath();
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new FileParameter(this,FileParameter.FileTypes.FILE_TYPE);
    }
}
