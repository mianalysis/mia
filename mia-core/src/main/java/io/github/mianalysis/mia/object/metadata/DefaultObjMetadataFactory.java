package io.github.mianalysis.mia.object.metadata;

public class DefaultObjMetadataFactory implements ObjMetadataFactoryI {

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public ObjMetadataI createMetadata(String name, String value) {
        return new DefaultObjMetadata(name, value);
    }

    @Override
    public ObjMetadataFactoryI duplicate() {
        return new DefaultObjMetadataFactory();
    }
}
