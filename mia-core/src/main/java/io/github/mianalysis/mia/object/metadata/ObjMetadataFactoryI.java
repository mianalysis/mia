package io.github.mianalysis.mia.object.metadata;

public interface ObjMetadataFactoryI {
    public String getName();
    public ObjMetadataI createMetadata(String name, String value);
    public ObjMetadataFactoryI duplicate();

}