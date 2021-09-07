package io.github.mianalysis.mia.module.objectmeasurements.intensity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.SubHyperstackMaker;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.imageprocessing.pixel.binary.Skeletonise;
import io.github.mianalysis.mia.module.inputoutput.ImageSaver;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.common.object.Point;
import io.github.sjcross.common.object.volume.CoordinateSet;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.SpatCal;
import io.github.sjcross.common.object.volume.Volume;
import io.github.sjcross.common.object.volume.VolumeType;

/**
 * Created by sc13967 on 22/06/2017.
 */
public class MeasureIntensityAlongPath extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String IMAGE_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String MEASURE_ANOTHER_IMAGE = "Measure another image";

    public static final String OUTPUT_SEPARATOR = "Data output";
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

    public static SXSSFWorkbook process(Objs objects, Image[] images, boolean includeTimepoints) {
        // Creating workbook
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        Sheet[] sheets = new Sheet[images.length];
        for (int i = 0; i < images.length; i++) {
            sheets[i] = workbook.createSheet(images[i].getName());
            addHeaderToSheet(sheets[i], includeTimepoints);
        }

        for (Obj object : objects.values())
            for (int i = 0; i < images.length; i++)
                process(object, images[i], sheets[i], includeTimepoints);

        return workbook;

    }

    public static void process(Obj object, Image image, Sheet sheet, boolean includeTimepoints) {
        CoordinateSet unorderedPoints = getUnorderedPoints(object);
        if (unorderedPoints == null)
            return;

        LinkedHashSet<Point<Integer>> orderedPoints = orderPoints(unorderedPoints);
        if (orderedPoints == null)
            return;

        LinkedHashMap<Double, Double> rawIntensities = measureIntensityProfile(orderedPoints, image, object.getT(),
                object.getSpatialCalibration());
        LinkedHashMap<Integer, Double> spacedIntensities = interpolateProfile(rawIntensities);

        addProfileToSheet(sheet, object, spacedIntensities, includeTimepoints);

    }

    protected static CoordinateSet getUnorderedPoints(Obj object) {
        Image skeletonImage = object.getAsTightImage("Skeleton");

        // Ensuring the input object is a single line
        Skeletonise.process(skeletonImage, true);

        Objs skeletons = skeletonImage.convertImageToObjects("Skeleton");
        if (skeletons.size() == 0)
            return null;

        Obj skeleton = skeletons.getFirst();
        skeleton.setSpatialCalibration(object.getSpatialCalibration());

        double[][] extents = object.getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]);
        int yOffs = (int) Math.round(extents[1][0]);
        int zOffs = (int) Math.round(extents[2][0]);

        skeleton.translateCoords(xOffs, yOffs, zOffs);

        return skeleton.getCoordinateSet();

    }

    protected static LinkedHashSet<Point<Integer>> orderPoints(Collection<Point<Integer>> unorderedPoints) {
        // Determining start point
        Point<Integer> startPoint = getStartPoint(unorderedPoints);
        if (startPoint == null)
            return null;

        // Duplicating points, so we can move them from one list to another without
        // affecting the input object
        ArrayList<Point<Integer>> inputPoints = new ArrayList<>();
        for (Point<Integer> point : unorderedPoints)
            inputPoints.add(new Point<Integer>(point.getX(), point.getY(), point.getZ()));

        // Creating sorted list
        LinkedHashSet<Point<Integer>> sortedPoints = new LinkedHashSet<>();
        sortedPoints.add(startPoint);
        inputPoints.remove(startPoint);

        int prevSize = inputPoints.size();
        while (inputPoints.size() > 0) {
            for (int i = 0; i < inputPoints.size(); i++) {
                Point<Integer> testPoint = inputPoints.get(i);
                if (isNeighbourPoint(testPoint, startPoint, 26)) {
                    sortedPoints.add(testPoint);
                    inputPoints.remove(testPoint);
                    startPoint = testPoint;
                    break;
                }
            }

            // If input point collection hasn't changed size this round then the object
            // isn't a single line
            if (inputPoints.size() == prevSize)
                return null;

            prevSize = inputPoints.size();

        }

        return sortedPoints;

    }

    protected static Point<Integer> getStartPoint(Collection<Point<Integer>> unorderedPoints) {
        Point<Integer> startPoint = null;
        for (Point<Integer> point : unorderedPoints) {
            if (getPointConnectivity(unorderedPoints, point, 26) == 1) {
                startPoint = point;
                break;
            }
        }

        return startPoint;

    }

    public static boolean isNeighbourPoint(Point<Integer> testPoint, Point<Integer> point, int connectivity) {
        int x = point.getX();
        int y = point.getY();
        int z = point.getZ();

        // Testing 6-day connectivity
        if (testPoint.equals(new Point<Integer>(x - 1, y, z)))
            return true;

        if (testPoint.equals(new Point<Integer>(x + 1, y, z)))
            return true;

        if (testPoint.equals(new Point<Integer>(x, y - 1, z)))
            return true;

        if (testPoint.equals(new Point<Integer>(x, y + 1, z)))
            return true;

        if (testPoint.equals(new Point<Integer>(x, y, z - 1)))
            return true;

        if (testPoint.equals(new Point<Integer>(x, y, z + 1)))
            return true;

        // If calculating 6-way connectivity, exit here
        if (connectivity == 6)
            return false;

        if (testPoint.equals(new Point<Integer>(x - 1, y - 1, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x, y - 1, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y - 1, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x - 1, y, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x - 1, y + 1, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x, y + 1, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y + 1, z - 1)))
            return true;

        if (testPoint.equals(new Point<Integer>(x - 1, y - 1, z)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y - 1, z)))
            return true;
        if (testPoint.equals(new Point<Integer>(x - 1, y + 1, z)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y + 1, z)))
            return true;

        if (testPoint.equals(new Point<Integer>(x - 1, y - 1, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x, y - 1, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y - 1, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x - 1, y, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x - 1, y + 1, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x, y + 1, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y + 1, z + 1)))
            return true;

        return false;

    }

    public static int getPointConnectivity(Collection<Point<Integer>> testPoints, Point<Integer> point,
            int connectivity) {
        int count = 0;

        int x = point.getX();
        int y = point.getY();
        int z = point.getZ();

        // Testing 6-day connectivity
        if (testPoints.contains(new Point<Integer>(x - 1, y, z)))
            count++;

        if (testPoints.contains(new Point<Integer>(x + 1, y, z)))
            count++;

        if (testPoints.contains(new Point<Integer>(x, y - 1, z)))
            count++;

        if (testPoints.contains(new Point<Integer>(x, y + 1, z)))
            count++;

        if (testPoints.contains(new Point<Integer>(x, y, z - 1)))
            count++;

        if (testPoints.contains(new Point<Integer>(x, y, z + 1)))
            count++;

        // If calculating 6-way connectivity, exit here
        if (connectivity == 6)
            return count;

        if (testPoints.contains(new Point<Integer>(x - 1, y - 1, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x, y - 1, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y - 1, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x - 1, y, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x - 1, y + 1, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x, y + 1, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y + 1, z - 1)))
            count++;

        if (testPoints.contains(new Point<Integer>(x - 1, y - 1, z)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y - 1, z)))
            count++;
        if (testPoints.contains(new Point<Integer>(x - 1, y + 1, z)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y + 1, z)))
            count++;

        if (testPoints.contains(new Point<Integer>(x - 1, y - 1, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x, y - 1, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y - 1, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x - 1, y, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x - 1, y + 1, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x, y + 1, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y + 1, z + 1)))
            count++;

        return count;

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
            // double intensity = ist.getVoxel(x, y, z);

            if (prevPoint != null) {
                Volume volume1 = new Volume(VolumeType.POINTLIST, spatCal.duplicate());
                Volume volume2 = new Volume(VolumeType.POINTLIST, spatCal.duplicate());

                try {
                    volume1.add(prevPoint.getX(), prevPoint.getY(), prevPoint.getZ());
                    volume2.add(point.getX(), point.getY(), point.getZ());
                } catch (PointOutOfRangeException e) {
                    e.printStackTrace();
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

        // Converting to double arrays
        double[] x = profile.keySet().stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = profile.values().stream().mapToDouble(Double::doubleValue).toArray();

        PolynomialSplineFunction spline = new LinearInterpolator().interpolate(x, y);

        int max = (int) Math.floor(x[x.length - 1]);
        for (int i = 0; i <= max; i++)
            interpolated.put(i, spline.value(i));

        return interpolated;

    }

    public static void addHeaderToSheet(Sheet sheet, boolean includeTimepoints) {
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

        if (includeTimepoints) {
            cell = row.createCell(colCount++);
            cell.setCellValue("TIMEPOINT");
            cell.setCellStyle(cellStyle);
        }

        cell = row.createCell(colCount++);
        cell.setCellValue("PROFILE -->");
        cell.setCellStyle(cellStyle);

    }

    public static void addProfileToSheet(Sheet sheet, Obj object, LinkedHashMap<?, Double> profile,
            boolean includeTimepoints) {
        int colCount = 0;
        int rowCount = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowCount++);

        row.createCell(colCount++).setCellValue(object.getID());

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
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_MEASUREMENTS_INTENSITY;
    }

    @Override
    public String getDescription() {
        return "Measures the intensity profile along the pixel-wide backbone of an object and outputs this profile to .xlsx file.  Input objects are skeletonised to single pixel-wide representations prior to measurement; however, pre-skeletonised objects can also be processed.<br><br>"
                +

                "Output results are stored in a multi-sheet .xlsx file, where each sheet includes the profile for a specific input image.  Each row of a sheet contains the profile for a single object.  Profiles are linearly-interpolated such that each measured position along a profile is 1px from the previous.<br><br>"

                + "Note: Objects must either form a single line (i.e. not contain multiple branches) or reduce to a single line during skeletonisation.  No profile will be recorded for any objects which fail this requirement.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        ParameterGroup inputImages = parameters.getParameter(MEASURE_ANOTHER_IMAGE);
        LinkedHashMap<Integer, Parameters> imageCollections = inputImages.getCollections(true);
        boolean includeTimepoints = parameters.getValue(INCLUDE_TIMEPOINTS);
        String saveNameMode = parameters.getValue(SAVE_NAME_MODE);
        String saveFileName = parameters.getValue(SAVE_FILE_NAME);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE);
        String suffix = parameters.getValue(SAVE_SUFFIX);

        // If there are no input objects skip the module
        if (inputObjects == null)
            return Status.PASS;
        Obj firstObj = inputObjects.getFirst();
        if (firstObj == null)
            return Status.PASS;

        Image[] images = new Image[imageCollections.size()];
        int i = 0;
        for (Parameters imageCollection : imageCollections.values()) {
            String imageName = imageCollection.getValue(INPUT_IMAGE);
            images[i++] = workspace.getImage(imageName);
        }

        SXSSFWorkbook workbook = process(inputObjects, images, includeTimepoints);

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
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(IMAGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASURE_ANOTHER_IMAGE));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INCLUDE_TIMEPOINTS));

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
        parameters.get(INPUT_OBJECTS).setDescription("Objects for which intensity profiles will be generated.");

        Parameters collection = ((ParameterGroup) parameters.get(MEASURE_ANOTHER_IMAGE)).getTemplateParameters();

        collection.get(INPUT_IMAGE).setDescription(
                "Image for which the intensity profile will be measured.  Results from this profile will be added to a separate sheet in the .xlsx file.");

        parameters.get(MEASURE_ANOTHER_IMAGE).setDescription(
                "Include another image from the workspace to be measured.  Each separate image will be measured at the same spatial points and be saved to a separate sheet of the .xlsx file.");

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
