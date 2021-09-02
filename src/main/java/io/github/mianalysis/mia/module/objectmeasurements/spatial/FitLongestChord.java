package io.github.mianalysis.mia.module.objectmeasurements.spatial;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.common.Analysis.LongestChordCalculator;
import io.github.sjcross.common.MathFunc.CumStat;

/**
 * Created by sc13967 on 20/06/2018.
 */
public class FitLongestChord extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String CALCULATION_SEPARATOR = "Longest chord calculation";
    public static final String MEASURE_OBJECT_WIDTH = "Measure object width";
    public static final String MEASURE_OBJECT_ORIENTATION = "Measure object orientation";
    public static final String STORE_END_POINTS = "Store end points";


    public FitLongestChord(Modules modules) {
        super("Fit longest chord", modules);
    }

    public interface Measurements {
        String LENGTH_PX = "LONGEST_CHORD // LENGTH (PX)";
        String LENGTH_CAL = "LONGEST_CHORD // LENGTH (${SCAL})";
        String X1_PX = "LONGEST_CHORD // X1 (PX)";
        String Y1_PX = "LONGEST_CHORD // Y1 (PX)";
        String Z1_SLICE = "LONGEST_CHORD // Z1 (SLICE)";
        String X2_PX = "LONGEST_CHORD // X2 (PX)";
        String Y2_PX = "LONGEST_CHORD // Y2 (PX)";
        String Z2_SLICE = "LONGEST_CHORD // Z2 (SLICE)";
        String MEAN_SURF_DIST_PX = "LONGEST_CHORD // MEAN_SURF_DIST (PX)";
        String MEAN_SURF_DIST_CAL = "LONGEST_CHORD // MEAN_SURF_DIST (${SCAL})";
        String STD_SURF_DIST_PX = "LONGEST_CHORD // STD_SURF_DIST (PX)";
        String STD_SURF_DIST_CAL = "LONGEST_CHORD // STD_SURF_DIST (${SCAL})";
        String MAX_SURF_DIST_PX = "LONGEST_CHORD // MAX_SURF_DIST (PX)";
        String MAX_SURF_DIST_CAL = "LONGEST_CHORD // MAX_SURF_DIST (${SCAL})";
        String ORIENTATION_XY_DEGS = "LONGEST_CHORD // ORIENTATION_XY_(DEGS)";

    }

    public void processObject(Obj object, boolean measureWidth, boolean measureOrientation, boolean storeEndPoints) {
        double dppXY = object.getDppXY();

        LongestChordCalculator calculator = new LongestChordCalculator(object);

        double longestChordLength = calculator.getLCLength();
        object.addMeasurement(new Measurement(Measurements.LENGTH_PX, longestChordLength));
        object.addMeasurement(new Measurement(Measurements.LENGTH_CAL, longestChordLength * dppXY));

        if (storeEndPoints) {
            double[][] LC = calculator.getLC();
            object.addMeasurement(new Measurement(Measurements.X1_PX, LC[0][0]));
            object.addMeasurement(new Measurement(Measurements.Y1_PX, LC[0][1]));
            object.addMeasurement(new Measurement(Measurements.Z1_SLICE, LC[0][2]));
            object.addMeasurement(new Measurement(Measurements.X2_PX, LC[1][0]));
            object.addMeasurement(new Measurement(Measurements.Y2_PX, LC[1][1]));
            object.addMeasurement(new Measurement(Measurements.Z2_SLICE, LC[1][2]));
        }

        if (measureWidth) {
            CumStat cumStat = calculator.calculateAverageDistanceFromLC();
            if (cumStat == null) {
                object.addMeasurement(new Measurement(Measurements.MEAN_SURF_DIST_PX, Double.NaN));
                object.addMeasurement(new Measurement(Measurements.MEAN_SURF_DIST_CAL, Double.NaN));
                object.addMeasurement(new Measurement(Measurements.STD_SURF_DIST_PX, Double.NaN));
                object.addMeasurement(new Measurement(Measurements.STD_SURF_DIST_CAL, Double.NaN));
                object.addMeasurement(new Measurement(Measurements.MAX_SURF_DIST_PX, Double.NaN));
                object.addMeasurement(new Measurement(Measurements.MAX_SURF_DIST_CAL, Double.NaN));
            } else {
                object.addMeasurement(new Measurement(Measurements.MEAN_SURF_DIST_PX, cumStat.getMean()));
                object.addMeasurement(new Measurement(Measurements.MEAN_SURF_DIST_CAL, cumStat.getMean() * dppXY));
                object.addMeasurement(new Measurement(Measurements.STD_SURF_DIST_PX, cumStat.getStd()));
                object.addMeasurement(new Measurement(Measurements.STD_SURF_DIST_CAL, cumStat.getStd() * dppXY));
                object.addMeasurement(new Measurement(Measurements.MAX_SURF_DIST_PX, cumStat.getMax()));
                object.addMeasurement(new Measurement(Measurements.MAX_SURF_DIST_CAL, cumStat.getMax() * dppXY));
            }
        }

        if (measureOrientation) {
            double orientationDegs = Math.toDegrees(calculator.getXYOrientationRads());

            // Ensuring the orientation is positive
            while (orientationDegs < 0)
                orientationDegs += 360;

            // Fitting to range -90 to + 90 degrees
            orientationDegs = (orientationDegs + 90) % 180 - 90;

            object.addMeasurement(new Measurement(Measurements.ORIENTATION_XY_DEGS, orientationDegs));

        }
    }



    @Override
    public Category getCategory() {
        return Categories.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "Measures the longest chord of each object in a specified object collection from the workspace.  The longest chord of an object is defined as the line passing between the two furthest-spaced points on the surface of the object.  This can act as an approximate measure of object length.  In addition to the longest chord length, the distance of all object surface points from the longest chord can be measured, which themselves act as an approximation of object width.";
        
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        boolean measureWidth = parameters.getValue(MEASURE_OBJECT_WIDTH);
        boolean measureOrientation = parameters.getValue(MEASURE_OBJECT_ORIENTATION);
        boolean storeEndPoints = parameters.getValue(STORE_END_POINTS);

        // Running through each object, taking measurements and adding new object to the
        // workspace where necessary
        int count = 0;
        int total = inputObjects.size();
        for (Obj inputObject : inputObjects.values()) {
            processObject(inputObject, measureWidth, measureOrientation, storeEndPoints);
            writeProgressStatus(++count, total, "objects");
        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(CALCULATION_SEPARATOR, this));
        parameters.add(new BooleanP(MEASURE_OBJECT_WIDTH, this, true));
        parameters.add(new BooleanP(MEASURE_OBJECT_ORIENTATION, this, true));
        parameters.add(new BooleanP(STORE_END_POINTS, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        boolean measureWidth = parameters.getValue(MEASURE_OBJECT_WIDTH);
        boolean measureOrientation = parameters.getValue(MEASURE_OBJECT_ORIENTATION);
        boolean storeEndPoints = parameters.getValue(STORE_END_POINTS);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.LENGTH_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Length of the longest chord (the vector passing between the two points on the " + "\""
                + inputObjectsName + "\" object surface with the greatest spacing).  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.LENGTH_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Length of the longest chord (the vector passing between the two points on the " + "\""
                + inputObjectsName + "\" object surface with the greatest spacing).  Measured in calibrated ("
                + SpatialUnit.getOMEUnit().getSymbol() + ") units.");
        returnedRefs.add(reference);

        if (storeEndPoints) {
            reference = objectMeasurementRefs.getOrPut(Measurements.X1_PX);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("X-coordinate for one end of the longest chord fit to the object \""
                    + inputObjectsName + "\" .  Measured in pixel units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.Y1_PX);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Y-coordinate for one end of the longest chord fit to the object \""
                    + inputObjectsName + "\" .  Measured in pixel units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.Z1_SLICE);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Z-coordinate for one end of the longest chord fit to the object \""
                    + inputObjectsName + "\" .  Measured in pixel units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.X2_PX);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("X-coordinate for one end of the longest chord fit to the object \""
                    + inputObjectsName + "\" .  Measured in pixel units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.Y2_PX);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Y-coordinate for one end of the longest chord fit to the object \""
                    + inputObjectsName + "\" .  Measured in pixel units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.Z2_SLICE);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Z-coordinate for one end of the longest chord fit to the object \""
                    + inputObjectsName + "\" .  Measured in pixel units.");
            returnedRefs.add(reference);
        }

        if (measureWidth) {
            reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_SURF_DIST_PX);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Mean distance of all points on the \"" + inputObjectsName
                    + "\" object surface to the "
                    + "respective closest point on the longest chord.  Measured in pixel units (i.e. Z-coordinates are "
                    + "converted to pixel units prior to calculation).");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_SURF_DIST_CAL);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription(
                    "Mean distance of all points on the \"" + inputObjectsName + "\" object surface to the "
                            + "respective closest point on the longest chord.  Measured in calibrated ("
                            + SpatialUnit.getOMEUnit().getSymbol() + ") units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.STD_SURF_DIST_PX);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Standard deviation distance of all points on the\"" + inputObjectsName
                    + "\"object "
                    + "surface to the respective closest point on the longest chord.  Measured in pixel units (i.e. "
                    + "Z-coordinates are converted to pixel units prior to calculation).");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.STD_SURF_DIST_CAL);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription(
                    "Standard deviation distance of all points on the \"" + inputObjectsName + "\" object "
                            + "surface to the respective closest point on the longest chord.  Measured in calibrated ("
                            + SpatialUnit.getOMEUnit().getSymbol() + ") units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.MAX_SURF_DIST_PX);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Maximum distance of all points on the \"" + inputObjectsName
                    + "\" object surface to the "
                    + "respective closest point on the longest chord.  Measured in pixel units (i.e. Z-coordinates are "
                    + "converted to pixel units prior to calculation).");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.MAX_SURF_DIST_CAL);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription(
                    "Maximum distance of all points on the \"" + inputObjectsName + "\" object surface to the "
                            + "respective closest point on the longest chord.  Measured in calibrated ("
                            + SpatialUnit.getOMEUnit().getSymbol() + ") units.");
            returnedRefs.add(reference);
        }

        if (measureOrientation) {
            reference = objectMeasurementRefs.getOrPut(Measurements.ORIENTATION_XY_DEGS);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Orientation in XY of longest chord fit to the object, \"" + inputObjectsName
                    + "\".  "
                    + "Measured in degrees, relative to positive x-axis (positive above x-axis, negative below x-axis).");
            returnedRefs.add(reference);
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
      parameters.get(INPUT_OBJECTS).setDescription("Objects from workspace to measure longest chord for.  Measurements will be associated with the corresponding object in this collection.");

      parameters.get(MEASURE_OBJECT_WIDTH).setDescription("When selected the width of the object from the longest chord will be estimated.  The distance of all object surface points (those with at least one non-object neighbour in 4/6-way connectivity) from the longest chord are calculated.  Statistics (mean, minimum, maximum, sum and standard deviation) of these distances for an object are stored as measurements associated with that object.");

      parameters.get(MEASURE_OBJECT_ORIENTATION).setDescription("When selected, the orientation of the line in the XY plane is measured and this measurement associated with the corresponding object.  Orientations are reported in degree units and are relative to positive x-axis (positive above x-axis, negative below x-axis).");

      parameters.get(STORE_END_POINTS).setDescription("When selected, the two coordinates corresponding to the end points of the longest chord (the two furthest-spaced points on the object surface) are stored as measurements associated with the corresponding input object.");

    }
}
