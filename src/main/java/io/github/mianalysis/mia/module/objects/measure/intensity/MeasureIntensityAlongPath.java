package io.github.mianalysis.mia.module.objects.measure.intensity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.ImageSaver;
import io.github.mianalysis.mia.module.objects.measure.spatial.MeasureSkeleton;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.object.Point;
import io.github.sjcross.sjcommon.object.volume.PointOutOfRangeException;
import io.github.sjcross.sjcommon.object.volume.SpatCal;
import io.github.sjcross.sjcommon.object.volume.Volume;
import io.github.sjcross.sjcommon.object.volume.VolumeType;

/**
 * Created by sc13967 on 22/06/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureIntensityAlongPath extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String IMAGE_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String MEASURE_ANOTHER_IMAGE = "Measure another image";

    public static final String OUTPUT_SEPARATOR = "Data output";
    public static final String INCLUDE_CENTROIDS = "Include centroids";
    public static final String INCLUDE_TIMEPOINTS = "Include timepoints";

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

    public MeasureIntensityAlongPath(Modules modules) {
        super("Measure intensity along path", modules);
    }

    public static SXSSFWorkbook process(Objs objects, Image[] images, boolean includeCentroids, boolean includeTimepoints) {
        // Creating workbook
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet[] sheets = new Sheet[images.length];
        for (int i = 0; i < images.length; i++) {
            sheets[i] = workbook.createSheet(images[i].getName());
            addHeaderToSheet(sheets[i], includeCentroids, includeTimepoints);
        }

        for (Obj object : objects.values())
            for (int i = 0; i < images.length; i++)
                process(object, images[i], sheets[i], includeCentroids, includeTimepoints);

        return workbook;

    }

    public static void process(Obj object, Image image, Sheet sheet, boolean includeCentroids, boolean includeTimepoints) {
        if (object.size() < 2)
            return;

        // Ordering points
        LinkedHashSet<Point<Integer>> orderedPoints = new LinkedHashSet<>(
                MeasureSkeleton.getLargestShortestPath(object));

        LinkedHashMap<Double, Double> rawIntensities = measureIntensityProfile(orderedPoints, image, object.getT(),
                object.getSpatialCalibration());
        LinkedHashMap<Integer, Double> spacedIntensities = interpolateProfile(rawIntensities);

        addProfileToSheet(sheet, object, spacedIntensities, includeCentroids, includeTimepoints);

    }

    public static LinkedHashMap<Double, Double> measureIntensityProfile(Collection<Point<Integer>> points, Image image,
            int t, SpatCal spatCal) {
        ImagePlus ipl = image.getImagePlus();

        LinkedHashMap<Double, Double> profile = new LinkedHashMap<>();
        Point<Integer> prevPoint = null;
        double distance = 0;
        for (Point<Integer> point : points) {
            int x = point.getX();
            int y = point.getY();
            int z = point.getZ();

            ipl.setPosition(1, z + 1, t + 1);
            double intensity = ipl.getProcessor().getPixel(x, y);

            if (prevPoint != null) {
                Volume volume1 = new Volume(VolumeType.POINTLIST, spatCal.duplicate());
                Volume volume2 = new Volume(VolumeType.POINTLIST, spatCal.duplicate());

                try {
                    volume1.add(prevPoint.getX(), prevPoint.getY(), prevPoint.getZ());
                    volume2.add(point.getX(), point.getY(), point.getZ());
                } catch (PointOutOfRangeException e) {
                    MIA.log.writeError(e);
                }

                distance += volume1.getCentroidSeparation(volume2, true);
            }

            profile.put(distance, intensity);

            prevPoint = point;

        }

        return profile;

    }

    public static LinkedHashMap<Integer, Double> interpolateProfile(LinkedHashMap<Double, Double> profile) {
        LinkedHashMap<Integer, Double> interpolated = new LinkedHashMap<>();

        if (profile.size() == 0) {
            return interpolated;
        } else if (profile.size() == 1) {
            interpolated.put(0, profile.values().iterator().next());
            return interpolated;
        }

        // Converting to double arrays (here, x is parametric location along the profile
        // and y is the raw intensity at that point)
        double[] x = profile.keySet().stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = profile.values().stream().mapToDouble(Double::doubleValue).toArray();

        PolynomialSplineFunction spline = new LinearInterpolator().interpolate(x, y);

        int max = (int) Math.floor(x[x.length - 1]);
        for (int i = 0; i <= max; i++)
            interpolated.put(i, spline.value(i));

        return interpolated;

    }

    public static void addHeaderToSheet(Sheet sheet, boolean includeCentroids, boolean includeTimepoints) {
        int colCount = 0;
        Row row = sheet.createRow(0);
        Workbook workbook = sheet.getWorkbook();

        // Header cells will be bold
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        Cell cell = row.createCell(colCount++);
        cell.setCellValue("OBJECT_ID");
        cell.setCellStyle(cellStyle);

        if (includeCentroids) {
            cell = row.createCell(colCount++);
            cell.setCellValue("X_CENTROID_(PX)");
            cell.setCellStyle(cellStyle);

            cell = row.createCell(colCount++);
            cell.setCellValue("Y_CENTROID_(PX)");
            cell.setCellStyle(cellStyle);

            cell = row.createCell(colCount++);
            cell.setCellValue("Z_CENTROID_(SLICE)");
            cell.setCellStyle(cellStyle);

        }

        if (includeTimepoints) {
            cell = row.createCell(colCount++);
            cell.setCellValue("TIMEPOINT");
            cell.setCellStyle(cellStyle);
        }

        cell = row.createCell(colCount++);
        cell.setCellValue("PROFILE -->");
        cell.setCellStyle(cellStyle);

    }

    public static void addProfileToSheet(Sheet sheet, Obj object, LinkedHashMap<?, Double> profile, boolean includeCentroids,
            boolean includeTimepoints) {
        int colCount = 0;
        int rowCount = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowCount++);

        row.createCell(colCount++).setCellValue(object.getID());

        if (includeCentroids) {
            row.createCell(colCount++).setCellValue(object.getXMean(true));
            row.createCell(colCount++).setCellValue(object.getYMean(true));
            row.createCell(colCount++).setCellValue(object.getZMean(true,false));
        }

        if (includeTimepoints)
            row.createCell(colCount++).setCellValue(object.getID());

        for (double value : profile.values())
            row.createCell(colCount++).setCellValue(value);

    }

    public static void writeDistancesFile(SXSSFWorkbook workbook, String path) {
        // Writing the workbook to file
        try {
            FileOutputStream outputStream = new FileOutputStream(path);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            try {
                String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                String rootPath = FilenameUtils.removeExtension(path);
                String newOutPath = rootPath + "_(" + dateTime + ").xlsx";
                FileOutputStream outputStream = null;

                outputStream = new FileOutputStream(newOutPath);
                workbook.write(outputStream);
                workbook.close();
                outputStream.close();

                MIA.log.writeWarning("Target file (" + new File(path).getName() + ") inaccessible");
                MIA.log.writeWarning("Saved to alternative file (" + new File(newOutPath).getName() + ")");

            } catch (IOException e1) {
                MIA.log.writeError(e);
            }
        } catch (IOException e) {
            MIA.log.writeError(e);
        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_INTENSITY;
    }

    @Override
    public String getDescription() {
        return "Measures the intensity profile along the pixel-wide backbone of an object and outputs this profile to .xlsx file.  Input objects are skeletonised to single pixel-wide representations prior to measurement; however, pre-skeletonised objects can also be processed.<br><br>"

                + "Output results are stored in a multi-sheet .xlsx file, where each sheet includes the profile for a specific input image.  Each row of a sheet contains the profile for a single object.  Profiles are linearly-interpolated such that each measured position along a profile is 1px from the previous.<br><br>"

                + "Note: Objects must either form a single line (i.e. not contain multiple branches) or reduce to a single line during skeletonisation.  No profile will be recorded for any objects which fail this requirement.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        ParameterGroup inputImages = parameters.getParameter(MEASURE_ANOTHER_IMAGE);
        LinkedHashMap<Integer, Parameters> imageCollections = inputImages.getCollections(true);
        boolean includeCentroids = parameters.getValue(INCLUDE_CENTROIDS,workspace);
        boolean includeTimepoints = parameters.getValue(INCLUDE_TIMEPOINTS,workspace);
        String saveNameMode = parameters.getValue(SAVE_NAME_MODE,workspace);
        String saveFileName = parameters.getValue(SAVE_FILE_NAME,workspace);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE,workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE,workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX,workspace);

        // If there are no input objects skip the module
        if (inputObjects == null)
            return Status.PASS;
        Obj firstObj = inputObjects.getFirst();
        if (firstObj == null)
            return Status.PASS;

        Image[] images = new Image[imageCollections.size()];
        int i = 0;
        for (Parameters imageCollection : imageCollections.values()) {
            String imageName = imageCollection.getValue(INPUT_IMAGE, workspace);
            images[i++] = workspace.getImage(imageName);
        }

        SXSSFWorkbook workbook = process(inputObjects, images, includeCentroids, includeTimepoints);

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

        writeDistancesFile(workbook, path);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(IMAGE_SEPARATOR, this));
        Parameters collection = new Parameters();
        collection.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ParameterGroup(MEASURE_ANOTHER_IMAGE, this, collection, 1));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(INCLUDE_CENTROIDS, this, false));
        parameters.add(new BooleanP(INCLUDE_TIMEPOINTS, this, false));

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
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(IMAGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASURE_ANOTHER_IMAGE));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INCLUDE_CENTROIDS));
        returnedParameters.add(parameters.getParameter(INCLUDE_TIMEPOINTS));

        returnedParameters.add(parameters.getParameter(FILE_SAVING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SAVE_NAME_MODE));
        switch ((String) parameters.getValue(SAVE_NAME_MODE,workspace)) {
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
Workspace workspace = null;
        return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
Workspace workspace = null;
        return null;

    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
Workspace workspace = null;
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription("Objects for which intensity profiles will be generated.");

        Parameters collection = ((ParameterGroup) parameters.get(MEASURE_ANOTHER_IMAGE)).getTemplateParameters();

        collection.get(INPUT_IMAGE).setDescription(
                "Image for which the intensity profile will be measured.  Results from this profile will be added to a separate sheet in the .xlsx file.");

        parameters.get(MEASURE_ANOTHER_IMAGE).setDescription(
                "Include another image from the workspace to be measured.  Each separate image will be measured at the same spatial points and be saved to a separate sheet of the .xlsx file.");

        parameters.get(INCLUDE_CENTROIDS)
                .setDescription("Include columns recording the XYZ object centroid in pixel (or slice) units.");

        parameters.get(INCLUDE_TIMEPOINTS)
                .setDescription("Include a column recording the timepoint that the objects were present in.");

        parameters.get(SAVE_NAME_MODE)
                .setDescription("Controls how saved profile file names will be generated.<br><ul>" +

                        "<li>\"" + SaveNameModes.MATCH_INPUT
                        + "\" Use the same name as the root file for this workspace (i.e. the input file in \"Input control\".</li>"

                        + "<li>\"" + SaveNameModes.SPECIFIC_NAME
                        + "\" Use a specific name for the output file.  Care should be taken with this when working in batch mode as it's easy to continuously write over output files from other runs.</li></ul>");

        parameters.get(SAVE_FILE_NAME).setDescription(
                "Filename for saved distance file.  Note: Care should be taken with this when working in batch mode as it's easy to continuously write over output files from other runs.");

        parameters.get(APPEND_SERIES_MODE).setDescription(
                "Controls if any series information should be appended to the end of the filename.  This is useful when working with multi-series files, as it should help prevent writing files from multiple runs with the same filename.  Series numbers are prepended by \"S\".  Choices are: "
                        + String.join(", ", AppendSeriesModes.ALL) + ".");

        parameters.get(APPEND_DATETIME_MODE).setDescription(
                "Controls under what conditions the time and date will be appended on to the end of the profile file filename.  This can be used to prevent accidental over-writing of files from previous runs:<br><ul>"

                        + "<li>\"" + AppendDateTimeModes.ALWAYS
                        + "\" Always append the time and date on to the end of the filename.</li>"

                        + "<li>\"" + AppendDateTimeModes.IF_FILE_EXISTS
                        + "\" Only append the time and date if the results file already exists.</li>"

                        + "<li>\"" + AppendDateTimeModes.NEVER
                        + "\" Never append time and date (unless the file is open and unwritable).</li></ul>");

        parameters.get(SAVE_SUFFIX).setDescription("A custom suffix to be added to each filename.");

    }
}
