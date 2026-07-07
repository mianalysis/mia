package io.github.mianalysis.mia.module.script;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apposed.appose.Appose;
import org.apposed.appose.BuildException;
import org.apposed.appose.Environment;
import org.apposed.appose.NDArray;
import org.apposed.appose.Service;
import org.apposed.appose.Service.Task;
import org.apposed.appose.Service.TaskStatus;
import org.apposed.appose.TaskEvent;
import org.apposed.appose.TaskException;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageType;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FileFolderPathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.ParameterGroup.ParameterUpdaterAndGetter;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImgPlus;
import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;

/**
 * Created by Stephen on 07/07/2026.
 */

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class RunPython extends Module {
    public static final String INPUT_SEPARATOR = "Script inputs";

    public static final String ADD_INPUT = "Add input";

    public static final String INPUT_TYPE = "Type";

    public static final String INPUT_BOOLEAN = "Boolean";

    public static final String INPUT_IMAGE = "Input image";

    public static final String INPUT_TEXT = "Text";

    public static final String INPUT_FILEPATH = "Input filepath";

    public static final String INPUT_VARIABLE_NAME = "Variable name";

    public static final String ENVIRONMENT_SEPARATOR = "Environment controls";

    public static final String ENVIRONMENT_TYPE = "Environment type";

    public static final String PIXI_TOML = "Pixi definition";

    public static final String SCRIPT_SEPARATOR = "Script controls";

    public static final String SCRIPT_TEXT = "Script text";

    public static final String OUTPUT_SEPARATOR = "Script outputs";

    public static final String ADD_OUTPUT = "Add output";

    public static final String OUTPUT_IMAGE = "Output image";

    public interface InputTypes {
        String BOOLEAN = "Boolean";
        String FILEPATH = "Filepath";
        String IMAGE = "Image";
        String TEXT = "Text";

        String[] ALL = new String[] { BOOLEAN, FILEPATH, IMAGE, TEXT };

    }

    public interface EnvironmentTypes {
        String PIXI = "Pixi";

        String[] ALL = new String[] { PIXI };

    }

    public RunPython(Modules modules) {
        super("Run Python", modules);
    }

    // This is just so other modules can extend this class
    protected RunPython(String name, Modules modules) {
        super(name, modules);
    }

    @Override
    public Category getCategory() {
        return Categories.SCRIPT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Run Python scripts using Appose library.  The Appose library allow full Python environments and dependencies to be used.  Currently, this only supports image inputs and outputs, but additional functionality will be added as required.";
    }

    protected static String getDefaultPixiToml() {
        return "[workspace]\n"
                + "channels = [\"conda-forge\"]\n"
                + "name = \"my-pixi-env\"\n"
                + "platforms = [\"osx-arm64\", \"win-64\", \"linux-64\", \"osx-64\"]\n"
                + "version = \"0.1.0\"\n"
                + "\n"
                + "[dependencies]\n"
                + "python = \">3.9\"\n"
                + "\n"
                + "[pypi-dependencies]\n"
                + "appose = \">=0.11.0, <0.12\"\n";

    }

    protected static String getDefaultPythonScript() {
        return "import appose \n"
                + "task.update(message=\"Python running\") \n";
    }

    protected static Map<String, Object> getInputs(LinkedHashMap<Integer, Parameters> inputParameters,
            Workspace workspace) {
        Map<String, Object> inputs = new HashMap<>();

        for (Parameters currInputParameters : inputParameters.values()) {
            String inputType = currInputParameters.getValue(INPUT_TYPE, workspace);
            String inputVariableName = currInputParameters.getValue(INPUT_VARIABLE_NAME, workspace);

            switch (inputType) {
                case InputTypes.BOOLEAN:
                    boolean inputBoolean = currInputParameters.getValue(INPUT_BOOLEAN, workspace);
                    inputs.put(inputVariableName, inputBoolean);
                    break;
                case InputTypes.FILEPATH:
                    String inputFilepath = currInputParameters.getValue(INPUT_FILEPATH, workspace);
                    inputs.put(inputVariableName, inputFilepath);
                    break;
                case InputTypes.IMAGE:
                    String inputImageName = currInputParameters.getValue(INPUT_IMAGE, workspace);
                    Image inputImage = workspace.getImage(inputImageName);
                    inputs.put(inputVariableName, NDArrays.asNDArray(inputImage.getImgPlus()));
                    break;
                case InputTypes.TEXT:
                    String inputText = currInputParameters.getValue(INPUT_TEXT, workspace);
                    inputs.put(inputVariableName, inputText);
                    break;
            }
        }

        return inputs;

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        LinkedHashMap<Integer, Parameters> inputParameters = ((ParameterGroup) parameters.get(ADD_INPUT))
                .getCollections(true);
        LinkedHashMap<Integer, Parameters> outputParameters = ((ParameterGroup) parameters.get(ADD_OUTPUT))
                .getCollections(true);
        String environmentType = parameters.getValue(ENVIRONMENT_TYPE, workspace);
        String pixiToml = parameters.getValue(PIXI_TOML, workspace);
        String scriptText = parameters.getValue(SCRIPT_TEXT, workspace);

        // Creating Python environment
        Environment env;
        try {
            switch (environmentType) {
                case EnvironmentTypes.PIXI:
                default:
                    env = Appose.pixi()
                            .content(pixiToml)
                            .logDebug()
                            .build();
                    break;
            }
        } catch (BuildException e) {
            MIA.log.writeError(e);
            return Status.FAIL;
        }

        // Getting inputs
        Map<String, Object> inputs = getInputs(inputParameters, workspace);

        // Running script
        try (Service python = env.python()) {
            Task task = python.task(scriptText, inputs);
            task.listen(new TaskConsumer());
            task.start();
            task.waitFor();

            // Verify that it worked.
            if (task.status != TaskStatus.COMPLETE)
                throw new RuntimeException("Python script failed with error: " + task.error);

            for (Parameters currOutputParameters : outputParameters.values()) {
                String outputImageName = currOutputParameters.getValue(OUTPUT_IMAGE, workspace);
                NDArray outputArray = (NDArray) task.outputs.get(outputImageName);
                ImgPlus outputImg = new ImgPlus(new ShmImg(outputArray));
                Image outputImage = ImageFactory.createImage(outputImageName, outputImg, ImageType.IMAGEPLUS);
                workspace.addImage(outputImage);

                if (showOutput)
                    outputImage.show();

            }

        } catch (TaskException | InterruptedException e) {
            MIA.log.writeError(e);
            return Status.FAIL;
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        Parameters templateInputParameters = new Parameters();
        templateInputParameters.add(new ChoiceP(INPUT_TYPE, this, InputTypes.IMAGE, InputTypes.ALL));
        templateInputParameters.add(new BooleanP(INPUT_BOOLEAN, this, false));
        templateInputParameters.add(new InputImageP(INPUT_IMAGE, this));
        templateInputParameters.add(new StringP(INPUT_TEXT, this));
        templateInputParameters.add(new FileFolderPathP(INPUT_FILEPATH, this));
        templateInputParameters.add(new StringP(INPUT_VARIABLE_NAME, this));
        parameters.add(
                new ParameterGroup(ADD_INPUT, this, templateInputParameters, new InputParameterUpdaterAndGetter()));

        parameters.add(new SeparatorP(ENVIRONMENT_SEPARATOR, this));
        parameters.add(new ChoiceP(ENVIRONMENT_TYPE, this, EnvironmentTypes.PIXI, EnvironmentTypes.ALL));
        parameters.add(new TextAreaP(PIXI_TOML, this, getDefaultPixiToml(), true, 160));

        parameters.add(new SeparatorP(SCRIPT_SEPARATOR, this));
        parameters.add(new TextAreaP(SCRIPT_TEXT, this, getDefaultPythonScript(), true, 240));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        Parameters templateOutputParameters = new Parameters();
        templateOutputParameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ParameterGroup(ADD_OUTPUT, this, templateOutputParameters));

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    class InputParameterUpdaterAndGetter implements ParameterUpdaterAndGetter {
        @Override
        public Parameters updateAndGet(Parameters parameters) {
            Parameters returnedParameters = new Parameters();

            returnedParameters.add(parameters.getParameter(INPUT_TYPE));
            switch ((String) parameters.getValue(INPUT_TYPE, null)) {
                case InputTypes.BOOLEAN:
                    returnedParameters.add(parameters.getParameter(INPUT_BOOLEAN));
                    break;
                case InputTypes.FILEPATH:
                    returnedParameters.add(parameters.getParameter(INPUT_FILEPATH));
                    break;
                case InputTypes.IMAGE:
                    returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
                    break;
                case InputTypes.TEXT:
                    returnedParameters.add(parameters.getParameter(INPUT_TEXT));
                    break;
            }
            returnedParameters.add(parameters.getParameter(INPUT_VARIABLE_NAME));

            return returnedParameters;

        }
    }

    class TaskConsumer implements Consumer<TaskEvent> {
        @Override
        public void accept(TaskEvent event) {
            switch (event.responseType) {
                case LAUNCH:
                    MIA.log.writeDebug("Task started");
                    break;
                case UPDATE:
                    int eventCurrent = (int) event.current;
                    int eventMaximum = (int) event.maximum;
                    if (eventCurrent == 0 && eventMaximum == 0)
                        MIA.log.writeDebug(event.message);
                    else
                        Module.writeProgressStatus(eventCurrent, eventMaximum, "steps", "Run Python");
                    break;
                case COMPLETION:
                    MIA.log.writeDebug("Result: "+event.task.result());
                    break;
                case FAILURE:
                    MIA.log.writeWarning(event.task.error);
                    break;
                case CRASH:
                    MIA.log.writeError(event.task.error);
                    break;
                case CANCELATION:
                    break;
            }
        }

    }
}
