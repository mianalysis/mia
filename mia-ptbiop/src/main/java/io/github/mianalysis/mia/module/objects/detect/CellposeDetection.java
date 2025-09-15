package io.github.mianalysis.mia.module.objects.detect;

import java.io.File;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.wrappers.cellpose.ij2commands.CellposeWrapper;
import ij.Prefs;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.TemporalUnit;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CellposeDetection extends Module {

    public static final String INPUT_SEPARATOR = "Image input/object output";

    public static final String INPUT_IMAGE = "Input image";

    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String CELLPOSE_SEPARATOR = "Cellpose settings";

    public static final String CELLPOSE_PATH = "Cellpose environment path";

    public static final String ENVIRONMENT_TYPE = "Environment type";

    public static final String USE_GPU = "Use GPU";

    public static final String MODEL_SEPARATOR = "Model settings";

    public static final String MODEL_MODE = "Model mode";

    public static final String CUSTOM_MODEL_PATH = "Custom model path";

    public static final String MODEL = "Model";

    public static final String MODEL_MESSAGE = "Model message";

    public static final String DIMENSION_MODE = "Dimension mode";

    public static final String SEGMENTATION_SEPARATOR = "Segmentation settings";

    public static final String DIAMETER = "Diameter";

    public static final String CELL_PROBABILITY_THRESHOLD = "Cell probability threshold";

    public static final String FLOW_THRESHOLD = "Flow threshold";

    public static final String ANISOTROPY = "Anisotropy";

    public static final String STITCH_THRESHOLD = "Stitch threshold";

    public static final String ADDITIONAL_FLAGS = "Additional flags";

    public static final String FLAGS_MESSAGE = "Flags message";

    public interface EnvironmentTypes {
        String CONDA = "Conda";
        String VENV = "Venv";

        String[] ALL = new String[] { CONDA, VENV };

    }

    public interface ModelModes {
        String CUSTOM = "Custom";
        String INCLUDED = "Included";

        String[] ALL = new String[] { CUSTOM, INCLUDED };

    }

    public interface DimensionModes {
        String TWOD = "2D";
        String THREED = "3D";

        String[] ALL = new String[] { TWOD, THREED };

    }

    public CellposeDetection(Modules modules) {
        super("Cellpose detection", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        String cellposePath = parameters.getValue(CELLPOSE_PATH, workspace);
        String environmentType = parameters.getValue(ENVIRONMENT_TYPE, workspace).toString().toLowerCase();
        boolean useGPU = parameters.getValue(USE_GPU, workspace);

        String modelMode = parameters.getValue(MODEL_MODE, workspace);
        String customModelPath = parameters.getValue(CUSTOM_MODEL_PATH, workspace);
        String model = parameters.getValue(MODEL, workspace);
        String dimensionMode = parameters.getValue(DIMENSION_MODE, workspace);

        int diameter = parameters.getValue(DIAMETER, workspace);
        double cellProbThresh = parameters.getValue(CELL_PROBABILITY_THRESHOLD, workspace);
        double flowThreshold = parameters.getValue(FLOW_THRESHOLD, workspace);
        double anisotropy = parameters.getValue(ANISOTROPY, workspace);
        double stitchThreshold = parameters.getValue(STITCH_THRESHOLD, workspace);
        String additionalFlags = parameters.getValue(ADDITIONAL_FLAGS, workspace);

        Image inputImage = workspace.getImages().get(inputImageName);

        CellposeWrapper cellpose = new CellposeWrapper();

        cellpose.setEnvPath(new File(cellposePath));
        cellpose.setEnvType(environmentType);
        switch (modelMode) {
            case ModelModes.CUSTOM:
                cellpose.setModel("");
                cellpose.setModelPath(new File(customModelPath));
                break;
            case ModelModes.INCLUDED:
                cellpose.setModel(model);
                break;
        }

        cellpose.setUseGPU(useGPU);
        cellpose.setCellProbabilityThreshold(cellProbThresh);

        switch (dimensionMode) {
            case DimensionModes.THREED:
                cellpose.setDo3D(true);
                break;
            case DimensionModes.TWOD:
                cellpose.setDo3D(false);
                break;
        }

        cellpose.setDiameter((float) diameter);
        cellpose.setFlowThreshold(flowThreshold);
        cellpose.setAnisotropy(anisotropy);
        cellpose.setStitchThreshold(stitchThreshold);
        cellpose.setAdditionalFlags(additionalFlags);

        cellpose.compileAdditionalFlags();

        SpatCal spatCal = SpatCal.getFromImage(inputImage.getImagePlus());
        int nFrames = inputImage.getImagePlus().getNFrames();
        int nSlices = inputImage.getImagePlus().getNSlices();
        double frameInterval = inputImage.getImagePlus().getCalibration().frameInterval;
        Objs outputObjects = new Objs(outputObjectsName, spatCal, nFrames, frameInterval, TemporalUnit.getOMEUnit());

        if (dimensionMode.equals(DimensionModes.TWOD)) {
            int count = 0;
            int total = nFrames * nSlices;
            for (int z = 0; z < nSlices; z++) {
                for (int t = 0; t < nFrames; t++) {
                    Image currImage = ExtractSubstack.extractSubstack(inputImage, "Timepoint", "1-end",
                            String.valueOf(z + 1) + "-" + String.valueOf(z + 1),
                            String.valueOf(t + 1) + "-" + String.valueOf(t + 1));

                    cellpose.setImagePlus(currImage.getImagePlus());
                    cellpose.run();

                    Image cellsImage = ImageFactory.createImage("Objects", cellpose.getLabels());
                    Objs currOutputObjects = cellsImage.convertImageToObjects(VolumeType.QUADTREE,
                            outputObjectsName);

                    for (Obj currOutputObject : currOutputObjects.values()) {
                        Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);
                        outputObject.setT(t);
                        outputObject.setCoordinateSet(currOutputObject.getCoordinateSet());
                        outputObject.translateCoords(0, 0, z);
                    }

                    writeProgressStatus(++count, total, "slices");

                }
            }
        } else {
            for (int t = 0; t < inputImage.getImagePlus().getNFrames(); t++) {
                Image currImage = ExtractSubstack.extractSubstack(inputImage, "Timepoint", "1-end", "1-end",
                        String.valueOf(t + 1) + "-" + String.valueOf(t + 1));

                cellpose.setImagePlus(currImage.getImagePlus());
                cellpose.run();

                Image cellsImage = ImageFactory.createImage("Objects", cellpose.getLabels());
                Objs currOutputObjects = cellsImage.convertImageToObjects(VolumeType.QUADTREE,
                        outputObjectsName);

                for (Obj currOutputObject : currOutputObjects.values()) {
                    Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);
                    outputObject.setT(t);
                    outputObject.setCoordinateSet(currOutputObject.getCoordinateSet());
                }

                writeProgressStatus((t + 1), nFrames, "frames");

            }
        }

        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        String defaultEnv = EnvironmentTypes.CONDA;
        switch (Prefs.get("MIA.Cellpose.EnvType", "conda")) {
            case "conda":
                defaultEnv = EnvironmentTypes.CONDA;
                break;
            case "venv":
                defaultEnv = EnvironmentTypes.VENV;
                break;
        }

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(CELLPOSE_SEPARATOR, this));
        parameters.add(new FolderPathP(CELLPOSE_PATH, this, Prefs.get("MIA.Cellpose.EnvDirPath", "")));
        parameters.add(new ChoiceP(ENVIRONMENT_TYPE, this, defaultEnv, EnvironmentTypes.ALL));
        parameters.add(new BooleanP(USE_GPU, this, false));

        parameters.add(new SeparatorP(MODEL_SEPARATOR, this));
        parameters.add(new ChoiceP(MODEL_MODE, this, ModelModes.INCLUDED, ModelModes.ALL));
        parameters.add(new FilePathP(CUSTOM_MODEL_PATH, this));
        parameters.add(new StringP(MODEL, this, "cpsam"));
        parameters.add(new MessageP(MODEL_MESSAGE, this,
                "Available models will depend on the version of Cellpose installed, but examples are \"cyto\", \"cyto2\", \"cyto3\", \"cpsam\", \"nuclei\" and \"bact\".",
                ParameterState.MESSAGE));

        parameters.add(new SeparatorP(SEGMENTATION_SEPARATOR, this));
        parameters.add(new ChoiceP(DIMENSION_MODE, this, DimensionModes.TWOD, DimensionModes.ALL));
        parameters.add(new IntegerP(DIAMETER, this, 30));
        parameters.add(new DoubleP(CELL_PROBABILITY_THRESHOLD, this, 0.0));
        parameters.add(new DoubleP(FLOW_THRESHOLD, this, 0.4));
        parameters.add(new DoubleP(ANISOTROPY, this, 1.0));
        parameters.add(new DoubleP(STITCH_THRESHOLD, this, -1d));
        parameters.add(new StringP(ADDITIONAL_FLAGS, this));
        parameters.add(new MessageP(FLAGS_MESSAGE, this,
                "<html>All flags are listed at <a href=\"https://cellpose.readthedocs.io/en/latest/cli.html\">https://cellpose.readthedocs.io/en/latest/cli.html</a></html>",
                ParameterState.MESSAGE));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(CELLPOSE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CELLPOSE_PATH));
        returnedParameters.add(parameters.getParameter(ENVIRONMENT_TYPE));
        returnedParameters.add(parameters.getParameter(USE_GPU));

        returnedParameters.add(parameters.getParameter(MODEL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MODEL_MODE));
        switch ((String) parameters.getValue(MODEL_MODE, workspace)) {
            case ModelModes.INCLUDED:
                returnedParameters.add(parameters.getParameter(MODEL));
                returnedParameters.add(parameters.getParameter(MODEL_MESSAGE));
                break;
            case ModelModes.CUSTOM:
                returnedParameters.add(parameters.getParameter(CUSTOM_MODEL_PATH));
                break;
        }

        returnedParameters.add(parameters.getParameter(SEGMENTATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(DIMENSION_MODE));
        returnedParameters.add(parameters.getParameter(DIAMETER));
        returnedParameters.add(parameters.getParameter(CELL_PROBABILITY_THRESHOLD));
        returnedParameters.add(parameters.getParameter(FLOW_THRESHOLD));
        returnedParameters.add(parameters.getParameter(ANISOTROPY));
        returnedParameters.add(parameters.getParameter(STITCH_THRESHOLD));
        returnedParameters.add(parameters.getParameter(ADDITIONAL_FLAGS));
        returnedParameters.add(parameters.getParameter(FLAGS_MESSAGE));

        return returnedParameters;

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
}
