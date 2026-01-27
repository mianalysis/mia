package io.github.mianalysis.mia.object.image;

import java.util.LinkedHashMap;

import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Created by stephen on 30/04/2017.
 */
public abstract class Image<T extends RealType<T> & NativeType<T>> implements ImageI<T> {
    protected String name;
    protected LinkedHashMap<String, MeasurementI> measurements = new LinkedHashMap<>();

    // PUBLIC METHODS

    public ObjsI convertImageToObjects(CoordinateSetFactoryI factory, String outputObjectsName, boolean singleObject) {
        return convertImageToObjects(factory, outputObjectsName, singleObject);
    }

    public void addMeasurement(MeasurementI measurement) {
        measurements.put(measurement.getName(), measurement);

    }

    public MeasurementI getMeasurement(String name) {
        return measurements.get(name);

    }

    public void removeMeasurement(String name) {
        measurements.remove(name);
        
    }


    // GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public LinkedHashMap<String, MeasurementI> getMeasurements() {
        return measurements;
    }

    @Override
    public void setMeasurements(LinkedHashMap<String, MeasurementI> measurements) {
        this.measurements = measurements;
    }
}