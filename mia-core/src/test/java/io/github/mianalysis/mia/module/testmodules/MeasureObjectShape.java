package io.github.mianalysis.mia.module.testmodules;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
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
        String THREE = "3 directions";
        String THIRTEEN = "13 directions";

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

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_SPATIAL;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
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
            reference.setDescription(
                    "Surface area of the 3D object, \"" + inputObjectsName + "\".  Measured in calibrated units.");

            reference = objectMeasurementRefs.getOrPut(Measurements.SPHERICITY);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Sphericity of the 3D object, \"" + inputObjectsName
                    + "\".  This is defined as <i>36*pi*(V^2)/(S^3)</i>, where <i>V</i> is the object volume and <i>S</i> is the surface area.");

            reference = objectMeasurementRefs.getOrPut(Measurements.EULER_NUMBER);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("The Euler number of the 3D object, \"" + inputObjectsName
                    + "\".  A detailed description of MorphoLibJ's Euler number implementation can be found \"<a href=\"https://imagej.net/plugins/morpholibj\">here</a>\".");

            reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_BREADTH_CAL);
            returnedRefs.add(reference);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("The mean breadth of the 3D object, \"" + inputObjectsName
                    + "\".  Measured in calibrated units.  A detailed description of MorphoLibJ's mean breadth calculation can be found \"<a href=\"https://imagej.net/plugins/morpholibj\">here</a>\".");

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
}
