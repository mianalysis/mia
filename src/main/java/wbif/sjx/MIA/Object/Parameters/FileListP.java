package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControls.FileListParameter;
import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisJob;

import java.util.ArrayList;

public class FileListP extends Parameter {
    ArrayList<AnalysisJob> fileList = new ArrayList<>();


    public FileListP(String name, Module module) {
        super(name, module);
    }

    public FileListP(String name, Module module, String description) {
        super(name, module, description);
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new FileListParameter(this);
    }

    @Override
    public <T> T getValue() {
        return (T) fileList;
    }

    @Override
    public <T> void setValue(T value) {

    }

    @Override
    public String getRawStringValue() {
        return "CONVERT TO COMMA-SEPARATED STRING OF FILE PATHS";
    }

    @Override
    public void setValueFromString(String string) {

    }

    @Override
    public boolean verify() {
        return false;
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        return null;
    }
}
