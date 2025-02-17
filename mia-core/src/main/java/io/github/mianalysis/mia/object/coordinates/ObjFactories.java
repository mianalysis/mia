package io.github.mianalysis.mia.object.coordinates;

import java.util.HashMap;

public class ObjFactories {
    private static ObjFactory defaultFactory = new DefaultObjFactory();

    public static void main(String[] args) {

    }

    private static HashMap<String, ObjFactory> factories = new HashMap<>();

    public static HashMap<String, ObjFactory> getFactories() {
        return factories;

    }

    public static void addFactory(ObjFactory factory) {
        factories.put(factory.getName(), factory);
    }

    public static ObjFactory getFactory(String name) {
        if (factories.containsKey(name))
            return factories.get(name);

        else
            return defaultFactory.duplicate();

    }

    public static ObjFactory getDefaultFactory() {
        return defaultFactory;
    }

    public static String getDefaultFactoryName() {
        return defaultFactory.getName();
    }

    public static void setDefaultFactory(ObjFactory factory) {
        defaultFactory = factory;
    }

    public static String[] listFactoryNames() {
        return factories.keySet().stream().toArray(s -> new String[factories.size()]);
    }
}
