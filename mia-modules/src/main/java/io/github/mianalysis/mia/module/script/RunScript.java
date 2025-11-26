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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptModule;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.ParameterGroup.ParameterUpdaterAndGetter;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
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
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

/**
 * Created by Stephen on 12/05/2021.
 */

/**
 * Run Fiji-compatible scripts directly within a MIA workflow. These can be used
 * to perform advanced actions, such as making measurements that aren't
 * explicitly supported in MIA or running additional plugins. Each script has
 * access to the current workspace, thus providing a route to interact with and
 * specify new images and objects. Scripts also have access to this module,
 * which in turn can be used to access all modules in the current workflow.
 * Scripts are run once per workflow execution.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class RunScript extends GeneralOutputter {

    /**
    * 
    */
    public static final String SCRIPT_SEPARATOR = "Script definition";

    /**
     * Select the source for the script code:<br>
     * <ul>
     * <li>"Script file" Load the macro from the file specified by the "Script file"
     * parameter.</li>
     * <li>"Script text" Script code is written directly into the "Script text"
     * box.</li>
     * </ul>
     */
    public static final String SCRIPT_MODE = "Script mode";

    /**
     * Specify the language of the script written in the "Script text" box. This
     * parameter is not necessary when loading a script from file, since the file
     * extension provides the language information.
     */
    public static final String SCRIPT_LANGUAGE = "Script language";

    /**
     * Script code to be executed. Access to the active MIA workspace and module are
     * provided by the first two lines of code ("#@
     * io.github.mianalysis.mia.object.Workspace workspace" and "#@
     * io.github.mianalysis.mia.module.Module thisModule"), which are included by
     * default. With these lines present in the script, the workspace can be
     * accessed via the "workspace" variable and the current module (i.e. this
     * script module) via the "thisModule" variable.
     */
    public static final String SCRIPT_TEXT = "Script text";

    public static final String PARAMETER_SEPARATOR = "Input parameters";
    public static final String PARAMETER_NAME = "Parameter name";
    public static final String PARAMETER_TYPE = "Parameter type";
    public static final String PARAMETER_CHECKBOX = "Parameter checkbox";
    public static final String PARAMETER_NUMERIC_VALUE = "Parameter numeric value";
    public static final String PARAMETER_TEXT_VALUE = "Parameter text value";
    public static final String ADD_PARAMETER = "Add parameter";

    /**
     * Select a script file to be run by this module. As with the "Script text"
     * parameter, this script can start with the lines "#@
     * io.github.mianalysis.mia.object.Workspace workspace" and "#@
     * io.github.mianalysis.mia.module.Module thisModule", which provide access to
     * the active workspace and this module.
     */
    public static final String SCRIPT_FILE = "Script file";

    /**
    * 
    */
    public static final String IMAGE_OUTPUT_SEPARATOR = "Script outputs";
    

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

    public interface ParameterTypes {
        String BOOLEAN = "Boolean";
        String NUMBER = "Number";
        String TEXT = "Text";

        String[] ALL = new String[] { BOOLEAN, NUMBER, TEXT };

    }

    public RunScript(Modules modules) {
        super("Run script", modules);
    }

    // This is just so other modules can extend this class
    protected RunScript(String name, Modules modules) {
        super(name, modules);
    }

    @Override
    public Category getCategory() {
        return Categories.SCRIPT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.2";
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

    /**
     * Some imports may have moved since the script was written. This method
     * replaces some commonly occurring ones.
     */
    public static String redirectImports(String scriptText, String extension) {
        HashMap<String, String> movedClasses = new HashMap<>();

        movedClasses.put("io.github.sjcross.sjcommon.exceptions.PointOutOfRangeException",
                PointOutOfRangeException.class.getName());
        movedClasses.put("io.github.sjcross.sjcommon.object.volume.PointOutOfRangeException",                          
                PointOutOfRangeException.class.getName());
        movedClasses.put("io.github.sjcross.common.object.volume.PointOutOfRangeException",                          
                PointOutOfRangeException.class.getName());

        movedClasses.put("io.github.sjcross.sjcommon.exceptions.IntegerOverflowException",
                IntegerOverflowException.class.getName());
        movedClasses.put("io.github.sjcross.common.exceptions.IntegerOverflowException",
                IntegerOverflowException.class.getName());

        movedClasses.put("io.github.sjcross.sjcommon.object.volume.SpatCal", SpatCal.class.getName());
        movedClasses.put("io.github.sjcross.common.object.volume.SpatCal", SpatCal.class.getName());

        movedClasses.put("io.github.sjcross.sjcommon.object.volume.VolumeType", VolumeType.class.getName());
        movedClasses.put("io.github.sjcross.common.object.volume.VolumeType", VolumeType.class.getName());
        
        movedClasses.put("io.github.mianalysis.mia.object.Image", Image.class.getName());
        
        movedClasses.put("io.github.sjcross.common.object.Point", Point.class.getName());
        
        movedClasses.put("io.github.mianalysis.mia.object.Measurement", Measurement.class.getName());

        for (String oldLocation : movedClasses.keySet()) {
            String newLocation = movedClasses.get(oldLocation);

            if (extension.equals("py")) {
                int oldIdx = oldLocation.lastIndexOf(".");
                oldLocation = oldLocation.substring(0, oldIdx) + " import " + oldLocation.substring(oldIdx + 1);

                int newIdx = newLocation.lastIndexOf(".");
                newLocation = newLocation.substring(0, newIdx) + " import " + newLocation.substring(newIdx + 1);
            }

            if (scriptText.contains(oldLocation))
                scriptText = scriptText.replace(oldLocation, newLocation);

        }

        return scriptText;

    }

    public static String insertParameterValues(String string, LinkedHashMap<Integer, Parameters> inputParameters) {
        Pattern pattern = Pattern.compile("P\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            String fullName = matcher.group(0);
            String variableName = matcher.group(1);

            for (Parameters inputParameterSet : inputParameters.values()) {
                if (inputParameterSet.getValue(PARAMETER_NAME, null).equals(variableName)) {
                    String value = "";
                    switch ((String) inputParameterSet.getValue(PARAMETER_TYPE, null)) {
                        case ParameterTypes.BOOLEAN:
                            value = inputParameterSet.getValue(PARAMETER_CHECKBOX, null).toString();
                            break;
                        case ParameterTypes.NUMBER:
                            value = inputParameterSet.getValue(PARAMETER_NUMERIC_VALUE, null).toString();
                            break;
                        case ParameterTypes.TEXT:
                            value = inputParameterSet.getValue(PARAMETER_TEXT_VALUE, null).toString();
                            break;
                    }

                    string = string.replace(fullName, value);
                    break;

                }
            }
        }

        return string;

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String scriptMode = parameters.getValue(SCRIPT_MODE, workspace);
        String scriptText = parameters.getValue(SCRIPT_TEXT, workspace);
        String scriptLanguage = parameters.getValue(SCRIPT_LANGUAGE, workspace);
        String scriptFile = parameters.getValue(SCRIPT_FILE, workspace);
        LinkedHashMap<Integer, Parameters> inputParameters = parameters.getValue(ADD_PARAMETER, workspace);

        Map<String, Object> scriptParameters = new HashMap<>();
        String extension = "";
        switch (scriptMode) {
            case ScriptModes.SCRIPT_FILE:
                extension = FilenameUtils.getExtension(scriptFile);
                try {
                    scriptText = new String(Files.readAllBytes(Paths.get(scriptFile)), StandardCharsets.UTF_8);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    return Status.FAIL;
                }
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

        for (Parameters inputParameterSet : inputParameters.values()) {
            String parameterName = inputParameterSet.getValue(PARAMETER_NAME, workspace);
            String parameterType = inputParameterSet.getValue(PARAMETER_TYPE, workspace);

            String classNameString = "";
            switch (parameterType) {
                case ParameterTypes.BOOLEAN:
                    classNameString = "#@ Boolean " + parameterName;
                    scriptParameters.put(parameterName, inputParameterSet.getValue(PARAMETER_CHECKBOX, workspace));
                    break;
                case ParameterTypes.NUMBER:
                    classNameString = "#@ Double " + parameterName;
                    scriptParameters.put(parameterName, inputParameterSet.getValue(PARAMETER_NUMERIC_VALUE, workspace));
                    break;
                case ParameterTypes.TEXT:
                    classNameString = "#@ String " + parameterName;
                    scriptParameters.put(parameterName, inputParameterSet.getValue(PARAMETER_TEXT_VALUE, workspace));
                    break;
            }

            if (!scriptText.contains(classNameString))
                scriptText = classNameString + "\n" + scriptText;

        }

        // Resolving moved files
        scriptText = redirectImports(scriptText, extension);

        // Running script
        try {
            ScriptModule scriptModule = MIA.getScriptService().run("." + extension, scriptText, false, scriptParameters)
                    .get();
        } catch (Exception e) {
            MIA.log.writeError(e);
        }

        // Displaying output images/objects
        if (showOutput) {
            LinkedHashMap<Integer, Parameters> parameterCollections = parameters.getValue(ADD_OUTPUT, workspace);
            for (Parameters parameterCollection : parameterCollections.values()) {
                String outputType = parameterCollection.getValue(OUTPUT_TYPE, workspace);
                switch (outputType) {
                    case OutputTypes.IMAGE:
                        workspace.getImage(parameterCollection.getValue(OUTPUT_IMAGE, workspace)).show();
                        break;
                    case OutputTypes.IMAGE_MEASUREMENT:
                        workspace.getImage(parameterCollection.getValue(ASSOCIATED_IMAGE, workspace))
                                .showMeasurements(this);
                        break;
                    case OutputTypes.METADATA:
                        workspace.showMetadata(this);
                        break;
                    case OutputTypes.OBJECTS:
                        if (workspace.getObjects(parameterCollection.getValue(OUTPUT_OBJECTS, workspace)) != null)
                            workspace.getObjects(parameterCollection.getValue(OUTPUT_OBJECTS, workspace))
                                    .convertToImageIDColours().show(false);
                        break;
                    case OutputTypes.OBJECT_MEASUREMENT:
                        if (workspace.getObjects(parameterCollection.getValue(ASSOCIATED_OBJECTS, workspace)) != null)
                            workspace.getObjects(parameterCollection.getValue(ASSOCIATED_OBJECTS, workspace))
                                    .showMeasurements(this, modules);
                        break;
                    case OutputTypes.OBJECT_METADATA_ITEM:
                        if (workspace.getObjects(parameterCollection.getValue(ASSOCIATED_OBJECTS, workspace)) != null)
                            workspace.getObjects(parameterCollection.getValue(ASSOCIATED_OBJECTS, workspace))
                                    .showMetadata(this, modules);
                        break;
                }
            }
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(SCRIPT_SEPARATOR, this));
        parameters.add(new ChoiceP(SCRIPT_MODE, this, ScriptModes.SCRIPT_TEXT, ScriptModes.ALL));
        parameters.add(new ChoiceP(SCRIPT_LANGUAGE, this, ScriptLanguages.IMAGEJ1, ScriptLanguages.ALL));
        parameters.add(new TextAreaP(SCRIPT_TEXT, this,
                "// The following two parameters will provide references to the workspace and current module.\n#@ io.github.mianalysis.mia.object.Workspace workspace\n#@ io.github.mianalysis.mia.module.Module thisModule\n\nimport io.github.mianalysis.mia.MIA",
                true));
        parameters.add(new FilePathP(SCRIPT_FILE, this));

        Parameters variableCollection = new Parameters();
        variableCollection.add(new ChoiceP(PARAMETER_TYPE, this, ParameterTypes.TEXT, ParameterTypes.ALL));
        variableCollection.add(new StringP(PARAMETER_NAME, this));
        variableCollection.add(new BooleanP(PARAMETER_CHECKBOX, this, true));
        variableCollection.add(new DoubleP(PARAMETER_NUMERIC_VALUE, this, 0d));
        variableCollection.add(new StringP(PARAMETER_TEXT_VALUE, this));

        parameters.add(new SeparatorP(PARAMETER_SEPARATOR, this));
        parameters.add(new ParameterGroup(ADD_PARAMETER, this, variableCollection, getParameterUpdaterAndGetter()));

        parameters.add(new SeparatorP(IMAGE_OUTPUT_SEPARATOR, this));

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
                break;
        }

        returnedParameters.add(parameters.getParameter(PARAMETER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_PARAMETER));

        returnedParameters.add(parameters.getParameter(IMAGE_OUTPUT_SEPARATOR));
        returnedParameters.addAll(super.updateAndGetParameters());

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        Workspace workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        ParameterGroup inputParameterGroup = parameters.getParameter(ADD_PARAMETER);
        LinkedHashMap<Integer, Parameters> inputParameters = inputParameterGroup.getCollections(true);

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            if (collection.getValue(OUTPUT_TYPE, workspace).equals(OutputTypes.IMAGE_MEASUREMENT)) {
                String imageName = collection.getValue(ASSOCIATED_IMAGE, workspace);
                String measurementName = collection.getValue(MEASUREMENT_NAME, workspace);

                imageName = insertParameterValues(imageName, inputParameters);
                measurementName = insertParameterValues(measurementName, inputParameters);

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

        ParameterGroup inputParameterGroup = parameters.getParameter(ADD_PARAMETER);
        LinkedHashMap<Integer, Parameters> inputParameters = inputParameterGroup.getCollections(true);

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            if (collection.getValue(OUTPUT_TYPE, workspace).equals(OutputTypes.OBJECT_MEASUREMENT)) {
                String objectsName = collection.getValue(ASSOCIATED_OBJECTS, workspace);
                String measurementName = collection.getValue(MEASUREMENT_NAME, workspace);

                objectsName = insertParameterValues(objectsName, inputParameters);
                measurementName = insertParameterValues(measurementName, inputParameters);

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

        ParameterGroup inputParameterGroup = parameters.getParameter(ADD_PARAMETER);
        LinkedHashMap<Integer, Parameters> inputParameters = inputParameterGroup.getCollections(true);

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            if (collection.getValue(OUTPUT_TYPE, workspace).equals(OutputTypes.OBJECT_METADATA_ITEM)) {
                String objectsName = collection.getValue(ASSOCIATED_OBJECTS, workspace);
                String metadataName = collection.getValue(OBJECT_METADATA_NAME, workspace);

                objectsName = insertParameterValues(objectsName, inputParameters);
                metadataName = insertParameterValues(metadataName, inputParameters);

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

        ParameterGroup inputParameterGroup = parameters.getParameter(ADD_PARAMETER);
        LinkedHashMap<Integer, Parameters> inputParameters = inputParameterGroup.getCollections(true);

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            if (collection.getValue(OUTPUT_TYPE, workspace).equals(OutputTypes.METADATA)) {
                String metadataName = collection.getValue(METADATA_NAME, workspace);

                metadataName = insertParameterValues(metadataName, inputParameters);

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

        ParameterGroup inputParameterGroup = parameters.getParameter(ADD_PARAMETER);
        LinkedHashMap<Integer, Parameters> inputParameters = inputParameterGroup.getCollections(true);

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            switch ((String) collection.getValue(OUTPUT_TYPE, workspace)) {
                case OutputTypes.PARENT_CHILD:
                    String parentsName = collection.getValue(PARENT_NAME, workspace);
                    String childrenName = collection.getValue(CHILDREN_NAME, workspace);

                    parentsName = insertParameterValues(parentsName, inputParameters);
                    childrenName = insertParameterValues(childrenName, inputParameters);

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

        ParameterGroup inputParameterGroup = parameters.getParameter(ADD_PARAMETER);
        LinkedHashMap<Integer, Parameters> inputParameters = inputParameterGroup.getCollections(true);

        ParameterGroup group = parameters.getParameter(ADD_OUTPUT);
        LinkedHashMap<Integer, Parameters> collections = group.getCollections(true);

        for (Parameters collection : collections.values()) {
            switch ((String) collection.getValue(OUTPUT_TYPE, workspace)) {
                case OutputTypes.PARTNERS:
                    String partnersName1 = collection.getValue(PARTNERS_NAME_1, workspace);
                    String partnersName2 = collection.getValue(PARTNERS_NAME_2, workspace);

                    partnersName1 = insertParameterValues(partnersName1, inputParameters);
                    partnersName2 = insertParameterValues(partnersName2, inputParameters);

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

        super.addParameterDescriptions();

    }

    private ParameterUpdaterAndGetter getParameterUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {

            @Override
            public Parameters updateAndGet(Parameters params) {
                Parameters returnedParameters = new Parameters();

                returnedParameters.add(params.getParameter(PARAMETER_NAME));
                returnedParameters.add(params.getParameter(PARAMETER_TYPE));
                switch ((String) params.getValue(PARAMETER_TYPE, null)) {
                    case ParameterTypes.BOOLEAN:
                        returnedParameters.add(params.getParameter(PARAMETER_CHECKBOX));
                        break;
                    case ParameterTypes.NUMBER:
                        returnedParameters.add(params.getParameter(PARAMETER_NUMERIC_VALUE));
                        break;
                    case ParameterTypes.TEXT:
                        returnedParameters.add(params.getParameter(PARAMETER_TEXT_VALUE));
                        break;
                }

                return returnedParameters;

            }
        };
    }
}
