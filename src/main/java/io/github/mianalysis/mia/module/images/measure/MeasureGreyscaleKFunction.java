// TODO: Could output plot of K-function as image

package io.github.mianalysis.mia.module.images.measure;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageCalculator;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.module.images.process.binary.BinaryOperations2D;
import io.github.mianalysis.mia.module.images.process.threshold.ManualThreshold;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.inputoutput.ImageSaver;
import io.github.mianalysis.mia.module.objects.measure.intensity.MeasureIntensityAlongPath;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;

/**
 * Created by sc13967 on 12/05/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureGreyscaleKFunction extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

    public static final String FUNCTION_SEPARATOR = "K-function controls";
    public static final String MINIMUM_RADIUS_PX = "Minimum radius (px)";
    public static final String MAXIMUM_RADIUS_PX = "Maximum radius (px)";
    public static final String RADIUS_INCREMENT = "Radius increment (px)";
    public static final String IGNORE_ZEROS = "Ignore zero values";

    public static final String FILE_SAVING_SEPARATOR = "File saving controls";
    public static final String SAVE_NAME_MODE = "Save name mode";
    public static final String SAVE_FILE_NAME = "File name";
    public static final String APPEND_SERIES_MODE = "Append series mode";
    public static final String APPEND_DATETIME_MODE = "Append date/time mode";
    public static final String SAVE_SUFFIX = "Add filename suffix";

    public interface SaveNameModes extends ImageSaver.SaveNameModes {
    }

    public interface AppendSeriesModes extends ImageSaver.AppendSeriesModes {
    }

    public interface AppendDateTimeModes extends ImageSaver.AppendDateTimeModes {
    }

    public MeasureGreyscaleKFunction(Modules modules) {
        super("Measure greyscale K-function", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_MEASURE;
    }

    @Override
    public String getDescription() {
        return "Measure's Ripley's K-function for greyscale images.  This method is re-written from the publication \"Extending Ripley’s K-Function to Quantify Aggregation in 2-D Grayscale Images\" by M. Amgad, et al. (doi: 10.1371/journal.pone.0144404).";

    }

    public static void main(String[] args) {
        Prefs.blackBackground = true;
        ImagePlus ipl = IJ.openImage(
                "C:\\Users\\steph\\Documents\\People\\Georgie McDonald\\2022-05-26 ALP scale segmentation\\Test Ripley\\TEST_crop_ROI.tif");
        Image image = new Image("Test", ipl);

        MeasureGreyscaleKFunction.calculateGSKfunction(image, 5, true);

    }

    public static double[] calculateGSKfunction(Image image, int radius, boolean ignoreZeros) {
        if (radius <= 0)
            return null;

        int dia = (int) Math.ceil(2 * radius);
        if (dia % 2 == 0)
            dia++;

        int r = (int) Math.floor(dia / 2);

        ArrayList<Integer> x = new ArrayList<>();
        ArrayList<Integer> y = new ArrayList<>();

        for (int xx = -r; xx <= r; xx++)
            for (int yy = -r; yy <= r; yy++)
                if (Math.sqrt(xx * xx + yy * yy) <= radius) {
                    x.add(xx);
                    y.add(yy);
                }

        // Padding image with NaN
        ImagePlus ipl = image.getImagePlus();
        ImageProcessor ipr = ipl.getProcessor();
        int w = ipr.getWidth();
        int h = ipr.getHeight();

        double imArea = 0;
        double imSum = 0;
        double aggSum = 0;

        // Creating aggregation map
        for (int xx = 0; xx < w; xx++) {
            for (int yy = 0; yy < h; yy++) {
                imSum = imSum + ipr.get(xx, yy);

                // Only calculate this value if the central value isn't NaN or 0
                float val = ipr.getf(xx, yy);
                if (Float.isNaN(val) || val == 0)
                    continue;

                imArea = imArea + 1;

                // Calculating edge correction
                int b = 0;
                float sum = 0;
                for (int i = 0; i < x.size(); i++) {
                    int xxx = xx + x.get(i);
                    int yyy = yy + y.get(i);
                    if (xxx < 0 || xxx >= w || yyy < 0 || yyy >= h)
                        continue;

                    float bVal = ipr.getf(xxx, yyy);
                    if (!Float.isNaN(bVal) && (bVal != 0 || !ignoreZeros))
                        b++;

                    if (x.get(i) == 0 && y.get(i) == 0)
                        continue;

                    float newVal = ipr.getf(xxx, yyy);
                    sum = Float.isNaN(newVal) ? sum : sum + newVal;

                }
                double edgeCorrection = b / (Math.PI * radius * radius);

                aggSum = aggSum + (val * (val - 1) + val * sum) / edgeCorrection;

            }
        }

        // Calculating K
        double K = aggSum * (imArea / (imSum * (imSum - 1)));

        // Calculating normalised and centred K
        double imPerimeter = 2 * (w + h);
        if (ignoreZeros) {
            ImagePlus edgeIpl = new ImagePlus("Edge",ipr.duplicate());
            ManualThreshold.applyThreshold(edgeIpl, 0);
            ImagePlus dilateIpl = edgeIpl.duplicate();
            BinaryOperations2D.process(dilateIpl, BinaryOperations2D.OperationModes.ERODE, 1, 1, true);
            ImageCalculator.process(edgeIpl, dilateIpl, ImageCalculator.CalculationMethods.SUBTRACT, ImageCalculator.OverwriteModes.OVERWRITE_IMAGE1, null, false, false);
            
            ImageProcessor edgeIpr = edgeIpl.getProcessor();

            imPerimeter = 0;
            for (int xx = 0; xx < w; xx++)
                for (int yy = 0; yy < h; yy++)
                    imPerimeter = edgeIpr.getValue(xx, yy) == 255 ? imPerimeter + 1 : imPerimeter;
                    
        }

        double betaR = Double.valueOf(Math.PI * radius * radius / imArea).floatValue();
        double gammaR = imPerimeter * radius / imArea;
        double varK = ((2 * Math.pow(imArea, 2) * betaR) / (imSum * imSum))
                * (1 + 0.305 * gammaR + betaR * (-1 + 0.0132 * imSum * gammaR));
        double normCentK = (K - Math.PI * radius * radius) / Math.sqrt(varK);

        // Estimating critical quantiles
        double skewK = ((4 * Math.pow(imArea, 3) * betaR) / (Math.pow(imSum, 4) * Math.pow(varK, 3.0 / 2)))
                * (1 + 0.76 * gammaR + imSum * betaR * (1.173 + 0.414 * gammaR)
                        + imSum * betaR * betaR * (-2 + 0.012 * imSum * gammaR));
        double kurtK = ((Math.pow(imArea, 4) * betaR) / (Math.pow(imSum, 6) * (varK * varK)))
                * (8 + 11.52 * gammaR
                        + imSum * betaR
                                * ((104.3 + 12 * imSum) + (78.7 + 7.32 * imSum) * gammaR
                                        + 1.116 * imSum * gammaR * gammaR)
                        + imSum * betaR * betaR
                                * ((-304.3 - 1.92 * imSum) + (-97.9 + 2.69 * imSum + 0.317 * imSum * imSum) * gammaR
                                        + 0.0966 * imSum * imSum * gammaR * gammaR)
                        + imSum * imSum * Math.pow(betaR, 3) * (-36 + 0.0021 * imSum * imSum * gammaR * gammaR));

        NormalDistribution distribution = new NormalDistribution(0, 1);
        double z01 = distribution.inverseCumulativeProbability(0.01);
        double z99 = distribution.inverseCumulativeProbability(0.99);

        double Q01 = z01 + (1d / 6) * (z01 * z01 - 1) * skewK
                + (1d / 24) * ((z01 * z01 * z01) - 3 * z01) * (kurtK - 3)
                - (1d / 36) * (2 * (z01 * z01 * z01) - 5 * z01) * skewK * skewK;
        double Q99 = z99 + (1d / 6) * (z99 * z99 - 1) * skewK
                + (1d / 24) * ((z99 * z99 * z99) - 3 * z99) * (kurtK - 3)
                - (1d / 36) * (2 * (z99 * z99 * z99) - 5 * z99) * skewK * skewK;

        return new double[] { normCentK, Q01, Q99 };

    }

    SXSSFWorkbook initialiseWorkbook() {
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
        cell.setCellValue("K-corr");

        cell = row.createCell(colI++);
        cell.setCellValue("Q01");

        cell = row.createCell(colI++);
        cell.setCellValue("Q99");

        return workbook;

    }

    @Override
    public Status process(Workspace workspace) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        int minRadius = parameters.getValue(MINIMUM_RADIUS_PX);
        int maxRadius = parameters.getValue(MAXIMUM_RADIUS_PX);
        int radiusInc = parameters.getValue(RADIUS_INCREMENT);
        boolean ignoreZeros = parameters.getValue(IGNORE_ZEROS);

        String saveNameMode = parameters.getValue(SAVE_NAME_MODE);
        String saveFileName = parameters.getValue(SAVE_FILE_NAME);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE);
        String suffix = parameters.getValue(SAVE_SUFFIX);

        SXSSFWorkbook workbook = initialiseWorkbook();
        SXSSFSheet sheet = workbook.getSheetAt(0);

        Image inputImage = workspace.getImage(inputImageName);

        int rowI = 1;
        for (int t = 0; t < inputImage.getImagePlus().getNFrames(); t++) {
            for (int z = 0; z < inputImage.getImagePlus().getNSlices(); z++) {
                Image currImage = ExtractSubstack.extractSubstack(inputImage, "Timepoint", "1-end",
                        String.valueOf(z) + 1, String.valueOf(t) + 1);
                for (int r = minRadius; r <= maxRadius; r = r + radiusInc) {
                    double[] kRes = calculateGSKfunction(currImage, r, ignoreZeros);

                    int colI = 0;
                    Row row = sheet.createRow(rowI++);

                    Cell cell = row.createCell(colI++);
                    cell.setCellValue(t);

                    cell = row.createCell(colI++);
                    cell.setCellValue(z);

                    cell = row.createCell(colI++);
                    cell.setCellValue(r);

                    cell = row.createCell(colI++);
                    cell.setCellValue(kRes[0]);

                    cell = row.createCell(colI++);
                    cell.setCellValue(kRes[1]);

                    cell = row.createCell(colI++);
                    cell.setCellValue(kRes[2]);

                }
            }
        }

        File rootFile = workspace.getMetadata().getFile();
        String path = rootFile.getParent() + File.separator;

        String name;
        switch (saveNameMode) {
            case SaveNameModes.MATCH_INPUT:
            default:
                name = FilenameUtils.removeExtension(rootFile.getName());
                break;

            case SaveNameModes.SPECIFIC_NAME:
                name = FilenameUtils.removeExtension(saveFileName);
                break;
        }

        // Adding last bits to name
        path = path + name;
        path = ImageSaver.appendSeries(path, workspace, appendSeriesMode);
        path = ImageSaver.appendDateTime(path, appendDateTimeMode);
        path = path + suffix + ".xlsx";

        MeasureIntensityAlongPath.writeDistancesFile(workbook, path);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(FUNCTION_SEPARATOR, this));
        parameters.add(new IntegerP(MINIMUM_RADIUS_PX, this, 3));
        parameters.add(new IntegerP(MAXIMUM_RADIUS_PX, this, 15));
        parameters.add(new IntegerP(RADIUS_INCREMENT, this, 1));
        parameters.add(new BooleanP(IGNORE_ZEROS, this, false));

        parameters.add(new SeparatorP(FILE_SAVING_SEPARATOR, this));
        parameters.add(new ChoiceP(SAVE_NAME_MODE, this, SaveNameModes.MATCH_INPUT, SaveNameModes.ALL));
        parameters.add(new StringP(SAVE_FILE_NAME, this));
        parameters.add(new ChoiceP(APPEND_SERIES_MODE, this, AppendSeriesModes.SERIES_NUMBER, AppendSeriesModes.ALL));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE, this, AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));
        parameters.add(new StringP(SAVE_SUFFIX, this));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(FUNCTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MINIMUM_RADIUS_PX));
        returnedParameters.add(parameters.getParameter(MAXIMUM_RADIUS_PX));
        returnedParameters.add(parameters.getParameter(RADIUS_INCREMENT));
        returnedParameters.add(parameters.getParameter(IGNORE_ZEROS));

        returnedParameters.add(parameters.getParameter(FILE_SAVING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SAVE_NAME_MODE));
        switch ((String) parameters.getValue(SAVE_NAME_MODE)) {
            case SaveNameModes.SPECIFIC_NAME:
                returnedParameters.add(parameters.getParameter(SAVE_FILE_NAME));
                break;
        }

        returnedParameters.add(parameters.getParameter(APPEND_SERIES_MODE));
        returnedParameters.add(parameters.getParameter(APPEND_DATETIME_MODE));
        returnedParameters.add(parameters.getParameter(SAVE_SUFFIX));

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