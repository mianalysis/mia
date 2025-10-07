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
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.module.objects.process.CreateSkeleton;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.Volume;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeFactories;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 22/06/2017.
 */

/**
* Measures the intensity profile along the pixel-wide backbone of an object and outputs this profile to .xlsx file.  Input objects are skeletonised to single pixel-wide representations prior to measurement; however, pre-skeletonised objects can also be processed.<br><br>Output results are stored in a multi-sheet .xlsx file, where each sheet includes the profile for a specific input image.  Each row of a sheet contains the profile for a single object.  Profiles are linearly-interpolated such that each measured position along a profile is 1px from the previous.<br><br>Note: Objects must either form a single line (i.e. not contain multiple branches) or reduce to a single line during skeletonisation.  No profile will be recorded for any objects which fail this requirement.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureIntensityAlongPath extends AbstractSaver {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object input";

	/**
	* Objects for which intensity profiles will be generated.
	*/
    public static final String INPUT_OBJECTS = "Input objects";


	/**
	* 
	*/
    public static final String IMAGE_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

	/**
	* Include another image from the workspace to be measured.  Each separate image will be measured at the same spatial points and be saved to a separate sheet of the .xlsx file.
	*/
    public static final String MEASURE_ANOTHER_IMAGE = "Measure another image";


	/**
	* 
	*/
    public static final String OUTPUT_SEPARATOR = "Data output";

	/**
	* Include columns recording the XYZ object centroid in pixel (or slice) units.
	*/
    public static final String INCLUDE_CENTROIDS = "Include centroids";

	/**
	* Include a column recording the timepoint that the objects were present in.
	*/
    public static final String INCLUDE_TIMEPOINTS = "Include timepoints";

    public MeasureIntensityAlongPath(Modules modules) {
        super("Measure intensity along path", modules);
    }

    public static SXSSFWorkbook process(Objs objects, ImageI[] images, boolean includeCentroids,
            boolean includeTimepoints) {
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

    public static void process(Obj object, ImageI image, Sheet sheet, boolean includeCentroids,
            boolean includeTimepoints) {
        if (object.size() < 2)
            return;

        // Ordering points
        LinkedHashSet<Point<Integer>> orderedPoints = new LinkedHashSet<>(
                CreateSkeleton.getLargestShortestPath(object));

        LinkedHashMap<Double, Double> rawIntensities = measureIntensityProfile(orderedPoints, image, object.getT(),
                object.getSpatialCalibration());
        LinkedHashMap<Integer, Double> spacedIntensities = interpolateProfile(rawIntensities);

        addProfileToSheet(sheet, object, spacedIntensities, includeCentroids, includeTimepoints);

    }

    public static LinkedHashMap<Double, Double> measureIntensityProfile(Collection<Point<Integer>> points, ImageI image,
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
                Volume volume1 = VolumeFactories.getDefaultFactory().createVolume(new PointListFactory(), spatCal.duplicate());
                Volume volume2 = VolumeFactories.getDefaultFactory().createVolume(new PointListFactory(), spatCal.duplicate());

                try {
                    volume1.addCoord(prevPoint.getX(), prevPoint.getY(), prevPoint.getZ());
                    volume2.addCoord(point.getX(), point.getY(), point.getZ());
                } catch (PointOutOfRangeException e) {
                    MIA.log.writeError(e);
                }

                distance += volume1.getCentroidSeparation(volume2, true, false);
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

    public static void addProfileToSheet(Sheet sheet, Obj object, LinkedHashMap<?, Double> profile,
            boolean includeCentroids,
            boolean includeTimepoints) {
        int colCount = 0;
        int rowCount = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowCount++);

        row.createCell(colCount++).setCellValue(object.getID());

        if (includeCentroids) {
            row.createCell(colCount++).setCellValue(object.getXMean(true));
            row.createCell(colCount++).setCellValue(object.getYMean(true));
            row.createCell(colCount++).setCellValue(object.getZMean(true, false));
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
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Measures the intensity profile along the pixel-wide backbone of an object and outputs this profile to .xlsx file.  Input objects are skeletonised to single pixel-wide representations prior to measurement; however, pre-skeletonised objects can also be processed.<br><br>"

                + "Output results are stored in a multi-sheet .xlsx file, where each sheet includes the profile for a specific input image.  Each row of a sheet contains the profile for a single object.  Profiles are linearly-interpolated such that each measured position along a profile is 1px from the previous.<br><br>"

                + "Note: Objects must either form a single line (i.e. not contain multiple branches) or reduce to a single line during skeletonisation.  No profile will be recorded for any objects which fail this requirement.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting parameters
        ParameterGroup inputImages = parameters.getParameter(MEASURE_ANOTHER_IMAGE);
        LinkedHashMap<Integer, Parameters> imageCollections = inputImages.getCollections(true);
        boolean includeCentroids = parameters.getValue(INCLUDE_CENTROIDS, workspace);
        boolean includeTimepoints = parameters.getValue(INCLUDE_TIMEPOINTS, workspace);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX, workspace);

        // If there are no input objects skip the module
        if (inputObjects == null)
            return Status.PASS;
        Obj firstObj = inputObjects.getFirst();
        if (firstObj == null)
            return Status.PASS;

        ImageI[] images = new ImageI[imageCollections.size()];
        int i = 0;
        for (Parameters imageCollection : imageCollections.values()) {
            String imageName = imageCollection.getValue(INPUT_IMAGE, workspace);
            images[i++] = workspace.getImage(imageName);
        }

        SXSSFWorkbook workbook = process(inputObjects, images, includeCentroids, includeTimepoints);

        String outputPath = getOutputPath(modules, workspace);
        String outputName = getOutputName(modules, workspace);

        // Adding last bits to name
        outputPath = outputPath + outputName;
        outputPath = appendSeries(outputPath, workspace, appendSeriesMode);
        outputPath = appendDateTime(outputPath, appendDateTimeMode);
        outputPath = outputPath + suffix + ".xlsx";

        writeDistancesFile(workbook, outputPath);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(IMAGE_SEPARATOR, this));
        Parameters collection = new Parameters();
        collection.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ParameterGroup(MEASURE_ANOTHER_IMAGE, this, collection, 1));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(INCLUDE_CENTROIDS, this, false));
        parameters.add(new BooleanP(INCLUDE_TIMEPOINTS, this, false));

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
        returnedParameters.add(parameters.getParameter(INCLUDE_CENTROIDS));
        returnedParameters.add(parameters.getParameter(INCLUDE_TIMEPOINTS));

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
        super.addParameterDescriptions();
        
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

    }
}
