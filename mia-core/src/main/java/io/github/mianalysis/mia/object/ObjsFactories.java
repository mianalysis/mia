package io.github.mianalysis.mia.object;

import java.util.HashMap;

public class ObjsFactories {
    private static ObjsFactoryI defaultFactory = new DefaultObjsFactory();

    private static HashMap<String, ObjsFactoryI> factories = new HashMap<>();

    public static HashMap<String, ObjsFactoryI> getFactories() {
        return factories;
    }

    public static void addFactory(ObjsFactoryI factory) {
        factories.put(factory.getName(), factory);
    }

    public static ObjsFactoryI getFactory(String name) {
        if (factories.containsKey(name))
            return factories.get(name);

        else
            return defaultFactory.duplicate();

    }

    public static ObjsFactoryI getDefaultFactory() {
        return defaultFactory;
    }

    public static String getDefaultFactoryName() {
        return defaultFactory.getName();
    }

    public static void setDefaultFactory(ObjsFactoryI factory) {
        defaultFactory = factory;
    }

    public static String[] listFactoryNames() {
        return factories.keySet().stream().toArray(s -> new String[factories.size()]);
    }
}
