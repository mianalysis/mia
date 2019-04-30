package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.common.Exceptions.LongOverflowException;

import java.util.HashMap;
import java.util.Iterator;

public class FillHolesByVolume extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String USE_MINIMUM_VOLUME = "Use minimum volume";
    public static final String MINIMUM_VOLUME = "Minimum size";
    public static final String USE_MAXIMUM_VOLUME = "Use maximum volume";
    public static final String MAXIMUM_VOLUME = "Maximum size";
    public static final String CALIBRATED_UNITS = "Calibrated units";


    public void process(ImagePlus ipl, double minVolume, double maxVolume, boolean calibratedUnits, int labelBitDepth)
            throws LongOverflowException {
        // If the units are calibrated, converting them to pixels
        if (calibratedUnits) {
            double dppXY = ipl.getCalibration().pixelWidth;
            double dppZ = ipl.getCalibration().pixelDepth;

            minVolume = minVolume/(dppXY*dppXY*dppZ);
            maxVolume = maxVolume/(dppXY*dppXY*dppZ);

        }

        int count = 0;
        int total = ipl.getNFrames() * ipl.getNChannels();
        int nSlices = ipl.getNSlices();
        for (int c = 1; c <= ipl.getNChannels(); c++) {
            for (int t = 1; t <= ipl.getNFrames(); t++) {
                writeMessage("Processing stack "+(++count)+" of "+total);

                // Creating the current sub-stack
                ImagePlus currStack;
                if (ipl.getNFrames() == 1) {
                    currStack = new Duplicator().run(ipl);
                } else {
                    currStack = SubHyperstackMaker.makeSubhyperstack(ipl, c + "-" + c, "1-" + nSlices, t + "-" + t);
                }

                // Applying connected components labelling
                FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(26,labelBitDepth);
                ImageStack labelIst = ffcl3D.computeLabels(currStack.getStack());

                // Counting the number of instances of each label
                HashMap<Integer,Long> labels = new HashMap<>();
                for (int x=0;x<labelIst.getWidth();x++) {
                    for (int y=0;y<labelIst.getHeight();y++) {
                        for (int z=0;z<labelIst.getSize();z++) {
                            int label = (int) labelIst.getVoxel(x,y,z);
                            labels.putIfAbsent(label,0l);
                            labels.put(label,labels.get(label)+1);
                            if (labels.get(label) == Long.MAX_VALUE) throw new LongOverflowException("Too many pixels (Long overflow).");
                        }
                    }
                }

                // Removing pixels with counts outside the limits
                Iterator<Integer> iterator = labels.keySet().iterator();
                while (iterator.hasNext()) {
                    int label = iterator.next();
                    long nPixels = labels.get(label);
                    if (nPixels >= minVolume && nPixels <= maxVolume) iterator.remove();
                }

                // Binarising the input image based on whether the label is still in the list
                for (int z=0;z<nSlices;z++) {
                    ipl.setPosition(c,z+1,t);
                    ImageProcessor ipr = ipl.getProcessor();
                    for (int x=0;x<labelIst.getWidth();x++) {
                        for (int y=0;y<labelIst.getHeight();y++) {
                            int label = (int) labelIst.getVoxel(x,y,z);
                            if (labels.containsKey(label)) ipr.setf(x,y,0);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getTitle() {
        return "Fill holes by volume";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getHelp() {
        return "Larger label bit depths will require more memory, but will enable more objects " +
                "\nto be detected (8-bit = 255 objects, 16-bit = 65535 objects, 32-bit = (near) unlimited.";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean useMinVolume = parameters.getValue(USE_MINIMUM_VOLUME);
        boolean useMaxVolume = parameters.getValue(USE_MAXIMUM_VOLUME);
        double minVolume = parameters.getValue(MINIMUM_VOLUME);
        double maxVolume = parameters.getValue(MAXIMUM_VOLUME);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);

        if (!useMinVolume) minVolume = -Float.MAX_VALUE;
        if (!useMaxVolume) maxVolume = Float.MAX_VALUE;
        try {
            try {
                process(inputImagePlus,minVolume,maxVolume,calibratedUnits,16);
            } catch (RuntimeException e2) {
                System.err.println("Maximum number of labels reached while filling holes.  Switching to 32-bit mode.  No action necessary.");
                process(inputImagePlus,minVolume,maxVolume,calibratedUnits,32);
            }
        } catch (LongOverflowException e) {
            return false;
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeMessage("Adding image ("+outputImageName+") to workspace");
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage();

        } else {
            if (showOutput) inputImage.showImage();

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new BooleanP(USE_MINIMUM_VOLUME, this,true));
        parameters.add(new DoubleP(MINIMUM_VOLUME, this,0d));
        parameters.add(new BooleanP(USE_MAXIMUM_VOLUME, this,true));
        parameters.add(new DoubleP(MAXIMUM_VOLUME, this,1000d));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(USE_MINIMUM_VOLUME));
        if (parameters.getValue(USE_MINIMUM_VOLUME)) {
            returnedParameters.add(parameters.getParameter(MINIMUM_VOLUME));
        }
        returnedParameters.add(parameters.getParameter(USE_MAXIMUM_VOLUME));
        if (parameters.getValue(USE_MAXIMUM_VOLUME)) {
            returnedParameters.add(parameters.getParameter(MAXIMUM_VOLUME));
        }

        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
