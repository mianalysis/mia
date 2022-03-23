package io.github.mianalysis.mia.object.parameters;

import io.github.mianalysis.mia.gui.parametercontrols.ParameterControl;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.gui.parametercontrols.FileParameter;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.FileFolderType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

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
