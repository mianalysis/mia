package wbif.sjx.ModularImageAnalysis.Process;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.Roi;
import wbif.sjx.common.MathFunc.CumStat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MultiPointPairSelector implements ActionListener {
    private static final String ADD_POINTS = "Add points";
    private static final String FINISH = "Finish";

    private JFrame frame;

    private ImagePlus ipl1;
    private ImagePlus ipl2;
    private Overlay overlay1;
    private Overlay overlay2;
    private ArrayList<PointRoi> points1;
    private ArrayList<PointRoi> points2;


    public void selectPoints(ImagePlus ipl1, ImagePlus ipl2) {
        this.ipl1 = ipl1;
        this.ipl2 = ipl2;

        ipl1.setTitle("Select points on this image");
        ipl1.show();
        overlay1 = ipl1.getOverlay();
        if (overlay1 == null) {
            overlay1 = new Overlay();
            ipl1.setOverlay(overlay1);
        }

        ipl2.setTitle("Select points on this image");
        ipl2.show();
        overlay2 = ipl2.getOverlay();
        if (overlay2 == null) {
            overlay2 = new Overlay();
            ipl2.setOverlay(overlay2);
        }

        showOptionsPanel();
        while (frame != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }

        return;

    }

    private void showOptionsPanel() {
        points1  = new ArrayList<>();
        points2  = new ArrayList<>();
        frame = new JFrame();
        frame.setAlwaysOnTop(true);

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5,5,5,5);

        JLabel headerLabel = new JLabel("<html>Add points to each image, then select \"Add points\"" +
                "<br>(or click \"Finish adding points\" at any time).</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        frame.add(headerLabel,c);

        JButton addPointsButton = new JButton("Add points");
        addPointsButton.addActionListener(this);
        addPointsButton.setActionCommand(ADD_POINTS);
        c.gridy++;
        c.gridwidth = 1;
        frame.add(addPointsButton,c);

        JButton finishButton = new JButton("Finish adding points");
        finishButton.addActionListener(this);
        finishButton.setActionCommand(FINISH);
        c.gridx++;
        frame.add(finishButton,c);

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (ADD_POINTS):
                addNewPoints();
                break;

            case (FINISH):
                finishAdding();
                break;
        }
    }

    public void addNewPoints() {
        Roi roi1 = ipl1.getRoi();
        Roi roi2 = ipl2.getRoi();

        if (roi1 == null || roi2 == null) {
            IJ.error("Select a single point in each image");
            return;
        }

        float[] centroidX1 = roi1.getFloatPolygon().xpoints;
        float[] centroidY1 = roi1.getFloatPolygon().ypoints;
        for (int i=0;i<centroidX1.length;i++) {
            PointRoi point1 = new PointRoi(centroidX1[i], centroidY1[i]);
            ipl1.deleteRoi();
            points1.add(point1);
        }

        float[] centroidX2 = roi2.getFloatPolygon().xpoints;
        float[] centroidY2 = roi2.getFloatPolygon().ypoints;
        for (int i=0;i<centroidX2.length;i++) {
            PointRoi point2 = new PointRoi(centroidX2[i], centroidY2[i]);
            ipl2.deleteRoi();
            points2.add(point2);
        }

        updateOverlay();

    }

    public void finishAdding() {
        if (points1.size() < 2 && points2.size() < 2) {
            IJ.error("Select at least two points per image");
            return;
        }

        // Closing the image, so the analysis can proceed
        frame.dispose();
        frame = null;
        ipl1.close();
        ipl2.close();

    }

    public void updateOverlay() {
        overlay1.clear();
        for (PointRoi point1:points1) {
            point1.setPointType(PointRoi.NORMAL);
            point1.setSize(2);
            point1.setStrokeColor(Color.RED);
            overlay1.add(point1);
        }

        overlay2.clear();
        for (PointRoi point2:points2) {
            point2.setPointType(PointRoi.NORMAL);
            point2.setSize(2);
            point2.setStrokeColor(Color.RED);
            overlay2.add(point2);
        }

        ipl1.updateAndDraw();
        ipl2.updateAndDraw();

    }

    public ArrayList<PointRoi> getPoints1() {
        return points1;
    }

    public ArrayList<PointRoi> getPoints2() {
        return points2;
    }
}
