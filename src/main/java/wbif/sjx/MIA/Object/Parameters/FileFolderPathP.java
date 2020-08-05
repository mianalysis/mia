package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;

import com.drew.lang.annotations.NotNull;

import wbif.sjx.MIA.GUI.ParameterControls.FileParameter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.FileFolderType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class FileFolderPathP extends FileFolderType {
    public FileFolderPathP(String name, Module module) {
        super(name, module);
    }

    public FileFolderPathP(String name, Module module, @NotNull String fileFolderPath) {
        super(name,module,fileFolderPath);
    }

    public FileFolderPathP(String name, Module module, @NotNull String fileFolderPath, String description) {
        super(name,module,fileFolderPath,description);
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new FileParameter(this,FileParameter.FileTypes.EITHER_TYPE);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        FileFolderPathP newParameter = new FileFolderPathP(name,newModule,getPath(),getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;
    }
}
