package io.github.mianalysis.mia.module.images.transform;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.drew.lang.annotations.Nullable;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.plugin.SubHyperstackMaker;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.process.CommaSeparatedStringInterpreter;

/**
 * Created by sc13967 on 18/01/2018.
 */

/**
* Extract a substack from the specified input image in terms of channels, slices and frames.  The output image is saved to the workspace for use later on in the workflow.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ExtractSubstack extends Module implements ActionListener {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* Image from which the substack will be taken.
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* Output substack image.
	*/
    public static final String OUTPUT_IMAGE = "Output image";


	/**
	* 
	*/
    public static final String RANGE_SEPARATOR = "Dimension ranges";

	/**
	* Method for selection of substack dimension ranges.<br><br>- "Fixed" (default) will apply the pre-specified dimension ranges to the input image.<br><br>- "Manual" will display a dialog asking the user to select the dimension ranges at runtime.  Each dimension (channel, slice or frame) can be fixed (i.e. not presented as an option to the user) using the "enable" toggles.
	*/
    public static final String SELECTION_MODE = "Selection mode";

	/**
	* Channel range to be extracted from the input image.  If "Selection mode" is set to "Fixed" this will be applied to each image.  If "Selection mode" is set to "Manual" this will be the default value present to the user, but can be changed for the final extraction.
	*/
    public static final String CHANNELS = "Channels";

	/**
	* Slice range to be extracted from the input image.  If "Selection mode" is set to "Fixed" this will be applied to each image.  If "Selection mode" is set to "Manual" this will be the default value present to the user, but can be changed for the final extraction.
	*/
    public static final String SLICES = "Slices";

	/**
	* Frame range to be extracted from the input image.  If "Selection mode" is set to "Fixed" this will be applied to each image.  If "Selection mode" is set to "Manual" this will be the default value present to the user, but can be changed for the final extraction.
	*/
    public static final String FRAMES = "Frames";

	/**
	* If enabled, the user will be able to specify the final channel range to be used in the substack extraction.
	*/
    public static final String ENABLE_CHANNELS_SELECTION = "Enable channels selection";

	/**
	* If enabled, the user will be able to specify the final slice range to be used in the substack extraction.
	*/
    public static final String ENABLE_SLICES_SELECTION = "Enable slices selection";

	/**
	* If enabled, the user will be able to specify the final frame range to be used in the substack extraction.
	*/
    public static final String ENABLE_FRAMES_SELECTION = "Enable frames selection";

    private static final String OK = "OK";

    private JFrame frame;
    private JTextField channelsNumberField;
    private JTextField slicesNumberField;
    private JTextField framesNumberField;

    private int elementHeight = 40;
    private boolean active = false;

    public ExtractSubstack(Modules modules) {
        super("Extract substack",modules);
    }


    public interface SelectionModes {
        String FIXED = "Fixed";
        String MANUAL = "Manual";

        String[] ALL = new String[]{FIXED,MANUAL};

    }

    private void showOptionsPanel(@Nullable String inputChannelsRange, @Nullable String inputSlicesRange, @Nullable String inputFramesRange) {
        active = true;
        frame = new JFrame();
        frame.setAlwaysOnTop(true);

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.insets = new Insets(5,5,5,5);

        // Header panel
        JLabel headerLabel = new JLabel("<html>Set the range for each type.</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        headerLabel.setPreferredSize(new Dimension(300,elementHeight));
        frame.add(headerLabel,c);

        if (inputChannelsRange != null) {
            JLabel channelsLabel = new JLabel("Channel range");
            channelsLabel.setPreferredSize(new Dimension(100, elementHeight));
            c.gridy++;
            c.gridwidth = 1;
            frame.add(channelsLabel, c);

            channelsNumberField = new JTextField(inputChannelsRange);
            channelsNumberField.setPreferredSize(new Dimension(200, elementHeight));
            c.gridx++;
            c.insets = new Insets(0, 0, 5, 5);
            frame.add(channelsNumberField, c);
        }

        if (inputSlicesRange != null) {
            JLabel slicesLabel = new JLabel("Slice range");
            slicesLabel.setPreferredSize(new Dimension(100, elementHeight));
            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 1;
            c.insets = new Insets(0, 5, 5, 5);
            frame.add(slicesLabel, c);

            slicesNumberField = new JTextField(inputSlicesRange);
            slicesNumberField.setPreferredSize(new Dimension(200, elementHeight));
            c.gridx++;
            c.insets = new Insets(0, 0, 5, 5);
            frame.add(slicesNumberField, c);
        }

        if (inputFramesRange != null) {
            JLabel framesLabel = new JLabel("Frame range");
            framesLabel.setPreferredSize(new Dimension(100, elementHeight));
            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 1;
            c.insets = new Insets(0, 5, 5, 5);
            frame.add(framesLabel, c);

            framesNumberField = new JTextField(inputFramesRange);
            framesNumberField.setPreferredSize(new Dimension(200, elementHeight));
            c.gridx++;
            c.insets = new Insets(0, 0, 5, 5);
            frame.add(framesNumberField, c);
        }

        JButton okButton = new JButton(OK);
        okButton.addActionListener(this);
        okButton.setActionCommand(OK);
        okButton.setPreferredSize(new Dimension(300,elementHeight));
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.insets = new Insets(0,5,5,5);
        frame.add(okButton,c);

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

    }

    public static ImagePlus extractSubstack(ImagePlus inputIpl, String outputImageName, String channels, String slices, String frames) {
        int[] channelsList = CommaSeparatedStringInterpreter.interpretIntegers(channels,true,inputIpl.getNChannels());
        int[] slicesList = CommaSeparatedStringInterpreter.interpretIntegers(slices,true,inputIpl.getNSlices());
        int[] framesList = CommaSeparatedStringInterpreter.interpretIntegers(frames,true,inputIpl.getNFrames());

        List<Integer> cList = java.util.Arrays.stream(channelsList).boxed().collect(Collectors.toList());
        List<Integer> zList = java.util.Arrays.stream(slicesList).boxed().collect(Collectors.toList());
        List<Integer> tList = java.util.Arrays.stream(framesList).boxed().collect(Collectors.toList());

        if (!checkLimits(inputIpl,cList,zList,tList)) return null;

        // Generating the substack and adding to the workspace
        ImagePlus outputImagePlus = SubHyperstackMaker.makeSubhyperstack(inputIpl,cList,zList,tList).duplicate();
        outputImagePlus.setCalibration(inputIpl.getCalibration());

        if (outputImagePlus.isComposite()) ((CompositeImage) outputImagePlus).setMode(CompositeImage.COLOR);

        return outputImagePlus;

    }

    public static Image extractSubstack(Image inputImage, String outputImageName, String channels, String slices, String frames) {
        ImagePlus inputIpl = inputImage.getImagePlus();

        ImagePlus outputIpl = extractSubstack(inputIpl, outputImageName, channels, slices, frames);

        return ImageFactory.createImage(outputImageName,outputIpl);

    }

    static boolean checkLimits(ImagePlus inputIpl, List<Integer> cList, List<Integer> zList, List<Integer> tList) {
        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();

        for (int c:cList) if (c>nChannels) {
            MIA.log.writeError("Channel index ("+c+") outside range (1-"+nChannels+").  Skipping image.");
            return false;
        }

        for (int z:zList) if (z>nSlices) {
            MIA.log.writeError("Slice index ("+z+") outside range (1-"+nSlices+").  Skipping image.");
            return false;
        }

        for (int t:tList) if (t>nFrames) {
            MIA.log.writeError("Frame index ("+t+") outside range (1-"+nFrames+").  Skipping image.");
            return false;
        }

        return true;
    }

    static boolean checkLimits(Image inputImage, List<Integer> cList, List<Integer> zList, List<Integer> tList) {
        return checkLimits(inputImage.getImagePlus(), cList, zList, tList);

    }


    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM;
    }

    @Override
    public String getDescription() {
        return "Extract a substack from the specified input image in terms of channels, slices and frames.  The output " +
                "image is saved to the workspace for use later on in the workflow.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String selectionMode = parameters.getValue(SELECTION_MODE,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        String channels = parameters.getValue(CHANNELS,workspace);
        String slices = parameters.getValue(SLICES,workspace);
        String frames = parameters.getValue(FRAMES,workspace);
        boolean enableChannels = parameters.getValue(ENABLE_CHANNELS_SELECTION,workspace);
        boolean enableSlices = parameters.getValue(ENABLE_SLICES_SELECTION,workspace);
        boolean enableFrames = parameters.getValue(ENABLE_FRAMES_SELECTION,workspace);

        switch (selectionMode) {
            case SelectionModes.MANUAL:
                // Displaying the image and control panel
                ImagePlus displayImagePlus = inputImage.getImagePlus().duplicate();
                displayImagePlus.show();

                String inputChannelsRange = enableChannels ? channels : null;
                String inputSlicesRange = enableSlices ? slices : null;
                String inputFramesRange = enableFrames ? frames : null;

                showOptionsPanel(inputChannelsRange,inputSlicesRange,inputFramesRange);

                // All the while the control is open, do nothing
                while (active) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // Do nothing as the user has selected this
                    }
                }

                if (enableChannels) channels = channelsNumberField.getText();
                if (enableSlices) slices = slicesNumberField.getText();
                if (enableFrames) frames = framesNumberField.getText();

                frame.dispose();
                frame = null;
                displayImagePlus.close();

                break;
        }

        Image outputImage = extractSubstack(inputImage,outputImageName,channels,slices,frames);
        if (outputImage == null) return Status.FAIL;

        workspace.addImage(outputImage);
        
        // If selected, displaying the image
        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE,this,"","Image from which the substack will be taken."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this,"","Output substack image."));

        parameters.add(new SeparatorP(RANGE_SEPARATOR,this));
        parameters.add(new ChoiceP(SELECTION_MODE,this, SelectionModes.FIXED, SelectionModes.ALL,"Method for selection of substack dimension ranges.<br>" +
                "<br>- \""+SelectionModes.FIXED+"\" (default) will apply the pre-specified dimension ranges to the input image.<br>" +
                "<br>- \""+SelectionModes.MANUAL+"\" will display a dialog asking the user to select the dimension ranges at runtime.  Each dimension (channel, slice or frame) can be fixed (i.e. not presented as an option to the user) using the \"enable\" toggles."));
        parameters.add(new StringP(CHANNELS,this,"1-end","Channel range to be extracted from the input image.  If \""+SELECTION_MODE+"\" is set to \""+SelectionModes.FIXED+"\" this will be applied to each image.  If \""+SELECTION_MODE+"\" is set to \""+SelectionModes.MANUAL+"\" this will be the default value present to the user, but can be changed for the final extraction."));
        parameters.add(new StringP(SLICES,this,"1-end","Slice range to be extracted from the input image.  If \""+SELECTION_MODE+"\" is set to \""+SelectionModes.FIXED+"\" this will be applied to each image.  If \""+SELECTION_MODE+"\" is set to \""+SelectionModes.MANUAL+"\" this will be the default value present to the user, but can be changed for the final extraction."));
        parameters.add(new StringP(FRAMES,this,"1-end","Frame range to be extracted from the input image.  If \""+SELECTION_MODE+"\" is set to \""+SelectionModes.FIXED+"\" this will be applied to each image.  If \""+SELECTION_MODE+"\" is set to \""+SelectionModes.MANUAL+"\" this will be the default value present to the user, but can be changed for the final extraction."));
        parameters.add(new BooleanP(ENABLE_CHANNELS_SELECTION,this,true,"If enabled, the user will be able to specify the final channel range to be used in the substack extraction."));
        parameters.add(new BooleanP(ENABLE_SLICES_SELECTION,this,true,"If enabled, the user will be able to specify the final slice range to be used in the substack extraction."));
        parameters.add(new BooleanP(ENABLE_FRAMES_SELECTION,this,true,"If enabled, the user will be able to specify the final frame range to be used in the substack extraction."));

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(RANGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CHANNELS));
        returnedParameters.add(parameters.getParameter(SLICES));
        returnedParameters.add(parameters.getParameter(FRAMES));

        returnedParameters.add(parameters.getParameter(SELECTION_MODE));
        switch ((String) parameters.getValue(SELECTION_MODE,workspace)) {
            case SelectionModes.MANUAL:
                returnedParameters.add(parameters.getParameter(ENABLE_CHANNELS_SELECTION));
                returnedParameters.add(parameters.getParameter(ENABLE_SLICES_SELECTION));
                returnedParameters.add(parameters.getParameter(ENABLE_FRAMES_SELECTION));
                break;
        }

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
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (OK):
                active = false;
                break;
        }
    }
}
