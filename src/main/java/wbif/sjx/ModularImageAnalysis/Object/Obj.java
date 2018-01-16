package wbif.sjx.ModularImageAnalysis.Object;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.Selection;
import ij.plugin.ZProjector;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.ObjectImageConverter;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.ProjectObjects;
import wbif.sjx.common.Object.Volume;

import java.util.*;

/**
 * Created by steph on 30/04/2017.
 */
public class Obj extends Volume {
    private String name;

    /**
     * Unique instance ID for this object
     */
    private int ID;

    /**
     * Each instance of an object is only present in XYZ and a single timepoint.  Any other dimensionality
     * (e.g. channel) must be added as a measurement.
     */
    private int T = 0;

    private LinkedHashMap<String, Obj> parents = new LinkedHashMap<>();
    private LinkedHashMap<String, ObjCollection> children = new LinkedHashMap<>();
    private LinkedHashMap<String, Measurement> measurements = new LinkedHashMap<>();


    // CONSTRUCTORS

    public Obj(String name, int ID, double dppXY, double dppZ, String calibratedUnits) {
        super(dppXY,dppZ,calibratedUnits);

        this.name = name;
        this.ID = ID;

    }


    // PUBLIC METHODS

    public int[][] getCoordinateRange() {
        int[][] dimSize = new int[3][2];

        ArrayList<Integer> x = getXCoords();
        ArrayList<Integer> y = getYCoords();
        ArrayList<Integer> z = getZCoords();

        dimSize[0][0] = Collections.min(x);
        dimSize[0][1] = Collections.max(x);
        dimSize[1][0] = Collections.min(y);
        dimSize[1][1] = Collections.max(y);
        dimSize[2][0] = Collections.min(z);
        dimSize[2][1] = Collections.max(z);

        return dimSize;

    }

    public void addMeasurement(Measurement measurement) {
        measurements.put(measurement.getName(), measurement);

    }

    public Measurement getMeasurement(String name) {
        if (measurements.get(name) == null) return null;
        return measurements.get(name);

    }

    public void removeMeasurement(String name) {
        measurements.remove(name);

    }

    @Override
    public String toString() {
        return "Object " + name + ", ID = "+ID;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getT() {
        return T;
    }

    public void setT(int t) {
        T = t;
    }

    public LinkedHashMap<String, Obj> getParents(boolean useFullHierarchy) {
        if (!useFullHierarchy) return parents;

        // Adding each parent and then the parent of that
        LinkedHashMap<String,Obj> parentHierarchy = new LinkedHashMap<>(parents);

        // Going through each parent, adding the parents of that.
        for (Obj parent:parents.values()) {
            if (parent == null) continue;

            LinkedHashMap<String,Obj> currentParents = parent.getParents(true);
            if (currentParents == null) continue;

            parentHierarchy.putAll(currentParents);

        }

        return parentHierarchy;

    }

    public void setParents(LinkedHashMap<String, Obj> parents) {
        this.parents = parents;
    }

    public Obj getParent(String name) {
        return parents.get(name);

    }

    public void addParent(Obj parent) {
        parents.put(parent.getName(), parent);
    }

    public void addParent(String name, Obj parent) {
        parents.put(name, parent);
    }

    public void removeParent(String name) {
        parents.remove(name);

    }

    public LinkedHashMap<String, ObjCollection> getChildren() {
        return children;
    }

    public ObjCollection getChildren(String name) {
        return children.get(name);
    }

    public void setChildren(LinkedHashMap<String, ObjCollection> children) {
        this.children = children;
    }

    public void addChildren(ObjCollection childSet) {
        children.put(childSet.getName(), childSet);
    }

    public void removeChildren(String name) {
        children.remove(name);
    }

    public void addChild(Obj child) {
        String childName = child.getName();

        children.computeIfAbsent(childName, k -> new ObjCollection(childName));
        children.get(childName).put(child.getID(), child);

    }

    public void removeChild(Obj child) {
        String childName = child.getName();
        children.get(childName).values().remove(child);

    }

    /**
     * Removes itself from any other objects as a parent or child.
     */
    public void removeRelationships() {
        // Removing itself as a child from its parent
        if (parents != null) {
            for (Obj parent:parents.values()) {
                if (parent != null) parent.removeChild(this);

            }
        }

        // Removing itself as a parent from any children
        if (children != null) {
            for (ObjCollection childSet:children.values()) {
                for (Obj child:childSet.values()) {
                    child.removeParent(name);

                }
            }
        }
    }

    public LinkedHashMap<String, Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(LinkedHashMap<String, Measurement> measurements) {
        this.measurements = measurements;

    }

    public Roi getRoi(Image templateImage) {
        // If the template image isn't in 2D, convert it to this
        ImagePlus templateIpl = templateImage.getImagePlus();
        if (templateIpl.getNSlices() != 1 && templateIpl.getNFrames() != 1 && templateIpl.getNChannels() != 1) {
            ImagePlus template2D = IJ.createImage("Template",templateIpl.getWidth(),templateIpl.getHeight(),1,8);
            templateImage = new Image("Template",template2D);
        }

        // Projecting object and converting to a binary 2D image
        Obj projectedObject = ProjectObjects.createProjection(this,"Projected");
        ObjCollection objectCollection = new ObjCollection("ProjectedObjects");
        objectCollection.add(projectedObject);
        Image objectImage = ObjectImageConverter.convertObjectsToImage(objectCollection,"ProjectedImage",templateImage,ObjectImageConverter.ColourModes.SINGLE_COLOUR,null,false);

        // Getting the object as a Roi
        objectImage.getImagePlus().getProcessor().invert();
        IJ.runPlugIn(objectImage.getImagePlus(),"ij.plugin.Selection","from");
        return objectImage.getImagePlus().getRoi();

    }
}

