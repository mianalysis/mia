package io.github.mianalysis.mia.object.coordinates;

import java.util.HashMap;
import java.util.LinkedHashMap;

import ij.gui.Roi;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.DefaultVolume;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.measurements.Measurement;

/**
 * Created by Stephen on 30/04/2017.
 */
public class DefaultObj extends DefaultVolume implements Obj {
    /**
     * Unique instance ID for this object
     */
    private int ID;

    private int T = 0;

    private Objs objCollection;

    private LinkedHashMap<String, Obj> parents = new LinkedHashMap<>();
    private LinkedHashMap<String, Objs> children = new LinkedHashMap<>();
    private LinkedHashMap<String, Objs> partners = new LinkedHashMap<>();
    private LinkedHashMap<String, Measurement> measurements = new LinkedHashMap<>();
    private LinkedHashMap<String, ObjMetadata> metadata = new LinkedHashMap<>();
    private HashMap<Integer, Roi> rois = new HashMap<>();

    // CONSTRUCTORS

    public DefaultObj(Objs objCollection, CoordinateSetFactoryI factory, int ID) {
        super(factory, objCollection.getSpatialCalibration());

        this.objCollection = objCollection;
        this.ID = ID;

    }

    public DefaultObj(Objs objCollection, CoordinateSetFactoryI factory, int ID, SpatCal spatCal) {
        super(factory, spatCal);

        this.objCollection = objCollection;
        this.ID = ID;

    }

    @Override
    public Objs getObjectCollection() {
        return objCollection;
    }

    @Override
    public void setObjectCollection(Objs objCollection) {
        this.objCollection = objCollection;
    }

    @Override
    public String getName() {
        return objCollection.getName();
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public Obj setID(int ID) {
        this.ID = ID;
        return this;
    }

    @Override
    public int getT() {
        return T;
    }

    @Override
    public Obj setT(int t) {
        this.T = t;
        return this;
    }

    @Override
    public LinkedHashMap<String, Obj> getAllParents() {
        return parents;
    }

    @Override
    public void setAllParents(LinkedHashMap<String, Obj> parents) {
        this.parents = parents;
    }

    @Override
    public LinkedHashMap<String, Objs> getAllChildren() {
        return children;
    }

    @Override
    public void setAllChildren(LinkedHashMap<String, Objs> children) {
        this.children = children;
    }

    @Override
    public LinkedHashMap<String, Objs> getAllPartners() {
        return partners;
    }

    @Override
    public void setAllPartners(LinkedHashMap<String, Objs> partners) {
        this.partners = partners;
    }

    /**
     * Removes itself from any other objects as a parent or child.
     */
    @Override
    public void removeRelationships() {
        // Removing itself as a child from its parent
        if (parents != null) {
            for (Obj parent : parents.values()) {
                if (parent != null)
                    parent.removeChild(this);
            }
        }

        // Removing itself as a parent from any children
        if (children != null) {
            for (Objs childSet : children.values()) {
                for (Obj child : childSet.values()) {
                    if (child.getParent(getName()) == this) {
                        child.removeParent(getName());
                    }
                }
            }
        }

        // Removing itself as a partner from any partners
        if (partners != null) {
            for (Objs partnerSet : partners.values()) {
                for (Obj partner : partnerSet.values()) {
                    partner.removePartner(this);
                }
            }
        }

        // Clearing children and parents
        children = new LinkedHashMap<>();
        parents = new LinkedHashMap<>();
        partners = new LinkedHashMap<>();

    }

    @Override
    public LinkedHashMap<String, Measurement> getMeasurements() {
        return measurements;
    }

    @Override
    public void setMeasurements(LinkedHashMap<String, Measurement> measurements) {
        this.measurements = measurements;

    }

    @Override
    public LinkedHashMap<String, ObjMetadata> getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(LinkedHashMap<String, ObjMetadata> metadata) {
        this.metadata = metadata;

    }

    @Override
    public Roi getRoi(int slice) {
        if (rois.containsKey(slice))
            return (Roi) rois.get(slice).clone();

        Roi roi = super.getRoi(slice);

        if (roi == null)
            return null;

        rois.put(slice, roi);

        return (Roi) roi.clone();

    }

    /**
     * Accesses and generates all ROIs for the object
     * 
     * @return HashMap containing each ROI. Keys are integers with zero-based
     *         numbering, specifying Z-axis slice.
     */
    @Override
    public HashMap<Integer, Roi> getRois() {
        // This will access and generate all ROIs for this object
        double[][] extents = getExtents(true, false);
        int minZ = (int) Math.round(extents[2][0]);
        int maxZ = (int) Math.round(extents[2][1]);
        for (int z = minZ; z <= maxZ; z++)
            getRoi(z);

        // For the sake of not wasting memory, this will output the original list of
        // ROIs
        return rois;

    }

    @Override
    public void clearROIs() {
        rois = new HashMap<>();

    }

    @Override
    public void clearAllCoordinates() {
        super.clearAllCoordinates();
        rois = new HashMap<>();
    }

    @Override
    public Obj duplicate(Objs newCollection, boolean duplicateRelationships, boolean duplicateMeasurement,
            boolean duplicateMetadata) {
        Obj newObj = new DefaultObj(newCollection, getCoordinateSetFactory(), getID());

        // Duplicating coordinates
        newObj.setCoordinateSet(getCoordinateSet().duplicate());

        // Setting timepoint
        newObj.setT(getT());

        // Duplicating relationships
        if (duplicateRelationships) {
            for (Obj parent : parents.values()) {
                newObj.addParent(parent);
                parent.addChild(this);
            }

            for (Objs currChildren : children.values())
                for (Obj child : currChildren.values()) {
                    newObj.addChild(child);
                    child.addParent(newObj);
                }

            for (Objs currPartners : partners.values())
                for (Obj partner : currPartners.values()) {
                    newObj.addPartner(partner);
                    partner.addPartner(newObj);
                }
        }

        // Duplicating measurements
        if (duplicateMeasurement)
            for (Measurement measurement : measurements.values())
                newObj.addMeasurement(measurement.duplicate());

        // Duplicating metadata
        if (duplicateMetadata)
            for (ObjMetadata metadataItem : metadata.values())
                newObj.addMetadataItem(metadataItem.duplicate());

        return newObj;

    }

    @Override
    public int hashCode() {
        // Updating the hash for time-point. Measurements and relationships aren't
        // included; only spatial location.
        return super.hashCode() * 31 + getID() * 31 + getT() * 31 + getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        int T = getT();

        if (!super.equals(obj))
            return false;

        if (obj == this)
            return true;
        if (!(obj instanceof Obj))
            return false;

        if (ID != ((Obj) obj).getID())
            return false;

        if (T != ((Obj) obj).getT())
            return false;

        return (getName().equals(((Obj) obj).getName()));

    }

    @Override
    public boolean equalsIgnoreNameAndID(Object obj) {
        int T = getT();

        if (!super.equals(obj))
            return false;

        if (obj == this)
            return true;
        if (!(obj instanceof Obj))
            return false;

        return (T == ((Obj) obj).getT());

    }

    @Override
    public String toString() {
        return "Object \"" + getName() + "\", ID = " + ID + ", frame = " + getT();
    }
}
