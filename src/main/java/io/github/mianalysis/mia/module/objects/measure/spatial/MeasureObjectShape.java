package io.github.mianalysis.mia.module.objects.measure.spatial;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.Prefs;
import inra.ijpb.measure.IntrinsicVolumes3D;
import inra.ijpb.measure.region3d.IntrinsicVolumesAnalyzer3D;
import inra.ijpb.measure.region3d.IntrinsicVolumesAnalyzer3D.Result;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.process.ProjectObjects;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.ConnectivityInterface;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.sjcross.sjcommon.exceptions.IntegerOverflowException;
import io.github.sjcross.sjcommon.object.Point;

/**
 * Created by sc13967 on 29/06/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureObjectShape extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String MEASUREMENT_SEPARATOR = "Measurement selection";
    public static final String MEASURE_VOLUME = "Measure volume";
    public static final String MEASURE_PROJECTED_AREA = "Measure projected area";
    public static final String MEASURE_PROJECTED_DIA = "Measure projected diameter";
    public static final String MEASURE_PROJECTED_PERIM = "Measure projected perimeter";
    public static final String MEASURE_3D_METRICS = "Measure 3D metrics";
    public static final String CONNECTIVITY = "Connectivity";
    public static final String SURFACE_AREA_METHOD = "Surface area method";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public MeasureObjectShape(Modules modules) {
        super("Measure object shape", modules);
    }

    public interface Connectivity extends ConnectivityInterface {
    }

    public interface SurfaceAreaMethods {
        String THREE = "3";
        String THIRTEEN = "13";

        String[] ALL = new String[] { THREE, THIRTEEN };

    }

    public interface Measurements {
        String N_VOXELS = "SHAPE // N_VOXELS";
        String VOLUME_PX = "SHAPE // VOLUME_(PX³)";
        String VOLUME_CAL = "SHAPE // VOLUME_(${SCAL}³)";
        String BASE_AREA_PX = "SHAPE // BASE_AREA_(PX²)";
        String BASE_AREA_CAL = "SHAPE // BASE_AREA_(${SCAL}²)";
        String HEIGHT_SLICE = "SHAPE // HEIGHT_(SLICE)";
        String HEIGHT_CAL = "SHAPE // HEIGHT_(${SCAL})";
        String PROJ_AREA_PX = "SHAPE // PROJ_AREA_(PX²)";
        String PROJ_AREA_CAL = "SHAPE // PROJ_AREA_(${SCAL}²)";
        String PROJ_DIA_PX = "SHAPE // PROJ_DIA_(PX)";
        String PROJ_DIA_CAL = "SHAPE // PROJ_DIA_(${SCAL})";
        String PROJ_PERIM_PX = "SHAPE // PROJ_PERIM_(PX)";
        String PROJ_PERIM_CAL = "SHAPE // PROJ_PERIM_(${SCAL})";
        String PROJ_CIRCULARITY = "SHAPE // PROJ_CIRCULARITY";
        String SURFACE_AREA_CAL = "SHAPE // SURFACE_AREA_(${SCAL}²)";
        String MEAN_BREADTH_CAL = "SHAPE // MEAN_BREADTH_(${SCAL})";
        String SPHERICITY = "SHAPE // SPHERICITY";
        String EULER_NUMBER = "SHAPE // EULER_NUMBER";

    }

    public double calculateBaseAreaPx(Obj object) {
        // Getting the lowest slice
        double[][] extents = object.getExtents(true, false);
        int baseZ = (int) Math.round(extents[2][0]);

        // Counting the number of pixels in this slice
        int count = 0;
        for (Point<Integer> point : object.getCoordinateSet()) {
            if (point.getZ() == baseZ)
                count++;
        }

        // The area in px² is simply the number of pixels
        return count;

    }

    /*
     * Calculates the maximum distance between any two points of the
     */
    public double calculateMaximumPointPointDistance(Obj object) {
        double[] x = object.getX(true);
        double[] y = object.getY(true);
        double[] z = object.getZ(true, true);

        double maxDistance = 0;

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x.length; j++) {
                if (i == j)
                    continue;

                double x1 = x[i];
                double y1 = y[i];
                double z1 = z[i];
                double x2 = x[j];
                double y2 = y[j];
                double z2 = z[j];

                double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));

                maxDistance = Math.max(distance, maxDistance);

            }
        }

        return maxDistance;

    }

    public void measure3DMetrics(Obj inputObject, int connectivity, int surfaceAreaMethod) {
        ImagePlus tightIpl = inputObject.getAsTightImage("Object").getImagePlus();

        IntrinsicVolumesAnalyzer3D analyser = new IntrinsicVolumesAnalyzer3D();
        analyser.setConnectivity(connectivity);
        analyser.setDirectionNumber(surfaceAreaMethod);

        Map<Integer, Result> results = new IntrinsicVolumesAnalyzer3D().analyzeRegions(tightIpl);
        Result result = results.values().iterator().next();
        
        inputObject.addMeasurement(new Measurement(Measurements.SURFACE_AREA_CAL, result.surfaceArea));
        inputObject.addMeasurement(new Measurement(Measurements.MEAN_BREADTH_CAL, result.meanBreadth));
        inputObject.addMeasurement(new Measurement(Measurements.EULER_NUMBER, result.eulerNumber));
        inputObject.addMeasurement(new Measurement(Measurements.SPHERICITY, IntrinsicVolumes3D.sphericity(result.volume, result.surfaceArea)));
        
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "Measures various spatial metrics for each object in a specified object collection from the workspace.  Measurements are associated with the relevant input object.  When dealing with 3D objects (those with coordinates spanning multiple Z-slices) a 2D projection into the XY plane will be used.  3D metrics are calculated using the \"<a href=\"https://github.com/ijpb/MorphoLibJ\">MorphoLibJ</a>\" plugin.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectName = parameters.getValue(INPUT_OBJECTS, workspace);
        boolean measureVolume = parameters.getValue(MEASURE_VOLUME, workspace);
        boolean measureProjectedArea = parameters.getValue(MEASURE_PROJECTED_AREA, workspace);
        boolean measureProjectedDiameter = parameters.getValue(MEASURE_PROJECTED_DIA, workspace);
        boolean measureProjectedPerimeter = parameters.getValue(MEASURE_PROJECTED_PERIM, workspace);
        boolean measure3dMetrics = parameters.getValue(MEASURE_3D_METRICS, workspace);
        int connectivity = Integer.parseInt(parameters.getValue(CONNECTIVITY, workspace));
        int surfaceAreaMethod = Integer.parseInt(parameters.getValue(SURFACE_AREA_METHOD, workspace));
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);

        // Getting input objects
        Objs inputObjects = workspace.getObjects().get(inputObjectName);

        // Configuring multithreading
        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        int total = inputObjects.size();
        AtomicInteger count = new AtomicInteger();

        // Running through each object, making the measurements
        for (Obj inputObject : inputObjects.values()) {
            Runnable task = () -> {
                // Adding the volume measurements
                if (measureVolume) {
                    int nVoxels = inputObject.size();
                    inputObject.addMeasurement(new Measurement(Measurements.N_VOXELS, nVoxels));

                    double containedVolumePx = inputObject.getContainedVolume(true);
                    inputObject.addMeasurement(new Measurement(Measurements.VOLUME_PX, containedVolumePx));

                    double containedVolumeCal = inputObject.getContainedVolume(false);
                    inputObject.addMeasurement(new Measurement(Measurements.VOLUME_CAL, containedVolumeCal));

                    double baseAreaPx = calculateBaseAreaPx(inputObject);
                    inputObject.addMeasurement(new Measurement(Measurements.BASE_AREA_PX, baseAreaPx));

                    double dppXY = inputObject.getDppXY();
                    inputObject.addMeasurement(new Measurement(Measurements.BASE_AREA_CAL, baseAreaPx * dppXY * dppXY));

                    double heightSlice = inputObject.getHeight(true, false);
                    inputObject.addMeasurement(new Measurement(Measurements.HEIGHT_SLICE, heightSlice));

                    double heightCal = inputObject.getHeight(false, false);
                    inputObject.addMeasurement(new Measurement(Measurements.HEIGHT_CAL, heightCal));
                }

                // If necessary analyses are included
                Objs projectedObjects = new Objs("Projected", inputObjects);
                Obj projectedObject = null;
                if (measureProjectedArea || measureProjectedDiameter || measureProjectedPerimeter) {
                    if (inputObject.is2D()) {
                        projectedObject = inputObject;
                    } else {
                        try {
                            if (inputObject.is2D())
                                projectedObject = inputObject;
                            else
                                projectedObject = ProjectObjects.process(inputObject, projectedObjects, false);
                        } catch (IntegerOverflowException e) {
                            MIA.log.writeWarning(e);
                            return;
                        }
                    }
                }

                // Adding the projected-object area measurements
                if (measureProjectedArea) {
                    double areaPx = projectedObject.size();
                    double areaCal = areaPx * projectedObject.getDppXY() * projectedObject.getDppXY();
                    inputObject.addMeasurement(new Measurement(Measurements.PROJ_AREA_PX, areaPx));
                    inputObject.addMeasurement(new Measurement(Measurements.PROJ_AREA_CAL, areaCal));

                }

                // Adding the projected-object diameter measurements
                if (measureProjectedDiameter) {
                    double maxDistancePx = calculateMaximumPointPointDistance(projectedObject);
                    double maxDistanceCal = calculateMaximumPointPointDistance(projectedObject)
                            * inputObject.getDppXY();
                    inputObject.addMeasurement(new Measurement(Measurements.PROJ_DIA_PX, maxDistancePx));
                    inputObject.addMeasurement(new Measurement(Measurements.PROJ_DIA_CAL, maxDistanceCal));
                }

                // Adding the projected-object perimeter measurements
                if (measureProjectedPerimeter) {
                    double areaPx = projectedObject.size();
                    double perimeterPx = projectedObject.getRoi(0).getLength();
                    double perimeterCal = perimeterPx * inputObject.getDppXY();
                    inputObject.addMeasurement(new Measurement(Measurements.PROJ_PERIM_PX, perimeterPx));
                    inputObject.addMeasurement(new Measurement(Measurements.PROJ_PERIM_CAL, perimeterCal));

                    double circularity = 4 * Math.PI * areaPx / (perimeterPx * perimeterPx);
                    inputObject.addMeasurement(new Measurement(Measurements.PROJ_CIRCULARITY, circularity));

                }

                if (measure3dMetrics)
                    measure3DMetrics(inputObject,connectivity,surfaceAreaMethod);
                
                // Clearing the projected object from memory to save some space
                if (projectedObject != null & !inputObject.is2D())
                    inputObject.clearProjected();

                writeProgressStatus(count.getAndIncrement(), total, "objects");

            };
            pool.submit(task);
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
            return Status.FAIL;
        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new BooleanP(MEASURE_VOLUME, this, true));
        parameters.add(new BooleanP(MEASURE_PROJECTED_AREA, this, false));
        parameters.add(new BooleanP(MEASURE_PROJECTED_DIA, this, false));
        parameters.add(new BooleanP(MEASURE_PROJECTED_PERIM, this, false));
        parameters.add(new BooleanP(MEASURE_3D_METRICS, this, false));
        parameters.add(new ChoiceP(CONNECTIVITY, this, Connectivity.TWENTYSIX, Connectivity.ALL));
        parameters.add(new ChoiceP(SURFACE_AREA_METHOD, this, SurfaceAreaMethods.THIRTEEN, SurfaceAreaMethods.ALL));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASURE_VOLUME));
        returnedParameters.add(parameters.getParameter(MEASURE_PROJECTED_AREA));
        returnedParameters.add(parameters.getParameter(MEASURE_PROJECTED_DIA));
        returnedParameters.add(parameters.getParameter(MEASURE_PROJECTED_PERIM));
        returnedParameters.add(parameters.getParameter(MEASURE_3D_METRICS));

        if ((Boolean) parameters.getValue(MEASURE_3D_METRICS, workspace)) {
            returnedParameters.add(parameters.getParameter(CONNECTIVITY));
            returnedParameters.add(parameters.getParameter(SURFACE_AREA_METHOD));
        }

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        if ((boolean) parameters.getValue(MEASURE_VOLUME, workspace)) {
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.N_VOXELS);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Number of voxels (3D pixels) in the object, \"" + inputObjectsName + "\".  Note: "
                    + "This doesn't take spatial scaling of XY and Z into account, so isn't a good measure of true "
                    + "object volume.");

            reference = objectMeasurementRefs.getOrPut(Measurements.VOLUME_PX);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Volume of the object, \"" + inputObjectsName
                    + "\".  Takes spatial scaling of XY vs "
                    + "Z into account (i.e. converts object height from slice units to pixel units.  Measured in pixel "
                    + "units.");

            reference = objectMeasurementRefs.getOrPut(Measurements.VOLUME_CAL);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Volume of the object, \"" + inputObjectsName
                    + "\".  Takes spatial scaling of XY vs "
                    + "Z into account (i.e. converts object height from slice units to pixel units prior to conversion"
                    + " to calibrated units.  Measured in calibrated (" + SpatialUnit.getOMEUnit().getSymbol()
                    + ") units.");

            reference = objectMeasurementRefs.getOrPut(Measurements.BASE_AREA_PX);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Area of the lowest slice present for the object, \"" + inputObjectsName + "\".  "
                    + "Measured in pixel units.");

            reference = objectMeasurementRefs.getOrPut(Measurements.BASE_AREA_CAL);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Area of the lowest slice present for the object, \"" + inputObjectsName + "\".  "
                    + "Measured in calibrated (" + SpatialUnit.getOMEUnit().getSymbol() + ") units.");

            reference = objectMeasurementRefs.getOrPut(Measurements.HEIGHT_SLICE);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Height of the object, \"" + inputObjectsName + "\".  Measured in slice unit.");

            reference = objectMeasurementRefs.getOrPut(Measurements.HEIGHT_CAL);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Height of the object, \"" + inputObjectsName + "\".  Measured in calibrated "
                    + "(" + SpatialUnit.getOMEUnit().getSymbol() + ") units.");

        }

        if ((boolean) parameters.getValue(MEASURE_PROJECTED_AREA, workspace)) {
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.PROJ_AREA_PX);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Area of the 2D Z-projection of the object, \"" + inputObjectsName
                    + "\".  Measured " + "in pixel units.");

            reference = objectMeasurementRefs.getOrPut(Measurements.PROJ_AREA_CAL);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Area of the 2D Z-projection of the object, \"" + inputObjectsName
                    + "\".  Measured " + "in calibrated (" + SpatialUnit.getOMEUnit().getSymbol() + ") units.");
        }

        if ((boolean) parameters.getValue(MEASURE_PROJECTED_DIA, workspace)) {
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.PROJ_DIA_PX);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Longest distance between any two points of the 2D Z-projection of the object, \""
                    + inputObjectsName + "\".  Measured in pixel units.");

            reference = objectMeasurementRefs.getOrPut(Measurements.PROJ_DIA_CAL);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Longest distance between any two points of the 2D Z-projection of the object, \""
                    + inputObjectsName + "\".  Measured in calibrated (" + SpatialUnit.getOMEUnit().getSymbol() + ") "
                    + "units.");
        }

        if ((boolean) parameters.getValue(MEASURE_PROJECTED_PERIM, workspace)) {
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.PROJ_PERIM_PX);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Perimeter of the 2D Z-projection of the object, \"" + inputObjectsName + "\".  "
                    + "Measured in pixel units.");

            reference = objectMeasurementRefs.getOrPut(Measurements.PROJ_PERIM_CAL);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Perimeter of the 2D Z-projection of the object, \"" + inputObjectsName + "\".  "
                    + "Measured in calibrated (" + SpatialUnit.getOMEUnit().getSymbol() + ") " + "units.");

            reference = objectMeasurementRefs.getOrPut(Measurements.PROJ_CIRCULARITY);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Circularity of the 2D Z-projection of the object, \"" + inputObjectsName + "\".  "
                    + "Uses the calculation <i>circularity = 4pi(area/perimeter^2)</i>.  Values approaching 1 correspond to perfect circles, while values approaching 0 indicate increasingly elongated shapes.  Due to rounding errors, very small objects can yield values outside the true range of 0-1.  This measurement has no units.");

        }

        if ((boolean) parameters.getValue(MEASURE_3D_METRICS, workspace)) {
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.SURFACE_AREA_CAL);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Surface area of the 3D object, \"" + inputObjectsName + "\".  Measured in calibrated units.");

            reference = objectMeasurementRefs.getOrPut(Measurements.SPHERICITY);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Sphericity of the 3D object, \"" + inputObjectsName + "\".  This is defined as <i>36*pi*(V^2)/(S^3)</i>, where <i>V</i> is the object volume and <i>S</i> is the surface area.");

            reference = objectMeasurementRefs.getOrPut(Measurements.EULER_NUMBER);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("The Euler number of the 3D object, \"" + inputObjectsName + "\".  A detailed description of MorphoLibJ's Euler number implementation can be found \"<a href=\"https://imagej.net/plugins/morpholibj\">here</a>\".");

            reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_BREADTH_CAL);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("The mean breadth of the 3D object, \"" + inputObjectsName + "\".  Measured in calibrated units.  A detailed description of MorphoLibJ's mean breadth calculation can be found \"<a href=\"https://imagej.net/plugins/morpholibj\">here</a>\".");

            
        }

        return returnedRefs;

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
        parameters.get(INPUT_OBJECTS).setDescription(
                "Input objects from workspace.  Shape metrics will be calculated for each object and stored as measurements associated with that object.");

        parameters.get(MEASURE_VOLUME).setDescription(
                "When selected, 3D volume metrics will be calculated for each input object.  Metrics are: volume (px³), volume (calibrated_units³), number of voxels, base area (px²), base area (calibrated_units²), height (n_slices) and height (calibrated_units).");

        parameters.get(MEASURE_PROJECTED_AREA).setDescription(
                "When selected, 2D area metrics will be calculated for each input object.  For 3D objects, a 2D projection in the XY plane is used for measurements.  This projection includes all XY coordinates present in any Z-slice.  Metrics are: area (px²) and area (calibrated_units²).");

        parameters.get(MEASURE_PROJECTED_DIA).setDescription(
                "When selected, the diameter of 2D objects will be calculated for each input object.  For 3D objects, a 2D projection in the XY plane is used for measurements.  This projection includes all XY coordinates present in any Z-slice.  Metrics are: diameter (px) and diameter (calibrated_units).");

        parameters.get(MEASURE_PROJECTED_PERIM).setDescription(
                "When selected, the perimeter and circularity of 2D objects will be calculated for each input object.  For 3D objects, a 2D projection in the XY plane is used for measurements.  This projection includes all XY coordinates present in any Z-slice.  Metrics are: perimeter (px), perimeter (calibrated_units) and circularity.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple input objects simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
