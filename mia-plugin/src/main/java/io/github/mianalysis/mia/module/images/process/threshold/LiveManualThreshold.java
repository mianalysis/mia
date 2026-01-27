package io.github.mianalysis.mia.module.images.process.threshold;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImageJ;


@Plugin(type = Module.class, priority = Priority.LOW, visible = true)

public class LiveManualThreshold extends Module {
    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input/output";

    /**
     * Image to apply threshold to.
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * Select if the threshold should be applied directly to the input image, or if
     * it should be applied to a duplicate, then stored as a different image in the
     * workspace.
     */
    public static final String APPLY_TO_INPUT = "Apply to input image";

    /**
     * Name of the output image created during the thresholding process. This image
     * will be added to the workspace.
     */
    public static final String OUTPUT_IMAGE = "Output image";

    /**
    * 
    */
    public static final String THRESHOLD_SEPARATOR = "Threshold controls";

    public static final String THRESHOLD_VALUE = "Threshold value";

    /**
     * Controls whether objects are considered to be white (255 intensity) on a
     * black (0 intensity) background, or black on a white background.
     */
    public static final String BINARY_LOGIC = "Binary logic";

    public static final String LIVE_SELECTION = "Live selection";

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public LiveManualThreshold(Modules modules) {
        // The first argument is the name by which the module will be seen in the GUI.
        super("Live manual threshold", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS_THRESHOLD;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    public double getThreshold(ImagePlus inputIpl, double thresholdValue, String binaryLogic) {
        // Creating a raw copy to display and one for the output
        ImagePlus showIpl = new Duplicator().run(inputIpl);
        ImagePlus testIpl = inputIpl.duplicate();

        JFrame frame = new JFrame();
        frame.setTitle("Select threshold");
        frame.setLayout(new FlowLayout(FlowLayout.CENTER));

        JTextField thresholdTextField = new JTextField();
        thresholdTextField.setText(String.valueOf(thresholdValue));
        thresholdTextField.selectAll();
        thresholdTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                testThreshold(inputIpl, testIpl, thresholdTextField, binaryLogic);

            }
        });
        frame.add(thresholdTextField);

        JButton acceptButton = new JButton();
        acceptButton.setText("Accept");
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                frame.dispose();
                showIpl.close();
                testIpl.close();
            }
        });
        frame.add(acceptButton);

        testThreshold(inputIpl, testIpl, thresholdTextField, binaryLogic);

        showIpl.show();
        testIpl.show();

        frame.pack();
        frame.setLocation(100, 100);
        frame.setVisible(true);
        frame.setResizable(false);

        // All the while the control is open, do nothing
        while (frame.isVisible() && showIpl != null && testIpl != null)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Do nothing as the user has selected this
            }

        return Double.parseDouble(thresholdTextField.getText());

    }

    static void testThreshold(ImagePlus inputIpl, ImagePlus testIpl, JTextField thresholdTextField,
            String binaryLogic) {
        double currThreshold = Double.parseDouble(thresholdTextField.getText());
        ImagePlus tempTestIpl = inputIpl.duplicate();

        ManualThreshold.applyThreshold(tempTestIpl, currThreshold);
        if (binaryLogic.equals(BinaryLogic.WHITE_BACKGROUND))
            InvertIntensity.process(tempTestIpl);

        // Replacing
        testIpl.setStack(tempTestIpl.getStack());
        testIpl.setTitle("Thresholded @ " + currThreshold);

        thresholdTextField.grabFocus();
        thresholdTextField.selectAll();
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        double thresholdValue = parameters.getValue(THRESHOLD_VALUE, workspace);
        String binaryLogic = parameters.getValue(BINARY_LOGIC, workspace);
        boolean liveSelection = parameters.getValue(LIVE_SELECTION, workspace);

        // Getting input image
        ImageI inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

        if (liveSelection)
            thresholdValue = getThreshold(inputImagePlus, thresholdValue, binaryLogic);

        // Updating threshold value parameter and updating GUI
        parameters.getParameter(THRESHOLD_VALUE).setValue(thresholdValue);
        GUI.updateParameters(false,null);

        // Calculating the threshold based on the selected algorithm
        ManualThreshold.applyThreshold(inputImagePlus, thresholdValue);

        if (binaryLogic.equals(BinaryLogic.WHITE_BACKGROUND))
            InvertIntensity.process(inputImagePlus);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            inputImage.addMeasurement(MeasurementFactories.getDefaultFactory().createMeasurement("THRESHOLD", thresholdValue));

            if (showOutput) {
                inputImage.showAsIs();
                inputImage.showMeasurements(this);
            }

        } else {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
            ImageI outputImage = ImageFactory.createImage(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);

            outputImage.addMeasurement(MeasurementFactories.getDefaultFactory().createMeasurement("THRESHOLD", thresholdValue));

            if (showOutput) {
                outputImage.showAsIs();
                outputImage.showMeasurements(this);
            }
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(THRESHOLD_SEPARATOR, this));
        parameters.add(new DoubleP(THRESHOLD_VALUE, this, 128));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));
        parameters.add(new BooleanP(LIVE_SELECTION, this, true));

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(THRESHOLD_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THRESHOLD_VALUE));
        returnedParameters.add(parameters.getParameter(BINARY_LOGIC));
        returnedParameters.add(parameters.getParameter(LIVE_SELECTION));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        WorkspaceI workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        String imageName = "";
        if ((boolean) parameters.getValue(APPLY_TO_INPUT, workspace))
            imageName = parameters.getValue(INPUT_IMAGE, workspace);
        else
            imageName = parameters.getValue(OUTPUT_IMAGE, workspace);

        ImageMeasurementRef reference = imageMeasurementRefs.getOrPut("THRESHOLD");
        reference.setImageName(imageName);
        reference.setDescription(
                "Threshold value applied to the image during binarisation. Specified in intensity units.");
        returnedRefs.add(reference);

        return returnedRefs;
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
}