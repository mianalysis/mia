package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.FileParameter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.FileFolderType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class FolderPathP extends FileFolderType {
    public FolderPathP(String name, Module module) {
        super(name, module);
    }

    public FolderPathP(String name, Module module, @Nonnull String filePath) {
        super(name, module, filePath);
    }

    public FolderPathP(String name, Module module, @Nonnull String filePath, String description) {
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
        return (T) new FilePathP(name,module,getPath(),getDescription());
    }
}
