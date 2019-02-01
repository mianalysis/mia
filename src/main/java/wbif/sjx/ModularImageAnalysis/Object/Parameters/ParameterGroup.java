package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.AddParametersButton;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import java.util.LinkedHashSet;

/**
 * The value for this parameter is the number of collections that have been created.  The value source is a comma,
 * separated list of parameter names which it serves.
 */

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class ParameterGroup extends Parameter {
    private LinkedHashSet<ParameterCollection> collections = new LinkedHashSet<>();
    private ParameterCollection templateParameters = new ParameterCollection();


    // CONSTRUCTORS

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters) {
        super(name, module);
        this.templateParameters = templateParameters;

    }

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters, int count) {
        super(name, module);
        this.templateParameters = templateParameters;

        // Initialising the specified number of collections
        for (int i=0;i<count;i++) addParameters();

    }


    // PUBLIC METHODS

    public ParameterCollection addParameters() {
        // Create new copy of template collections
        ParameterCollection newParameters = new ParameterCollection();
        for (Parameter templateParameter:templateParameters) {
            newParameters.add(templateParameter.duplicate());
        }

        // Adding the necessary RemoveParameter Parameter
        newParameters.add(new RemoveParameters("Remove collections",module,this,newParameters));

        collections.add(newParameters);

        return newParameters;

    }

    public void addParameters(ParameterCollection collection) {
        // Adding the necessary RemoveParameter Parameter
        collection.add(new RemoveParameters("Remove collections",module,this,collection));

        collections.add(collection);

    }

    public void removeAllParameters() {
        collections = new LinkedHashSet<>();
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new AddParametersButton(this);
    }

    @Override
    public <T> T getValue() {
        return (T) collections;
    }

    @Override
    public <T> void setValue(T value) {

    }

    @Override
    public String getValueAsString() {
        return "";
    }

    @Override
    public boolean verify() {
        boolean runnable = true;
        for (ParameterCollection collection: collections) {
            for (Parameter parameter:collection) {
                boolean currentRunnable = parameter.verify();
                parameter.setValid(currentRunnable);
                if (!currentRunnable && runnable) runnable = false;
            }
        }
        return runnable;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return null;
    }

    public LinkedHashSet<ParameterCollection> getCollections() {
        return collections;
    }

    public void removeCollection(ParameterCollection collection) {
        collections.remove(collection);
    }
}