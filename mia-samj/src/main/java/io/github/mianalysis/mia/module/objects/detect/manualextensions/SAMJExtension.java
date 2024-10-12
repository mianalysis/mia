package io.github.mianalysis.mia.module.objects.detect.manualextensions;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.apache.commons.compress.archivers.ArchiveException;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ai.nets.samj.install.EfficientSamEnvManager;
import ai.nets.samj.install.Sam2EnvManager;
import ai.nets.samj.install.SamEnvManagerAbstract;
import ai.nets.samj.models.AbstractSamJ;
import ai.nets.samj.models.EfficientSamJ;
import ai.nets.samj.models.Sam2;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import io.bioimage.modelrunner.apposed.appose.MambaInstallException;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.objects.detect.ManuallyIdentifyObjects;
import io.github.mianalysis.mia.module.objects.detect.extensions.ManualExtension;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.selectors.ObjectSelector;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

@Plugin(type = ManualExtension.class, priority = Priority.LOW, visible = true)
public class SAMJExtension extends ManualExtension implements MouseListener {
    public static final String SAMJ_SEPARATOR = "Segment Anything (SAMJ) controls";
    public static final String USE_SAM = "Enable Segment Anything";
    public static final String MODEL = "Segment Anything model";
    public static final String ENVIRONMENT_PATH_MODE = "Environment path mode";
    public static final String ENVIRONMENT_PATH = "Environment path";
    public static final String INSTALL_IF_MISSING = "Install model if missing";
    public static final String USE_ENCODING_IF_AVAILABLE = "Use encoding if available";
    public static final String STORE_ENCODING = "Store encoding";
    public static final String TERMINATE_AFTER_ENCODING = "Terminate after encoding";

    HashMap<String, String> encodings = new HashMap<>();

    public interface SAMJModels {
        public String EFFICIENT = "Efficient";
        public String LARGE = "Large";
        public String SMALL = "Small";
        public String TINY = "Tiny";

        public String[] ALL = new String[] { EFFICIENT, TINY };

    }

    public interface EnvironmentPathModes {
        String DEFAULT = "Default";
        String SPECIFIC = "Specific path";

        String[] ALL = new String[] { DEFAULT, SPECIFIC };

    }

    protected Workspace workspace;
    protected AbstractSamJ samJ = null;
    protected ImagePlus displayIpl;
    protected boolean useSAM = true;

    protected String prevModelName = "";
    protected String prevEnvionmentPathMode = "";
    protected String prevEnvironmentPath = "";

    public static void main(String[] args) throws Exception {
        try {
            LegacyInjector.preinit();
        } catch (Exception e) {
        }

        try {
            new ij.ImageJ();
            new ImageJ().command().run("io.github.mianalysis.mia.MIA_", false);

        } catch (Exception e) {
            MIA.log.writeError(e);
        }
    }

    public SAMJExtension(Module module) {
        super(module);
    }

    protected void initialiseSAMJ() {
        String modelName = parameters.getValue(MODEL, workspace);
        prevModelName = modelName;
        String envionmentPathMode = parameters.getValue(ENVIRONMENT_PATH_MODE, workspace);
        prevEnvionmentPathMode = envionmentPathMode;
        String environmentPath = parameters.getValue(ENVIRONMENT_PATH, workspace) + "/";
        prevEnvironmentPath = environmentPath;
        boolean installIfMissing = parameters.getValue(INSTALL_IF_MISSING, workspace);

        AbstractSamJ.MAX_ENCODED_AREA_RS = 3000;
        AbstractSamJ.MAX_ENCODED_SIDE = 3000;

        try {
            switch (envionmentPathMode) {
                case EnvironmentPathModes.DEFAULT:
                    environmentPath = SamEnvManagerAbstract.DEFAULT_DIR;
                    break;
            }

            SamEnvManagerAbstract manager;
            switch (modelName) {
                case SAMJModels.EFFICIENT:
                default:
                    manager = EfficientSamEnvManager.create(environmentPath);
                    break;
                case SAMJModels.LARGE:
                    manager = Sam2EnvManager.create(environmentPath, "large");
                    break;
                case SAMJModels.SMALL:
                    manager = Sam2EnvManager.create(environmentPath, "small");
                    break;
                case SAMJModels.TINY:
                    manager = Sam2EnvManager.create(environmentPath, "tiny");
                    break;
            }

            if (!manager.checkEverythingInstalled())
                if (installIfMissing) {
                    module.writeStatus("Installing SAM model");
                    manager.installEverything();
                } else {
                    MIA.log.writeWarning(modelName + " not available.  Please install manually or enable \""
                            + INSTALL_IF_MISSING + "\" parameter.");
                }

            switch (modelName) {
                case SAMJModels.EFFICIENT:
                default:
                    samJ = EfficientSamJ.initializeSam(manager);
                    break;
                case SAMJModels.LARGE:
                case SAMJModels.SMALL:
                case SAMJModels.TINY:
                    samJ = Sam2.initializeSam(manager);
                    break;
            }

        } catch (IOException | RuntimeException | InterruptedException | ArchiveException | URISyntaxException
                | MambaInstallException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Status initialiseBeforeImageShown(Workspace workspace) {
        this.workspace = workspace;

        // Getting parameters
        boolean useSAM = parameters.getValue(USE_SAM, workspace);
        if (!useSAM)
            return Status.PASS;

        String modelName = parameters.getValue(MODEL, workspace);
        String envionmentPathMode = parameters.getValue(ENVIRONMENT_PATH_MODE, workspace);
        String environmentPath = parameters.getValue(ENVIRONMENT_PATH, workspace) + "/";
        boolean useEncoding = parameters.getValue(USE_ENCODING_IF_AVAILABLE, workspace);
        boolean storeEncoding = parameters.getValue(STORE_ENCODING, workspace);
        boolean terminateAfterEncoding = parameters.getValue(TERMINATE_AFTER_ENCODING, workspace);

        if (samJ == null | !modelName.equals(prevModelName) | !envionmentPathMode.equals(prevEnvionmentPathMode)
                | !environmentPath.equals(prevEnvironmentPath))
            initialiseSAMJ();

        String path = workspace.getMetadata().getFile().getAbsolutePath();
        if (useEncoding && encodings.containsKey(path))
            try {
                samJ.selectEncoding(encodings.get(path));
            } catch (Exception e) {
                MIA.log.writeError(e);
            }
        else {
            try {
                String imageName = module.getParameterValue(ManuallyIdentifyObjects.INPUT_IMAGE, workspace);
                Image image = workspace.getImage(imageName);
                samJ.setImage(image.getImgPlus());
            } catch (IOException | RuntimeException | InterruptedException e) {
                MIA.log.writeError(e);
            }
        }

        if (storeEncoding) {
            try {
                encodings.put(path, samJ.persistEncoding());

                if (terminateAfterEncoding)
                    return Status.TERMINATE_SILENT;

            } catch (IOException | InterruptedException e) {
                MIA.log.writeError(e);
            }
        }

        return Status.PASS;

    }

    @Override
    public Status initialiseAfterImageShown(@Nullable ImagePlus displayIpl) {
        // Getting parameters
        boolean useSAM = parameters.getValue(USE_SAM, workspace);
        if (!useSAM)
            return Status.PASS;

        this.displayIpl = displayIpl;
        displayIpl.getCanvas().addMouseListener(this);

        return Status.PASS;

    }

    @Override
    public Parameters initialiseParameters() {
        parameters.add(new SeparatorP(SAMJ_SEPARATOR, module));
        parameters.add(new BooleanP(USE_SAM, module, false));
        parameters.add(new ChoiceP(MODEL, module, SAMJModels.EFFICIENT, SAMJModels.ALL));
        parameters.add(
                new ChoiceP(ENVIRONMENT_PATH_MODE, module, EnvironmentPathModes.DEFAULT, EnvironmentPathModes.ALL));
        parameters.add(new FolderPathP(ENVIRONMENT_PATH, module));
        parameters.add(new BooleanP(INSTALL_IF_MISSING, module, true));
        parameters.add(new BooleanP(USE_ENCODING_IF_AVAILABLE, module, true));
        parameters.add(new BooleanP(STORE_ENCODING, module, true));
        parameters.add(new BooleanP(TERMINATE_AFTER_ENCODING, module, false));

        return parameters;

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(SAMJ_SEPARATOR));
        returnedParameters.add(parameters.getParameter(USE_SAM));

        if ((boolean) parameters.getValue(USE_SAM, workspace)) {
            returnedParameters.add(parameters.getParameter(MODEL));
            returnedParameters.add(parameters.getParameter(ENVIRONMENT_PATH_MODE));

            switch ((String) parameters.getValue(ENVIRONMENT_PATH_MODE, workspace)) {
                case EnvironmentPathModes.SPECIFIC:
                    returnedParameters.add(parameters.getParameter(ENVIRONMENT_PATH));
                    break;
            }

            returnedParameters.add(parameters.getParameter(INSTALL_IF_MISSING));
            returnedParameters.add(parameters.getParameter(USE_ENCODING_IF_AVAILABLE));
            returnedParameters.add(parameters.getParameter(STORE_ENCODING));
            returnedParameters.add(parameters.getParameter(TERMINATE_AFTER_ENCODING));

        }

        return returnedParameters;

    }

    @Override
    public boolean skipAutoAccept() {
        if ((boolean) parameters.getValue(USE_SAM, workspace) && useSAM)
            return true;
        else
            return false;
    }

    @Override
    public JPanel getControlPanel() {
        if (!(boolean) parameters.getValue(USE_SAM, workspace))
            return null;

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridwidth = 1;
        c.gridx = 0;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 5, 5, 0);

        JLabel useSAMLabel = new JLabel("Use Segment Anything");
        useSAMLabel.setHorizontalAlignment(JLabel.RIGHT);
        panel.add(useSAMLabel, c);

        JCheckBox useSAMCheckBox = new JCheckBox();
        useSAMCheckBox.setEnabled(true);
        useSAMCheckBox.setSelected(true);
        useSAMCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useSAM = useSAMCheckBox.isSelected();
            }
        });
        c.gridx++;
        panel.add(useSAMCheckBox, c);

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(0, 0, 0, 0));
        c.gridx++;
        c.gridwidth = 4;
        c.weightx = 1;
        panel.add(separator, c);

        return panel;

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!useSAM)
            return;

        if (displayIpl.getRoi() == null)
            return;

        if (samJ == null) {
            MIA.log.writeWarning("SAMJ not ready yet");
            return;
        }

        Roi roi = displayIpl.getRoi();

        List<Polygon> polygons = null;
        switch (roi.getType()) {
            case Roi.FREELINE:
            case Roi.LINE:
            case Roi.POINT:
            case Roi.POLYLINE:
            default:
                polygons = getPolygonFromPoints(roi);
                break;

            case Roi.FREEROI:
            case Roi.OVAL:
            case Roi.POLYGON:
            case Roi.TRACED_ROI:
            //     polygons = getPolygonFromMask(roi);
            // break;
            case Roi.RECTANGLE:
                polygons = getPolygonFromRectangle(roi);
                break;
        }

        if (polygons == null)
            return;

        for (Polygon polygon : polygons) {
            PolygonRoi polygonRoi = new PolygonRoi(polygon, PolygonRoi.POLYGON);
            displayIpl.setRoi(polygonRoi);
        }

        // Checking if the object selector is set to automatically accept objects
        ObjectSelector objectSelector = ((ManuallyIdentifyObjects) module).getObjectSelector();

        switch (objectSelector.getAutoAcceptMode()) {
            case ObjectSelector.AutoAcceptModes.DO_NOTHING:
            default:
                break;
            case ObjectSelector.AutoAcceptModes.ADD_AS_NEW_OBJECT:
                new Thread(() -> {
                    objectSelector.addNewObject();
                }).start();
                break;
            case ObjectSelector.AutoAcceptModes.ADD_TO_EXISTING_OBJECT:
                objectSelector.addToExistingObject();
                break;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    // public List<Polygon> getPolygonFromMask(Roi roi) {
        
    //     ImageProcessor mask = roi.getMask();
    //     Img img = ImageJFunctions.wrap(new ImagePlus("Mask",mask));
    //     ImageJFunctions.show(img);
    //     try {
    //         return samJ.processMask(img);
    //     } catch (IOException | InterruptedException | RuntimeException e) {
    //         MIA.log.writeError(e);
    //     }

    //     return null;

    // }

    public List<Polygon> getPolygonFromPoints(Roi roi) {
        Point[] points = roi.getContainedPoints();

        ArrayList<int[]> pts = new ArrayList<>();
        for (Point point : points)
            pts.add(new int[] { point.x, point.y });
        
        try {
            return samJ.processPoints(pts);
        } catch (IOException | InterruptedException | RuntimeException e) {
            MIA.log.writeError(e);
        }

        return null;

    }

    public List<Polygon> getPolygonFromRectangle(Roi roi) {
        Rectangle rectangle = roi.getBounds();
        int x0 = rectangle.x;
        int y0 = rectangle.y;
        int x1 = x0 + rectangle.width;
        int y1 = y0 + rectangle.height;
        int[] box = new int[] { x0, y0, x1, y1 };

        try {
            return samJ.processBox(box);
        } catch (IOException | InterruptedException | RuntimeException e) {
            MIA.log.writeError(e);
        }

        return null;

    }
}
