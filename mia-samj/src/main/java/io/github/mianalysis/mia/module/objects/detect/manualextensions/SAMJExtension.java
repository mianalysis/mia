package io.github.mianalysis.mia.module.objects.detect.manualextensions;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.apache.commons.compress.archivers.ArchiveException;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ai.nets.samj.annotation.Mask;
import ai.nets.samj.install.EfficientSamEnvManager;
import ai.nets.samj.install.SamEnvManagerAbstract;
import ai.nets.samj.models.AbstractSamJ;
import ai.nets.samj.models.EfficientSamJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import io.bioimage.modelrunner.apposed.appose.MambaInstallException;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.detect.ManuallyIdentifyObjects;
import io.github.mianalysis.mia.module.objects.detect.extensions.ManualExtension;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
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
    public static final String ENVIRONMENT_PATH_MODE = "Environment path mode";
    public static final String ENVIRONMENT_PATH = "Environment path";
    public static final String INSTALL_IF_MISSING = "Install model if missing";
    public static final String PREINITIALISE = "Preinitialise (batch only)";

    public interface EnvironmentPathModes {
        String DEFAULT = "Default";
        String SPECIFIC = "Specific path";

        String[] ALL = new String[] { DEFAULT, SPECIFIC };

    }

    protected Workspace workspace;
    protected AbstractSamJ samJ = null;
    protected AbstractSamJ preinitSamJ = null;
    protected String preinitPath = null;
    protected boolean preinitComplete = false;
    protected ImagePlus displayIpl;
    protected boolean useSAM = true;
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

    public AbstractSamJ initialiseSAMJ(String environmentPath, boolean installIfMissing) {
        AbstractSamJ.MAX_ENCODED_AREA_RS = 3000;
        AbstractSamJ.MAX_ENCODED_SIDE = 3000;

        AbstractSamJ loadedSamJ = null;
        try {
            SamEnvManagerAbstract manager = EfficientSamEnvManager.create(environmentPath);

            if (!manager.checkEverythingInstalled())
                if (installIfMissing) {
                    module.writeStatus("Installing SAM model");
                    MIA.log.writeDebug("Installing SAM model to " + environmentPath);
                    manager.installEverything();
                } else {
                    MIA.log.writeWarning("Model not available.  Please install manually or enable \""
                            + INSTALL_IF_MISSING + "\" parameter.");
                }

            loadedSamJ = EfficientSamJ.initializeSam(manager);

        } catch (IOException | RuntimeException | InterruptedException | ArchiveException | URISyntaxException
                | MambaInstallException e) {
            e.printStackTrace();
        }

        return loadedSamJ;

    }

    @Override
    public Status initialiseBeforeImageShown(Workspace workspace) {
        this.workspace = workspace;

        // Getting parameters
        boolean useSAM = parameters.getValue(USE_SAM, workspace);
        if (!useSAM)
            return Status.PASS;

        String imageName = module.getParameterValue(ManuallyIdentifyObjects.INPUT_IMAGE, workspace);
        String envionmentPathMode = parameters.getValue(ENVIRONMENT_PATH_MODE, workspace);
        String environmentPath = parameters.getValue(ENVIRONMENT_PATH, workspace) + "/";
        boolean installIfMissing = parameters.getValue(INSTALL_IF_MISSING, workspace);
        boolean preinitialise = parameters.getValue(PREINITIALISE, workspace);

        switch (envionmentPathMode) {
            case EnvironmentPathModes.DEFAULT:
                environmentPath = SamEnvManagerAbstract.DEFAULT_DIR;
                break;
        }

        // Checking if SamJ has been preinitialised for this file
        String path = workspace.getMetadata().getFile().getAbsolutePath();
        try {
            // Checking if preinitialisation is for this file
            if (path.equals(preinitPath)) {
                // Waiting till preinitialisation is complete for this file
                while (!preinitComplete)
                    Thread.sleep(100);

                // Transfer preinitialised SamJ to main copy
                samJ = preinitSamJ;

            } else {
                // Initialise a new copy of SamJ
                samJ = initialiseSAMJ(environmentPath, installIfMissing);
                samJ.setImage(workspace.getImage(imageName).getImgPlus());
            }
        } catch (IOException | RuntimeException e1) {
            e1.printStackTrace();
            return Status.FAIL;
        } catch (InterruptedException e2) {
            // Don't throw an error in this case, as it's likely the sleep was interrupted
            // by the thread being manually stopped (e.g. "Stop" button on GUI)
            return Status.TERMINATE_SILENT;
        }

        if (preinitialise) {
            // Preinitialise next file on a separate thread
            preinitComplete = false;
            boolean isNext = false;
            Workspace nextWorkspace = null;
            for (Workspace currWorkspace : workspace.getWorkspaces()) {
                if (isNext) {
                    nextWorkspace = currWorkspace;
                    break;
                } else if (currWorkspace == workspace) {
                    isNext = true;
                }
            }

            if (nextWorkspace != null) {
                preinitPath = nextWorkspace.getMetadata().getFile().getAbsolutePath();

                // Creating a dummy workspace so we don't alter the real one
                Workspace dummyWorkspace = new Workspaces().getNewWorkspace(nextWorkspace.getMetadata().getFile(), nextWorkspace.getMetadata().getSeriesNumber());
                String finalEnvironmentPath = environmentPath;
                Thread t = new Thread(() -> {
                    // Running modules up to this point
                    Modules modules = module.getModules();
                    for (Module currModule : modules) {
                        if (currModule == module)
                            break;
                        currModule.execute(dummyWorkspace);
                    }

                    try {
                        preinitSamJ = initialiseSAMJ(finalEnvironmentPath, installIfMissing);
                        preinitSamJ.setImage(dummyWorkspace.getImage(imageName).getImgPlus());
                    } catch (IOException | RuntimeException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e2) {
                        // Don't throw an error in this case, as it's likely the sleep was interrupted
                        // by the thread being manually stopped (e.g. "Stop" button on GUI)
                    }

                    preinitComplete = true;

                });
                t.start();
            }
        }

        prevEnvironmentPath = environmentPath;

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
    public Status onFinishAddingObjects() {
        if (samJ != null)
            samJ.close();

        return Status.PASS;

    }

    @Override
    public Parameters initialiseParameters() {
        parameters.add(new SeparatorP(SAMJ_SEPARATOR, module));
        parameters.add(new BooleanP(USE_SAM, module, false));
        parameters.add(
                new ChoiceP(ENVIRONMENT_PATH_MODE, module, EnvironmentPathModes.DEFAULT, EnvironmentPathModes.ALL));
        parameters.add(new FolderPathP(ENVIRONMENT_PATH, module));
        parameters.add(new BooleanP(INSTALL_IF_MISSING, module, true));
        parameters.add(new BooleanP(PREINITIALISE, module, false));

        return parameters;

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(SAMJ_SEPARATOR));
        returnedParameters.add(parameters.getParameter(USE_SAM));

        if ((boolean) parameters.getValue(USE_SAM, workspace)) {
            returnedParameters.add(parameters.getParameter(ENVIRONMENT_PATH_MODE));

            switch ((String) parameters.getValue(ENVIRONMENT_PATH_MODE, workspace)) {
                case EnvironmentPathModes.SPECIFIC:
                    returnedParameters.add(parameters.getParameter(ENVIRONMENT_PATH));
                    break;
            }

            returnedParameters.add(parameters.getParameter(INSTALL_IF_MISSING));
            returnedParameters.add(parameters.getParameter(PREINITIALISE));

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

        List<Mask> masks = null;
        switch (roi.getType()) {
            case Roi.FREELINE:
            case Roi.LINE:
            case Roi.POINT:
            case Roi.POLYLINE:
            default:
                masks = getPolygonFromPoints(roi);
                break;

            case Roi.FREEROI:
            case Roi.OVAL:
            case Roi.POLYGON:
            case Roi.TRACED_ROI:
                // polygons = getPolygonFromMask(roi);
                // break;
            case Roi.RECTANGLE:
                masks = getPolygonFromRectangle(roi);
                break;
        }

        if (masks == null)
            return;

        for (Mask mask : masks) {
            PolygonRoi polygonRoi = new PolygonRoi(mask.getContour(), PolygonRoi.POLYGON);
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

    // ImageProcessor mask = roi.getMask();
    // Img img = ImageJFunctions.wrap(new ImagePlus("Mask",mask));
    // ImageJFunctions.show(img);
    // try {
    // return samJ.processMask(img);
    // } catch (IOException | InterruptedException | RuntimeException e) {
    // MIA.log.writeError(e);
    // }

    // return null;

    // }

    public List<Mask> getPolygonFromPoints(Roi roi) {
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

    public List<Mask> getPolygonFromRectangle(Roi roi) {
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
