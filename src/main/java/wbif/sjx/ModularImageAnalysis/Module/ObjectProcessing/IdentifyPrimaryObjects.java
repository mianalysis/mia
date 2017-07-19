// TODO: Create my own 3D filters, which don't result in 32-bit images, so I don't need to switch to 8-bit

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import fiji.threshold.Auto_Threshold;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Filters3D;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.morphology.GeodesicReconstruction;
import inra.ijpb.morphology.GeodesicReconstruction3D;
import inra.ijpb.segment.Threshold;
import inra.ijpb.watershed.ExtendedMinimaWatershed;
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
    public static final String FILL_HOLES_BOOLEAN = "Fill holes";
    public static final String FILL_HOLES_MODE = "Fill holes mode";

    private static final String FILL_HOLES_3D = "Full 3D";
    private static final String FILL_HOLES_SLICE = "Slice-by-slice in 2D";
    private static final String[] FILL_HOLES_MODES = new String[]{FILL_HOLES_3D,FILL_HOLES_SLICE};

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
        boolean fillHoles = parameters.getValue(FILL_HOLES_BOOLEAN);
        String fillHolesMode = parameters.getValue(FILL_HOLES_MODE);

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

        // Applying fill holes 3D
        if (fillHoles) {
            switch (fillHolesMode) {
                case FILL_HOLES_3D:
                    if (verbose) System.out.println("[" + moduleName + "] Applying fill holes 3D");
                    ipl.setStack(GeodesicReconstruction3D.fillHoles(ipl.getImageStack()));

                    break;

                case FILL_HOLES_SLICE:
                    if (verbose) System.out.println("[" + moduleName + "] Applying fill holes slice-by-slice");
                    for (int z = 0; z < ipl.getNSlices(); z++) {
                        for (int c = 0; c < ipl.getNChannels(); c++) {
                            for (int t = 0; t < ipl.getNFrames(); t++) {
                                ipl.setPosition(c, z, t);

                                ipl.setProcessor(GeodesicReconstruction.fillHoles(ipl.getProcessor()));

                            }
                        }
                    }

                    break;

            }
        }

        // Calculating distance map
        if (verbose) System.out.println("["+moduleName+"] Calculating distance map");
        ImagePlus distIpl = new ImagePlus("Dist",BinaryImages.distanceMap(ipl.getImageStack()));
        distIpl.getProcessor().invert();

        // Calculating extended minima and maxima
        if (verbose) System.out.println("["+moduleName+"] Applying watershed segmentation");
        ipl.setStack(ExtendedMinimaWatershed.extendedMinimaWatershed(distIpl.getImageStack(),ipl.getImageStack(),1,6,false));

        // Converting image to objects
        if (verbose) System.out.println("["+moduleName+"] Converting image to objects");
        Image tempImage = new Image("Temp image",ipl);
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
        parameters.addParameter(new Parameter(FILL_HOLES_BOOLEAN,Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(FILL_HOLES_MODE,Parameter.CHOICE_ARRAY,FILL_HOLES_MODES[0],FILL_HOLES_MODES));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(OUTPUT_OBJECT));
        returnedParameters.addParameter(parameters.getParameter(MEDIAN_FILTER_RADIUS));
        returnedParameters.addParameter(parameters.getParameter(THRESHOLD_MULTIPLIER));
        returnedParameters.addParameter(parameters.getParameter(FILL_HOLES_BOOLEAN));

        if (parameters.getValue(FILL_HOLES_BOOLEAN)) {
            returnedParameters.addParameter(parameters.getParameter(FILL_HOLES_MODE));

        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
