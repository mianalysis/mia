// TODO: Could addRef an optional parameter to select the channel of the input image to use for measurement

package io.github.mianalysis.mia.module.objects.measure.intensity;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.measure.MeasureGreyscaleKFunction;
import io.github.mianalysis.mia.module.images.transform.CropImage;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 08/07/2022.
 */

/**
 * Measure's Ripley's K-function for greyscale images on an object-by-object
 * basis. This method is re-written from the publication "Extending Ripley’s
 * K-Function to Quantify Aggregation in 2-D Grayscale Images" by M. Amgad, et
 * al. (doi: 10.1371/journal.pone.0144404). Results are output to an Excel
 * spreadsheet, with one file per input image.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureObjectGreyscaleKFunction extends AbstractSaver {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object and image input";

    /**
    * 
    */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String INPUT_IMAGE = "Input image";

    /**
    * 
    */
    public static final String FUNCTION_SEPARATOR = "K-function controls";
    public static final String MINIMUM_RADIUS_PX = "Minimum radius (px)";
    public static final String MAXIMUM_RADIUS_PX = "Maximum radius (px)";
    public static final String RADIUS_INCREMENT = "Radius increment (px)";

    public static final String PERCENTILE_INTERVAL_SEPARATOR = "Percentile interval controls";
    public static final String CALCULATE_PERCENTILE_INTERVAL = "Calculate percentile interval";
    public static final String PERCENTILE_INTERVAL = "Percentile interval";
    public static final String NUMBER_OF_SIMULATIONS = "Number of simulations";
    public static final String PERCENTILE_INTERVAL_WARNING = "Percentile intervals warning";

    /**
    * 
    */
    public static final String EXECUTION_SEPARATOR = "Execution controls";

    /**
     * Process multiple input objects simultaneously. This can provide a speed
     * improvement when working on a computer with a multi-core CPU.
     */
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface Measurements {
        String MAX_LOCATION_PX = "L(r)-r // MAX_LOCATION_(PX)";
        String MIN_LOCATION_PX = "L(r)-r // MIN_LOCATION_(PX)";
        String MAX_VALUE = "L(r)-r // MAX_VALUE";
        String MIN_VALUE = "L(r)-r // MAX_VALUE";
    }

    public MeasureObjectGreyscaleKFunction(Modules modules) {
        super("Measure object greyscale K-function", modules);
    }

    public static String getFullName(String imageName, String measurementName) {
        return "GREYSCALE_K_FUNCTION // " + imageName + " // " + measurementName;
    }

    void initialiseSheetHeader(SXSSFSheet sheet, boolean calculatePercentileInterval, double percentileLow, double percentileHigh) {
        int colI = 0;

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(colI++);
        cell.setCellValue("Object ID");

        cell = row.createCell(colI++);
        cell.setCellValue("Timepoint");

        cell = row.createCell(colI++);
        cell.setCellValue("Slice");

        cell = row.createCell(colI++);
        cell.setCellValue("Radius (px)");

        cell = row.createCell(colI++);
        cell.setCellValue("K(r)");

        cell = row.createCell(colI++);
        cell.setCellValue("L(r)");

        cell = row.createCell(colI++);
        cell.setCellValue("L(r)-r");

        if (calculatePercentileInterval) {
            cell = row.createCell(colI++);
            cell.setCellValue("Percentile: K(r)_" + percentileLow + "%");

            cell = row.createCell(colI++);
            cell.setCellValue("Percentile: K(r)_" + percentileHigh + "%");

            cell = row.createCell(colI++);
            cell.setCellValue("Percentile: L(r)_" + percentileLow + "%");

            cell = row.createCell(colI++);
            cell.setCellValue("Percentile: L(r)_" + percentileHigh + "%");

            cell = row.createCell(colI++);
            cell.setCellValue("Percentile: L(r)-r_" + percentileLow + "%");

            cell = row.createCell(colI++);
            cell.setCellValue("Percentile: L(r)-r_" + percentileHigh + "%");

        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_INTENSITY;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Measure's Ripley's K-function for greyscale images on an object-by-object basis.  This method is re-written from the publication \"Extending Ripley’s K-Function to Quantify Aggregation in 2-D Grayscale Images\" by M. Amgad, et al. (doi: 10.1371/journal.pone.0144404).  Results are output to an Excel spreadsheet, with one file per input image.";
    }

    public static void process(Obj inputObject, Image inputImage, int minRadius, int maxRadius, int radiusInc,
            SXSSFSheet sheet, boolean calculatePercentileInterval, double percentileLow, double percentileHigh,
            int nSimulations) {
        int rowI = 1;
        int t = inputObject.getT();

        // Getting images cropped to this object
        double[][] extents = inputObject.getExtents(true, false);
        int top = (int) Math.round(extents[1][0]);
        int left = (int) Math.round(extents[0][0]);
        int width = (int) Math.round(extents[0][1] - left) + 1;
        int height = (int) Math.round(extents[1][1] - top) + 1;
        Image cropImage = CropImage.cropImage(inputImage, "Crop", left, top, width, height);

        // Cropping image in Z
        int minZ = (int) Math.round(extents[2][0]);
        int maxZ = (int) Math.round(extents[2][1]);
        Image subsImage = ExtractSubstack.extractSubstack(cropImage, "Substack", "1",
                (minZ + 1) + "-" + (maxZ + 1),
                String.valueOf(t + 1));

        double maxL_r = -Double.MAX_VALUE;
        double maxL_rLoc = -1;
        double minL_r = Double.MAX_VALUE;
        double minL_rLoc = -1;

        Image maskImage = inputObject.getAsTightImage("Mask");
        for (int z = 0; z < maskImage.getImagePlus().getNSlices(); z++) {
            Image currImage = ExtractSubstack.extractSubstack(subsImage, "TimepointImage", "1",
                    String.valueOf(z + 1), "1");
            Image currMask = ExtractSubstack.extractSubstack(maskImage, "TimepointMask", "1",
                    String.valueOf(z + 1), "1");

            for (int r = minRadius; r <= maxRadius; r = r + radiusInc) {
                double K = MeasureGreyscaleKFunction.calculateGSKFunction(currImage, r, currMask);

                Row row = sheet.createRow(rowI++);
                int colI = 0;

                Cell cell = row.createCell(colI++);
                cell.setCellValue(inputObject.getID());

                cell = row.createCell(colI++);
                cell.setCellValue(t);

                cell = row.createCell(colI++);
                cell.setCellValue(z);

                cell = row.createCell(colI++);
                cell.setCellValue(r);

                cell = row.createCell(colI++);
                cell.setCellValue(K);

                cell = row.createCell(colI++);
                cell.setCellValue(MeasureGreyscaleKFunction.getLValue(K));

                cell = row.createCell(colI++);
                cell.setCellValue(MeasureGreyscaleKFunction.getL_rValue(K, r));

                // Calculate percentile intervals
                if (calculatePercentileInterval) {
                    double[] pcResults = new double[nSimulations];

                    for (int i = 0; i < nSimulations; i++) {
                        Image randomisedImage = MeasureGreyscaleKFunction.randomiseImage(currImage);
                        pcResults[i] = MeasureGreyscaleKFunction.calculateGSKFunction(randomisedImage, r, currMask);
                    }

                    Percentile percentile = new Percentile();
                    double kPercentileLow = percentile.evaluate(pcResults, percentileLow);
                    double kPercentileHigh = percentile.evaluate(pcResults, percentileHigh);

                    cell = row.createCell(colI++);
                    cell.setCellValue(kPercentileLow);

                    cell = row.createCell(colI++);
                    cell.setCellValue(kPercentileHigh);

                    cell = row.createCell(colI++);
                    cell.setCellValue(MeasureGreyscaleKFunction.getLValue(kPercentileLow));

                    cell = row.createCell(colI++);
                    cell.setCellValue(MeasureGreyscaleKFunction.getLValue(kPercentileHigh));

                    cell = row.createCell(colI++);
                    cell.setCellValue(MeasureGreyscaleKFunction.getL_rValue(kPercentileLow, r));

                    cell = row.createCell(colI++);
                    cell.setCellValue(MeasureGreyscaleKFunction.getL_rValue(kPercentileHigh, r));

                }

                // Updating measurement values
                double L_r = MeasureGreyscaleKFunction.getL_rValue(K, r);
                if (L_r > maxL_r) {
                    maxL_r = L_r;
                    maxL_rLoc = r;
                }

                if (L_r < minL_r) {
                    minL_r = L_r;
                    minL_rLoc = r;
                }
            }
        }

        // Adding measurements
        inputObject.addMeasurement(
                new Measurement(getFullName(inputImage.getName(), Measurements.MAX_LOCATION_PX), maxL_rLoc));
        inputObject.addMeasurement(
                new Measurement(getFullName(inputImage.getName(), Measurements.MIN_LOCATION_PX), minL_rLoc));
        inputObject
                .addMeasurement(new Measurement(getFullName(inputImage.getName(), Measurements.MAX_VALUE), maxL_r));
        inputObject
                .addMeasurement(new Measurement(getFullName(inputImage.getName(), Measurements.MIN_VALUE), minL_r));

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        int minRadius = parameters.getValue(MINIMUM_RADIUS_PX, workspace);
        int maxRadius = parameters.getValue(MAXIMUM_RADIUS_PX, workspace);
        int radiusInc = parameters.getValue(RADIUS_INCREMENT, workspace);
        boolean calculatePercentileInterval = parameters.getValue(CALCULATE_PERCENTILE_INTERVAL, workspace);
        double percentileInterval = parameters.getValue(PERCENTILE_INTERVAL, workspace);
        int nSimulations = parameters.getValue(NUMBER_OF_SIMULATIONS, workspace);
        double percentileLow = (100 - percentileInterval) / 2;
        double percentileHigh = 100 - percentileLow;
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX, workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);

        Objs inputObjects = workspace.getObjects(inputObjectsName);
        Image inputImage = workspace.getImage(inputImageName);
        SXSSFWorkbook workbook = new SXSSFWorkbook();

        // Setting up multithreading options
        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        AtomicInteger count = new AtomicInteger(1);
        int total = inputObjects.size();
        for (Obj inputObject : inputObjects.values()) {
            SXSSFSheet sheet = workbook.createSheet("Obj"+inputObject.getID());
            initialiseSheetHeader(sheet, calculatePercentileInterval, percentileLow, percentileHigh);
            
            Runnable task = () -> {
                try {
                    process(inputObject, inputImage, minRadius, maxRadius, radiusInc, sheet, calculatePercentileInterval,
                            percentileLow, percentileHigh, nSimulations);
                } catch (Exception e) {
                    MIA.log.writeError(e);
                }
                writeProgressStatus(count.getAndIncrement(), total, "measurements");
            };
            pool.submit(task);

        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        } catch (Exception e) {
            MIA.log.writeError(e);
        }

        String outputPath = getOutputPath(modules, workspace);
        String outputName = getOutputName(modules, workspace);

        // Adding last bits to name
        outputPath = outputPath + outputName;
        outputPath = appendSeries(outputPath, workspace, appendSeriesMode);
        outputPath = appendDateTime(outputPath, appendDateTimeMode);
        outputPath = outputPath + suffix + ".xlsx";

        MeasureIntensityAlongPath.writeDistancesFile(workbook, outputPath);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(FUNCTION_SEPARATOR, this));
        parameters.add(new IntegerP(MINIMUM_RADIUS_PX, this, 3) {
            @Override
            public boolean verify() {
                if ((int) getValue(null) == 0) {
                    MIA.log.writeWarning(getName() + ": Minimum radius must be greater than 0");
                    return false;
                }

                return true;

            }
        });
        parameters.add(new IntegerP(MAXIMUM_RADIUS_PX, this, 15));
        parameters.add(new IntegerP(RADIUS_INCREMENT, this, 1));

        parameters.add(new SeparatorP(PERCENTILE_INTERVAL_SEPARATOR, this));
        parameters.add(new BooleanP(CALCULATE_PERCENTILE_INTERVAL, this, false));
        parameters.add(new DoubleP(PERCENTILE_INTERVAL, this, 95));
        parameters.add(new IntegerP(NUMBER_OF_SIMULATIONS, this, 100));
        parameters.add(new MessageP(PERCENTILE_INTERVAL_WARNING, this,
                "Calculating percentile intervals can be slow.  To avoid excessive wait times, it's best to keep maximum radii as small as possible.",
                ParameterState.WARNING));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(FUNCTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MINIMUM_RADIUS_PX));
        returnedParameters.add(parameters.getParameter(MAXIMUM_RADIUS_PX));
        returnedParameters.add(parameters.getParameter(RADIUS_INCREMENT));

        returnedParameters.add(parameters.getParameter(PERCENTILE_INTERVAL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATE_PERCENTILE_INTERVAL));
        if ((Boolean) parameters.getValue(CALCULATE_PERCENTILE_INTERVAL, null)) {
            returnedParameters.add(parameters.getParameter(PERCENTILE_INTERVAL));
            returnedParameters.add(parameters.getParameter(NUMBER_OF_SIMULATIONS));
            returnedParameters.add(parameters.getParameter(PERCENTILE_INTERVAL_WARNING));
        }

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        returnedParameters.addAll(super.updateAndGetParameters());

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputImageName = parameters.getValue(INPUT_IMAGE, null);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, null);

        String measurementName = getFullName(inputImageName, Measurements.MAX_LOCATION_PX);
        ObjMeasurementRef measurementRef = objectMeasurementRefs.getOrPut(measurementName);
        measurementRef.setObjectsName(inputObjectsName);
        returnedRefs.add(measurementRef);

        measurementName = getFullName(inputImageName, Measurements.MIN_LOCATION_PX);
        measurementRef = objectMeasurementRefs.getOrPut(measurementName);
        measurementRef.setObjectsName(inputObjectsName);
        returnedRefs.add(measurementRef);

        measurementName = getFullName(inputImageName, Measurements.MAX_VALUE);
        measurementRef = objectMeasurementRefs.getOrPut(measurementName);
        measurementRef.setObjectsName(inputObjectsName);
        returnedRefs.add(measurementRef);

        measurementName = getFullName(inputImageName, Measurements.MIN_VALUE);
        measurementRef = objectMeasurementRefs.getOrPut(measurementName);
        measurementRef.setObjectsName(inputObjectsName);
        returnedRefs.add(measurementRef);

        return returnedRefs;

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

    protected void addParameterDescriptions() {
        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple input objects simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");
    }
}
