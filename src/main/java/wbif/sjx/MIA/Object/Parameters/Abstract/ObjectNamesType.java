package wbif.sjx.MIA.Object.Parameters.Abstract;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.OutputObjectsP;

import java.util.LinkedHashSet;

public abstract class ObjectNamesType extends ChoiceType {
    public ObjectNamesType(String name, Module module) {
        super(name,module);
    }

    public ObjectNamesType(String name, Module module, String description) {
        super(name,module,description);
    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputObjectsP> objects = module.getModules().getAvailableObjects(module);
        return objects.stream().map(OutputObjectsP::getObjectsName).distinct().toArray(String[]::new);
    }
}
