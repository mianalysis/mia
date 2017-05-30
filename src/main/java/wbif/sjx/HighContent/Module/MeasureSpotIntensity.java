package wbif.sjx.HighContent.Module;

import ij.ImagePlus;
import wbif.sjx.HighContent.Object.*;
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
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting image to measure spot intensity for
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting objects to measure
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        double radius = parameters.getValue(MEASUREMENT_RADIUS);
        boolean calibrated = parameters.getValue(CALIBRATED_RADIUS);

        // Getting local object region (this overwrites the original inputObjects)
        inputObjects = GetLocalObjectRegion.getLocalRegions(inputObjects, inputObjectsName, radius, calibrated);

        // Running through each object's timepoints, getting intensity measurements
        for (HCObject object:inputObjects.values()) {
            // Getting pixel coordinates
            ArrayList<Integer> x = object.getCoordinates(HCObject.X);
            ArrayList<Integer> y = object.getCoordinates(HCObject.Y);
            ArrayList<Integer> z = object.getCoordinates(HCObject.Z);
            Integer c = object.getCoordinates(HCObject.C);
            Integer t = object.getCoordinates(HCObject.T);

            // Initialising the cumulative statistics object to store pixel intensities.  Unlike MeasureObjectIntensity,
            // this uses a multi-element CumStat where each element corresponds to a different frame
            CumStat cs = new CumStat(1);

            // Running through all pixels in this object and adding the intensity to the CumStat object
            for (int i=0;i<x.size();i++) {
                int zPos = z==null ? 0 : z.get(i);
                int cPos = c==null ? 0 : c;
                int tPos = t==null ? 0 : t;

                ipl.setPosition(cPos+1,zPos+1,tPos+1);
                cs.addMeasure(ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));

            }

            // Calculating mean, std, min and max intensity and adding to the parent (we will discard the expanded
            // objects after this module has run)
            HCMeasurement meanIntensity = new HCMeasurement(inputImageName.getName()+"_MEAN", cs.getMean()[0]);
            meanIntensity.setSource(this);
            object.getParent().addMeasurement(meanIntensity);

            HCMeasurement stdIntensity = new HCMeasurement(inputImageName.getName()+"_STD", cs.getStd(CumStat.SAMPLE)[0]);
            stdIntensity.setSource(this);
            object.getParent().addMeasurement(stdIntensity);

            HCMeasurement minIntensity = new HCMeasurement(inputImageName.getName()+"_MIN", cs.getMin()[0]);
            minIntensity.setSource(this);
            object.getParent().addMeasurement(minIntensity);

            HCMeasurement maxIntensity = new HCMeasurement(inputImageName.getName()+"_MAX", cs.getMax()[0]);
            maxIntensity.setSource(this);
            object.getParent().addMeasurement(maxIntensity);

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(INPUT_OBJECTS,HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(CALIBRATED_RADIUS, HCParameter.BOOLEAN,false));
        parameters.addParameter(new HCParameter(MEASUREMENT_RADIUS, HCParameter.DOUBLE,2.0));
        

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        return parameters;
        
    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),inputImageName+"_MEAN");
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),inputImageName+"_STD");
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),inputImageName+"_MIN");
        measurements.addMeasurement(parameters.getValue(INPUT_OBJECTS),inputImageName+"_MAX");

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
