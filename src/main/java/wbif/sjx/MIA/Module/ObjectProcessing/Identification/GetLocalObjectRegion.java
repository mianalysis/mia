package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Core.InputControl;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
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

    public static final String REGION_SEPARATOR = "Local region controls";
    public static final String RADIUS_SOURCE = "Radius value source";
    public static final String FIXED_VALUE = "Fixed value";
    public static final String RADIUS_MEASUREMENT = "Radius measurement";
    public static final String PARENT_OBJECT = "Parent object";
    public static final String PARENT_RADIUS_MEASUREMENT = "Parent radius measurement";
    public static final String CALIBRATED_UNITS = "Calibrated units";

    public interface RadiusSources {
        String FIXED_VALUE = "Fixed value";
        String MEASUREMENT = "Measurement";
        String PARENT_MEASUREMENT = "Parent measurement";
        String SINGLE_POINT = "Single point";

        String[] ALL = new String[] { FIXED_VALUE, MEASUREMENT, PARENT_MEASUREMENT, SINGLE_POINT };

    }

    public GetLocalObjectRegion(ModuleCollection modules) {
        super("Get local object region", modules);
    }

    public static Obj getLocalRegion(Obj inputObject, ObjCollection outputObjects, double radius, boolean calibrated,
            boolean addRelationship) throws IntegerOverflowException {
        // Getting spatial calibration
        double dppXY = inputObject.getDppXY();
        double dppZ = inputObject.getDppZ();
        double xy_z_ratio = dppXY / dppZ;

        // Creating new object and assigning relationship to input objects
        Obj outputObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType(), inputObject.getID());

        // Getting centroid coordinates
        double xCent = inputObject.getXMean(true);
        double yCent = inputObject.getYMean(true);
        double zCent = inputObject.getZMean(true, false);

        if (radius == 0) {
            // The output object is a single point
            try {
                outputObject.add((int) Math.round(xCent), (int) Math.round(yCent), (int) Math.round(zCent));
            } catch (PointOutOfRangeException e) {
            }
        } else {
            if (calibrated) {
                int xMin = Math.max((int) Math.floor(xCent - radius / dppXY), 0);
                int xMax = Math.min((int) Math.ceil(xCent + radius / dppXY), inputObject.getWidth());
                int yMin = Math.max((int) Math.floor(yCent - radius / dppXY), 0);
                int yMax = Math.min((int) Math.ceil(yCent + radius / dppXY), inputObject.getHeight());
                int zMin = Math.max((int) Math.floor(zCent - radius / dppZ), 0);
                int zMax = Math.min((int) Math.ceil(zCent + radius / dppZ), inputObject.getNSlices());

                for (int x = xMin; x < xMax; x++) {
                    double xx = (xCent - x) * dppXY;

                    for (int y = yMin; y < yMax; y++) {
                        double yy = (yCent - y) * dppXY;

                        if (inputObject.is2D()) {
                            if (Math.sqrt(xx * xx + yy * yy) < radius) {
                                try {
                                    outputObject.add(x, y, 0);
                                } catch (PointOutOfRangeException e) {
                                }
                            }
                        } else {
                            for (int z = zMin; z < zMax; z++) {
                                double zz = (zCent - z) * dppZ;
                                if (Math.sqrt(xx * xx + yy * yy + zz * zz) < radius) {
                                    try {
                                        outputObject.add(x, y, z);
                                    } catch (PointOutOfRangeException e) {
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                int xMin = Math.max((int) Math.floor(xCent - radius), 0);
                int xMax = Math.min((int) Math.ceil(xCent + radius), inputObject.getWidth());
                int yMin = Math.max((int) Math.floor(yCent - radius), 0);
                int yMax = Math.min((int) Math.ceil(yCent + radius), inputObject.getHeight());
                int zMin = Math.max((int) Math.floor(zCent - radius * xy_z_ratio), 0);
                int zMax = Math.min((int) Math.ceil(zCent + radius * xy_z_ratio), inputObject.getNSlices());

                for (int x = xMin; x < xMax; x++) {
                    double xx = xCent - x;

                    for (int y = yMin; y < yMax; y++) {
                        double yy = yCent - y;

                        if (inputObject.is2D()) {
                            if (Math.sqrt(xx * xx + yy * yy) < radius) {
                                try {
                                    outputObject.add(x, y, 0);
                                } catch (PointOutOfRangeException e) {
                                }
                            }
                        } else {
                            for (int z = zMin; z < zMax; z++) {
                                double zz = (zCent - z) / xy_z_ratio;
                                if (Math.sqrt(xx * xx + yy * yy + zz * zz) < radius) {
                                    try {
                                        outputObject.add(x, y, z);
                                    } catch (PointOutOfRangeException e) {
                                    }
                                }
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
        String radiusSource = parameters.getValue(RADIUS_SOURCE);
        double radius = parameters.getValue(FIXED_VALUE);
        String radiusMeasurement = parameters.getValue(RADIUS_MEASUREMENT);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT);
        String parentRadiusMeasurement = parameters.getValue(PARENT_RADIUS_MEASUREMENT);
        boolean calibrated = parameters.getValue(CALIBRATED_UNITS);

        // Getting input objects
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Creating store for output objects
        ObjCollection outputObjects = new ObjCollection(outputObjectsName, inputObjects);

        // Iterating over each input object, creating an output object
        for (Obj inputObject : inputObjects.values()) {
            // Getting radius
            switch (radiusSource) {
            case RadiusSources.MEASUREMENT:
                radius = inputObject.getMeasurement(radiusMeasurement).getValue();
                break;
            case RadiusSources.PARENT_MEASUREMENT:
                Obj parentObject = inputObject.getParent(parentObjectsName);
                if (parentObject == null)
                    radius = Double.NaN;
                else
                    radius = parentObject.getMeasurement(parentRadiusMeasurement).getValue();
                break;
            case RadiusSources.SINGLE_POINT:
                radius = 0;
                break;
            }

            // Getting local region object
            getLocalRegion(inputObject, outputObjects, radius, calibrated, true);

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

        parameters.add(new SeparatorP(REGION_SEPARATOR, this));
        parameters.add(new ChoiceP(RADIUS_SOURCE, this, RadiusSources.FIXED_VALUE, RadiusSources.ALL));
        parameters.add(new DoubleP(FIXED_VALUE, this, 2.0));
        parameters.add(new ObjectMeasurementP(RADIUS_MEASUREMENT, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT, this));
        parameters.add(new ObjectMeasurementP(PARENT_RADIUS_MEASUREMENT, this));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT);

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(REGION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RADIUS_SOURCE));
        switch ((String) parameters.getValue(RADIUS_SOURCE)) {
        case RadiusSources.FIXED_VALUE:
            returnedParameters.add(parameters.getParameter(FIXED_VALUE));
            break;

        case RadiusSources.MEASUREMENT:
            returnedParameters.add(parameters.getParameter(RADIUS_MEASUREMENT));
            ((ObjectMeasurementP) parameters.getParameter(RADIUS_MEASUREMENT)).setObjectName(inputObjectsName);
            break;

        case RadiusSources.PARENT_MEASUREMENT:
            returnedParameters.add(parameters.getParameter(PARENT_OBJECT));
            ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT)).setChildObjectsName(inputObjectsName);
            returnedParameters.add(parameters.getParameter(PARENT_RADIUS_MEASUREMENT));
            ((ObjectMeasurementP) parameters.getParameter(PARENT_RADIUS_MEASUREMENT)).setObjectName(parentObjectsName);
            break;
        }
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

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

                + "<li>\"" + RadiusSources.FIXED_VALUE + "\" A single radius, defined by \"" + FIXED_VALUE
                + "\" will be used for all objects.</li>"

                + "<li>\"" + RadiusSources.MEASUREMENT
                + "\" The radius will be equal to the value of a measurement (specified by \"" + RADIUS_MEASUREMENT
                + "\") associated with the object being measured.  Radii will potentially be different for each object.</li>"

                + "<li>\"" + RadiusSources.PARENT_MEASUREMENT
                + "\" The radius will be equal to the value of a measurement (specified by \""
                + PARENT_RADIUS_MEASUREMENT + "\") associated a parent of the object being measured (specified by \""
                + PARENT_OBJECT + "\").  Radii will potentially be different for each object.</li>"

                + "<li>\"" + RadiusSources.SINGLE_POINT
                + "\" The output objects will all be a single point corresponding to the centroid of the input object.</li></ul>");

        parameters.get(FIXED_VALUE)
                .setDescription("Fixed spot radius to use for generating all local object regions when \""
                        + RADIUS_SOURCE + "\" is in \"" + RadiusSources.FIXED_VALUE + "\" mode.");

        parameters.get(RADIUS_MEASUREMENT).setDescription(
                "Measurement associated with the input object.  This will be used as spot the radius for generating the local object region when \""
                        + RADIUS_SOURCE + "\" is in \"" + RadiusSources.MEASUREMENT + "\" mode.");

        parameters.get(PARENT_OBJECT).setDescription(
                "Parent object of the input object being processed.  This parent will provide the measurement (specified by \""
                        + PARENT_RADIUS_MEASUREMENT
                        + "\") to be used as the spot radius for generating the local object region when \""
                        + RADIUS_SOURCE + "\" is in \"" + RadiusSources.PARENT_MEASUREMENT + "\" mode.");

        parameters.get(PARENT_RADIUS_MEASUREMENT).setDescription(
                "Measurement associated with a parent of the input object.  This will be used as the spot radius for generating the local object region when \""
                        + RADIUS_SOURCE + "\" is in \"" + RadiusSources.PARENT_MEASUREMENT + "\" mode.");

        parameters.get(CALIBRATED_UNITS).setDescription(
                "When selected, spot radius values (irrespective of whether they are fixed values, measurements or parent measurements) are assumed to be specified in calibrated units (as defined by the \""
                        + new InputControl(null).getName() + "\" parameter \"" + InputControl.SPATIAL_UNIT
                        + "\").  Otherwise, pixel units are assumed.");

    }
}
