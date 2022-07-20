// TODO: Could addRef an optional parameter to select the channel of the input image to use for measurement

package io.github.mianalysis.mia.module.objects.measure.intensity;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.measure.MeasureGreyscaleKFunction;
import io.github.mianalysis.mia.module.images.transform.CropImage;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.inputoutput.ImageSaver;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
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
 * Created by sc13967 on 08/07/2022.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureObjectGreyscaleKFunction extends Module {
    public static final String INPUT_SEPARATOR = "Object and image input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";

    public static final String FUNCTION_SEPARATOR = "K-function controls";
    public static final String MINIMUM_RADIUS_PX = "Minimum radius (px)";
    public static final String MAXIMUM_RADIUS_PX = "Maximum radius (px)";
    public static final String RADIUS_INCREMENT = "Radius increment (px)";

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

    public MeasureObjectGreyscaleKFunction(Modules modules) {
        super("Measure object greyscale K-function", modules);
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
        cell.setCellValue("Object ID");

        cell = row.createCell(colI++);
        cell.setCellValue("K-corr");

        cell = row.createCell(colI++);
        cell.setCellValue("Q01");

        cell = row.createCell(colI++);
        cell.setCellValue("Q99");

        return workbook;

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_INTENSITY;
    }

    @Override
    public String getDescription() {
        return "Measure's Ripley's K-function for greyscale images on an object-by-object basis.  This method is re-written from the publication \"Extending Ripley’s K-Function to Quantify Aggregation in 2-D Grayscale Images\" by M. Amgad, et al. (doi: 10.1371/journal.pone.0144404).  Results are output to an Excel spreadsheet, with one file per input image.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String objectName = parameters.getValue(INPUT_OBJECTS);
        Objs objects = workspace.getObjects().get(objectName);

        int minRadius = parameters.getValue(MINIMUM_RADIUS_PX);
        int maxRadius = parameters.getValue(MAXIMUM_RADIUS_PX);
        int radiusInc = parameters.getValue(RADIUS_INCREMENT);

        String saveNameMode = parameters.getValue(SAVE_NAME_MODE);
        String saveFileName = parameters.getValue(SAVE_FILE_NAME);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE);
        String suffix = parameters.getValue(SAVE_SUFFIX);

        SXSSFWorkbook workbook = initialiseWorkbook();
        SXSSFSheet sheet = workbook.getSheetAt(0);

        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputIpl = inputImage.getImagePlus();

        int rowI = 1;
        int count = 0;
        int total = objects.size();
        for (Obj object : objects.values()) {
            int t = object.getT();

            // Getting images cropped to this object
            double[][] extents = object.getExtents(true, false);
            int top = (int) Math.round(extents[1][0]);
            int left = (int) Math.round(extents[0][0]);
            int width = (int) Math.round(extents[0][1]-left)+1;
            int height = (int) Math.round(extents[1][1]-top)+1;
            Image cropImage = CropImage.cropImage(inputImage, "Crop", top, left, width, height);

            // Cropping image in Z
            int minZ = (int) Math.round(extents[2][0]);
            int maxZ = (int) Math.round(extents[2][1]);           
            Image subsImage = ExtractSubstack.extractSubstack(cropImage, "Substack", "1", (minZ+1)+"-"+(maxZ+1), "1");

            // Getting 
            Image maskImage = object.getAsTightImage("Mask");

            for (int z = 0; z < maskImage.getImagePlus().getNSlices(); z++) {
                Image currImage = ExtractSubstack.extractSubstack(subsImage, "TimepointImage", "1",
                        String.valueOf(z + 1), String.valueOf(t + 1));
                Image currMask = ExtractSubstack.extractSubstack(maskImage, "TimepointMask", "1",
                        String.valueOf(z + 1), String.valueOf(t + 1));

                for (int r = minRadius; r <= maxRadius; r = r + radiusInc) {
                    double[] kRes = MeasureGreyscaleKFunction.calculateGSKfunction(currImage, r, currMask);

                    int colI = 0;
                    Row row = sheet.createRow(rowI++);

                    Cell cell = row.createCell(colI++);
                    cell.setCellValue(t);

                    cell = row.createCell(colI++);
                    cell.setCellValue(z);

                    cell = row.createCell(colI++);
                    cell.setCellValue(r);

                    cell = row.createCell(colI++);
                    cell.setCellValue(object.getID());

                    cell = row.createCell(colI++);
                    cell.setCellValue(kRes[0]);

                    cell = row.createCell(colI++);
                    cell.setCellValue(kRes[1]);

                    cell = row.createCell(colI++);
                    cell.setCellValue(kRes[2]);

                    writeProgressStatus(++count, total, "steps");
                }
            }
            writeProgressStatus(++count, total, "objects");
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
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(FUNCTION_SEPARATOR, this));
        parameters.add(new IntegerP(MINIMUM_RADIUS_PX, this, 3));
        parameters.add(new IntegerP(MAXIMUM_RADIUS_PX, this, 15));
        parameters.add(new IntegerP(RADIUS_INCREMENT, this, 1));

        parameters.add(new SeparatorP(FILE_SAVING_SEPARATOR, this));
        parameters.add(new ChoiceP(SAVE_NAME_MODE, this, SaveNameModes.MATCH_INPUT, SaveNameModes.ALL));
        parameters.add(new StringP(SAVE_FILE_NAME, this));
        parameters.add(new ChoiceP(APPEND_SERIES_MODE, this, AppendSeriesModes.SERIES_NUMBER, AppendSeriesModes.ALL));
        parameters.add(new ChoiceP(APPEND_DATETIME_MODE, this, AppendDateTimeModes.NEVER, AppendDateTimeModes.ALL));
        parameters.add(new StringP(SAVE_SUFFIX, this));

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

    void addParameterDescriptions() {

    }
}
