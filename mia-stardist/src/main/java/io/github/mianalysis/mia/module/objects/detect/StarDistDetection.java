package io.github.mianalysis.mia.module.objects.detect;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;
import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.command.CommandModule;
import org.scijava.plugin.Plugin;

import de.csbdresden.stardist.Candidates;
import de.csbdresden.stardist.StarDist2DModel;
import de.csbdresden.stardist.Utils;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.configure.SetDisplayRange;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.visualise.overlays.AddObjectOutline;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.volume.QuadtreeFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;


/**
* Implements the StarDist plugin to detect objects.  For more information on StarDist please see <a href="https://imagej.net/plugins/stardist">https://imagej.net/plugins/stardist</a>.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class StarDistDetection extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/object output";

	/**
	* 
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* 
	*/
    public static final String OUTPUT_OBJECTS = "Output objects";


	/**
	* 
	*/
    public static final String MODEL_SEPARATOR = "Model settings";

	/**
	* 
	*/
    public static final String MODEL_MODE = "Model mode";

	/**
	* 
	*/
    public static final String MODEL_NAME = "Model name";

	/**
	* 
	*/
    public static final String MODEL_PATH = "Model path";


	/**
	* 
	*/
    public static final String PREDICTION_SEPARATOR = "Prediction settings";

	/**
	* 
	*/
    public static final String NORMALISE_INPUT = "Normalise image";

	/**
	* 
	*/
    public static final String PERCENTILE_LOW = "Percentile low";

	/**
	* 
	*/
    public static final String PERCENTILE_HIGH = "Percentile high";

	/**
	* 
	*/
    public static final String NUMBER_OF_TILES = "Number of tiles";


	/**
	* 
	*/
    public static final String NMS_SEPARATOR = "NMS post-processing";

	/**
	* 
	*/
    public static final String PROB_THRESHOLD = "Probability threshold";

	/**
	* 
	*/
    public static final String OVERLAP_THRESHOLD = "Overlap threshold";

	/**
	* 
	*/
    public static final String BOUNDARY_EXCLUSION = "Boundary exclusion";

    // From
    // https://github.com/stardist/stardist-imagej/blob/master/src/main/java/de/csbdresden/stardist/StarDist2DModel.java
    // (Accessed 2021-11-10)
    static final String MODEL_DSB2018_HEAVY_AUGMENTATION = "Versatile (fluorescent nuclei)";
    static final String MODEL_DSB2018_PAPER = "DSB 2018 (from StarDist 2D paper)";
    static final String MODEL_HE_HEAVY_AUGMENTATION = "Versatile (H&E nuclei)";
    static final String MODEL_DEFAULT = MODEL_DSB2018_HEAVY_AUGMENTATION;

    static final Map<String, StarDist2DModel> MODELS = new LinkedHashMap<String, StarDist2DModel>();
    static {
        MODELS.put(MODEL_DSB2018_PAPER,
                new StarDist2DModel(StarDist2DModel.class.getClassLoader().getResource("models/2D/dsb2018_paper.zip"),
                        0.417819, 0.5, 8, 48));
        MODELS.put(MODEL_DSB2018_HEAVY_AUGMENTATION,
                new StarDist2DModel(
                        StarDist2DModel.class.getClassLoader().getResource("models/2D/dsb2018_heavy_augment.zip"),
                        0.479071, 0.3, 16, 96));
        MODELS.put(MODEL_HE_HEAVY_AUGMENTATION,
                new StarDist2DModel(
                        StarDist2DModel.class.getClassLoader().getResource("models/2D/he_heavy_augment.zip"), 0.692478,
                        0.3, 16, 96));
    }

    public interface ModelModes {
        String FROM_FILE = "From file";
        String PRE_DEFINED = "Pre-defined";

        String[] ALL = new String[] { FROM_FILE, PRE_DEFINED };

    }

    public interface ModelNames {
        String VERSATILE_FLUORESCENT_NUCLEI = "Versatile (fluorescent nuclei)";
        String VERSATILE_HE_NUCLEI = "Versatile (H&E nuclei)";
        String DSB_2018 = "DSB 2018 (from StarDist 2D paper)";

        String[] ALL = new String[] { VERSATILE_FLUORESCENT_NUCLEI, VERSATILE_HE_NUCLEI, DSB_2018 };

    }

    public StarDistDetection(Modules modules) {
        super("StarDist detection", modules);

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }
    
    public String getDescription() {
        return "Implements the StarDist plugin to detect objects.  For more information on StarDist please see <a href=\"https://imagej.net/plugins/stardist\">https://imagej.net/plugins/stardist</a>.";
    }

    File getModelFile(@Nullable HashMap<String, Object> paramsCNN, WorkspaceI workspace) {
        switch ((String) parameters.getValue(MODEL_MODE, workspace)) {
            case ModelModes.FROM_FILE:
                return new File((String) parameters.getValue(MODEL_PATH, workspace));

            case ModelModes.PRE_DEFINED:
                StarDist2DModel model = null;
                switch ((String) parameters.getValue(MODEL_NAME, workspace)) {
                    case ModelNames.DSB_2018:
                        model = MODELS.get(MODEL_DSB2018_PAPER);
                        break;
                    case ModelNames.VERSATILE_FLUORESCENT_NUCLEI:
                        model = MODELS.get(MODEL_DSB2018_HEAVY_AUGMENTATION);
                        break;
                    case ModelNames.VERSATILE_HE_NUCLEI:
                        model = MODELS.get(MODEL_HE_HEAVY_AUGMENTATION);
                        break;
                }

                if (model.canGetFile()) {
                    try {
                        paramsCNN.put("blockMultiple", model.sizeDivBy);
                        paramsCNN.put("overlap", model.tileOverlap);
                        return model.getFile();
                    } catch (IOException e) {
                        MIA.log.writeWarning("Can't get StarDist model");
                        return null;
                    }
                } else {
                    MIA.log.writeWarning("Can't get StarDist model");
                    return null;
                }
        }

        return null;

    }

    Pair<Dataset, Dataset> splitPrediction(final Dataset prediction, final DatasetService dataset) {
        RandomAccessibleInterval<FloatType> predictionRAI = (RandomAccessibleInterval<FloatType>) prediction
                .getImgPlus();
        LinkedHashSet<AxisType> predAxes = Utils.orderedAxesSet(prediction);

        final int predChannelDim = IntStream.range(0, predAxes.size())
                .filter(d -> prediction.axis(d).type() == Axes.CHANNEL).findFirst().getAsInt();
        final long[] predStart = predAxes.stream().mapToLong(axis -> {
            return axis == Axes.CHANNEL ? 1L : 0L;
        }).toArray();
        final long[] predSize = predAxes.stream().mapToLong(axis -> {
            return axis == Axes.CHANNEL ? prediction.dimension(axis) - 1 : prediction.dimension(axis);
        }).toArray();

        IntervalView<FloatType> probRAI = Views.hyperSlice(predictionRAI, predChannelDim, 0);
        IntervalView<FloatType> distRAI = Views.offsetInterval(predictionRAI, predStart, predSize);

        Dataset probDS = Utils.raiToDataset(dataset, "Probability/Score Image", probRAI,
                predAxes.stream().filter(axis -> axis != Axes.CHANNEL));
        Dataset distDS = Utils.raiToDataset(dataset, "Distance Image", distRAI, predAxes);

        return new ValuePair<>(probDS, distDS);

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        ImageI inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputIpl = inputImage.getImagePlus();

        // Creating output object collection
        ObjsI outputObjects = ObjsFactories.getDefaultFactory().createFromImage(outputObjectsName, inputImage);

        ImageJ ij = new ImageJ();
        Context context = MIA.getIJService().context();
        DatasetService datasetService = (DatasetService) MIA.getIJService().context()
                .service("net.imagej.DatasetService");

        // Initialising parameter sets
        HashMap<String, Object> paramsCNN = new HashMap<>();
        paramsCNN.put("normalizeInput", parameters.getValue(NORMALISE_INPUT, workspace));
        paramsCNN.put("percentileBottom", parameters.getValue(PERCENTILE_LOW, workspace));
        paramsCNN.put("percentileTop", parameters.getValue(PERCENTILE_HIGH, workspace));
        paramsCNN.put("clip", false);
        paramsCNN.put("nTiles", parameters.getValue(NUMBER_OF_TILES, workspace));
        paramsCNN.put("blockMultiple", 64);
        paramsCNN.put("overlap", 64);
        paramsCNN.put("batchSize", 1);
        paramsCNN.put("showProgressDialog", false);

        File modelFile = getModelFile(paramsCNN, workspace);
        if (modelFile == null)
            return Status.FAIL;
        paramsCNN.put("modelFile", modelFile);

        HashMap<String, Object> paramsNMS = new HashMap<>();
        paramsNMS.put("probThresh", parameters.getValue(PROB_THRESHOLD, workspace));
        paramsNMS.put("nmsThresh", parameters.getValue(OVERLAP_THRESHOLD, workspace));
        paramsNMS.put("excludeBoundary", parameters.getValue(BOUNDARY_EXCLUSION, workspace));
        paramsNMS.put("roiPosition", "Stack");
        paramsNMS.put("verbose", false);
        paramsNMS.put("outputType", "Polygons");

        int count = 0;
        int total = inputIpl.getNFrames() * inputIpl.getNSlices();
        for (int t = 0; t < inputIpl.getNFrames(); t++) {
            for (int z = 0; z < inputIpl.getNSlices(); z++) {
                ImageI subs = ExtractSubstack.extractSubstack(inputImage, "Subs", "1-end", String.valueOf(z + 1),
                        String.valueOf(t + 1));
                ImgPlus img = subs.getImgPlus();
                DefaultDataset dataset = new DefaultDataset(context, img);
                paramsCNN.put("input", dataset);

                try {
                    Future<CommandModule> futureCNN = ij.command().run(
                            de.csbdresden.csbdeep.commands.GenericNetwork.class,
                            false, paramsCNN);
                    Dataset prediction = (Dataset) futureCNN.get().getOutput("output");

                    Pair<Dataset, Dataset> probAndDist = splitPrediction(prediction, datasetService);
                    Dataset probDS = probAndDist.getA();
                    Dataset distDS = probAndDist.getB();
                    paramsNMS.put("prob", probDS);
                    paramsNMS.put("dist", distDS);

                    Future<CommandModule> futureNMS = ij.command().run(de.csbdresden.stardist.StarDist2DNMS.class,
                            false,
                            paramsNMS);
                    Candidates polygons = (Candidates) futureNMS.get().getOutput("polygons");
                    List<Integer> indices = polygons.getWinner();

                    for (Integer idx : indices) {
                        PolygonRoi polygon = polygons.getPolygonRoi(idx);
                        ObjI obj = outputObjects.createAndAddNewObject(new QuadtreeFactory());
                        try {
                            obj.addPointsFromRoi(polygon, z);
                        } catch (IntegerOverflowException e) {
                        }
                        obj.setT(t);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    return Status.FAIL;
                }

                Module.writeProgressStatus(++count, total, "slices", "StarDist");

            }
        }

        workspace.addObjects(outputObjects);

        if (showOutput) {
            ImagePlus overlayIpl = inputImage.getImagePlus().duplicate();

            HashMap<Integer, Float> hues = ColourFactory.getIDHues(outputObjects, true);
            HashMap<Integer, Color> colours = ColourFactory.getColours(hues);
            AddObjectOutline.addOverlay(overlayIpl, outputObjects, 1, 1, colours, false, true);
            SetDisplayRange.setDisplayRangeAuto(overlayIpl, SetDisplayRange.CalculationModes.FAST,
                    new double[] { 0, 0 }, new boolean[] { true, true }, null);
            overlayIpl.show();
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(MODEL_SEPARATOR, this));
        parameters.add(new ChoiceP(MODEL_MODE, this, ModelModes.PRE_DEFINED, ModelModes.ALL));
        parameters.add(new ChoiceP(MODEL_NAME, this, ModelNames.VERSATILE_FLUORESCENT_NUCLEI, ModelNames.ALL));
        parameters.add(new FilePathP(MODEL_PATH, this));

        parameters.add(new SeparatorP(PREDICTION_SEPARATOR, this));
        parameters.add(new BooleanP(NORMALISE_INPUT, this, true));
        parameters.add(new DoubleP(PERCENTILE_LOW, this, 0.0));
        parameters.add(new DoubleP(PERCENTILE_HIGH, this, 95.0));
        parameters.add(new IntegerP(NUMBER_OF_TILES, this, 1));

        parameters.add(new SeparatorP(NMS_SEPARATOR, this));
        parameters.add(new DoubleP(PROB_THRESHOLD, this, 0.4));
        parameters.add(new DoubleP(OVERLAP_THRESHOLD, this, 0.4));
        parameters.add(new IntegerP(BOUNDARY_EXCLUSION, this, 2));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(MODEL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MODEL_MODE));
        switch ((String) parameters.getValue(MODEL_MODE, null)) {
            case ModelModes.FROM_FILE:
                returnedParameters.add(parameters.getParameter(MODEL_PATH));
                break;
            case ModelModes.PRE_DEFINED:
                returnedParameters.add(parameters.getParameter(MODEL_NAME));
                break;
        }

        returnedParameters.add(parameters.getParameter(PREDICTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(NORMALISE_INPUT));
        returnedParameters.add(parameters.getParameter(PERCENTILE_LOW));
        returnedParameters.add(parameters.getParameter(PERCENTILE_HIGH));
        returnedParameters.add(parameters.getParameter(NUMBER_OF_TILES));

        returnedParameters.add(parameters.getParameter(NMS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(PROB_THRESHOLD));
        returnedParameters.add(parameters.getParameter(OVERLAP_THRESHOLD));
        returnedParameters.add(parameters.getParameter(BOUNDARY_EXCLUSION));

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
