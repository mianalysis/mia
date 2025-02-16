package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.HashMap;

public class VolumeFactories {
    private static VolumeFactory defaultFactory = new DefaultVolumeFactory();

    public static void main(String[] args) {

    }

    private static HashMap<String, VolumeFactory> factories = new HashMap<>();

    public static HashMap<String, VolumeFactory> getFactories() {
        return factories;

    }

    public static void addFactory(VolumeFactory factory) {
        factories.put(factory.getName(), factory);
    }

    public static VolumeFactory getFactory(String name) {
        if (factories.containsKey(name))
            return factories.get(name);

        else
            return defaultFactory.duplicate();

    }

    public static VolumeFactory getDefaultFactory() {
        return defaultFactory;
    }

    public static String getDefaultFactoryName() {
        return defaultFactory.getName();
    }

    public static void setDefaultFactory(VolumeFactory factory) {
        defaultFactory = factory;
    }

    public static String[] listFactoryNames() {
        return factories.keySet().stream().toArray(s -> new String[factories.size()]);
    }
}
