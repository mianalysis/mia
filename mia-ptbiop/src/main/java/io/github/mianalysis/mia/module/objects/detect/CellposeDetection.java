package io.github.mianalysis.mia.module.objects.detect;

import java.io.File;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ch.epfl.biop.wrappers.cellpose.Cellpose;
import ch.epfl.biop.wrappers.cellpose.ij2commands.CellposeWrapper;
import ij.Prefs;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.volume.QuadtreeFactory;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
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

    public static final String CELLPOSE_VERSION = "Cellpose version";

    public static final String USE_MXNET = "Use MXNet";

    public static final String USE_RESAMPLE = "Use resample";

    public static final String USE_GPU = "Use GPU";

    public static final String USE_FASTMODE = "Use fast mode";

    public static final String MODEL_SEPARATOR = "Model settings";

    public static final String MODEL_MODE = "Model mode";

    public static final String CUSTOM_MODEL_PATH = "Custom model path";

    public static final String MODEL = "Model";

    public static final String USE_NUCLEI_CHANNEL = "Use nuclei channel";

    public static final String NUCLEI_CHANNEL = "Nuclei channel";

    public static final String USE_CYTO_CHANNEL = "Use cyto channel";

    public static final String CYTO_CHANNEL = "Cyto channel";

    public static final String DIMENSION_MODE = "Dimension mode";

    public static final String SEGMENTATION_SEPARATOR = "Segmentation settings";

    public static final String DIAMETER = "Diameter";

    public static final String CELL_PROBABILITY_THRESHOLD = "Cell probability threshold";

    public static final String FLOW_THRESHOLD = "Flow threshold";

    public static final String ANISOTROPY = "Anisotropy";

    public static final String DIAMETER_THRESHOLD = "Diameter threshold";

    public static final String STITCH_THRESHOLD = "Stitch threshold";

    public static final String USE_OMNI = "Use Omnipose mask reconstruction features";

    public static final String USE_CLUSTERING = "Use DBSCAN clustering";

    public static final String ADDITIONAL_FLAGS = "Additional flags";

    public interface EnvironmentTypes {
        String CONDA = "Conda";
        String VENV = "Venv";

        String[] ALL = new String[] { CONDA, VENV };

    }

    public interface CellposeVersions {
        String V0P6 = "0.6";
        String V0P7 = "0.7";
        String V1P0 = "1.0";
        String V2P0 = "2.0";

        String[] ALL = new String[] { V0P6, V0P7, V1P0, V2P0 };

    }

    public interface ModelModes {
        String CUSTOM = "Custom";
        String INCLUDED = "Included";

        String[] ALL = new String[] { CUSTOM, INCLUDED };

    }

    public interface Models {
        String BACT_OMNI = "Bact omni";
        String CYTO = "Cyto";
        String CYTO2 = "Cyto 2";
        String CYTO2_OMNI = "Cyto 2 omni";
        String NUCLEI = "Nuclei";

        String[] ALL = new String[] { BACT_OMNI, CYTO, CYTO2, CYTO2_OMNI, NUCLEI };

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

    public static String getModelName(String model) {
        switch (model) {
            case Models.BACT_OMNI:
                return "bact_omni";
            case Models.CYTO:
                return "cyto";
            case Models.CYTO2:
                return "cyto2";
            case Models.CYTO2_OMNI:
                return "cyto2_omni";
            case Models.NUCLEI:
            default:
                return "nuclei";
        }
    }

    public static String getCustomModelType(String model) {
        return "own model " + getModelName(model);

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        String cellposePath = parameters.getValue(CELLPOSE_PATH, workspace);
        String environmentType = parameters.getValue(ENVIRONMENT_TYPE, workspace).toString().toLowerCase();
        String version = parameters.getValue(CELLPOSE_VERSION, workspace);
        boolean useMXNet = parameters.getValue(USE_MXNET, workspace);
        boolean useFastMode = parameters.getValue(USE_FASTMODE, workspace);
        boolean useGPU = parameters.getValue(USE_GPU, workspace);
        boolean useResample = parameters.getValue(USE_RESAMPLE, workspace);

        String modelMode = parameters.getValue(MODEL_MODE, workspace);
        String customModelPath = parameters.getValue(CUSTOM_MODEL_PATH, workspace);
        String model = parameters.getValue(MODEL, workspace);
        boolean useNucleiChannel = parameters.getValue(USE_NUCLEI_CHANNEL, workspace);
        int nucleiChannel = parameters.getValue(NUCLEI_CHANNEL, workspace);
        boolean useCytoChannel = parameters.getValue(USE_CYTO_CHANNEL, workspace);
        int cytoChannel = parameters.getValue(CYTO_CHANNEL, workspace);
        String dimensionMode = parameters.getValue(DIMENSION_MODE, workspace);

        int diameter = parameters.getValue(DIAMETER, workspace);
        double cellProbThresh = parameters.getValue(CELL_PROBABILITY_THRESHOLD, workspace);
        double flowThreshold = parameters.getValue(FLOW_THRESHOLD, workspace);
        double anisotropy = parameters.getValue(ANISOTROPY, workspace);
        double diameterThreshold = parameters.getValue(DIAMETER_THRESHOLD, workspace);
        double stitchThreshold = parameters.getValue(STITCH_THRESHOLD, workspace);
        boolean useOmni = parameters.getValue(USE_OMNI, workspace);
        boolean useClustering = parameters.getValue(USE_CLUSTERING, workspace);
        String additionalFlags = parameters.getValue(ADDITIONAL_FLAGS, workspace);

        ImageI inputImage = workspace.getImages().get(inputImageName);

        Cellpose.setEnvDirPath(new File(cellposePath));
        Cellpose.setEnvType(environmentType);
        Cellpose.setVersion(version);
        switch ((String) parameters.getValue(CELLPOSE_VERSION, workspace)) {
            case CellposeVersions.V0P6:
            case CellposeVersions.V0P7:
                Cellpose.setUseMxnet(useMXNet);
                Cellpose.setUseFastMode(useFastMode);
                break;
            case CellposeVersions.V1P0:
                Cellpose.setUseMxnet(useMXNet);
                break;
        }
        Cellpose.setUseGpu(useGPU);
        Cellpose.setUseResample(useResample);

        CellposeWrapper cellpose = new CellposeWrapper();

        switch (modelMode) {
            case ModelModes.CUSTOM:
                cellpose.setModel(getCustomModelType(model));
                cellpose.setModelPath(new File(customModelPath));
                break;
            case ModelModes.INCLUDED:
                cellpose.setModel(getModelName(model));
                break;
        }

        switch (model) {
            case Models.BACT_OMNI:
                cellpose.setNucleiChannel(-1);
                cellpose.setCytoChannel(cytoChannel);
                break;
            case Models.CYTO:
            case Models.CYTO2:
            case Models.CYTO2_OMNI:
                if (useNucleiChannel)
                    cellpose.setNucleiChannel(nucleiChannel);
                else
                    cellpose.setNucleiChannel(-1);

                if (useCytoChannel)
                    cellpose.setCytoChannel(cytoChannel);
                else
                    cellpose.setCytoChannel(-1);

                break;
            case Models.NUCLEI:
                cellpose.setNucleiChannel(nucleiChannel);
                cellpose.setCytoChannel(-1);
                break;
        }

        cellpose.setDimensionMode(dimensionMode);
        cellpose.setDiameter(diameter);
        cellpose.setCellProbabilityThreshold(cellProbThresh);
        cellpose.setFlowThreshold(flowThreshold);
        cellpose.setAnisotropy(anisotropy);
        cellpose.setDiameterThreshold(diameterThreshold);
        cellpose.setStitchThreshold(stitchThreshold);
        cellpose.setUseOmni(useOmni);
        cellpose.setUseClustering(useClustering);
        cellpose.setAdditionalFlags(additionalFlags);

        Objs outputObjects = null;
        int count = 0;
        if (dimensionMode.equals(DimensionModes.TWOD) && inputImage.getImagePlus().getNSlices() > 1) {
            SpatCal spatCal = SpatCal.getFromImage(inputImage.getImagePlus());
            int nFrames = inputImage.getImagePlus().getNFrames();
            double frameInterval = inputImage.getImagePlus().getCalibration().frameInterval;
            outputObjects = new Objs(outputObjectsName, spatCal, nFrames, frameInterval,
                    TemporalUnit.getOMEUnit());

            for (int z = 0; z < inputImage.getImagePlus().getNSlices(); z++) {
                for (int t = 0; t < inputImage.getImagePlus().getNFrames(); t++) {
                    ImageI currImage = ExtractSubstack.extractSubstack(inputImage, "Timepoint", "1-end",
                            String.valueOf(z + 1) + "-" + String.valueOf(z + 1),
                            String.valueOf(t + 1) + "-" + String.valueOf(t + 1));

                    cellpose.setImagePlus(currImage.getImagePlus());
                    cellpose.run();

                    ImageI cellsImage = ImageFactory.createImage("Objects", cellpose.getLabels());
                    Objs currOutputObjects = cellsImage.convertImageToObjects(new QuadtreeFactory(), outputObjectsName);

                    for (Obj currOutputObject : currOutputObjects.values()) {
                        Obj outputObject = outputObjects.createAndAddNewObject(new QuadtreeFactory());
                        outputObject.setT(t);
                        outputObject.setCoordinateSet(currOutputObject.getCoordinateSet());
                        outputObject.translateCoords(0, 0, z);
                    }

                    writeProgressStatus(++count, inputImage.getImagePlus().getStackSize(), "slices");

                }
            }
            
        } else {
            cellpose.setImagePlus(inputImage.getImagePlus());
            cellpose.run();
            ImageI cellsImage = ImageFactory.createImage("Objects", cellpose.getLabels());
            outputObjects = cellsImage.convertImageToObjects(new QuadtreeFactory(), outputObjectsName);
        }

        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        // Getting defaults
        String keyPrefix = Cellpose.class.getName() + ".";

        String defaultEnv = EnvironmentTypes.CONDA;
        switch (Prefs.get(keyPrefix + "envType", "conda")) {
            case "conda":
                defaultEnv = EnvironmentTypes.CONDA;
                break;
            case "venv":
                defaultEnv = EnvironmentTypes.VENV;
                break;
        }

        String defaultVersion = CellposeVersions.V2P0;
        switch (Prefs.get(keyPrefix + "version", "2.0")) {
            case "0.6":
                defaultVersion = CellposeVersions.V0P6;
                break;
            case "0.7":
                defaultVersion = CellposeVersions.V0P7;
                break;
            case "1.0":
                defaultVersion = CellposeVersions.V1P0;
                break;
            case "2.0":
                defaultVersion = CellposeVersions.V2P0;
                break;
        }

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(CELLPOSE_SEPARATOR, this));
        parameters.add(new FolderPathP(CELLPOSE_PATH, this, Prefs.get(keyPrefix + "envDirPath", "")));
        parameters.add(new ChoiceP(ENVIRONMENT_TYPE, this, defaultEnv, EnvironmentTypes.ALL));
        parameters.add(new ChoiceP(CELLPOSE_VERSION, this, defaultVersion, CellposeVersions.ALL));
        parameters.add(new BooleanP(USE_MXNET, this, Prefs.get(keyPrefix + "useMxnet", false)));
        parameters.add(new BooleanP(USE_RESAMPLE, this, Prefs.get(keyPrefix + "useResample", false)));
        parameters.add(new BooleanP(USE_GPU, this, Prefs.get(keyPrefix + "useGpu", false)));
        parameters.add(new BooleanP(USE_FASTMODE, this, Prefs.get(keyPrefix + "useFastMode", false)));

        parameters.add(new SeparatorP(MODEL_SEPARATOR, this));
        parameters.add(new ChoiceP(MODEL_MODE, this, ModelModes.INCLUDED, ModelModes.ALL));
        parameters.add(new FilePathP(CUSTOM_MODEL_PATH, this));
        parameters.add(new ChoiceP(MODEL, this, Models.NUCLEI, Models.ALL));
        parameters.add(new BooleanP(USE_NUCLEI_CHANNEL, this, true));
        parameters.add(new IntegerP(NUCLEI_CHANNEL, this, 1));
        parameters.add(new BooleanP(USE_CYTO_CHANNEL, this, true));
        parameters.add(new IntegerP(CYTO_CHANNEL, this, 1));
        parameters.add(new ChoiceP(DIMENSION_MODE, this, DimensionModes.TWOD, DimensionModes.ALL));

        parameters.add(new SeparatorP(SEGMENTATION_SEPARATOR, this));
        parameters.add(new IntegerP(DIAMETER, this, 30));
        parameters.add(new DoubleP(CELL_PROBABILITY_THRESHOLD, this, 0.0));
        parameters.add(new DoubleP(FLOW_THRESHOLD, this, 0.4));
        parameters.add(new DoubleP(ANISOTROPY, this, 1.0));
        parameters.add(new DoubleP(DIAMETER_THRESHOLD, this, 12));
        parameters.add(new DoubleP(STITCH_THRESHOLD, this, -1d));
        parameters.add(new BooleanP(USE_OMNI, this, false));
        parameters.add(new BooleanP(USE_CLUSTERING, this, false));
        parameters.add(new StringP(ADDITIONAL_FLAGS, this));

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(CELLPOSE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CELLPOSE_PATH));
        returnedParameters.add(parameters.getParameter(ENVIRONMENT_TYPE));
        returnedParameters.add(parameters.getParameter(CELLPOSE_VERSION));
        switch ((String) parameters.getValue(CELLPOSE_VERSION, workspace)) {
            case CellposeVersions.V0P6:
            case CellposeVersions.V0P7:
                returnedParameters.add(parameters.getParameter(USE_MXNET));
                returnedParameters.add(parameters.getParameter(USE_RESAMPLE));
                break;
            case CellposeVersions.V1P0:
                returnedParameters.add(parameters.getParameter(USE_MXNET));
                break;
        }
        returnedParameters.add(parameters.getParameter(USE_GPU));
        returnedParameters.add(parameters.getParameter(USE_FASTMODE));

        returnedParameters.add(parameters.getParameter(MODEL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MODEL_MODE));
        switch ((String) parameters.getValue(MODEL_MODE, workspace)) {
            case ModelModes.CUSTOM:
                returnedParameters.add(parameters.getParameter(CUSTOM_MODEL_PATH));
                break;
        }
        returnedParameters.add(parameters.getParameter(MODEL));
        switch ((String) parameters.getValue(MODEL, workspace)) {
            case Models.BACT_OMNI:
                returnedParameters.add(parameters.getParameter(CYTO_CHANNEL));
                break;
            case Models.CYTO:
            case Models.CYTO2:
            case Models.CYTO2_OMNI:
                returnedParameters.add(parameters.getParameter(USE_NUCLEI_CHANNEL));
                if ((boolean) parameters.getValue(USE_NUCLEI_CHANNEL, workspace))
                    returnedParameters.add(parameters.getParameter(NUCLEI_CHANNEL));
                returnedParameters.add(parameters.getParameter(USE_CYTO_CHANNEL));
                if ((boolean) parameters.getValue(USE_CYTO_CHANNEL, workspace))
                    returnedParameters.add(parameters.getParameter(CYTO_CHANNEL));
                break;
            case Models.NUCLEI:
                returnedParameters.add(parameters.getParameter(NUCLEI_CHANNEL));
                break;
        }
        returnedParameters.add(parameters.getParameter(DIMENSION_MODE));

        returnedParameters.add(parameters.getParameter(SEGMENTATION_SEPARATOR));
        switch ((String) parameters.getValue(CELLPOSE_VERSION, workspace)) {
            case CellposeVersions.V0P6:
                returnedParameters.add(parameters.getParameter(DIAMETER));
                returnedParameters.add(parameters.getParameter(CELL_PROBABILITY_THRESHOLD));
                returnedParameters.add(parameters.getParameter(FLOW_THRESHOLD));
                break;
            case CellposeVersions.V0P7:
                returnedParameters.add(parameters.getParameter(DIAMETER));
                returnedParameters.add(parameters.getParameter(CELL_PROBABILITY_THRESHOLD));
                returnedParameters.add(parameters.getParameter(FLOW_THRESHOLD));
                returnedParameters.add(parameters.getParameter(ANISOTROPY));
                returnedParameters.add(parameters.getParameter(DIAMETER_THRESHOLD));
                break;
            case CellposeVersions.V1P0:
                returnedParameters.add(parameters.getParameter(DIAMETER));
                returnedParameters.add(parameters.getParameter(CELL_PROBABILITY_THRESHOLD));
                returnedParameters.add(parameters.getParameter(FLOW_THRESHOLD));
                returnedParameters.add(parameters.getParameter(DIAMETER_THRESHOLD));
                break;
            case CellposeVersions.V2P0:
                returnedParameters.add(parameters.getParameter(DIAMETER));
                returnedParameters.add(parameters.getParameter(CELL_PROBABILITY_THRESHOLD));
                returnedParameters.add(parameters.getParameter(FLOW_THRESHOLD));
                returnedParameters.add(parameters.getParameter(ANISOTROPY));
                break;
        }

        returnedParameters.add(parameters.getParameter(STITCH_THRESHOLD));
        returnedParameters.add(parameters.getParameter(USE_OMNI));
        returnedParameters.add(parameters.getParameter(USE_CLUSTERING));
        returnedParameters.add(parameters.getParameter(ADDITIONAL_FLAGS));

        // Updating default parameters
        String keyPrefix = Cellpose.class.getName() + ".";

        Prefs.set(keyPrefix + "envDirPath", parameters.getValue(CELLPOSE_PATH, workspace));

        String defaultEnv = "conda";
        switch ((String) parameters.getValue(ENVIRONMENT_TYPE, workspace)) {
            case EnvironmentTypes.CONDA:
                defaultEnv = "conda";
                break;
            case EnvironmentTypes.VENV:
                defaultEnv = "venv";
                break;
        }
        Prefs.set(keyPrefix + "envType", defaultEnv);

        String defaultVersion = "2.0";
        switch ((String) parameters.getValue(CELLPOSE_VERSION, workspace)) {
            case CellposeVersions.V0P6:
                defaultVersion = "0.6";
                break;
            case CellposeVersions.V0P7:
                defaultVersion = "0.7";
                break;
            case CellposeVersions.V1P0:
                defaultVersion = "1.0";
                break;
            case CellposeVersions.V2P0:
                defaultVersion = "2.0";
                break;
        }
        Prefs.set(keyPrefix + "version", defaultVersion);

        Prefs.set(keyPrefix + "useMxnet", parameters.getValue(USE_MXNET, workspace).toString());
        Prefs.set(keyPrefix + "useResample", parameters.getValue(USE_RESAMPLE, workspace).toString());
        Prefs.set(keyPrefix + "useGpu", parameters.getValue(USE_GPU, workspace).toString());
        Prefs.set(keyPrefix + "useFastMode", parameters.getValue(USE_FASTMODE, workspace).toString());

        Prefs.savePreferences();
        
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
