package io.github.mianalysis.mia.module.visualise.overlays;

import java.awt.Color;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;


/**
* Adds an overlay to the specified input image representing each object by a single marker.  Unlike "Add object centroid" the position of the marker is determined by measurements associated with the relevant object.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class AddFromPositionMeasurement extends AbstractOverlay {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image and object input";

	/**
	* Image onto which overlay will be rendered.  Input image will only be updated if "Apply to input image" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by "Output image".
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* Objects to represent as overlays.
	*/
    public static final String INPUT_OBJECTS = "Input objects";


	/**
	* 
	*/
    public static final String OUTPUT_SEPARATOR = "Image output";

	/**
	* Determines if the modifications made to the input image (added overlay elements) will be applied to that image or directed to a new image.  When selected, the input image will be updated.
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.
	*/
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";

	/**
	* The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).
	*/
    public static final String OUTPUT_IMAGE = "Output image";


	/**
	* 
	*/
    public static final String POSITION_SEPARATOR = "Overlay position";

	/**
	* Object measurement specifying the X-position of the overlay marker.  Measurement value must be specified in pixel units.
	*/
    public static final String X_POSITION_MEASUREMENT = "X-position measurement";

	/**
	* Object measurement specifying the Y-position of the overlay marker.  Measurement value must be specified in pixel units.
	*/
    public static final String Y_POSITION_MEASUREMENT = "Y-position measurement";

	/**
	* Object measurement specifying the Z-position (slice) of the overlay marker.  Measurement value must be specified in slice units.
	*/
    public static final String Z_POSITION_MEASUREMENT = "Z-position measurement";

	/**
	* When selected, the radius of the overlay marker circle is controlled by the measurement specified by "Measurement for radius".  When not selected, marker size is controlled by the "Point size" parameter.
	*/
    public static final String USE_RADIUS = "Use radius measurement";

	/**
	* Object measurement use to specify the radius of the overlay marker circle.  Measurement value must be specified in pixel units.
	*/
    public static final String MEASUREMENT_FOR_RADIUS = "Measurement for radius";


	/**
	* 
	*/
    public static final String RENDERING_SEPARATOR = "Overlay rendering";

	/**
	* Width of the rendered lines.  Specified in pixel units.
	*/
    public static final String LINE_WIDTH = "Line width";

	/**
	* Size of each overlay marker.  Choices are: Tiny, Small, Medium, Large, Extra large.
	*/
    public static final String POINT_SIZE = "Point size";

	/**
	* Type of overlay marker used to represent each object.  Choices are: Circle, Cross, Dot, Hybrid.
	*/
    public static final String POINT_TYPE = "Point type";

	/**
	* Display the overlay elements in all frames (time axis) of the input image stack, irrespective of whether the object was present in that frame.
	*/
    public static final String RENDER_IN_ALL_FRAMES = "Render in all frames";


	/**
	* 
	*/
    public static final String EXECUTION_SEPARATOR = "Execution controls";

	/**
	* Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.
	*/
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface PointSizes extends AddObjectCentroid.PointSizes {}

    public interface PointTypes extends AddObjectCentroid.PointTypes {}

    public AddFromPositionMeasurement(Modules modules) {
        super("Add from position measurement",modules);
    }


    public static void addOverlay(Obj object, ImagePlus ipl, Color colour, String size, String type, double lineWidth, String[] posMeasurements, @Nullable String radiusMeasurement, boolean renderInAllFrames) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new ij.gui.Overlay());

        double xMean = object.getMeasurement(posMeasurements[0]).getValue();
        double yMean = object.getMeasurement(posMeasurements[1]).getValue();
        double zMean = object.getMeasurement(posMeasurements[2]).getValue();

        // Getting coordinates and settings for plotting
        int z = (int) Math.round(zMean+1);
        int t = object.getT()+1;
        int sizeVal = AddObjectCentroid.getSize(size);
        int typeVal = AddObjectCentroid.getType(type);

        if (renderInAllFrames) t = 0;
        if (radiusMeasurement == null) {
            PointRoi pointRoi = new PointRoi(xMean,yMean);
            pointRoi.setPointType(typeVal);
            pointRoi.setSize(sizeVal);
            if (ipl.isHyperStack()) {
                pointRoi.setPosition(1, z, t);
            } else {
                int pos = Math.max(Math.max(1,z),t);
                pointRoi.setPosition(pos);
            }
            pointRoi.setStrokeColor(colour);
            ipl.getOverlay().addElement(pointRoi);

        } else {
            double r = object.getMeasurement(radiusMeasurement).getValue();
            OvalRoi ovalRoi = new OvalRoi(xMean - r, yMean - r, 2 * r, 2 * r);
            if (ipl.isHyperStack()) {
                ovalRoi.setPosition(1, z, t);
            } else {
                int pos = Math.max(Math.max(1, z), t);
                ovalRoi.setPosition(pos);
            }
            ovalRoi.setStrokeColor(colour);
            ovalRoi.setStrokeWidth(lineWidth);
            ipl.getOverlay().addElement(ovalRoi);
        }
    }



    @Override
    public Category getCategory() {
        return Categories.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Adds an overlay to the specified input image representing each object by a single marker.  Unlike \""+new AddObjectCentroid(null).getName()+"\" the position of the marker is determined by measurements associated with the relevant object.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        String xPosMeas = parameters.getValue(X_POSITION_MEASUREMENT,workspace);
        String yPosMeas = parameters.getValue(Y_POSITION_MEASUREMENT,workspace);
        String zPosMeas = parameters.getValue(Z_POSITION_MEASUREMENT,workspace);
        boolean useRadius = parameters.getValue(USE_RADIUS,workspace);
        String tempRadiusMeasurement = parameters.getValue(MEASUREMENT_FOR_RADIUS,workspace);

        String pointSize = parameters.getValue(POINT_SIZE,workspace);
        String pointType = parameters.getValue(POINT_TYPE,workspace);
        double lineWidth = parameters.getValue(LINE_WIDTH,workspace);
        boolean renderInAllFrames = parameters.getValue(RENDER_IN_ALL_FRAMES,workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING,workspace);

        // Only add output to workspace if not applying to input
        if (applyToInput) addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput) ipl = new Duplicator().run(ipl);

        // Setting position measurements
        final String radiusMeasurement = useRadius ? tempRadiusMeasurement : null;
        String[] posMeasurements = new String[]{xPosMeas, yPosMeas, zPosMeas};

        // Generating colours for each object
        HashMap<Integer,Color> colours = getColours(inputObjects, workspace);

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        // Adding the overlay element
            int nThreads = multithread ? Prefs.getThreads() : 1;
            ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

            // Running through each object, adding it to the overlay along with an ID label
            AtomicInteger count = new AtomicInteger();
            for (Obj object:inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    Color colour = colours.get(object.getID());
                
                    addOverlay(object, finalIpl, colour, pointSize, pointType, lineWidth, posMeasurements, radiusMeasurement, renderInAllFrames);

                    writeProgressStatus(count.incrementAndGet(), inputObjects.size(), "objects");
                    
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

        Image outputImage = ImageFactory.createImage(outputImageName,ipl);

        // If necessary, adding output image to workspace.  This also allows us to show it.
        if (addOutputToWorkspace) workspace.addImage(outputImage);
        if (showOutput) outputImage.show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this,false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(POSITION_SEPARATOR,this));
        parameters.add(new ObjectMeasurementP(X_POSITION_MEASUREMENT, this));
        parameters.add(new ObjectMeasurementP(Y_POSITION_MEASUREMENT, this));
        parameters.add(new ObjectMeasurementP(Z_POSITION_MEASUREMENT, this));
        parameters.add(new BooleanP(USE_RADIUS, this,true));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_RADIUS, this));

        parameters.add(new SeparatorP(RENDERING_SEPARATOR,this));
        parameters.add(new DoubleP(LINE_WIDTH,this,1));
        parameters.add(new ChoiceP(POINT_SIZE,this,PointSizes.SMALL,PointSizes.ALL));
        parameters.add(new ChoiceP(POINT_TYPE,this,PointTypes.CIRCLE,PointTypes.ALL));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES,this,false));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR,this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);

        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE,workspace)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

            }
        }

        returnedParameters.add(parameters.getParameter(POSITION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT));
        returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT));
        returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT));

        ((ObjectMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(Z_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);

        returnedParameters.add(parameters.getParameter(USE_RADIUS));
        if ((boolean) parameters.getValue(USE_RADIUS,workspace)) {
            returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_RADIUS));
            ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_RADIUS)).setObjectName(inputObjectsName);
        }
        
        returnedParameters.addAll(super.updateAndGetParameters(inputObjectsName));

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        if ((boolean) parameters.getValue(USE_RADIUS,workspace)) {            
            returnedParameters.add(parameters.getParameter(LINE_WIDTH));
        } else {
            returnedParameters.add(parameters.getParameter(POINT_SIZE));
            returnedParameters.add(parameters.getParameter(POINT_TYPE));
        }
        returnedParameters.add(parameters.getParameter(RENDER_IN_ALL_FRAMES));

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

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();
        
        parameters.get(INPUT_IMAGE).setDescription("Image onto which overlay will be rendered.  Input image will only be updated if \""+APPLY_TO_INPUT+"\" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by \""+OUTPUT_IMAGE+"\".");

        parameters.get(INPUT_OBJECTS).setDescription("Objects to represent as overlays.");

        parameters.get(APPLY_TO_INPUT).setDescription("Determines if the modifications made to the input image (added overlay elements) will be applied to that image or directed to a new image.  When selected, the input image will be updated.");

        parameters.get(ADD_OUTPUT_TO_WORKSPACE).setDescription("If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription("The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).");

        parameters.get(X_POSITION_MEASUREMENT).setDescription("Object measurement specifying the X-position of the overlay marker.  Measurement value must be specified in pixel units.");

        parameters.get(Y_POSITION_MEASUREMENT).setDescription("Object measurement specifying the Y-position of the overlay marker.  Measurement value must be specified in pixel units.");

        parameters.get(Z_POSITION_MEASUREMENT).setDescription("Object measurement specifying the Z-position (slice) of the overlay marker.  Measurement value must be specified in slice units.");

        parameters.get(USE_RADIUS).setDescription("When selected, the radius of the overlay marker circle is controlled by the measurement specified by \""+MEASUREMENT_FOR_RADIUS+"\".  When not selected, marker size is controlled by the \""+POINT_SIZE+"\" parameter.");

        parameters.get(MEASUREMENT_FOR_RADIUS).setDescription("Object measurement use to specify the radius of the overlay marker circle.  Measurement value must be specified in pixel units.");

        parameters.get(LINE_WIDTH).setDescription("Width of the rendered lines.  Specified in pixel units.");

        parameters.get(POINT_SIZE).setDescription("Size of each overlay marker.  Choices are: "+String.join(", ", PointSizes.ALL)+".");

        parameters.get(POINT_TYPE).setDescription("Type of overlay marker used to represent each object.  Choices are: "+String.join(", ", PointTypes.ALL)+".");

        parameters.get(RENDER_IN_ALL_FRAMES).setDescription("Display the overlay elements in all frames (time axis) of the input image stack, irrespective of whether the object was present in that frame.");

        parameters.get(ENABLE_MULTITHREADING).setDescription("Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
