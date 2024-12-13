package io.github.mianalysis.mia.object.image;

import java.util.HashMap;
import java.util.LinkedHashMap;

import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.measurements.Measurement;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Created by stephen on 30/04/2017.
 */
public abstract class Image<T extends RealType<T> & NativeType<T>> implements ImageI<T> {
    protected String name;
    protected LinkedHashMap<String, Measurement> measurements = new LinkedHashMap<>();

    // PUBLIC METHODS


    public Objs convertImageToObjects(String outputObjectsName) {
        String type = ImageI.getVolumeType(VolumeType.POINTLIST);
        return convertImageToObjects(type, outputObjectsName, false);
    }
        
    public Objs convertImageToObjects(String outputObjectsName, boolean singleObject) {
        String type = ImageI.getVolumeType(VolumeType.POINTLIST);
        return convertImageToObjects(type, outputObjectsName, singleObject);
    }

    public Objs convertImageToObjects(VolumeType volumeType, String outputObjectsName) {
        String type = ImageI.getVolumeType(volumeType);
        return convertImageToObjects(type, outputObjectsName, false);
    }

    public Objs convertImageToObjects(VolumeType volumeType, String outputObjectsName, boolean singleObject) {
        String type = ImageI.getVolumeType(volumeType);
        return convertImageToObjects(type, outputObjectsName, singleObject);
    }

    public void addMeasurement(Measurement measurement) {
        measurements.put(measurement.getName(), measurement);

    }

    public Measurement getMeasurement(String name) {
        return measurements.get(name);

    }


    // GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public HashMap<String, Measurement> getMeasurements() {
        return measurements;
    }

    @Override
    public void setMeasurements(LinkedHashMap<String, Measurement> measurements) {
        this.measurements = measurements;
    }
}