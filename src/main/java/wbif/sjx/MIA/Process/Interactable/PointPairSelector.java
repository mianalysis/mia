package wbif.sjx.MIA.Process.Interactable;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.*;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.Image;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class PointPairSelector implements ActionListener {
    private static final String ADD_PAIRS = "Add pair(s)";
    private static final String REMOVE_PAIR = "Remove pair(s)";
    private static final String TEST = "Test";
    private static final String FINISH = "Finish";

    private final Interactable interactable;
    private boolean showTest;

    private JFrame frame;
    private static int xPos = -1;
    private static int yPos = -1;
    private final JPanel objectsPanel = new JPanel();
    DefaultListModel<PointPair> listModel = new DefaultListModel<>();
    JList<PointPair> list = new JList<>(listModel);
    JScrollPane objectsScrollPane = new JScrollPane(list);

    private ImagePlus ipl1;
    private ImagePlus ipl2;
    private Overlay overlay1;
    private Overlay overlay2;
    private ArrayList<PointPair> pairs;
    private int maxID = 0;

    public PointPairSelector(Interactable interactable, boolean showTest) {
        this.interactable = interactable;
        this.showTest = showTest;

    }

    public ArrayList<PointPair> getPointPairs(ImagePlus ipl1, ImagePlus ipl2) {
        // Displaying the images and options panel.  While the control is open, do nothing
        IJ.setTool(Toolbar.POINT);

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
        list.removeAll();
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateOverlay();
            }
        });

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = showTest ? 4 : 3;
        c.gridheight = 1;
        c.weightx = 0.3333;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5,5,5,5);

        JLabel headerLabel = new JLabel("<html>Add point(s) to each image, then select \"Add pair(s)\"" +
                "<br>(or click \"Finish adding pairs\" at any time).</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        frame.add(headerLabel,c);

        JButton addPairButton = new JButton("Add pair(s)");
        addPairButton.addActionListener(this);
        addPairButton.setActionCommand(ADD_PAIRS);
        c.gridy++;
        c.gridwidth = 1;
        frame.add(addPairButton,c);

        JButton removePairButton = new JButton("Remove pair");
        removePairButton.addActionListener(this);
        removePairButton.setActionCommand(REMOVE_PAIR);
        c.gridx++;
        frame.add(removePairButton,c);

        if (showTest) {
            JButton testPairButton = new JButton("Test process");
            testPairButton.addActionListener(this);
            testPairButton.setActionCommand(TEST);
            c.gridx++;
            frame.add(testPairButton, c);
        }

        JButton finishButton = new JButton("Finish adding pairs");
        finishButton.addActionListener(this);
        finishButton.setActionCommand(FINISH);
        c.gridx++;
        frame.add(finishButton,c);

        objectsScrollPane.setPreferredSize(new Dimension(0,200));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = showTest ? 4 : 3;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        frame.add(objectsScrollPane,c);

        frame.pack();
        if (xPos == -1) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            xPos = (screenSize.width - frame.getWidth()) / 2;
            yPos = (screenSize.height - frame.getHeight()) / 2;
        }
        frame.setLocation(xPos, yPos);
        frame.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (ADD_PAIRS):
                addNewPairs();
                break;

            case (REMOVE_PAIR):
                removePair();
                break;

            case (TEST):
                interactable.doAction(new Object[]{pairs});
                break;

            case (FINISH):
                finishAdding();
                break;
        }
    }

    public void addNewPairs() {
        Roi roi1 = ipl1.getRoi();
        Roi roi2 = ipl2.getRoi();

        if (roi1 == null || roi2 == null) {
            IJ.error("Select at least one point in each image");
            return;
        }

        float[] centroidX1 = roi1.getFloatPolygon().xpoints;
        float[] centroidY1 = roi1.getFloatPolygon().ypoints;
        float[] centroidX2 = roi2.getFloatPolygon().xpoints;
        float[] centroidY2 = roi2.getFloatPolygon().ypoints;

        if (centroidX1.length != centroidX2.length) {
            IJ.error("Select the same number of points in each image");
            return;
        }

        for (int i=0;i<centroidX1.length;i++) {
            PointRoi point1 = new PointRoi(centroidX1[i], centroidY1[i]);
            ipl1.deleteRoi();

            PointRoi point2 = new PointRoi(centroidX2[i], centroidY2[i]);
            ipl2.deleteRoi();

            PointPair pair = new PointPair(point1, point2, ++maxID);
            pairs.add(pair);

            // Adding to the list of objects
            addPairToList(pair);

        }

        updateOverlay();

    }

    public void removePair() {
        // Get selected points
        List<PointPair> selected = list.getSelectedValuesList();

        pairs.removeAll(selected);
//        listModel.removeElement(selected);
        for (PointPair pair:selected) listModel.removeElement(pair);

        updateOverlay();

    }

    public void finishAdding() {
        if (pairs.size() < 2) {
            IJ.error("Select at least two pairs");
            return;
        }

        // Closing the image, so the analysis can proceed
        xPos = frame.getX();
        yPos = frame.getY();
        frame.dispose();
        frame = null;

        ipl1.close();
        ipl2.close();

    }

    public void addPairToList(PointPair pair) {
        listModel.addElement(pair);

        objectsPanel.validate();
        objectsPanel.repaint();

        // Ensuring the scrollbar is visible if necessary and moving to the bottom
        JScrollBar scrollBar = objectsScrollPane.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum()-1);
        objectsScrollPane.revalidate();

        updateOverlay();

    }

    public void updateOverlay() {
        overlay1.clear();
        overlay2.clear();

        // Get selected points
        List<PointPair> selected = list.getSelectedValuesList();

        for (PointPair pair:pairs) {
            Color color = selected.contains(pair) ? Color.CYAN : Color.RED;

            PointRoi point1 = pair.getPoint1();
            point1.setPointType(PointRoi.NORMAL);
            point1.setSize(2);
            point1.setStrokeColor(color);
            overlay1.add(point1);

            TextRoi textRoi1 = new TextRoi((int) point1.getXBase(), (int) point1.getYBase(), String.valueOf(pair.getID()));
            textRoi1.setStrokeColor(color);
            overlay1.add(textRoi1);

            PointRoi point2 = pair.getPoint2();
            point2.setPointType(PointRoi.NORMAL);
            point2.setSize(2);
            point2.setStrokeColor(color);
            overlay2.add(point2);

            TextRoi textRoi2 = new TextRoi((int) point2.getXBase(), (int) point2.getYBase(), String.valueOf(pair.getID()));
            textRoi2.setStrokeColor(color);
            overlay2.add(textRoi2);

        }

        ipl1.updateAndDraw();
        ipl2.updateAndDraw();

    }

    public static ArrayList<PointPair> getPreselectedPoints(Image inputImage, Image reference) {
        ArrayList<PointPair> pairs = new ArrayList<>();
        Roi roi1 = inputImage.getImagePlus().getRoi();
        Roi roi2 = reference.getImagePlus().getRoi();

        if (roi1 == null || roi2 == null) {
            MIA.log.writeError("No points selected in at least one image");
            return null;
        }

        float[] centroidX1 = roi1.getFloatPolygon().xpoints;
        float[] centroidY1 = roi1.getFloatPolygon().ypoints;
        float[] centroidX2 = roi2.getFloatPolygon().xpoints;
        float[] centroidY2 = roi2.getFloatPolygon().ypoints;

        if (centroidX1.length != centroidX2.length) {
            MIA.log.writeError("Unequal number of points selected in each image");
            return null;
        }

        int maxID = 0;
        for (int i=0;i<centroidX1.length;i++) {
            PointRoi point1 = new PointRoi(centroidX1[i], centroidY1[i]);
            PointRoi point2 = new PointRoi(centroidX2[i], centroidY2[i]);

            PointPair pair = new PointPair(point1, point2, ++maxID);
            pairs.add(pair);

        }

        return pairs;

    }

    public static class PointPair {
        private PointRoi p1;
        private PointRoi p2;
        private int ID;

        public PointPair(PointRoi p1, PointRoi p2, int ID) {
            this.p1 = p1;
            this.p2 = p2;
            this.ID = ID;
        }

        public PointRoi getPoint1() {
            return p1;
        }

        public PointRoi getPoint2() {
            return p2;
        }

        public int getID() {
            return ID;
        }

        @Override
        public String toString() {
            return "Pair "+ID;
        }
    }
}