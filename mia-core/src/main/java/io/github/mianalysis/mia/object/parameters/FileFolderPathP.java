package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.abstrakt.FileFolderType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.process.ParameterControlFactory;
import io.github.mianalysis.mia.process.system.FileTools.FileTypes;

public class FileFolderPathP extends FileFolderType {
    public FileFolderPathP(String name, ModuleI module) {
        super(name, module);
    }

    public FileFolderPathP(String name, ModuleI module, @NotNull String fileFolderPath) {
        super(name,module,fileFolderPath);
    }

    public FileFolderPathP(String name, ModuleI module, @NotNull String fileFolderPath, String description) {
        super(name,module,fileFolderPath,description);
    }

    @Override
    protected ParameterControl initialiseControl() {
        return ParameterControlFactory.getFileFolderSelectionControl(this,FileTypes.EITHER_TYPE);
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        FileFolderPathP newParameter = new FileFolderPathP(name,newModule,getPath(),getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;
    }
}
