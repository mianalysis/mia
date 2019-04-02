package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.FileParameter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.FileFolderType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class FilePathP extends FileFolderType {
    public FilePathP(String name, Module module) {
        super(name, module);
    }

    public FilePathP(String name, Module module, @Nonnull String filePath) {
        super(name, module, filePath);
    }

    public FilePathP(String name, Module module, @Nonnull String filePath, String description) {
        super(name, module, filePath, description);
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new FileParameter(this,FileParameter.FileTypes.FILE_TYPE);
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new FilePathP(name,module,getPath(),getDescription());
    }
}
