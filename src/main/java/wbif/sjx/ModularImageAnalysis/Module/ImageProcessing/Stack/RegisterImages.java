package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.*;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.ij.Mapping;
import mpicbg.ij.SIFT;
import mpicbg.ij.util.Util;
import mpicbg.imagefeatures.Feature;
import mpicbg.imagefeatures.FloatArray2DSIFT;
import mpicbg.models.*;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;

import com.drew.lang.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RegisterImages extends Module implements ActionListener {
    private JFrame frame;
    private JTextField objectNumberField;
    private final JPanel objectsPanel = new JPanel();
    JScrollPane objectsScrollPane = new JScrollPane(objectsPanel);
    private final GridBagConstraints objectsC = new GridBagConstraints();

    private ImagePlus displayImagePlus1;
    private ImagePlus displayImagePlus2;
    private Overlay overlay1;
    private Overlay overlay2;
    private ArrayList<PointPair> pairs;

    private int elementHeight = 40;

    private static final String ADD_PAIR = "Add pair";
    private static final String FINISH = "Finish";

    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String ALIGNMENT_MODE = "Alignment mode";
    public static final String RELATIVE_MODE = "Relative mode";
    public static final String ROLLING_CORRECTION = "Rolling correction";
    public static final String CORRECTION_INTERVAL = "Correction interval";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String CALCULATION_SOURCE = "Calculation source";
    public static final String EXTERNAL_SOURCE = "External source";
    public static final String CALCULATION_CHANNEL = "Calculation channel";
    public static final String TRANSFORMATION_MODE = "Transformation mode";
    public static final String INITIAL_SIGMA = "Initial Gaussian blur (px)";
    public static final String STEPS = "Steps per scale";
    public static final String MINIMUM_IMAGE_SIZE = "Minimum image size (px)";
    public static final String MAXIMUM_IMAGE_SIZE = "Maximum image size (px)";
    public static final String FD_SIZE = "Feature descriptor size";
    public static final String FD_ORIENTATION_BINS = "Feature descriptor orientation bins";
    public static final String ROD = "Closest/next closest ratio";
    public static final String MAX_EPSILON = "Maximal alignment error (px)";
    public static final String MIN_INLIER_RATIO = "Inlier ratio";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";


    public interface AlignmentModes {
        final String AUTOMATIC = "Automatic (feature extraction)";
        final String MANUAL = "Manual (landmarks)";

        final String[] ALL = new String[]{AUTOMATIC,MANUAL};

    }

    public interface RelativeModes {
        final String FIRST_FRAME = "First frame";
        final String PREVIOUS_FRAME = "Previous frame";
        final String SPECIFIC_IMAGE = "Specific image";

        final String[] ALL = new String[]{FIRST_FRAME,PREVIOUS_FRAME,SPECIFIC_IMAGE};

    }

    public interface RollingCorrectionModes {
        final String NONE = "None";
        final String EVERY_NTH_FRAME = "Every nth frame";

        final String[] ALL = new String[]{NONE,EVERY_NTH_FRAME};

    }

    public interface CalculationSources {
        String INTERNAL = "Internal";
        String EXTERNAL = "External";

        String[] ALL = new String[]{INTERNAL,EXTERNAL};

    }

    public interface TransformationModes {
        String AFFINE = "Affine";
        String RIGID = "Rigid";
        String SIMILARITY = "Similarity";
        String TRANSLATION = "Translation";

        String[] ALL = new String[]{AFFINE,RIGID,SIMILARITY,TRANSLATION};

    }


    private void showOptionsPanel() {
        pairs  = new ArrayList<>();
        frame = new JFrame();

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5,5,5,5);

        JLabel headerLabel = new JLabel("<html>Add a point to each image, then select \"Add pair\"" +
                "<br>(or click \"Finish adding pairs\" at any time).</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        frame.add(headerLabel,c);

        JButton addPairButton = new JButton("Add pair");
        addPairButton.addActionListener(this);
        addPairButton.setActionCommand(ADD_PAIR);
        c.gridy++;
        c.gridwidth = 1;
        frame.add(addPairButton,c);

        JButton finishButton = new JButton("Finish adding pairs");
        finishButton.addActionListener(this);
        finishButton.setActionCommand(FINISH);
        c.gridx++;
        frame.add(finishButton,c);

        // Object number panel
        objectsC.gridx = 0;
        objectsC.gridy = 0;
        objectsC.weightx = 1;
        objectsC.weighty = 1;
        objectsC.anchor = GridBagConstraints.NORTHWEST;
        objectsC.fill = GridBagConstraints.HORIZONTAL;
        objectsPanel.setLayout(new GridBagLayout());

        objectsScrollPane.setPreferredSize(new Dimension(0,200));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        frame.add(objectsScrollPane,c);

        JCheckBox overlayCheck = new JCheckBox("Display overlay");
        overlayCheck.setSelected(true);
        overlayCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayImagePlus1.setHideOverlay(!overlayCheck.isSelected());
                displayImagePlus2.setHideOverlay(!overlayCheck.isSelected());
            }
        });
        c.gridy++;
        c.gridy++;
        c.gridy++;
        c.gridwidth = 1;
        c.gridheight = 1;
        frame.add(overlayCheck,c);

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

    }

    public void processAutomatic(Image inputImage, int calculationChannel, String relativeMode, Param param, int correctionInterval, boolean multithread, @Nullable Image reference, @Nullable Image externalSource) {
        // Creating a reference image
        Image projectedReference = null;

        // Assigning source image
        Image source = externalSource == null ? inputImage : externalSource;

        // Assigning fixed reference images
        switch (relativeMode) {
            case RelativeModes.FIRST_FRAME:
                reference = ExtractSubstack.extractSubstack(source, "Reference", String.valueOf(calculationChannel), "1-end", "1");
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);
                break;

            case RelativeModes.SPECIFIC_IMAGE:
                if (reference == null) return;
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);
                break;
        }

        // Iterate over each time-step
        int count = 0;
        int total = source.getImagePlus().getNFrames();
        for (int t = 1; t <= source.getImagePlus().getNFrames(); t++) {
            writeMessage("Processing timepoint "+(++count)+" of "+total);

            // If the reference image is the previous frame, calculate this now
            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME)) {
                // Can't processAutomatic if this is the first frame
                if (t == 1) continue;

                reference = ExtractSubstack.extractSubstack(source, "Reference", String.valueOf(calculationChannel), "1-end", String.valueOf(t - 1));
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);
            }

            // Getting the projected image at this time-point
            Image warped = ExtractSubstack.extractSubstack(source, "Warped", String.valueOf(calculationChannel), "1-end", String.valueOf(t));
            Image projectedWarped = ProjectImage.projectImageInZ(warped, "ProjectedWarped", ProjectImage.ProjectionModes.MAX);

            // Calculating the transformation for this image pair
            if (projectedReference == null) return;

            Mapping mapping = getFeatureTransformation(projectedReference,projectedWarped,param);

            int t2 = t;
            switch (relativeMode) {
                case UnwarpImages.RelativeModes.PREVIOUS_FRAME:
                    if (correctionInterval != -1 && t%correctionInterval == 0) {
                        t2 = source.getImagePlus().getNFrames();
                    }
                    break;
            }

            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same transformation.
            for (int tt = t; tt <= t2; tt++) {
                for (int c = 1; c <= inputImage.getImagePlus().getNChannels(); c++) {
                    warped = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(c), "1-end", String.valueOf(tt));
                    try {
                        applyTransformation(warped, mapping,multithread);
                    } catch (InterruptedException e) {
                        return;
                    }
                    replaceStack(inputImage, warped, c, tt);
                }
            }

            // Need to apply the warp to an external image
            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME) && externalSource != null) {
                for (int tt = t; tt <= t2; tt++) {
                    for (int c = 1; c <= source.getImagePlus().getNChannels(); c++) {
                        warped = ExtractSubstack.extractSubstack(source, "Warped", String.valueOf(c), "1-end", String.valueOf(tt));
                        try {
                            applyTransformation(warped, mapping,multithread);
                        } catch (InterruptedException e) {
                            return;
                        }
                        replaceStack(source, warped, c, tt);
                    }
                }
            }

            mapping = null;

        }
    }

    public void processManual(Image inputImage, String transformationMode, boolean multithread, Image reference) {
        // Creating a reference image
        Image projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);

        // Creating a projection of the main image
        Image projectedWarped = ProjectImage.projectImageInZ(inputImage, "ProjectedWarped", ProjectImage.ProjectionModes.MAX);

        // Displaying the images and options panel.  While the control is open, do nothing
        IJ.setTool(Toolbar.POINT);

        displayImagePlus1 = new Duplicator().run(projectedReference.getImagePlus());
        displayImagePlus1.setTitle("Select points on this image");
        displayImagePlus1.show();
        overlay1 = displayImagePlus1.getOverlay();
        if (overlay1 == null) {
            overlay1 = new Overlay();
            displayImagePlus1.setOverlay(overlay1);
        }

        displayImagePlus2 = new Duplicator().run(projectedWarped.getImagePlus());
        displayImagePlus2.setTitle("Select points on this image");
        displayImagePlus2.show();
        overlay2 = displayImagePlus2.getOverlay();
        if (overlay2 == null) {
            overlay2 = new Overlay();
            displayImagePlus2.setOverlay(overlay2);
        }

        showOptionsPanel();
        while (frame != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Getting transform
        Mapping mapping = getLandmarkTransformation(pairs,transformationMode);

        // Iterate over each time-step
        int count = 0;
        int total = inputImage.getImagePlus().getNFrames();
        for (int t = 1; t <= inputImage.getImagePlus().getNFrames(); t++) {
            writeMessage("Processing timepoint "+(++count)+" of "+total);

            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same transformation.
            for (int c = 1; c <= inputImage.getImagePlus().getNChannels(); c++) {
                Image warped = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(c), "1-end", String.valueOf(t));
                try {
                    applyTransformation(warped, mapping,multithread);
                } catch (InterruptedException e) {
                    return;
                }
                replaceStack(inputImage, warped, c, t);
            }

            mapping = null;

        }
    }

    private static AbstractAffineModel2D getModel(String transformationMode) {
        switch (transformationMode) {
            case TransformationModes.AFFINE:
                return new AffineModel2D();
            case TransformationModes.RIGID:
            default:
                return new RigidModel2D();
            case TransformationModes.SIMILARITY:
                return new SimilarityModel2D();
            case TransformationModes.TRANSLATION:
                return new TranslationModel2D();
        }
    }

    public static Mapping getFeatureTransformation(Image referenceImage, Image warpedImage, Param param) {
        ImagePlus referenceIpl = referenceImage.getImagePlus();
        ImagePlus warpedIpl = warpedImage.getImagePlus();

        // Initialising SIFT feature extractor
        FloatArray2DSIFT sift = new FloatArray2DSIFT(param);
        SIFT ijSIFT = new SIFT(sift);

        // Extracting features
        ArrayList<Feature> featureList1 = new ArrayList<Feature>();
        ijSIFT.extractFeatures(referenceIpl.getProcessor(),featureList1);
        ArrayList<Feature> featureList2 = new ArrayList<Feature>();
        ijSIFT.extractFeatures(warpedIpl.getProcessor(),featureList2);

        // Running registration
        AbstractAffineModel2D model = getModel(param.transformationMode);

        Mapping mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>(model);
        Vector<PointMatch> candidates = FloatArray2DSIFT.createMatches(featureList2,featureList1,1.5f,null, Float.MAX_VALUE,param.rod);
        Vector<PointMatch> inliers = new Vector<PointMatch>();

        try {
            model.filterRansac(candidates,inliers,1000,param.maxEpsilon,param.minInlierRatio);
        } catch (NotEnoughDataPointsException e) {
            e.printStackTrace();
            return null;
        }

        return mapping;

    }

    public static Mapping getLandmarkTransformation(List<PointPair> pairs, String transformationMode) {
        // Getting registration model
        AbstractAffineModel2D model = getModel(transformationMode);

        Mapping mapping = new InverseTransformMapping<AbstractAffineModel2D<?>>(model);
        final ArrayList< PointMatch > candidates = new ArrayList< PointMatch >();

        for (PointPair pair:pairs) {
            candidates.addAll(Util.pointRoisToPointMatches(pair.getPoint1(),pair.getPoint2()));
        }

        try {
            model.fit(candidates);
        } catch (NotEnoughDataPointsException | IllDefinedDataPointsException e) {
            e.printStackTrace();
            return null;
        }

        return mapping;

    }

    public static void applyTransformation(Image inputImage, Mapping mapping, boolean multithread) throws InterruptedException {
        // Iterate over all images in the stack
        ImagePlus inputIpl = inputImage.getImagePlus();
        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        int nTotal = nChannels*nFrames;
        AtomicInteger count = new AtomicInteger();

        for (int c=1;c<=nChannels;c++) {
            for (int z=1;z<=nSlices;z++) {
                for (int t=1;t<=nFrames;t++) {
                    int finalC = c;
                    int finalZ = z;
                    int finalT = t;

                    Runnable task = () -> {
                        ImageProcessor slice = getSetStack(inputIpl, finalT, finalC, finalZ, null).getProcessor();

                        slice.setInterpolationMethod(ImageProcessor.BILINEAR);
                        ImageProcessor alignedSlice = slice.createProcessor(slice.getWidth(), slice.getHeight());
                        alignedSlice.setMinAndMax(slice.getMin(), slice.getMax());
                        mapping.mapInterpolated(slice, alignedSlice);

                        getSetStack(inputIpl, finalT, finalC, finalZ, alignedSlice);

                    };
                    pool.submit(task);
                }
            }
        }
        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
    }

    synchronized private static ImagePlus getSetStack(ImagePlus inputImagePlus, int timepoint, int channel, int slice, @Nullable ImageProcessor toPut) {
        if (toPut == null) {
            // Get mode
            return SubHyperstackMaker.makeSubhyperstack(inputImagePlus, channel + "-" + channel, slice + "-" + slice, timepoint + "-" + timepoint);
        } else {
            inputImagePlus.setPosition(channel,slice,timepoint);
            inputImagePlus.setProcessor(toPut);
            return null;
        }
    }

    public static void replaceStack(Image inputImage, Image newStack, int channel, int timepoint) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();
        ImagePlus newStackImagePlus = newStack.getImagePlus();

        for (int z=1;z<=newStackImagePlus.getNSlices();z++) {
            inputImagePlus.setPosition(channel,z,timepoint);
            newStackImagePlus.setPosition(1,z,1);

            inputImagePlus.setProcessor(newStackImagePlus.getProcessor());

        }
    }

    @Override
    public String getTitle() {
        return "Register images";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return "Uses SIFT image registration toolbox";
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String alignmentMode = parameters.getValue(ALIGNMENT_MODE);
        String relativeMode = parameters.getValue(RELATIVE_MODE);
        String rollingCorrectionMode = parameters.getValue(ROLLING_CORRECTION);
        int correctionInterval = parameters.getValue(CORRECTION_INTERVAL);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        String calculationSource = parameters.getValue(CALCULATION_SOURCE);
        String externalSourceName = parameters.getValue(EXTERNAL_SOURCE);
        int calculationChannel = parameters.getValue(CALCULATION_CHANNEL);
        double initialSigma = parameters.getValue(INITIAL_SIGMA);
        String transformationMode = parameters.getValue(TRANSFORMATION_MODE);
        double rod = parameters.getValue(ROD);
        double maxEpsilon = parameters.getValue(MAX_EPSILON);
        double minInlierRatio = parameters.getValue(MIN_INLIER_RATIO);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        if (!applyToInput) inputImage = new Image(outputImageName,inputImage.getImagePlus().duplicate());

        Image reference = relativeMode.equals(RelativeModes.SPECIFIC_IMAGE) ? workspace.getImage(referenceImageName) : null;

        switch (alignmentMode) {
            case AlignmentModes.AUTOMATIC:
                // If the rolling correction mode is off, set the interval to -1
                if (rollingCorrectionMode.equals(RollingCorrectionModes.NONE)) correctionInterval = -1;

                // Setting up the parameters
                Param param = new Param();
                param.transformationMode = transformationMode;
                param.initialSigma = (float) initialSigma;
                param.steps = parameters.getValue(STEPS);
                param.minOctaveSize = parameters.getValue(MINIMUM_IMAGE_SIZE);
                param.maxOctaveSize = parameters.getValue(MAXIMUM_IMAGE_SIZE);
                param.fdSize = parameters.getValue(FD_SIZE);
                param.fdBins = parameters.getValue(FD_ORIENTATION_BINS);
                param.rod = (float) rod;
                param.maxEpsilon = (float) maxEpsilon;
                param.minInlierRatio = (float) minInlierRatio;

                Image externalSource = calculationSource.equals(CalculationSources.EXTERNAL) ? workspace.getImage(externalSourceName) : null;

                processAutomatic(inputImage, calculationChannel, relativeMode, param, correctionInterval, multithread, reference, externalSource);
                break;

            case AlignmentModes.MANUAL:
                processManual(inputImage,transformationMode,multithread,reference);
                break;
        }

        // Dealing with module outputs
        if (!applyToInput) workspace.addImage(inputImage);
        if (showOutput) showImage(inputImage);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(ALIGNMENT_MODE,Parameter.CHOICE_ARRAY,AlignmentModes.AUTOMATIC,AlignmentModes.ALL));
        parameters.add(new Parameter(RELATIVE_MODE,Parameter.CHOICE_ARRAY,RelativeModes.FIRST_FRAME,RelativeModes.ALL));
        parameters.add(new Parameter(ROLLING_CORRECTION,Parameter.CHOICE_ARRAY,RollingCorrectionModes.NONE,RollingCorrectionModes.ALL));
        parameters.add(new Parameter(CORRECTION_INTERVAL, Parameter.INTEGER,1));
        parameters.add(new Parameter(REFERENCE_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(CALCULATION_SOURCE,Parameter.CHOICE_ARRAY,CalculationSources.INTERNAL,CalculationSources.ALL));
        parameters.add(new Parameter(EXTERNAL_SOURCE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(CALCULATION_CHANNEL, Parameter.INTEGER,1));
        parameters.add(new Parameter(TRANSFORMATION_MODE,Parameter.CHOICE_ARRAY,TransformationModes.RIGID,TransformationModes.ALL));
        parameters.add(new Parameter(INITIAL_SIGMA, Parameter.DOUBLE,1.6));
        parameters.add(new Parameter(STEPS, Parameter.INTEGER,3));
        parameters.add(new Parameter(MINIMUM_IMAGE_SIZE, Parameter.INTEGER,64));
        parameters.add(new Parameter(MAXIMUM_IMAGE_SIZE, Parameter.INTEGER,1024));
        parameters.add(new Parameter(FD_SIZE, Parameter.INTEGER,4));
        parameters.add(new Parameter(FD_ORIENTATION_BINS, Parameter.INTEGER,8));
        parameters.add(new Parameter(ROD, Parameter.DOUBLE,0.92));
        parameters.add(new Parameter(MAX_EPSILON, Parameter.DOUBLE,25.0));
        parameters.add(new Parameter(MIN_INLIER_RATIO, Parameter.DOUBLE,0.05));
        parameters.add(new Parameter(ENABLE_MULTITHREADING, Parameter.BOOLEAN, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(ALIGNMENT_MODE));
        switch ((String) parameters.getValue(ALIGNMENT_MODE)) {
            case AlignmentModes.AUTOMATIC:
                returnedParameters.add(parameters.getParameter(RELATIVE_MODE));
                switch ((String) parameters.getValue(RELATIVE_MODE)) {
                    case UnwarpImages.RelativeModes.PREVIOUS_FRAME:
                        returnedParameters.add(parameters.getParameter(ROLLING_CORRECTION));
                        switch ((String) parameters.getValue(ROLLING_CORRECTION)) {
                            case UnwarpImages.RollingCorrectionModes.EVERY_NTH_FRAME:
                                returnedParameters.add(parameters.getParameter(CORRECTION_INTERVAL));
                                break;
                        }
                        break;

                    case UnwarpImages.RelativeModes.SPECIFIC_IMAGE:
                        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                        break;
                }

                returnedParameters.add(parameters.getParameter(CALCULATION_SOURCE));
                switch ((String) parameters.getValue(CALCULATION_SOURCE)) {
                    case UnwarpImages.CalculationSources.EXTERNAL:
                        returnedParameters.add(parameters.getParameter(EXTERNAL_SOURCE));
                        break;
                }

                returnedParameters.add(parameters.getParameter(CALCULATION_CHANNEL));
                returnedParameters.add(parameters.getParameter(TRANSFORMATION_MODE));
                returnedParameters.add(parameters.getParameter(INITIAL_SIGMA));
                returnedParameters.add(parameters.getParameter(STEPS));
                returnedParameters.add(parameters.getParameter(MINIMUM_IMAGE_SIZE));
                returnedParameters.add(parameters.getParameter(MAXIMUM_IMAGE_SIZE));
                returnedParameters.add(parameters.getParameter(FD_SIZE));
                returnedParameters.add(parameters.getParameter(FD_ORIENTATION_BINS));
                returnedParameters.add(parameters.getParameter(ROD));
                returnedParameters.add(parameters.getParameter(MAX_EPSILON));
                returnedParameters.add(parameters.getParameter(MIN_INLIER_RATIO));
                break;

            case AlignmentModes.MANUAL:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                returnedParameters.add(parameters.getParameter(TRANSFORMATION_MODE));
                break;
        }

        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (ADD_PAIR):
                addNewPair();
                break;

            case (FINISH):
                frame.dispose();
                frame = null;
                displayImagePlus1.close();
                displayImagePlus2.close();

                break;
        }
    }

    public void addNewPair() {
        Roi roi1 = displayImagePlus1.getRoi();
        double[] centroid1 = roi1.getContourCentroid();
        PointRoi point1 = new PointRoi(centroid1[0],centroid1[1]);
        displayImagePlus1.deleteRoi();

        Roi roi2 = displayImagePlus2.getRoi();
        double[] centroid2 = roi2.getContourCentroid();
        PointRoi point2 = new PointRoi(centroid2[0],centroid2[1]);
        displayImagePlus2.deleteRoi();

        pairs.add(new PointPair(point1,point2));

    }

    public void addToOverlay(PointPair pair) {
        overlay1.add(pair.getPoint1());
        displayImagePlus1.updateAndDraw();

        overlay2.add(pair.getPoint2());
        displayImagePlus2.updateAndDraw();

    }


    private class Param extends FloatArray2DSIFT.Param {
        String transformationMode = TransformationModes.RIGID;
        float rod = 0.92f;
        float maxEpsilon = 25.0f;
        float minInlierRatio = 0.05f;

    }

    private class PointPair {
        private PointRoi p1;
        private PointRoi p2;

        PointPair(PointRoi p1, PointRoi p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        PointRoi getPoint1() {
            return p1;
        }

        PointRoi getPoint2() {
            return p2;
        }
    }
}

