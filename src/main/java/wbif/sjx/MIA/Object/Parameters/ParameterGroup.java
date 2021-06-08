package wbif.sjx.MIA.Object.Parameters;

import static wbif.sjx.MIA.Process.AnalysisHandling.AnalysisWriter.prepareRefsXML;

import java.util.LinkedHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.GUI.ParameterControls.AddParametersButton;
import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

/**
 * The value for this parameter is the number of collections that have been created.  The value source is a comma,
 * separated list of parameter names which it serves.
 */

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class ParameterGroup extends Parameter {
    private LinkedHashMap<Integer, ParameterCollection> collections = new LinkedHashMap<>();
    private ParameterCollection templateParameters;
    private ParameterUpdaterAndGetter updaterAndGetter;
    private int maxIdx = 0;

    // CONSTRUCTORS

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters,
            ParameterUpdaterAndGetter updaterAndGetter) {
        super(name, module);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = updaterAndGetter;

    }

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters,
            ParameterUpdaterAndGetter updaterAndGetter, String description) {
        super(name, module, description);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = updaterAndGetter;

    }

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters, int count,
            ParameterUpdaterAndGetter updaterAndGetter) {
        super(name, module);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = updaterAndGetter;

        // Initialising the specified number of collections
        for (int i = 0; i < count; i++)
            addParameters();

    }

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters, int count,
            ParameterUpdaterAndGetter updaterAndGetter, String description) {
        super(name, module, description);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = updaterAndGetter;

        // Initialising the specified number of collections
        for (int i = 0; i < count; i++)
            addParameters();

    }

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters) {
        super(name, module);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = getFullUpdaterAndGetter();

    }

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters, String description) {
        super(name, module, description);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = getFullUpdaterAndGetter();

    }

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters, int count) {
        super(name, module);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = getFullUpdaterAndGetter();

        // Initialising the specified number of collections
        for (int i = 0; i < count; i++)
            addParameters();

    }

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters, int count,
            String description) {
        super(name, module, description);
        this.templateParameters = templateParameters;
        this.updaterAndGetter = getFullUpdaterAndGetter();

        // Initialising the specified number of collections
        for (int i = 0; i < count; i++)
            addParameters();

    }

    // PUBLIC METHODS

    public ParameterCollection addParameters() {
        // Create new copy of template collections
        ParameterCollection newParameters = new ParameterCollection();
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

    public void addParameters(ParameterCollection collection) {
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
    public <T> T getValue() {
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
        for (ParameterCollection collection : getCollections(true).values()) {
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

        LinkedHashMap<Integer, ParameterCollection> newCollections = new LinkedHashMap<>();
        for (int idx : collections.keySet()) {
            ParameterCollection collection = collections.get(idx);
            newCollections.put(idx, collection.duplicate());
        }
        newParameter.setCollections(newCollections);

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }

    public void setCollections(LinkedHashMap<Integer, ParameterCollection> collections) {
        this.collections = collections;
    }

    public LinkedHashMap<Integer, ParameterCollection> getCollections(boolean getAvailableOnly) {
        // If getting available parameters only, run the ParameterUpdaterAndGetter
        // class, which returns a collection of ParameterCollections for the visible
        // parameters only. This is necessary for populating the GUI.
        if (getAvailableOnly) {
            LinkedHashMap<Integer, ParameterCollection> returnedCollections = new LinkedHashMap<>();

            // if (collections == null) return returnedCollections;

            for (int idx : collections.keySet()) {
                ParameterCollection collection = collections.get(idx);
                returnedCollections.put(idx, updaterAndGetter.updateAndGet(collection));
            }

            return returnedCollections;

        }

        // Otherwise, just return the full set of collections (i.e. when saving a .mia
        // file)
        return collections;

    }

    public void removeCollection(ParameterCollection collection) {
        for (int idx : collections.keySet()) {
            ParameterCollection currCollection = collections.get(idx);
            if (collection == currCollection) {
                collections.remove(idx);
                return;
            }
        }
    }

    public void removeCollection(int idx) {
        collections.remove(idx);
    }

    public ParameterCollection getTemplateParameters() {
        return templateParameters;
    }

    @Override
    public void appendXMLAttributes(Element element) {
        super.appendXMLAttributes(element);

        Document doc = element.getOwnerDocument();
        Element collectionElement = doc.createElement("COLLECTION");

        for (ParameterCollection collection : collections.values()) {
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
            ParameterCollection newParameters = addParameters();

            NodeList newParametersNodes = collectionNodes.item(i).getChildNodes();

            // Iterating over each parameter in this parameter collection
            for (int j = 0; j < newParametersNodes.getLength(); j++) {
                Node newParametersNode = newParametersNodes.item(j);

                NamedNodeMap attributes = newParametersNode.getAttributes();
                String parameterName = attributes.getNamedItem("NAME").getNodeValue();
                String parameterValue = attributes.getNamedItem("VALUE").getNodeValue();
                Parameter parameter = newParameters.getParameter(parameterName);

                if (parameter == null) {
                    MIA.log.writeWarning("Parameter \"" + parameterName + "\" (value = \"" + parameterValue
                            + "\") not found for module \"" + module.getName() + "\", skipping.");
                    continue;
                }

                parameter.setAttributesFromXML(newParametersNode);

            }
        }
    }

    public interface ParameterUpdaterAndGetter {
        public ParameterCollection updateAndGet(ParameterCollection parameters);
    }

    ParameterUpdaterAndGetter getFullUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {
            @Override
            public ParameterCollection updateAndGet(ParameterCollection parameters) {
                // Return all parameters
                return parameters;
            }
        };
    }
}