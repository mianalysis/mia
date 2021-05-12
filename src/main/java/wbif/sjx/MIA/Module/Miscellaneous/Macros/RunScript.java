// // Example Groovy script taking an image "ExampleInputImage" from the workspace,
// // cropping it and returning it as "ExampleOutputImage"
// #@ wbif.sjx.MIA.Object.Workspace workspace
// #@output wbif.sjx.MIA.Object.Image ExampleOutputImage

// import wbif.sjx.MIA.MIA
// import wbif.sjx.MIA.Module.ImageProcessing.Stack.CropImage

// im = workspace.getImage("ExampleInputImage")
// ExampleOutputImage=CropImage.cropImage(im,"ExampleOutputImage",100,200,400,300)

package wbif.sjx.MIA.Module.Miscellaneous.Macros;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FilenameUtils;
import org.scijava.Context;
import org.scijava.script.ScriptModule;
import org.scijava.script.ScriptService;
import org.scijava.service.SciJavaService;

import net.imagej.ImageJ;
import io.scif.SCIFIOService;
import net.imagej.ImageJService;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.FilePathP;
import wbif.sjx.MIA.Object.Parameters.GenericButtonP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup.ParameterUpdaterAndGetter;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.TextAreaP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by Stephen on 12/05/2021.
 */
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
        String OBJECTS = "Objects";

        String[] ALL = new String[] { IMAGE, OBJECTS };

    }

    public RunScript(ModuleCollection modules) {
        super("Run script", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.MISCELLANEOUS_MACROS;
    }

    @Override
    public String getDescription() {
        return "";

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
        String scriptMode = parameters.getValue(SCRIPT_MODE);
        String scriptText = parameters.getValue(SCRIPT_TEXT);
        String scriptLanguage = parameters.getValue(SCRIPT_LANGUAGE);
        String scriptFile = parameters.getValue(SCRIPT_FILE);

        try {
            Map<String, Object> scriptParameters = new HashMap<>();
            String extension = "";
            switch (scriptMode) {
                case ScriptModes.SCRIPT_FILE:
                    extension = FilenameUtils.getExtension(scriptFile);
                    scriptText = new String(Files.readAllBytes(Paths.get(scriptFile)), StandardCharsets.UTF_8);
                    break;
                case ScriptModes.SCRIPT_TEXT:
                    extension = getLanguageExtension(scriptLanguage);
                    break;
            }

            if (scriptText.contains("@ wbif.sjx.MIA.Object.Workspace workspace"))
                scriptParameters.put("workspace", workspace);

            // Running script
            MIA.scriptService.setContext(MIA.context);
            ScriptModule scriptModule = MIA.scriptService
                    .run("script." + extension, scriptText, false, scriptParameters).get();
            
            // Adding output images/objects to the workspace
            LinkedHashMap<Integer, ParameterCollection> parameterCollections = parameters.getValue(ADD_OUTPUT);
            for (ParameterCollection parameterCollection : parameterCollections.values()) {
                String outputType = parameterCollection.getValue(OUTPUT_TYPE);
                switch (outputType) {
                    case OutputTypes.IMAGE:
                        String outputImageName = parameterCollection.getValue(OUTPUT_IMAGE);
                        Image outputImage = (Image) scriptModule.getOutput(outputImageName);
                        workspace.addImage(outputImage);
                        if (showOutput)
                            outputImage.showImage();
                        break;
                    case OutputTypes.OBJECTS:
                        String outputObjectsName = parameterCollection.getValue(OUTPUT_OBJECTS);
                        ObjCollection outputObjects = (ObjCollection) scriptModule.getOutput(outputObjectsName);
                        workspace.addObjects(outputObjects);
                        if (showOutput)
                            outputObjects.convertToImageRandomColours().showImage();
                        break;
                }
            }

        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(SCRIPT_SEPARATOR, this));
        parameters.add(new ChoiceP(SCRIPT_MODE, this, ScriptModes.SCRIPT_TEXT, ScriptModes.ALL));
        parameters.add(new ChoiceP(SCRIPT_LANGUAGE, this, ScriptLanguages.IMAGEJ1, ScriptLanguages.ALL));
        parameters.add(new TextAreaP(SCRIPT_TEXT, this, "#@ wbif.sjx.MIA.Object.Workspace Workspace", true));
        parameters.add(new FilePathP(SCRIPT_FILE, this));
        parameters.add(new GenericButtonP(REFRESH_BUTTON, this, "Refresh", GenericButtonP.DefaultModes.REFRESH));

        parameters.add(new SeparatorP(IMAGE_OUTPUT_SEPARATOR, this));
        ParameterCollection parameterCollection = new ParameterCollection();
        parameterCollection.add(new ChoiceP(OUTPUT_TYPE, this, OutputTypes.IMAGE, OutputTypes.ALL));
        parameterCollection.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameterCollection.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new ParameterGroup(ADD_OUTPUT, this, parameterCollection, getUpdaterAndGetter()));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(SCRIPT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SCRIPT_MODE));
        switch ((String) parameters.getValue(SCRIPT_MODE)) {
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
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;

    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    private ParameterUpdaterAndGetter getUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {

            @Override
            public ParameterCollection updateAndGet(ParameterCollection params) {
                ParameterCollection returnedParameters = new ParameterCollection();

                returnedParameters.add(params.getParameter(OUTPUT_TYPE));
                switch ((String) params.getValue(OUTPUT_TYPE)) {
                    case OutputTypes.IMAGE:
                        returnedParameters.add(params.getParameter(OUTPUT_IMAGE));
                        break;
                    case OutputTypes.OBJECTS:
                        returnedParameters.add(params.getParameter(OUTPUT_OBJECTS));
                        break;
                }

                return returnedParameters;

            }
        };
    }
}
