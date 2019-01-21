package wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.OutputObjectsP;

import java.util.LinkedHashSet;

public abstract class ObjectNamesType extends ChoiceType {
    public ObjectNamesType(String name, Module module) {
        super(name,module);
    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputObjectsP> objects = GUI.getModules().getAvailableObjects(module);
        return objects.stream().map(OutputObjectsP::getObjectsName).toArray(String[]::new);
    }
}
