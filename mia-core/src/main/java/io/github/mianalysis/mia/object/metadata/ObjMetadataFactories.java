package io.github.mianalysis.mia.object.metadata;

import java.util.HashMap;

public class ObjMetadataFactories {
    private static ObjMetadataFactoryI defaultFactory = new DefaultObjMetadataFactory();

    private static HashMap<String, ObjMetadataFactoryI> factories = new HashMap<>();

    public static HashMap<String, ObjMetadataFactoryI> getFactories() {
        return factories;
    }

    public static void addFactory(ObjMetadataFactoryI factory) {
        factories.put(factory.getName(), factory);
    }

    public static ObjMetadataFactoryI getFactory(String name) {
        if (factories.containsKey(name))
            return factories.get(name);

        else
            return defaultFactory.duplicate();

    }

    public static ObjMetadataFactoryI getDefaultFactory() {
        return defaultFactory;
    }

    public static String getDefaultFactoryName() {
        return defaultFactory.getName();
    }

    public static void setDefaultFactory(ObjMetadataFactoryI factory) {
        defaultFactory = factory;
    }

    public static String[] listFactoryNames() {
        return factories.keySet().stream().toArray(s -> new String[factories.size()]);
    }
}
