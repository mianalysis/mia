package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParentObjectsP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;

/**
 * Returns a spherical object around a point object. This is useful for
 * calculating local object features.
 */
public class GetLocalObjectRegion extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String CENTROID_SEPARATOR = "Centroid controls";
    public static final String CENTROID_SOURCE = "Centoid value source";
    public static final String X_POSITION = "X position";
    public static final String Y_POSITION = "Y position";
    public static final String Z_POSITION = "Z position";
    public static final String X_MEASUREMENT = "X-measurement";
    public static final String Y_MEASUREMENT = "Y-measurement";
    public static final String Z_MEASUREMENT = "Z-measurement";
    public static final String PARENT_OBJECT_FOR_CENTROID = "Parent object for centroid";
    public static final String CENTROID_SPATIAL_UNITS = "Centroid spatial units";

    public static final String RADIUS_SEPARATOR = "Radius controls";
    public static final String RADIUS_SOURCE = "Radius value source";
    public static final String FIXED_VALUE_FOR_RADIUS = "Radius (px)";
    public static final String RADIUS_MEASUREMENT = "Radius measurement";
    public static final String PARENT_OBJECT_FOR_RADIUS = "Parent object for radius";
    public static final String RADIUS_SPATIAL_UNITS = "Radius spatial units";

    public interface CentroidSources {
        String FIXED_VALUE = "Fixed value";
        String MEASUREMENT = "Measurement";
        String OBJECT_CENTROID = "Object centroid";
        String PARENT_MEASUREMENT = "Parent measurement";

        String[] ALL = new String[] { FIXED_VALUE, MEASUREMENT, OBJECT_CENTROID, PARENT_MEASUREMENT };

    }

    public interface RadiusSources {
        String FIXED_VALUE = "Fixed value";
        String MEASUREMENT = "Measurement";
        String PARENT_MEASUREMENT = "Parent measurement";
        String SINGLE_POINT = "Single point";

        String[] ALL = new String[] { FIXED_VALUE, MEASUREMENT, PARENT_MEASUREMENT, SINGLE_POINT };

    }

    public interface SpatialUnits {
        String CALIBRATED = "Calibrated";
        String PIXELS = "Pixel/slice";

        String[] ALL = new String[] { CALIBRATED, PIXELS };

    }

    public GetLocalObjectRegion(ModuleCollection modules) {
        super("Get local object region", modules);
    }

    public static Obj getLocalRegion(Obj inputObject, ObjCollection outputObjects, int[] centroid, int radius,
            boolean addRelationship) throws IntegerOverflowException {
        double xy_z_ratio = inputObject.getDppXY() / inputObject.getDppZ();

        // Creating new object and assigning relationship to input objects
        Obj outputObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType(), inputObject.getID());

        if (radius == 0) {
            // The output object is a single point
            try {
                outputObject.add(centroid[0], centroid[1], centroid[2]);
            } catch (PointOutOfRangeException e) {
            }
        } else {
            int xMin = Math.max((int) Math.floor(centroid[0] - radius), 0);
            int xMax = Math.min((int) Math.ceil(centroid[0] + radius), inputObject.getWidth());
            int yMin = Math.max((int) Math.floor(centroid[1] - radius), 0);
            int yMax = Math.min((int) Math.ceil(centroid[1] + radius), inputObject.getHeight());
            int zMin = Math.max((int) Math.floor(centroid[2] - radius * xy_z_ratio), 0);
            int zMax = Math.min((int) Math.ceil(centroid[2] + radius * xy_z_ratio), inputObject.getNSlices());

            for (int x = xMin; x <= xMax; x++) {
                double xx = centroid[0] - x;

                for (int y = yMin; y <= yMax; y++) {
                    double yy = centroid[1] - y;

                    if (inputObject.is2D()) {
                        if (Math.sqrt(xx * xx + yy * yy) < radius)
                            try {
                                outputObject.add(x, y, 0);
                            } catch (PointOutOfRangeException e) {
                            }
                    } else {
                        for (int z = zMin; z <= zMax; z++) {
                            double zz = (centroid[2] - z) / xy_z_ratio;
                            if (Math.sqrt(xx * xx + yy * yy + zz * zz) < radius)
                                try {
                                    outputObject.add(x, y, z);
                                } catch (PointOutOfRangeException e) {
                                }
                        }
                    }
                }
            }
        }

        // Copying timepoint of input object
        outputObject.setT(inputObject.getT());

        // If adding relationships
        if (addRelationship) {
            outputObject.addParent(inputObject);
            inputObject.addChild(outputObject);
        }

        return outputObject;

    }

    protected int getRadius(Obj inputObject) {
        String radiusSource = parameters.getValue(RADIUS_SOURCE);
        double radius = parameters.getValue(FIXED_VALUE_FOR_RADIUS);
        String radiusMeasurement = parameters.getValue(RADIUS_MEASUREMENT);
        String radiusParentObjectsName = parameters.getValue(PARENT_OBJECT_FOR_RADIUS);
        String radiusUnits = parameters.getValue(RADIUS_SPATIAL_UNITS);

        switch (radiusSource) {
            case RadiusSources.MEASUREMENT:
                radius = inputObject.getMeasurement(radiusMeasurement).getValue();
                break;
            case RadiusSources.PARENT_MEASUREMENT:
                Obj parentObject = inputObject.getParent(radiusParentObjectsName);
                if (parentObject == null)
                    return -1;
                else {
                    Measurement measurement = parentObject.getMeasurement(radiusMeasurement);
                    if (measurement == null)
                        return -1;
                    radius = measurement.getValue();
                }
                break;
            case RadiusSources.SINGLE_POINT:
                return 0;
        }

        switch (radiusUnits) {
            case SpatialUnits.CALIBRATED:
                return (int) Math.round(radius / inputObject.getDppXY());
            case SpatialUnits.PIXELS:
            default:
                return (int) Math.round(radius);
        }
    }

    protected int[] getCentroid(Obj inputObject) {
        String centroidSource = parameters.getValue(CENTROID_SOURCE);
        double xPosition = parameters.getValue(X_POSITION);
        double yPosition = parameters.getValue(Y_POSITION);
        double zPosition = parameters.getValue(Z_POSITION);
        String xMeasurementName = parameters.getValue(X_MEASUREMENT);
        String yMeasurementName = parameters.getValue(Y_MEASUREMENT);
        String zMeasurementName = parameters.getValue(Z_MEASUREMENT);
        String centroidParentObjectsName = parameters.getValue(PARENT_OBJECT_FOR_CENTROID);
        String centroidUnits = parameters.getValue(CENTROID_SPATIAL_UNITS);

        if (inputObject.is2D())
            zPosition = 0;

        switch (centroidSource) {
            case CentroidSources.MEASUREMENT:
                Measurement xMeasurement = inputObject.getMeasurement(xMeasurementName);
                Measurement yMeasurement = inputObject.getMeasurement(yMeasurementName);
                Measurement zMeasurement = inputObject.getMeasurement(zMeasurementName);

                if (xMeasurement == null || yMeasurement == null || (!inputObject.is2D() && zMeasurement == null))
                    return null;

                xPosition = xMeasurement.getValue();
                yPosition = yMeasurement.getValue();
                if (!inputObject.is2D())
                    zPosition = zMeasurement.getValue();
                break;

            case CentroidSources.OBJECT_CENTROID:
                xPosition = inputObject.getXMean(true);
                yPosition = inputObject.getYMean(true);
                if (!inputObject.is2D())
                    zPosition = inputObject.getZMean(true, false);
                break;

            case CentroidSources.PARENT_MEASUREMENT:
                Obj parentObject = inputObject.getParent(centroidParentObjectsName);
                if (parentObject == null)
                    return null;
                else {
                    xMeasurement = parentObject.getMeasurement(xMeasurementName);
                    yMeasurement = parentObject.getMeasurement(yMeasurementName);
                    zMeasurement = parentObject.getMeasurement(zMeasurementName);

                    if (xMeasurement == null || yMeasurement == null || (!inputObject.is2D() && zMeasurement == null))
                        return null;

                    xPosition = xMeasurement.getValue();
                    yPosition = yMeasurement.getValue();
                    if (!inputObject.is2D())
                        zPosition = zMeasurement.getValue();
                }
                break;
        }

        switch (centroidUnits) {
            case SpatialUnits.CALIBRATED:
                xPosition = xPosition / inputObject.getDppXY();
                yPosition = yPosition / inputObject.getDppXY();
                if (!inputObject.is2D())
                    zPosition = zPosition / inputObject.getDppXY();
                break;
        }

        return new int[] { (int) Math.round(xPosition), (int) Math.round(yPosition), (int) Math.round(zPosition) };

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "Creates a local object region (sphere centred on the centroid of the input object) for each object in a specified object collection.  The radius of each local region can be based on a fixed value, or taken from an object measurement.  Local object regions are stored as children of their respective input object.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Getting input objects
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Creating store for output objects
        ObjCollection outputObjects = new ObjCollection(outputObjectsName, inputObjects);

        // Iterating over each input object, creating an output object
        for (Obj inputObject : inputObjects.values()) {
            int radius = getRadius(inputObject);
            if (radius == -1) {
                MIA.log.writeWarning("Could not get radius for object " + inputObject.getID());
                continue;
            }
                
            int[] centroid = getCentroid(inputObject);
            if (centroid == null) {
                MIA.log.writeWarning("Could not get centroid for object " + inputObject.getID());
                continue;
            }                

            // Getting local region object
            getLocalRegion(inputObject, outputObjects, centroid, radius, true);

        }

        // Adding output objects to workspace
        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(CENTROID_SEPARATOR, this));
        parameters.add(new ChoiceP(CENTROID_SOURCE, this, CentroidSources.OBJECT_CENTROID, RadiusSources.ALL));
        parameters.add(new DoubleP(X_POSITION, this, 0.0));
        parameters.add(new DoubleP(Y_POSITION, this, 0.0));
        parameters.add(new DoubleP(Z_POSITION, this, 0.0));
        parameters.add(new ObjectMeasurementP(X_MEASUREMENT, this));
        parameters.add(new ObjectMeasurementP(Y_MEASUREMENT, this));
        parameters.add(new ObjectMeasurementP(Z_MEASUREMENT, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_CENTROID, this));
        parameters.add(new ChoiceP(CENTROID_SPATIAL_UNITS, this, SpatialUnits.PIXELS, SpatialUnits.ALL));

        parameters.add(new SeparatorP(RADIUS_SEPARATOR, this));
        parameters.add(new ChoiceP(RADIUS_SOURCE, this, RadiusSources.FIXED_VALUE, RadiusSources.ALL));
        parameters.add(new DoubleP(FIXED_VALUE_FOR_RADIUS, this, 2.0));
        parameters.add(new ObjectMeasurementP(RADIUS_MEASUREMENT, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_RADIUS, this));
        parameters.add(new ChoiceP(RADIUS_SPATIAL_UNITS, this, SpatialUnits.PIXELS, SpatialUnits.ALL));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String parentObjectsForCentroidName = parameters.getValue(PARENT_OBJECT_FOR_CENTROID);
        String parentObjectsForRadiusName = parameters.getValue(PARENT_OBJECT_FOR_RADIUS);

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(CENTROID_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CENTROID_SOURCE));
        switch ((String) parameters.getValue(CENTROID_SOURCE)) {
            case CentroidSources.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(X_POSITION));
                returnedParameters.add(parameters.getParameter(Y_POSITION));
                returnedParameters.add(parameters.getParameter(Z_POSITION));

                break;

            case CentroidSources.MEASUREMENT:
                returnedParameters.add(parameters.getParameter(X_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(X_MEASUREMENT)).setObjectName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(Y_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(Y_MEASUREMENT)).setObjectName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(Z_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(Z_MEASUREMENT)).setObjectName(inputObjectsName);

                break;

            case CentroidSources.PARENT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_CENTROID));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_CENTROID))
                        .setChildObjectsName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(X_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(X_MEASUREMENT))
                        .setObjectName(parentObjectsForCentroidName);

                returnedParameters.add(parameters.getParameter(Y_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(Y_MEASUREMENT))
                        .setObjectName(parentObjectsForCentroidName);

                returnedParameters.add(parameters.getParameter(Z_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(Z_MEASUREMENT))
                        .setObjectName(parentObjectsForCentroidName);

                break;

        }

        returnedParameters.add(parameters.getParameter(CENTROID_SPATIAL_UNITS));

        returnedParameters.add(parameters.getParameter(RADIUS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RADIUS_SOURCE));
        switch ((String) parameters.getValue(RADIUS_SOURCE)) {
            case RadiusSources.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(FIXED_VALUE_FOR_RADIUS));
                break;

            case RadiusSources.MEASUREMENT:
                returnedParameters.add(parameters.getParameter(RADIUS_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(RADIUS_MEASUREMENT)).setObjectName(inputObjectsName);
                break;

            case RadiusSources.PARENT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_RADIUS));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_RADIUS))
                        .setChildObjectsName(inputObjectsName);
                returnedParameters.add(parameters.getParameter(RADIUS_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(RADIUS_MEASUREMENT))
                        .setObjectName(parentObjectsForRadiusName);
                break;
        }

        returnedParameters.add(parameters.getParameter(RADIUS_SPATIAL_UNITS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        ParentChildRefCollection returnedRelationships = new ParentChildRefCollection();

        returnedRelationships
                .add(parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS), parameters.getValue(OUTPUT_OBJECTS)));

        return returnedRelationships;

    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Object collection from the workspace for which local object regions will be generated.  One region will be generated for each object and assigned as a child of the respective input object.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output local region objects to add to the workspace.  Each local object region is a sphere with centroid coincident with the centroid of the corresponding input object.  These objects are assigned as a child of their respective input object.");

        parameters.get(RADIUS_SOURCE).setDescription("Controls how the radius of the spot is defined:<br><ul>"

                + "<li>\"" + RadiusSources.FIXED_VALUE + "\" A single radius, defined by \"" + FIXED_VALUE_FOR_RADIUS
                + "\" will be used for all objects.</li>"

                + "<li>\"" + RadiusSources.MEASUREMENT
                + "\" The radius will be equal to the value of a measurement (specified by \"" + RADIUS_MEASUREMENT
                + "\") associated with the object being measured.  Radii will potentially be different for each object.</li>"

                + "<li>\"" + RadiusSources.PARENT_MEASUREMENT
                + "\" The radius will be equal to the value of a measurement (specified by \"" + RADIUS_MEASUREMENT
                + "\") associated a parent of the object being measured (specified by \"" + PARENT_OBJECT_FOR_RADIUS
                + "\").  Radii will potentially be different for each object.</li>"

                + "<li>\"" + RadiusSources.SINGLE_POINT
                + "\" The output objects will all be a single point corresponding to the centroid of the input object.</li></ul>");

        parameters.get(FIXED_VALUE_FOR_RADIUS)
                .setDescription("Fixed spot radius to use for generating all local object regions when \""
                        + RADIUS_SOURCE + "\" is in \"" + RadiusSources.FIXED_VALUE + "\" mode.");

        parameters.get(RADIUS_MEASUREMENT).setDescription(
                "Measurement associated with the input or specified parent object.  This will be used as spot the radius for generating the local object region when \""
                        + RADIUS_SOURCE + "\" is in \"" + RadiusSources.MEASUREMENT + "\" or \""
                        + RadiusSources.PARENT_MEASUREMENT + "\" mode.");

        parameters.get(PARENT_OBJECT_FOR_RADIUS).setDescription(
                "Parent object of the input object being processed.  This parent will provide the measurement (specified by \""
                        + RADIUS_MEASUREMENT
                        + "\") to be used as the spot radius for generating the local object region when \""
                        + RADIUS_SOURCE + "\" is in \"" + RadiusSources.PARENT_MEASUREMENT + "\" mode.");

        // parameters.get(CALIBRATED_UNITS).setDescription(
        // "When selected, spot radius values (irrespective of whether they are fixed
        // values, measurements or parent measurements) are assumed to be specified in
        // calibrated units (as defined by the \""
        // + new InputControl(null).getName() + "\" parameter \"" +
        // InputControl.SPATIAL_UNIT
        // + "\"). Otherwise, pixel units are assumed.");

    }
}
