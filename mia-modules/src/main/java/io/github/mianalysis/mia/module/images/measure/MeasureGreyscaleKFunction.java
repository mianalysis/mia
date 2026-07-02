// TODO: Could output plot of K-function as image

package io.github.mianalysis.mia.module.images.measure;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageMath;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.module.objects.measure.intensity.MeasureIntensityAlongPath;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.math.Indexer;

/**
 * Created by sc13967 on 12/05/2017.
 */

/**
 * Measure's Ripley's K-function for greyscale images. This method is re-written
 * from the publication "Extending Ripley’s K-Function to Quantify Aggregation
 * in 2-D Grayscale Images" by M. Amgad, et al. (doi:
 * 10.1371/journal.pone.0144404). Results are output to an Excel spreadsheet,
 * with one file per input image.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureGreyscaleKFunction extends AbstractSaver {

  /**
  * 
  */
  public static final String INPUT_SEPARATOR = "Image input/output";

  /**
  * 
  */
  public static final String INPUT_IMAGE = "Input image";

  /**
  * 
  */
  public static final String USE_MASK = "Use mask";

  /**
  * 
  */
  public static final String MASK_IMAGE = "Mask image";

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

  public interface Measurements {
    String MAX_LOCATION_PX = "GREYSCALE_K_FUNCTION // L(r)-r // MAX_LOCATION_(PX)";
    String MIN_LOCATION_PX = "GREYSCALE_K_FUNCTION // L(r)-r // MIN_LOCATION_(PX)";
    String MAX_VALUE = "GREYSCALE_K_FUNCTION // L(r)-r // MAX_VALUE";
    String MIN_VALUE = "GREYSCALE_K_FUNCTION // L(r)-r // MIN_VALUE";
  }

  public MeasureGreyscaleKFunction(Modules modules) {
    super("Measure greyscale K-function", modules);
  }

  @Override
  public Category getCategory() {
    return Categories.IMAGES_MEASURE;
  }

  @Override
  public String getVersionNumber() {
    return "2.0.0";
  }

  @Override
  public String getDescription() {
    return "Measure's Ripley's K-function for greyscale images.  This method is re-written from the publication \"Extending Ripley’s K-Function to Quantify Aggregation in 2-D Grayscale Images\" by M. Amgad, et al. (doi: 10.1371/journal.pone.0144404).  Results are output to an Excel spreadsheet, with one file per input image.";
  }

  public static double calculateGSKFunction(Image image, int r, Image maskImage) {
    if (r <= 0)
      return Double.NaN;

    ArrayList<Integer> x_arr = new ArrayList<>();
    ArrayList<Integer> y_arr = new ArrayList<>();

    for (int x = -r; x <= r; x++)
      for (int y = -r; y <= r; y++)
        if (Math.sqrt(x * x + y * y) <= r) {
          x_arr.add(x);
          y_arr.add(y);
        }

    Integer[] xx = (Integer[]) x_arr.toArray(new Integer[x_arr.size()]);
    Integer[] yy = (Integer[]) y_arr.toArray(new Integer[y_arr.size()]);

    ImagePlus ipl = image.getImagePlus();
    ImageProcessor ipr = ipl.getProcessor();
    int imWidth = ipr.getWidth();
    int imHeight = ipr.getHeight();

    ImageProcessor maskIpr = maskImage.getImagePlus().getProcessor().duplicate();
    maskIpr.threshold(0);
    maskIpr.multiply(1d / 255d);

    double imArea = 0;
    double imSumInt = 0;
    for (int x = 0; x < ipl.getWidth(); x++) {
      for (int y = 0; y < ipl.getHeight(); y++) {
        // Only count points inside the mask
        if (maskIpr.getValue(x, y) == 0)
          continue;

        imArea++;
        imSumInt = imSumInt + ipr.get(x, y);

      }
    }

    int cc = 0;
    double perPixelAcc = 0;
    for (int x = 0; x < ipl.getWidth(); x++) {
      for (int y = 0; y < ipl.getHeight(); y++) {
        double currValidArea = 0;
        double currSum = 0;

        for (int i = 0; i < xx.length; i++) {
          int xxx = xx[i] + x;
          int yyy = yy[i] + y;

          if (xxx < 0 || xxx >= imWidth || yyy < 0 || yyy >= imHeight)
            continue;

          // Only count points inside the mask
          if (maskIpr.getValue(xxx, yyy) == 0)
            continue;

          currValidArea++;

          // Don't do the indicator function if referring to self
          if (xxx == x && yyy == y)
            continue;

          currSum = currSum + ipr.getValue(xxx, yyy);

        }

        if (currValidArea > 0)
          perPixelAcc = perPixelAcc
              + ((double) r * r * Math.PI / (double) currValidArea) * ipr.get(x, y) * (ipr.get(x, y) - 1 + currSum);

      }
    }

    return (imArea / (imSumInt * (imSumInt - 1))) * perPixelAcc;

  }

  public static double getLValue(double K) {
    return Math.sqrt(K / Math.PI);
  }

  public static double getL_rValue(double K, double r) {
    return Math.sqrt(K / Math.PI) - r;
  }

  SXSSFWorkbook initialiseWorkbook(boolean calculatePercentileInterval, double percentileLow, double percentileHigh) {
    SXSSFWorkbook workbook = new SXSSFWorkbook();
    SXSSFSheet sheet = workbook.createSheet();

    int rowI = 0;
    int colI = 0;

    Row row = sheet.createRow(rowI++);
    Cell cell = row.createCell(colI++);
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

    return workbook;

  }

  public static Image randomiseImage(Image inputImage) {
    ImagePlus inputIpl = inputImage.getImagePlus();
    ImageProcessor inputIpr = inputIpl.getProcessor();

    TreeMap<Integer, Integer> idxs = new TreeMap<>();
    Random random = new Random();
    for (int i = 0; i < inputIpl.getWidth() * inputIpl.getHeight(); i++)
      idxs.put(random.nextInt(), i);
    Integer[] randomisedIdxs = idxs.values().toArray(new Integer[idxs.size()]);

    Image outputImage = inputImage.duplicate("Randomised");
    ImageMath.process(outputImage, ImageMath.CalculationModes.MULTIPLY, 0);
    ImagePlus outputIpl = outputImage.getImagePlus();
    ImageProcessor outputIpr = outputIpl.getProcessor();

    Indexer indexer = new Indexer(inputIpl.getWidth(), inputIpl.getHeight());
    for (int i = 0; i < randomisedIdxs.length; i++) {
      int[] coordIn = indexer.getCoord(i);
      int[] coordOut = indexer.getCoord(randomisedIdxs[i]);
      outputIpr.putPixel(coordOut[0], coordOut[1], inputIpr.get(coordIn[0], coordIn[1]));
    }

    return outputImage;

  }

  @Override
  public Status process(Workspace workspace) {
    String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
    boolean useMask = parameters.getValue(USE_MASK, workspace);
    String maskImageName = parameters.getValue(MASK_IMAGE, workspace);
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

    SXSSFWorkbook workbook = initialiseWorkbook(calculatePercentileInterval, percentileLow, percentileHigh);
    SXSSFSheet sheet = workbook.getSheetAt(0);

    Image inputImage = workspace.getImage(inputImageName);
    Image maskImage = useMask ? workspace.getImage(maskImageName) : null;
    ImagePlus inputIpl = inputImage.getImagePlus();

    double maxL_r = -Double.MAX_VALUE;
    double maxL_rLoc = -1;
    double minL_r = Double.MAX_VALUE;
    double minL_rLoc = -1;

    int rowI = 1;
    int count = 0;
    int nRadii = Math.floorDiv(maxRadius - minRadius, radiusInc);
    int total = inputIpl.getNFrames() * inputIpl.getNSlices() * nRadii;

    if (calculatePercentileInterval)
      total = total + nRadii * nSimulations;

    for (int t = 0; t < inputIpl.getNFrames(); t++) {
      for (int z = 0; z < inputIpl.getNSlices(); z++) {
        Image currImage = ExtractSubstack.extractSubstack(inputImage, "TimepointImage", "1-end",
            String.valueOf(z + 1), String.valueOf(t + 1));
        Image currMask = null;
        if (useMask) {
          currMask = ExtractSubstack.extractSubstack(maskImage, "TimepointMask", "1-end",
              String.valueOf(z) + 1, String.valueOf(t) + 1);
        } else {
          currMask = inputImage.duplicate("Mask");
          ImageMath.process(currMask, ImageMath.CalculationModes.ADD, 1);
        }

        for (int r = minRadius; r < maxRadius; r = r + radiusInc) {
          double K = calculateGSKFunction(currImage, r, currMask);

          int colI = 0;
          Row row = sheet.createRow(rowI++);

          Cell cell = row.createCell(colI++);
          cell.setCellValue(t);

          cell = row.createCell(colI++);
          cell.setCellValue(z);

          cell = row.createCell(colI++);
          cell.setCellValue(r);

          cell = row.createCell(colI++);
          cell.setCellValue(K);

          cell = row.createCell(colI++);
          cell.setCellValue(getLValue(K));

          cell = row.createCell(colI++);
          cell.setCellValue(getL_rValue(K, r));

          // Calculate percentile intervals
          if (calculatePercentileInterval) {
            double[] pcResults = new double[nSimulations];

            for (int i = 0; i < nSimulations; i++) {
              Image randomisedImage = randomiseImage(currImage);
              double kPC = calculateGSKFunction(randomisedImage, r, currMask);
              pcResults[i] = kPC;

              writeProgressStatus(++count, total, "steps");

            }

            Percentile percentile = new Percentile();
            double kPercentileLow = percentile.evaluate(pcResults, percentileLow);
            double kPercentileHigh = percentile.evaluate(pcResults, percentileHigh);

            cell = row.createCell(colI++);
            cell.setCellValue(kPercentileLow);

            cell = row.createCell(colI++);
            cell.setCellValue(kPercentileHigh);

            cell = row.createCell(colI++);
            cell.setCellValue(getLValue(kPercentileLow));

            cell = row.createCell(colI++);
            cell.setCellValue(getLValue(kPercentileHigh));

            cell = row.createCell(colI++);
            cell.setCellValue(getL_rValue(kPercentileLow, r));

            cell = row.createCell(colI++);
            cell.setCellValue(getL_rValue(kPercentileHigh, r));

          } else {
            ++count;
          }

          // Updating measurement values
          double L_r = getL_rValue(K, r);
          if (L_r > maxL_r) {
            maxL_r = L_r;
            maxL_rLoc = r;
          }

          if (L_r < minL_r) {
            minL_r = L_r;
            minL_rLoc = r;
          }

          writeProgressStatus(count, total, "steps");

        }
      }
    }

    // Adding measurements
    inputImage.addMeasurement(new Measurement(Measurements.MAX_LOCATION_PX, maxL_rLoc));
    inputImage.addMeasurement(new Measurement(Measurements.MIN_LOCATION_PX, minL_rLoc));
    inputImage.addMeasurement(new Measurement(Measurements.MAX_VALUE, maxL_r));
    inputImage.addMeasurement(new Measurement(Measurements.MIN_VALUE, minL_r));

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
    parameters.add(new InputImageP(INPUT_IMAGE, this));
    parameters.add(new BooleanP(USE_MASK, this, false));
    parameters.add(new InputImageP(MASK_IMAGE, this));

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

  }

  @Override
  public Parameters updateAndGetParameters() {
    Workspace workspace = null;
    Parameters returnedParameters = new Parameters();

    returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
    returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
    returnedParameters.add(parameters.getParameter(USE_MASK));
    if ((boolean) parameters.getValue(USE_MASK, workspace))
      returnedParameters.add(parameters.getParameter(MASK_IMAGE));

    returnedParameters.add(parameters.getParameter(FUNCTION_SEPARATOR));
    returnedParameters.add(parameters.getParameter(MINIMUM_RADIUS_PX));
    returnedParameters.add(parameters.getParameter(MAXIMUM_RADIUS_PX));
    returnedParameters.add(parameters.getParameter(RADIUS_INCREMENT));

    returnedParameters.add(parameters.getParameter(PERCENTILE_INTERVAL_SEPARATOR));
    returnedParameters.add(parameters.getParameter(CALCULATE_PERCENTILE_INTERVAL));
    if ((Boolean) parameters.getValue(CALCULATE_PERCENTILE_INTERVAL, workspace)) {
      returnedParameters.add(parameters.getParameter(PERCENTILE_INTERVAL));
      returnedParameters.add(parameters.getParameter(NUMBER_OF_SIMULATIONS));
      returnedParameters.add(parameters.getParameter(PERCENTILE_INTERVAL_WARNING));
    }

    returnedParameters.addAll(super.updateAndGetParameters());

    return returnedParameters;

  }

  @Override
  public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
    ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

    String inputImageName = parameters.getValue(INPUT_IMAGE, null);

    ImageMeasurementRef measurementRef = imageMeasurementRefs.getOrPut(Measurements.MAX_LOCATION_PX);
    measurementRef.setImageName(inputImageName);
    returnedRefs.add(measurementRef);

    measurementRef = imageMeasurementRefs.getOrPut(Measurements.MIN_LOCATION_PX);
    measurementRef.setImageName(inputImageName);
    returnedRefs.add(measurementRef);

    measurementRef = imageMeasurementRefs.getOrPut(Measurements.MAX_VALUE);
    measurementRef.setImageName(inputImageName);
    returnedRefs.add(measurementRef);

    measurementRef = imageMeasurementRefs.getOrPut(Measurements.MIN_VALUE);
    measurementRef.setImageName(inputImageName);
    returnedRefs.add(measurementRef);

    return returnedRefs;

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

// the cat ate the milk
// mummy is the best
