package io.github.mianalysis.mia.object.coordinates;

import java.util.HashMap;
import java.util.LinkedHashMap;

import ij.gui.Roi;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.DefaultVolume;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

/**
 * Created by Stephen on 30/04/2017.
 */
public class DefaultObj extends DefaultVolume implements ObjI {
    /**
     * Unique instance ID for this object
     */
    private int ID;

    private int T = 0;

    private ObjsI objCollection;

    protected int nFrames;
    protected double frameInterval;
    protected Unit<Time> temporalUnit;

    private LinkedHashMap<String, ObjI> parents = new LinkedHashMap<>();
    private LinkedHashMap<String, ObjsI> children = new LinkedHashMap<>();
    private LinkedHashMap<String, ObjsI> partners = new LinkedHashMap<>();
    private LinkedHashMap<String, MeasurementI> measurements = new LinkedHashMap<>();
    private LinkedHashMap<String, ObjMetadata> metadata = new LinkedHashMap<>();
    private HashMap<Integer, Roi> rois = new HashMap<>();

    // CONSTRUCTORS

    public DefaultObj(CoordinateSetFactoryI factory, ObjsI objCollection) {
        super(factory, objCollection);

        this.objCollection = objCollection;

        this.nFrames = objCollection.getNFrames();
        this.frameInterval = objCollection.getFrameInterval();
        this.temporalUnit = objCollection.getTemporalUnit();
        
        this.ID = objCollection.getAndIncrementID();

    }

    public DefaultObj(CoordinateSetFactoryI factory, ObjsI objCollection, int ID) {
        super(factory, objCollection);

        this.objCollection = objCollection;

        this.nFrames = objCollection.getNFrames();
        this.frameInterval = objCollection.getFrameInterval();
        this.temporalUnit = objCollection.getTemporalUnit();

        this.ID = ID;

    }

    @Override
    public ObjsI getObjectCollection() {
        return objCollection;
    }

    @Override
    public void setObjectCollection(ObjsI objCollection) {
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
    public ObjI setID(int ID) {
        this.ID = ID;
        return this;
    }

    @Override
    public int getT() {
        return T;
    }

    @Override
    public ObjI setT(int t) {
        this.T = t;
        return this;
    }

    @Override
    public LinkedHashMap<String, ObjI> getAllParents() {
        return parents;
    }

    @Override
    public void setAllParents(LinkedHashMap<String, ObjI> parents) {
        this.parents = parents;
    }

    @Override
    public LinkedHashMap<String, ObjsI> getAllChildren() {
        return children;
    }

    @Override
    public void setAllChildren(LinkedHashMap<String, ObjsI> children) {
        this.children = children;
    }

    @Override
    public LinkedHashMap<String, ObjsI> getAllPartners() {
        return partners;
    }

    @Override
    public void setAllPartners(LinkedHashMap<String, ObjsI> partners) {
        this.partners = partners;
    }

    /**
     * Removes itself from any other objects as a parent or child.
     */
    @Override
    public void removeRelationships() {
        // Removing itself as a child from its parent
        if (parents != null) {
            for (ObjI parent : parents.values()) {
                if (parent != null)
                    parent.removeChild(this);
            }
        }

        // Removing itself as a parent from any children
        if (children != null) {
            for (ObjsI childSet : children.values()) {
                for (ObjI child : childSet.values()) {
                    if (child.getParent(getName()) == this) {
                        child.removeParent(getName());
                    }
                }
            }
        }

        // Removing itself as a partner from any partners
        if (partners != null) {
            for (ObjsI partnerSet : partners.values()) {
                for (ObjI partner : partnerSet.values()) {
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
    public LinkedHashMap<String, MeasurementI> getMeasurements() {
        return measurements;
    }

    @Override
    public void setMeasurements(LinkedHashMap<String, MeasurementI> measurements) {
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
    public ObjI duplicate(ObjsI newCollection, boolean duplicateRelationships, boolean duplicateMeasurement,
            boolean duplicateMetadata) {
        ObjI newObj = new DefaultObj(getCoordinateSetFactory(), newCollection, getID());

        // Duplicating coordinates
        newObj.setCoordinateSet(getCoordinateSet().duplicate());

        // Setting timepoint
        newObj.setT(getT());

        // Duplicating relationships
        if (duplicateRelationships) {
            for (ObjI parent : parents.values()) {
                newObj.addParent(parent);
                parent.addChild(this);
            }

            for (ObjsI currChildren : children.values())
                for (ObjI child : currChildren.values()) {
                    newObj.addChild(child);
                    child.addParent(newObj);
                }

            for (ObjsI currPartners : partners.values())
                for (ObjI partner : currPartners.values()) {
                    newObj.addPartner(partner);
                    partner.addPartner(newObj);
                }
        }

        // Duplicating measurements
        if (duplicateMeasurement)
            for (MeasurementI measurement : measurements.values())
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
        if (ID != ((ObjI) obj).getID())
            return false;

        if (!getName().equals(((ObjI) obj).getName()))
            return false;

        return equalsIgnoreNameAndID(obj);

    }

    @Override
    public boolean equalsIgnoreNameAndID(Object obj) {
        int T = getT();

        if (!super.equals(obj))
            return false;
        
        if (obj == this)
            return true;

        if (!(obj instanceof ObjI))
            return false;

        return (T == ((ObjI) obj).getT());

    }

    @Override
    public String toString() {
        return "Object \"" + getName() + "\", ID = " + ID + ", frame = " + getT();
    }

    @Override
    public int getNFrames() {
        return nFrames;
    }

    @Override
    public double getFrameInterval() {
        return frameInterval;
    }

    @Override
    public Unit<Time> getTemporalUnit() {
        return temporalUnit;
    }

    @Override
    public void setNFrames(int nFrames) {
        this.nFrames = nFrames;
    }

    @Override
    public void setFrameInterval(double frameInterval) {
        this.frameInterval = frameInterval;
    }

    @Override
    public void setTemporalUnit(Unit<Time> temporalUnit) {
        this.temporalUnit = temporalUnit;
    }
}
