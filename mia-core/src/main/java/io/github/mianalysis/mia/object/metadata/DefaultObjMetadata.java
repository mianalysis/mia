package io.github.mianalysis.mia.object.metadata;

import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;

/**
 * Metadata value that holds a single value for an object
 */
public class DefaultObjMetadata implements ObjMetadataI {
    private final String name;
    private String value = "";


    // CONSTRUCTOR

    public DefaultObjMetadata(String name, String value) {
        name = SpatialUnit.replace(name);
        name = TemporalUnit.replace(name);
        this.name = name;
        this.value = value;
    }

    public ObjMetadataI duplicate() {
        return new DefaultObjMetadata(getName(), getValue());
    }


    // GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
