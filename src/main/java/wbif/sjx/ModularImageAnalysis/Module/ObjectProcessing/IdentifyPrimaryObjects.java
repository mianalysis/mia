// TODO: Create my own 3D filters, which don't result in 32-bit images, so I don't need to switch to 8-bit

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import fiji.threshold.Auto_Threshold;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Filters3D;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import inra.ijpb.segment.Threshold;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.ParameterCollection;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class IdentifyPrimaryObjects extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECT = "Output object";
    public static final String MEDIAN_FILTER_RADIUS = "Median filter radius";
    public static final String THRESHOLD_MULTIPLIER = "Threshold multiplier";

    @Override
    public String getTitle() {
        return "Identify primary objects";

    }

    @Override
    public String getHelp() {
        return null;
    }

    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting parameters
        double medFiltR = parameters.getValue(MEDIAN_FILTER_RADIUS);
        double thrMult = parameters.getValue(THRESHOLD_MULTIPLIER);
        String outputObjectName = parameters.getValue(OUTPUT_OBJECT);

        // Getting image stack
        String targetImageName = parameters.getValue(INPUT_IMAGE);
        ImagePlus ipl = workspace.getImages().get(targetImageName).getImagePlus();

        // Applying smoothing filter
        if (verbose) System.out.println("["+moduleName+"] Applying filter (radius = "+medFiltR+" px)");
        ipl.setStack(Filters3D.filter(ipl.getImageStack(), Filters3D.MEDIAN, (float) medFiltR, (float) medFiltR, (float) medFiltR));

        // Converting to 8-bit object
        IntensityMinMax.run(ipl,true);
        IJ.run(ipl,"8-bit",null);

        // Applying threshold
        if (verbose) System.out.println("["+moduleName+"] Applying thresholding (multplier = "+thrMult+" x)");
        Auto_Threshold auto_threshold = new Auto_Threshold();
        Object[] results1 = auto_threshold.exec(ipl,"Otsu",true,false,true,true,false,true);
        ipl = Threshold.threshold(ipl,(Integer) results1[0]*thrMult,Integer.MAX_VALUE);

        // Applying connected components labelling
        if (verbose) System.out.println("["+moduleName+"] Applying connected components labelling");
        FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D();
        ipl.setStack(ffcl3D.computeLabels(ipl.getImageStack()));

        // Converting image to objects
        if (verbose) System.out.println("["+moduleName+"] Converting image to objects");
        Image tempImage = new Image(new String("Temp image"),ipl);
        ObjSet outputObjects = new ObjectImageConverter().convertImageToObjects(tempImage,outputObjectName);

        // Adding objects to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding objects ("+outputObjectName+") to workspace");
        workspace.addObjects(outputObjects);

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        // Setting the input image stack name
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(OUTPUT_OBJECT, Parameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(MEDIAN_FILTER_RADIUS, Parameter.DOUBLE,2.0));
        parameters.addParameter(new Parameter(THRESHOLD_MULTIPLIER, Parameter.DOUBLE,1.0));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
