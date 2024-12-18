package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.HashMap;

import ij.util.ArrayUtil;

public class CoordinateSetFactories {
    private static CoordinateSetFactoryI defaultFactory = new PointListFactory();

    public static void main(String[] args) {

    }

    private static HashMap<String, CoordinateSetFactoryI> factories = new HashMap<>();

    public static HashMap<String, CoordinateSetFactoryI> getFactories() {
        return factories;

    }

    public static void addFactory(CoordinateSetFactoryI factory) {
        factories.put(factory.getName(), factory);
    }

    public static CoordinateSetFactoryI getFactory(String name) {
        if (factories.containsKey(name))
            return factories.get(name);

        else
            return defaultFactory.duplicate();

    }

    public static CoordinateSetFactoryI getDefaultFactory() {
        return defaultFactory;
    }

    public static String getDefaultFactoryName() {
    return defaultFactory.getName();
    }

    public static void setDefaultFactory(CoordinateSetFactoryI factory) {
        defaultFactory = factory;
    }

    public static String[] listFactoryNames() {
        return factories.keySet().stream().toArray(s -> new String[factories.size()]);
    }
}
