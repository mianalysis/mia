package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.HashMap;

public class VolumeFactories {
    private static VolumeFactoryI defaultFactory = new DefaultVolumeFactory();

    public static void main(String[] args) {

    }

    private static HashMap<String, VolumeFactoryI> factories = new HashMap<>();

    public static HashMap<String, VolumeFactoryI> getFactories() {
        return factories;

    }

    public static void addFactory(VolumeFactoryI factory) {
        factories.put(factory.getName(), factory);
    }

    public static VolumeFactoryI getFactory(String name) {
        if (factories.containsKey(name))
            return factories.get(name);

        else
            return defaultFactory.duplicate();

    }

    public static VolumeFactoryI getDefaultFactory() {
        return defaultFactory;
    }

    public static String getDefaultFactoryName() {
    return defaultFactory.getName();
    }

    public static void setDefaultFactory(VolumeFactoryI factory) {
        defaultFactory = factory;
    }

    public static String[] listFactoryNames() {
        return factories.keySet().stream().toArray(s -> new String[factories.size()]);
    }
}
