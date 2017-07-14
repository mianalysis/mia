package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.GetLocalObjectRegion;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.ArrayList;

/**
 * Similar to MeasureObjectIntensity, but performed on circular (or spherical) regions of interest around each point in
 * 3D.  Allows the user to specify the region around each point to be measured.  Intensity traces are stored as
 * HCMultiMeasurements
 */
public class MeasureSpotIntensity extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input spot objects";
    public static final String MEASUREMENT_RADIUS = "Measurement radius";
    public static final String CALIBRATED_RADIUS = "Calibrated radius";


    @Override
    public String getTitle() {
        return "Measure spot intensity";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting image to measure spot intensity for
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        double radius = parameters.getValue(MEASUREMENT_RADIUS);
        boolean calibrated = parameters.getValue(CALIBRATED_RADIUS);

        // Getting local object region (this overwrites the original inputObjects)
        inputObjects = GetLocalObjectRegion.getLocalRegions(inputObjects, inputObjectsName, radius, calibrated);

        // Running through each object's timepoints, getting intensity measurements
        for (Obj inputObject:inputObjects.values()) {
            // Getting pixel coordinates
            ArrayList<Integer> x = inputObject.getCoordinates(Obj.X);
            ArrayList<Integer> y = inputObject.getCoordinates(Obj.Y);
            ArrayList<Integer> z = inputObject.getCoordinates(Obj.Z);
            Integer c = inputObject.getCoordinates(Obj.C);
            Integer t = inputObject.getCoordinates(Obj.T);

            // Initialising the cumulative statistics object to store pixel intensities.  Unlike MeasureObjectIntensity,
            // this uses a multi-element MultiCumStat where each element corresponds to a different frame
            CumStat cs = new CumStat();

            // Running through all pixels in this object and adding the intensity to the MultiCumStat object
            for (int i=0;i<x.size();i++) {
                int zPos = z==null ? 0 : z.get(i);
                int cPos = c==null ? 0 : c;
                int tPos = t==null ? 0 : t;

                ipl.setPosition(cPos+1,zPos+1,tPos+1);
                cs.addMeasure(ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));

            }

            // Calculating mean, std, min and max intensity and adding to the parent (we will discard the expanded
            // objects after this module has run)
            MIAMeasurement meanIntensity = new MIAMeasurement(inputImageName+"_MEAN", cs.getMean());
            meanIntensity.setSource(this);
            inputObject.getParent(inputObjectsName).addMeasurement(meanIntensity);

            MIAMeasurement stdIntensity = new MIAMeasurement(inputImageName+"_STD", cs.getStd(CumStat.SAMPLE));
            stdIntensity.setSource(this);
            inputObject.getParent(inputObjectsName).addMeasurement(stdIntensity);

            MIAMeasurement minIntensity = new MIAMeasurement(inputImageName+"_MIN", cs.getMin());
            minIntensity.setSource(this);
            inputObject.getParent(inputObjectsName).addMeasurement(minIntensity);

            MIAMeasurement maxIntensity = new MIAMeasurement(inputImageName+"_MAX", cs.getMax());
            maxIntensity.setSource(this);
            inputObject.getParent(inputObjectsName).addMeasurement(maxIntensity);

        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(CALIBRATED_RADIUS, Parameter.BOOLEAN,false));
        parameters.addParameter(new Parameter(MEASUREMENT_RADIUS, Parameter.DOUBLE,2.0));
        

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
        
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),inputImageName+"_MEAN");
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),inputImageName+"_STD");
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),inputImageName+"_MIN");
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),inputImageName+"_MAX");

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
