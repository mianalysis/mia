package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.FileParameter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.FileFolderType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;
import java.io.File;

public class FileFolderPathP extends FileFolderType {
    public FileFolderPathP(String name, Module module) {
        super(name,module);
    }

    public FileFolderPathP(String name, Module module, @Nonnull String fileFolderPath) {
        super(name,module,fileFolderPath);
    }

    public FileFolderPathP(String name, Module module, @Nonnull String fileFolderPath, String description) {
        super(name,module,fileFolderPath,description);
    }

    @Override
    public boolean isDirectory() {
        String fileFolderPath = getPath();
        if (fileFolderPath == null) return false;
        return new File(fileFolderPath).isDirectory();
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new FileParameter(this,FileParameter.FileTypes.EITHER_TYPE);
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new FileFolderPathP(name,module,getPath(),getDescription());
    }
}
