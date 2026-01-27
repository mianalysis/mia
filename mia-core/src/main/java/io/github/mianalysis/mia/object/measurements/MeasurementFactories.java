package io.github.mianalysis.mia.object.measurements;

import java.util.HashMap;

public class MeasurementFactories {
    private static MeasurementFactoryI defaultFactory = new DefaultMeasurementFactory();

    private static HashMap<String, MeasurementFactoryI> factories = new HashMap<>();

    public static HashMap<String, MeasurementFactoryI> getFactories() {
        return factories;
    }

    public static void addFactory(MeasurementFactoryI factory) {
        factories.put(factory.getName(), factory);
    }

    public static MeasurementFactoryI getFactory(String name) {
        if (factories.containsKey(name))
            return factories.get(name);

        else
            return defaultFactory.duplicate();

    }

    public static MeasurementFactoryI getDefaultFactory() {
        return defaultFactory;
    }

    public static String getDefaultFactoryName() {
        return defaultFactory.getName();
    }

    public static void setDefaultFactory(MeasurementFactoryI factory) {
        defaultFactory = factory;
    }

    public static String[] listFactoryNames() {
        return factories.keySet().stream().toArray(s -> new String[factories.size()]);
    }
}
