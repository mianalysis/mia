// TODO: Could output plot of K-function as image

package io.github.mianalysis.mia.module.images.measure;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageCalculator;
import io.github.mianalysis.mia.module.images.process.binary.BinaryOperations2D;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.module.objects.measure.intensity.MeasureIntensityAlongPath;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 12/05/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureGreyscaleKFunction extends AbstractSaver {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String USE_MASK = "Use mask";
    public static final String MASK_IMAGE = "Mask image";

    public static final String FUNCTION_SEPARATOR = "K-function controls";
    public static final String MINIMUM_RADIUS_PX = "Minimum radius (px)";
    public static final String MAXIMUM_RADIUS_PX = "Maximum radius (px)";
    public static final String RADIUS_INCREMENT = "Radius increment (px)";

    public MeasureGreyscaleKFunction(Modules modules) {
        super("Measure greyscale K-function", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_MEASURE;
    }

    @Override
    public String getDescription() {
        return "Measure's Ripley's K-function for greyscale images.  This method is re-written from the publication \"Extending Ripley’s K-Function to Quantify Aggregation in 2-D Grayscale Images\" by M. Amgad, et al. (doi: 10.1371/journal.pone.0144404).  Results are output to an Excel spreadsheet, with one file per input image.";
    }

    public static double[] calculateGSKfunction(Image image, int radius, @Nullable Image maskImage) {
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

        ImagePlus ipl = image.getImagePlus();
        ImageProcessor ipr = ipl.getProcessor();
        int w = ipr.getWidth();
        int h = ipr.getHeight();

        ImageProcessor maskIpr = maskImage == null ? null : maskImage.getImagePlus().getProcessor();

        double imArea = 0;
        double imSum = 0;
        double aggSum = 0;

        // Creating aggregation map
        for (int xx = 0; xx < w; xx++) {
            for (int yy = 0; yy < h; yy++) {
                // Only calculate this value if the central value isn't 0
                if (maskIpr != null && maskIpr.getValue(xx, yy) == 0)
                    continue;

                imSum = imSum + ipr.get(xx, yy);
                imArea++;

                // Calculating edge correction
                int b = 0;
                float sum = 0;
                for (int i = 0; i < x.size(); i++) {
                    int xxx = xx + x.get(i);
                    int yyy = yy + y.get(i);

                    // Ignore points outside the image
                    if (xxx < 0 || xxx >= w || yyy < 0 || yyy >= h)
                        continue;

                    // Only count points inside the mask
                    if (maskIpr != null && maskIpr.getValue(xxx, yyy) == 0)
                        continue;

                    b++;

                    // Don't count the central pixel itself
                    if (x.get(i) == 0 && y.get(i) == 0)
                        continue;

                    sum = sum + ipr.getf(xxx, yyy);

                }

                double edgeCorrection = ((double) b) / (Math.PI * radius * radius);
                float val = ipr.getf(xx, yy);
                aggSum = aggSum + (val * (val - 1) + val * sum) / edgeCorrection;

            }
        }

        // Calculating K
        double K = aggSum * (imArea / (imSum * (imSum - 1)));

        // Calculating normalised and centred K
        double imPerimeter = 2 * (w + h);
        if (maskIpr != null) {
            ImagePlus edgeIpl = new ImagePlus("Edge", maskIpr.duplicate());
            ImagePlus erodeIpl = edgeIpl.duplicate();
            BinaryOperations2D.process(erodeIpl, BinaryOperations2D.OperationModes.ERODE, 1, 1, true);
            ImageCalculator.process(edgeIpl, erodeIpl, ImageCalculator.CalculationMethods.SUBTRACT,
                    ImageCalculator.OverwriteModes.OVERWRITE_IMAGE1, null, false, false);

            ImageProcessor edgeIpr = edgeIpl.getProcessor();

            imPerimeter = 0;
            for (int xx = 0; xx < w; xx++)
                for (int yy = 0; yy < h; yy++)
                    imPerimeter = edgeIpr.getValue(xx, yy) != 0 ? imPerimeter + 1 : imPerimeter;

        }

        double betaR = Double.valueOf(Math.PI * radius * radius / imArea).floatValue();
        double gammaR = ((double) imPerimeter) * radius / ((double) imArea);
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
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        boolean useMask = parameters.getValue(USE_MASK, workspace);
        String maskImageName = parameters.getValue(MASK_IMAGE, workspace);
        int minRadius = parameters.getValue(MINIMUM_RADIUS_PX, workspace);
        int maxRadius = parameters.getValue(MAXIMUM_RADIUS_PX, workspace);
        int radiusInc = parameters.getValue(RADIUS_INCREMENT, workspace);

        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX, workspace);

        SXSSFWorkbook workbook = initialiseWorkbook();
        SXSSFSheet sheet = workbook.getSheetAt(0);

        Image inputImage = workspace.getImage(inputImageName);
        Image maskImage = useMask ? workspace.getImage(maskImageName) : null;
        ImagePlus inputIpl = inputImage.getImagePlus();

        int rowI = 1;
        int count = 0;
        int nRadii = Math.floorDiv(maxRadius - minRadius, radiusInc);
        int total = inputIpl.getNFrames() * inputIpl.getNSlices() * nRadii;
        for (int t = 0; t < inputIpl.getNFrames(); t++) {
            for (int z = 0; z < inputIpl.getNSlices(); z++) {
                Image currImage = ExtractSubstack.extractSubstack(inputImage, "TimepointImage", "1-end",
                        String.valueOf(z) + 1, String.valueOf(t) + 1);
                Image currMask = useMask ? ExtractSubstack.extractSubstack(maskImage, "TimepointMask", "1-end",
                        String.valueOf(z) + 1, String.valueOf(t) + 1) : null;

                for (int r = minRadius; r <= maxRadius; r = r + radiusInc) {
                    double[] kRes = calculateGSKfunction(currImage, r, currMask);

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

                    writeProgressStatus(++count, total, "steps");
                }
            }
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
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(USE_MASK, this, false));
        parameters.add(new InputImageP(MASK_IMAGE, this));

        parameters.add(new SeparatorP(FUNCTION_SEPARATOR, this));
        parameters.add(new IntegerP(MINIMUM_RADIUS_PX, this, 3));
        parameters.add(new IntegerP(MAXIMUM_RADIUS_PX, this, 15));
        parameters.add(new IntegerP(RADIUS_INCREMENT, this, 1));

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

        returnedParameters.addAll(super.updateAndGetParameters());

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