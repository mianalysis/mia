package io.github.mianalysis.mia.object.metadata;

/**
 * Metadata value that holds a single value for an object
 */
public interface ObjMetadataI {
    public ObjMetadataI duplicate();


    // GETTERS AND SETTERS

    public String getName();

    public String getValue();

    public void setValue(String value);

}
