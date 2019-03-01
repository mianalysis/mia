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

public class PointPairSelector implements ActionListener {
    private static final String ADD_PAIR = "Add pair";
    private static final String FINISH = "Finish";

    private JFrame frame;

    private ImagePlus ipl1;
    private ImagePlus ipl2;
    private Overlay overlay1;
    private Overlay overlay2;
    private ArrayList<PointPair> pairs;


    public ArrayList<PointPair> getPointPairs(ImagePlus ipl1, ImagePlus ipl2) {
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
                return null;
            }
        }

        return pairs;

    }

    private void showOptionsPanel() {
        pairs  = new ArrayList<>();
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

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (ADD_PAIR):
                addNewPair();
                break;

            case (FINISH):
                finishAdding();
                break;
        }
    }

    public void addNewPair() {
        Roi roi1 = ipl1.getRoi();
        Roi roi2 = ipl2.getRoi();

        if (roi1 == null || roi2 == null) {
            IJ.error("Select a single point in each image");
            return;
        }

        float[] centroidX1 = roi1.getFloatPolygon().xpoints;
        float[] centroidY1 = roi1.getFloatPolygon().ypoints;
        double x1 = new CumStat(centroidX1).getMean();
        double y1 = new CumStat(centroidY1).getMean();
        PointRoi point1 = new PointRoi(x1,y1);
        ipl1.deleteRoi();

        float[] centroidX2 = roi2.getFloatPolygon().xpoints;
        float[] centroidY2 = roi2.getFloatPolygon().ypoints;
        double x2 = new CumStat(centroidX2).getMean();
        double y2 = new CumStat(centroidY2).getMean();

        PointRoi point2 = new PointRoi(x2,y2);
        ipl2.deleteRoi();

        PointPair pair = new PointPair(point1,point2);
        pairs.add(pair);
        addToOverlay(pair);

    }

    public void finishAdding() {
        if (pairs.size() < 2) {
            IJ.error("Select at least two pairs");
            return;
        }

        // Closing the image, so the analysis can proceed
        frame.dispose();
        frame = null;
        ipl1.close();
        ipl2.close();

    }

    public void addToOverlay(PointPair pair) {
        PointRoi point1 = pair.getPoint1();
        point1.setPointType(PointRoi.NORMAL);
        point1.setSize(2);
        point1.setStrokeColor(Color.RED);
        overlay1.add(point1);

        PointRoi point2 = pair.getPoint2();
        point2.setPointType(PointRoi.NORMAL);
        point2.setSize(2);
        point2.setStrokeColor(Color.RED);
        overlay2.add(point2);

        ipl1.updateAndDraw();
        ipl2.updateAndDraw();

    }

    public class PointPair {
        private PointRoi p1;
        private PointRoi p2;

        public PointPair(PointRoi p1, PointRoi p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        public PointRoi getPoint1() {
            return p1;
        }

        public PointRoi getPoint2() {
            return p2;
        }
    }
}
