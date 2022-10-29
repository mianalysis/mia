// // Example Groovy script taking an image "ExampleInputImage" from the workspace,
// // cropping it and returning it as "ExampleOutputImage"
// #@ io.github.mianalysis.MIA.Object.Workspace workspace

// import io.github.mianalysis.MIA.MIA
// import io.github.mianalysis.MIA.Module.ImageProcessing.Stack.CropImage

// im = workspace.getImage("ExampleInputImage")
// ExampleOutputImage=CropImage.cropImage(im,"ExampleOutputImage",100,200,400,300)

package io.github.mianalysis.mia.module.script;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FilenameUtils;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptModule;
import org.scijava.script.ScriptService;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.GenericButtonP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.ParameterGroup.ParameterUpdaterAndGetter;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.MetadataRef;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ParentChildRef;
import io.github.mianalysis.mia.object.refs.PartnerRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Stephen on 12/05/2021.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class RunScript extends Module {
    public static final String SCRIPT_SEPARATOR = "Script definition";
    public static final String SCRIPT_MODE = "Script mode";
    public static final String SCRIPT_LANGUAGE = "Script language";
    public static final String SCRIPT_TEXT = "Script text";
    public static final String SCRIPT_FILE = "Script file";
    public static final String REFRESH_BUTTON = "Refresh script";

    public static final String IMAGE_OUTPUT_SEPARATOR = "Script outputs";
    public static final String ADD_OUTPUT = "Add output";
    public static final String OUTPUT_TYPE = "Output type";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String ASSOCIATED_IMAGE = "Associated image";
    public static final String METADATA_NAME = "Metadata name";
    public static final String ASSOCIATED_OBJECTS = "Associated objects";
    public static final String MEASUREMENT_NAME = "Measurement name";
    public static final String PARENT_NAME = "Parent name";
    public static final String CHILDREN_NAME = "Children name";
    public static final String PARTNERS_NAME_1 = "Partners name 1";
    public static final String PARTNERS_NAME_2 = "Partners name 2";

    public interface ScriptModes {
        String SCRIPT_FILE = "Script file";
        String SCRIPT_TEXT = "Script text";

        String[] ALL = new String[] { SCRIPT_FILE, SCRIPT_TEXT };

    }

    public interface ScriptLanguages {
        String GROOVY = "Groovy";
        String JAVASCRIPT = "Javascript";
        String JYTHON = "Jython";
        String JRUBY = "JRuby";
        String CLOJURE = "Clojure";
        String BEANSHELL = "Beanshell";
        String IMAGEJ1 = "ImageJ 1.x macro";

        String[] ALL = new String[] { BEANSHELL, CLOJURE, GROOVY, IMAGEJ1, JAVASCRIPT, JRUBY, JYTHON };

    }

    public interface OutputTypes {
        String IMAGE = "Image";
        String IMAGE_MEASUREMENT = "Image measurement";
        String METADATA = "Metadata";
        String OBJECTS = "Objects";
        String OBJECT_MEASUREMENT = "Object measurement";
        String PARENT_CHILD = "Parent-child relationship";
        String PARTNERS = "Partner relationship";

        String[] ALL = new String[] { IMAGE, IMAGE_MEASUREMENT, METADATA, OBJECTS, OBJECT_MEASUREMENT, PARENT_CHILD,
                PARTNERS };

    }

    public RunScript(Modules modules) {
        super("Run script", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.SCRIPT;
    }

    @Override
    public String getDescription() {
        return "Run Fiji-compatible scripts directly within a MIA workflow.  These can be used to perform advanced actions, such as making measurements that aren't explicitly supported in MIA or running additional plugins.  Each script has access to the current workspace, thus providing a route to interact with and specify new images and objects.  Scripts also have access to this module, which in turn can be used to access all modules in the current workflow.  Scripts are run once per workflow execution.";

    }

    public static String getLanguageExtension(String language) {
        switch (language) {
            case ScriptLanguages.GROOVY:
                return "groovy";
            case ScriptLanguages.JAVASCRIPT:
                return "js";
            case ScriptLanguages.JYTHON:
                return "py";
            case ScriptLanguages.JRUBY:
                return "rb";
            case ScriptLanguages.CLOJURE:
                return "clj";
            case ScriptLanguages.BEANSHELL:
                return "bsh";
            case ScriptLanguages.IMAGEJ1:
                return "ijm";
        }

        return null;

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String scriptMode = parameters.getValue(SCRIPT_MODE, workspace);
        String scriptText = parameters.getValue(SCRIPT_TEXT, workspace);
        String scriptLanguage = parameters.getValue(SCRIPT_LANGUAGE, workspace);
        String scriptFile = parameters.getValue(SCRIPT_FILE, workspace);

        try {
            Map<String, Object> scriptParameters = new HashMap<>();
            String extension = "";
            switch (scriptMode) {
                case ScriptModes.SCRIPT_FILE:
                    extension = FilenameUtils.getExtension(scriptFile);
                    scriptText = new String(Files.readAllBytes(Paths.get(scriptFile)), StandardCharsets.UTF_8);
                    scriptText = GlobalVariables.convertString(scriptText, modules);
                    scriptText = TextType.applyCalculation(scriptText);
                    break;
                case ScriptModes.SCRIPT_TEXT:
                    extension = getLanguageExtension(scriptLanguage);
                    break;
            }

            if (scriptText.contains("@ io.github.mianalysis.mia.object.Workspace workspace"))
                scriptParameters.put("workspace", workspace);

            if (scriptText.contains("@ io.github.mianalysis.mia.module.Module thisModule"))
                scriptParameters.put("thisModule", this);

            // Running script
            ScriptModule scriptModule = MIA.getScriptService().run("." + extension, scriptText, false, scriptParameters).get();

            // Displaying output images/objects
            if (showOutput) {
                LinkedHashMap<Integer, Parameters> parameterCollections = parameters.getValue(ADD_OUTPUT, workspace);
                for (Parameters parameterCollection : parameterCollections.values()) {
                    String outputType = parameterCollection.getValue(OUTPUT_TYPE, workspace);
                    switch (outputType) {
                        case OutputTypes.IMAGE:
                            workspace.getImage(parameterCollection.getValue(OUTPUT_IMAGE, workspace)).showImage();
                            break;
                        case OutputTypes.IMAGE_MEASUREMENT:
                            workspace.getImage(parameterCollection.getValue(ASSOCIATED_IMAGE, workspace))
                                    .showMeasurements(this);
                            break;
                        case OutputTypes.METADATA:
                            workspace.showMetadata(this);
                            break;
                        case OutputTypes.OBJECTS:
                            workspace.getObjectSet(parameterCollection.getValue(OUTPUT_OBJECTS, workspace))
                                    .convertToImageRandomColours().showImage();
                            break;
                        case OutputTypes.OBJECT_MEASUREMENT:
                            workspace.getObjectSet(parameterCollection.getValue(ASSOCIATED_OBJECTS, workspace))
                                    .showMeasurements(this, modules);
                            break;
                    }
                }
            }

        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        } catch (Exception e) {
            MIA.log.writeError(e);
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(SCRIPT_SEPARATOR, this));
        parameters.add(new ChoiceP(SCRIPT_MODE, this, ScriptModes.SCRIPT_TEXT, ScriptModes.ALL));
        parameters.add(new ChoiceP(SCRIPT_LANGUAGE, this, ScriptLanguages.IMAGEJ1, ScriptLanguages.ALL));
        parameters.add(new TextAreaP(SCRIPT_TEXT, this,
                "// The following two parameters will provide references to the workspace and current module.\n#@ io.github.mianalysis.mia.object.Workspace workspace\n#@ io.github.mianalysis.mia.module.Module thisModule\n\nimport io.github.mianalysis.mia.MIA",
                true));
        parameters.add(new FilePathP(SCRIPT_FILE, this));
        parameters.add(new GenericButtonP(REFRESH_BUTTON, this, "Refresh", GenericButtonP.DefaultModes.REFRESH));

        parameters.add(new SeparatorP(IMAGE_OUTPUT_SEPARATOR, this));
        Parameters parameterCollection = new Parameters();
        parameterCollection.add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.IMAGE, OutputTypes.ALL));
        parameterCollection.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameterCollection.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameterCollection.add(new InputImageP(ASSOCIATED_IMAGE, this));
        parameterCollection.add(new StringP(METADATA_NAME, this));
        parameterCollection.add(new InputObjectsInclusiveP(ASSOCIATED_OBJECTS, this));
        parameterCollection.add(new StringP(MEASUREMENT_NAME, this));
        parameterCollection.add(new InputObjectsInclusiveP(PARENT_NAME, this));
        parameterCollection.add(new InputObjectsInclusiveP(CHILDREN_NAME, this));
        parameterCollection.add(new InputObjectsInclusiveP(PARTNERS_NAME_1, this));
        parameterCollection.add(new InputObjectsInclusiveP(PARTNERS_NAME_2, this));
        parameters.add(new ParameterGroup(ADD_OUTPUT, this, parameterCollection, getUpdaterAndGetter()));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(SCRIPT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SCRIPT_MODE));
        switch ((String) parameters.getValue(SCRIPT_MODE, workspace)) {
            case ScriptModes.SCRIPT_FILE:
                returnedParameters.add(parameters.getParameter(SCRIPT_FILE));
                break;
            case ScriptModes.SCRIPT_TEXT:
                returnedParameters.add(parameters.getParameter(SCRIPT_LANGUAGE));
                returnedParameters.add(parameters.getParameter(SCRIPT_TEXT));
                returnedParameters.add(parameters.getParameter(REFRESH_BUTTON));
                break;
        }

        returnedParameters.add(parameters.getParameter(IMAGE_OUTPUT_SEPARATOR));
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
        parameters.get(SCRIPT_MODE)
                .setDescription("Select the source for the script code:<br><ul>" + "<li>\"" + ScriptModes.SCRIPT_FILE
                        + "\" Load the macro from the file specified by the \"" + SCRIPT_FILE + "\" parameter.</li>"

                        + "<li>\"" + ScriptModes.SCRIPT_TEXT + "\" Script code is written directly into the \""
                        + SCRIPT_TEXT + "\" box.</li></ul>");

        parameters.get(SCRIPT_LANGUAGE).setDescription("Specify the language of the script written in the \""
                + SCRIPT_TEXT
                + "\" box.  This parameter is not necessary when loading a script from file, since the file extension provides the language information.");

        parameters.get(SCRIPT_TEXT).setDescription(
                "Script code to be executed.  Access to the active MIA workspace and module are provided by the first two lines of code (\"#@ io.github.mianalysis.mia.object.Workspace workspace\" and \"#@ io.github.mianalysis.mia.module.Module thisModule\"), which are included by default.  With these lines present in the script, the workspace can be accessed via the \"workspace\" variable and the current module (i.e. this script module) via the \"thisModule\" variable.");

        parameters.get(SCRIPT_FILE).setDescription("Select a script file to be run by this module.  As with the \""
                + SCRIPT_TEXT
                + "\" parameter, this script can start with the lines \"#@ io.github.mianalysis.mia.object.Workspace workspace\" and \"#@ io.github.mianalysis.mia.module.Module thisModule\", which provide access to the active workspace and this module.");

        parameters.get(REFRESH_BUTTON).setDescription(
                "This button refreshes the script code as stored within MIA.  Clicking this will create an \"undo\" checkpoint and validate any global variables that have been used.");

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

    private ParameterUpdaterAndGetter getUpdaterAndGetter() {
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

    class InputObjectsInclusiveP extends InputObjectsP {

        public InputObjectsInclusiveP(String name, Module module) {
            super(name, module);
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

            newParameter.setChoice(getChoice());
            newParameter.setDescription(getDescription());
            newParameter.setNickname(getNickname());
            newParameter.setVisible(isVisible());
            newParameter.setExported(isExported());

            return (T) newParameter;

        }
    }

}
