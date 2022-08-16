package io.github.mianalysis.mia.object.parameters;

import static io.github.mianalysis.mia.process.analysishandling.AnalysisWriter.prepareRefsXML;

import java.util.LinkedHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.mianalysis.mia.gui.parametercontrols.AddParametersButton;
import io.github.mianalysis.mia.gui.parametercontrols.ParameterControl;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;

/**
 * The value for this parameter is the number of collections that have been created.  The value source is a comma,
 * separated list of parameter names which it serves.
 */

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class ParameterGroup extends Parameter {
    private LinkedHashMap<Integer, Parameters> collections = new LinkedHashMap<>();
    private Parameters templateParameters;
    private ParameterUpdaterAndGetter updaterAndGetter;
    private int maxIdx = 0;

    // CONSTRUCTORS

    public ParameterGroup(String name, Module module, Parameters templateParameters,
            ParameterUpdaterAndGetter updaterAndGetter) {
        super(name, module);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = updaterAndGetter;

    }

    public ParameterGroup(String name, Module module, Parameters templateParameters,
            ParameterUpdaterAndGetter updaterAndGetter, String description) {
        super(name, module, description);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = updaterAndGetter;

    }

    public ParameterGroup(String name, Module module, Parameters templateParameters, int count,
            ParameterUpdaterAndGetter updaterAndGetter) {
        super(name, module);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = updaterAndGetter;

        // Initialising the specified number of collections
        for (int i = 0; i < count; i++)
            addParameters();

    }

    public ParameterGroup(String name, Module module, Parameters templateParameters, int count,
            ParameterUpdaterAndGetter updaterAndGetter, String description) {
        super(name, module, description);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = updaterAndGetter;

        // Initialising the specified number of collections
        for (int i = 0; i < count; i++)
            addParameters();

    }

    public ParameterGroup(String name, Module module, Parameters templateParameters) {
        super(name, module);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = getFullUpdaterAndGetter();

    }

    public ParameterGroup(String name, Module module, Parameters templateParameters, String description) {
        super(name, module, description);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = getFullUpdaterAndGetter();

    }

    public ParameterGroup(String name, Module module, Parameters templateParameters, int count) {
        super(name, module);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = getFullUpdaterAndGetter();

        // Initialising the specified number of collections
        for (int i = 0; i < count; i++)
            addParameters();

    }

    public ParameterGroup(String name, Module module, Parameters templateParameters, int count,
            String description) {
        super(name, module, description);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = getFullUpdaterAndGetter();

        // Initialising the specified number of collections
        for (int i = 0; i < count; i++)
            addParameters();

    }

    // PUBLIC METHODS

    public Parameters addParameters() {
        // Create new copy of template collections
        Parameters newParameters = new Parameters();
        for (Parameter templateParameter : templateParameters.values()) {
            Parameter newParameter = templateParameter.duplicate(templateParameter.getModule());

            // New parameters should inherit the visibility of the addRef button
            newParameter.setVisible(isVisible());

            // Adding parameter to the collection
            newParameters.add(newParameter);

        }

        collections.put(maxIdx++, newParameters);

        return newParameters;

    }

    public void addParameters(Parameters collection) {
        collections.put(maxIdx++, collection);

    }

    public void removeAllParameters() {
        collections = new LinkedHashMap<>();
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new AddParametersButton(this);
    }

    @Override
    public <T> T getValue(Workspace workspace) {
        return (T) collections;
    }

    @Override
    public <T> void setValue(T value) {

    }

    @Override
    public String getRawStringValue() {
        return "";
    }

    @Override
    public void setValueFromString(String string) {

    }

    @Override
    public boolean verify() {
        boolean runnable = true;
        for (Parameters collection : getCollections(true).values()) {
            for (Parameter parameter : collection.values()) {
                boolean currentRunnable = parameter.verify();
                parameter.setValid(currentRunnable);
                if (!currentRunnable && runnable)
                    runnable = false;

            }
        }

        return runnable;

    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        ParameterGroup newParameter = new ParameterGroup(name, newModule, templateParameters.duplicate(),
                updaterAndGetter, getDescription());

        LinkedHashMap<Integer, Parameters> newCollections = new LinkedHashMap<>();
        for (int idx : collections.keySet()) {
            Parameters collection = collections.get(idx);
            newCollections.put(idx, collection.duplicate());
        }
        newParameter.setCollections(newCollections);

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }

    public void setCollections(LinkedHashMap<Integer, Parameters> collections) {
        this.collections = collections;
    }

    public LinkedHashMap<Integer, Parameters> getCollections(boolean getAvailableOnly) {
        // If getting available parameters only, run the ParameterUpdaterAndGetter
        // class, which returns a collection of Parameterss for the visible
        // parameters only. This is necessary for populating the GUI.
        if (getAvailableOnly) {
            LinkedHashMap<Integer, Parameters> returnedCollections = new LinkedHashMap<>();

            // if (collections == null) return returnedCollections;

            for (int idx : collections.keySet()) {
                Parameters collection = collections.get(idx);
                returnedCollections.put(idx, updaterAndGetter.updateAndGet(collection));
            }

            return returnedCollections;

        }

        // Otherwise, just return the full set of collections (i.e. when saving a .mia
        // file)
        return collections;

    }

    public void removeCollection(Parameters collection) {
        for (int idx : collections.keySet()) {
            Parameters currCollection = collections.get(idx);
            if (collection == currCollection) {
                collections.remove(idx);
                return;
            }
        }
    }

    public void removeCollection(int idx) {
        collections.remove(idx);
    }

    public Parameters getTemplateParameters() {
        return templateParameters;
    }

    @Override
    public void appendXMLAttributes(Element element) {
        super.appendXMLAttributes(element);

        Document doc = element.getOwnerDocument();
        Element collectionElement = doc.createElement("COLLECTION");

        for (Parameters collection : collections.values()) {
            Element paramElement = doc.createElement("PARAMS");

            collectionElement.appendChild(prepareRefsXML(doc, paramElement, collection, "PARAM"));

        }

        element.appendChild(collectionElement);

    }

    @Override
    public void setAttributesFromXML(Node node) {
        super.setAttributesFromXML(node);

        // If there are no collections, skip this
        if (node.getChildNodes().item(0) == null)
            return;

        NodeList collectionNodes = node.getChildNodes().item(0).getChildNodes();

        // Resetting the collections
        removeAllParameters();

        // Iterating over each parameter collection
        for (int i = 0; i < collectionNodes.getLength(); i++) {
            Parameters newParameters = addParameters();

            NodeList newParametersNodes = collectionNodes.item(i).getChildNodes();

            // Iterating over each parameter in this parameter collection
            for (int j = 0; j < newParametersNodes.getLength(); j++)
                AnalysisReader.initialiseParameter(newParametersNodes.item(j), module, newParameters);

        }
    }

    public interface ParameterUpdaterAndGetter {
        public Parameters updateAndGet(Parameters parameters);
    }

    ParameterUpdaterAndGetter getFullUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {
            @Override
            public Parameters updateAndGet(Parameters parameters) {
                // Return all parameters
                return parameters;
            }
        };
    }
}