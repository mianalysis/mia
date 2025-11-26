package io.github.mianalysis.mia.module.script;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.ParameterGroup.ParameterUpdaterAndGetter;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.MetadataRef;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.ParentChildRef;
import io.github.mianalysis.mia.object.refs.PartnerRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;

public abstract class GeneralOutputter extends Module {
    public static final String ADD_OUTPUT = "Add output";
    public static final String OUTPUT_TYPE = "Output type";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String ASSOCIATED_IMAGE = "Associated image";
    public static final String METADATA_NAME = "Metadata name";
    public static final String ASSOCIATED_OBJECTS = "Associated objects";
    public static final String MEASUREMENT_NAME = "Measurement name";
    public static final String OBJECT_METADATA_NAME = "Object metadata name";
    public static final String PARENT_NAME = "Parent name";
    public static final String CHILDREN_NAME = "Children name";
    public static final String PARTNERS_NAME_1 = "Partners name 1";
    public static final String PARTNERS_NAME_2 = "Partners name 2";

    public interface OutputTypes {
        String IMAGE = "Image";
        String IMAGE_MEASUREMENT = "Image measurement";
        String METADATA = "Metadata";
        String OBJECTS = "Objects";
        String OBJECT_MEASUREMENT = "Object measurement";
        String OBJECT_METADATA_ITEM = "Object metadata item";
        String PARENT_CHILD = "Parent-child relationship";
        String PARTNERS = "Partner relationship";

        String[] ALL = new String[] { IMAGE, IMAGE_MEASUREMENT, METADATA, OBJECTS, OBJECT_MEASUREMENT,
                OBJECT_METADATA_ITEM, PARENT_CHILD, PARTNERS };

    }

    public GeneralOutputter(String name, Modules modules) {
        super(name, modules);
    }

    @Override
    protected void initialiseParameters() {
        Parameters parameterCollection = new Parameters();

        parameterCollection.add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.IMAGE, OutputTypes.ALL));
        parameterCollection.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameterCollection.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameterCollection.add(new InputImageP(ASSOCIATED_IMAGE, this));
        parameterCollection.add(new StringP(METADATA_NAME, this));
        parameterCollection.add(new InputObjectsInclusiveP(ASSOCIATED_OBJECTS, this));
        parameterCollection.add(new StringP(MEASUREMENT_NAME, this));
        parameterCollection.add(new StringP(OBJECT_METADATA_NAME, this));
        parameterCollection.add(new InputObjectsInclusiveP(PARENT_NAME, this));
        parameterCollection.add(new InputObjectsInclusiveP(CHILDREN_NAME, this));
        parameterCollection.add(new InputObjectsInclusiveP(PARTNERS_NAME_1, this));
        parameterCollection.add(new InputObjectsInclusiveP(PARTNERS_NAME_2, this));

        parameters.add(new ParameterGroup(ADD_OUTPUT, this, parameterCollection, getOutputUpdaterAndGetter()));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(ADD_OUTPUT));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        Workspace workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            if (collection.getValue(OUTPUT_TYPE, workspace).equals(OutputTypes.IMAGE_MEASUREMENT)) {
                String imageName = collection.getValue(ASSOCIATED_IMAGE, workspace);
                String measurementName = collection.getValue(MEASUREMENT_NAME, workspace);

                ImageMeasurementRef ref = imageMeasurementRefs.getOrPut(measurementName);
                ref.setImageName(imageName);
                returnedRefs.add(ref);
            }
        }

        return returnedRefs;

    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            if (collection.getValue(OUTPUT_TYPE, workspace).equals(OutputTypes.OBJECT_MEASUREMENT)) {
                String objectsName = collection.getValue(ASSOCIATED_OBJECTS, workspace);
                String measurementName = collection.getValue(MEASUREMENT_NAME, workspace);

                ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(measurementName);
                ref.setObjectsName(objectsName);
                returnedRefs.add(ref);
            }
        }

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        Workspace workspace = null;
        ObjMetadataRefs returnedRefs = new ObjMetadataRefs();

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            if (collection.getValue(OUTPUT_TYPE, workspace).equals(OutputTypes.OBJECT_METADATA_ITEM)) {
                String objectsName = collection.getValue(ASSOCIATED_OBJECTS, workspace);
                String metadataName = collection.getValue(OBJECT_METADATA_NAME, workspace);

                ObjMetadataRef ref = objectMetadataRefs.getOrPut(metadataName);
                ref.setObjectsName(objectsName);
                returnedRefs.add(ref);
            }
        }

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        Workspace workspace = null;
        MetadataRefs returnedRefs = new MetadataRefs();

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            if (collection.getValue(OUTPUT_TYPE, workspace).equals(OutputTypes.METADATA)) {
                String metadataName = collection.getValue(METADATA_NAME, workspace);

                MetadataRef ref = metadataRefs.getOrPut(metadataName);
                returnedRefs.add(ref);
            }
        }

        return returnedRefs;

    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        Workspace workspace = null;
        ParentChildRefs returnedRefs = new ParentChildRefs();

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            switch ((String) collection.getValue(OUTPUT_TYPE, workspace)) {
                case OutputTypes.PARENT_CHILD:
                    String parentsName = collection.getValue(PARENT_NAME, workspace);
                    String childrenName = collection.getValue(CHILDREN_NAME, workspace);

                    ParentChildRef ref = parentChildRefs.getOrPut(parentsName, childrenName);
                    returnedRefs.add(ref);
                    break;
            }
        }

        return returnedRefs;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        Workspace workspace = null;
        PartnerRefs returnedRefs = new PartnerRefs();

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            switch ((String) collection.getValue(OUTPUT_TYPE, workspace)) {
                case OutputTypes.PARTNERS:
                    String partnersName1 = collection.getValue(PARTNERS_NAME_1, workspace);
                    String partnersName2 = collection.getValue(PARTNERS_NAME_2, workspace);

                    PartnerRef ref = partnerRefs.getOrPut(partnersName1, partnersName2);
                    returnedRefs.add(ref);
                    break;
            }
        }

        return returnedRefs;

    }

    @Override
    public boolean verify() {
        return true;
    }

    protected void addParameterDescriptions() {
        ParameterGroup group = (ParameterGroup) parameters.get(ADD_OUTPUT);
        Parameters collection = group.getTemplateParameters();
        collection.get(OUTPUT_TYPE).setDescription(
                "Specifies the type of variable that has been added to the workspace during the script.  These can either be images, new object collections or measurements associated with existing images or object collections.");

        collection.get(OUTPUT_IMAGE).setDescription(
                "Name of the image that has been added to the workspace during script execution.  This name must match that assigned to the image.");

        collection.get(OUTPUT_OBJECTS).setDescription(
                "Name of the object collection that has been added to the workspace during script execution.  This name must match that assigned to the object collection.");

        collection.get(ASSOCIATED_IMAGE).setDescription(
                "Image from the workspace (i.e. one already present before running this module) to which a measurement has been added during execution of the script.");

        collection.get(METADATA_NAME).setDescription(
                "Name of a metadata item which has been added during execution of the script.");

        collection.get(ASSOCIATED_OBJECTS).setDescription(
                "Object collection from the workspace (i.e. one already present before running this module) to which a measurement has been added during execution of the script.");

        collection.get(MEASUREMENT_NAME).setDescription(
                "Name of the measurement that has been added to either an image or objects of an object collection.  This name must exactly match that assigned in the script.");

        collection.get(PARENT_NAME).setDescription(
                "Name of a parent object already in the workspace which has been related to the objects specified by \""
                        + CHILDREN_NAME + "\" during execution of the script.");

        collection.get(CHILDREN_NAME).setDescription(
                "Name of child objects already in the workspace which have been related to the objects specified by \""
                        + PARENT_NAME + "\" during execution of the script.");

        collection.get(PARTNERS_NAME_1).setDescription(
                "Name of partner object already in the workspace which have been related to the objects specified by \""
                        + PARTNERS_NAME_2 + "\" during execution of the script.");

        collection.get(PARTNERS_NAME_2).setDescription(
                "Name of partner object already in the workspace which have been related to the objects specified by \""
                        + PARTNERS_NAME_1 + "\" during execution of the script.");

        parameters.get(ADD_OUTPUT).setDescription(
                "If images or new object collections have been added to the workspace during script execution they must be added here, so subsequent modules are aware of their presence.  The act of adding an output via this method simply tells subsequent MIA modules the relevant images/object collections were added to the workspace; the image/object collection must be added to the workspace during script execution using the \"workspace.addImage([image])\" or \"workspace.addObjects([object collection])\" commands.");

    }

    private ParameterUpdaterAndGetter getOutputUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {

            @Override
            public Parameters updateAndGet(Parameters params) {
                Parameters returnedParameters = new Parameters();

                returnedParameters.add(params.getParameter(OUTPUT_TYPE));
                switch ((String) params.getValue(OUTPUT_TYPE, null)) {
                    case OutputTypes.IMAGE:
                        returnedParameters.add(params.getParameter(OUTPUT_IMAGE));
                        break;
                    case OutputTypes.IMAGE_MEASUREMENT:
                        returnedParameters.add(params.getParameter(ASSOCIATED_IMAGE));
                        returnedParameters.add(params.getParameter(MEASUREMENT_NAME));
                        break;
                    case OutputTypes.METADATA:
                        returnedParameters.add(params.getParameter(METADATA_NAME));
                        break;
                    case OutputTypes.OBJECTS:
                        returnedParameters.add(params.getParameter(OUTPUT_OBJECTS));
                        break;
                    case OutputTypes.OBJECT_MEASUREMENT:
                        returnedParameters.add(params.getParameter(ASSOCIATED_OBJECTS));
                        returnedParameters.add(params.getParameter(MEASUREMENT_NAME));
                        break;
                    case OutputTypes.OBJECT_METADATA_ITEM:
                        returnedParameters.add(params.getParameter(ASSOCIATED_OBJECTS));
                        returnedParameters.add(params.getParameter(OBJECT_METADATA_NAME));
                        break;
                    case OutputTypes.PARENT_CHILD:
                        returnedParameters.add(params.getParameter(PARENT_NAME));
                        returnedParameters.add(params.getParameter(CHILDREN_NAME));
                        break;
                    case OutputTypes.PARTNERS:
                        returnedParameters.add(params.getParameter(PARTNERS_NAME_1));
                        returnedParameters.add(params.getParameter(PARTNERS_NAME_2));
                        break;
                }

                return returnedParameters;

            }
        };
    }

    protected class InputObjectsInclusiveP extends InputObjectsP {

        public InputObjectsInclusiveP(String name, Module module) {
            super(name, module);
        }

        public InputObjectsInclusiveP(String name, Module module, String choice) {
            super(name, module, choice);
        }

        @Override
        public String[] getChoices() {
            LinkedHashSet<OutputObjectsP> objects = module.getModules().getAvailableObjects(module,
                    OutputObjectsP.class,
                    true);

            // Adding objects output by this module
            ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
            LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

            for (Parameters collection : collections.values())
                if (collection.getValue(OUTPUT_TYPE, null).equals(OutputTypes.OBJECTS))
                    objects.add((OutputObjectsP) collection.getParameter(OUTPUT_OBJECTS));

            return objects.stream().map(OutputObjectsP::getObjectsName).distinct().toArray(String[]::new);
        }

        @Override
        public boolean verify() {
            LinkedHashSet<OutputObjectsP> objects = module.getModules().getAvailableObjects(module,
                    OutputObjectsP.class, true);

            for (OutputObjectsP currChoice : objects)
                if (choice.equals(currChoice.getValue(null)))
                    return true;

            // If not found yet, it could have been generated in this module
            ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
            LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

            for (Parameters collection : collections.values())
                if (collection.getValue(OUTPUT_TYPE, null).equals(OutputTypes.OBJECTS))
                    if (((String) collection.getValue(OUTPUT_OBJECTS, null)).equals(choice))
                        return true;

            return false;

        }

        @Override
        public <T extends Parameter> T duplicate(Module newModule) {
            InputObjectsInclusiveP newParameter = new InputObjectsInclusiveP(name,
                    newModule);

            newParameter.setChoice(getRawStringValue());
            newParameter.setDescription(getDescription());
            newParameter.setNickname(getNickname());
            newParameter.setVisible(isVisible());
            newParameter.setExported(isExported());

            return (T) newParameter;

        }
    }
}
