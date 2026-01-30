package io.github.mianalysis.mia.object.image;

import java.util.HashMap;

public class ImageFactories {
    private static ImageFactoryI defaultFactory = new ImagePlusImageFactory();

    private static HashMap<String, ImageFactoryI> factories = new HashMap<>();

    public static HashMap<String, ImageFactoryI> getFactories() {
        return factories;

    }

    public static void addFactory(ImageFactoryI factory) {
        factories.put(factory.getName(), factory);
    }

    public static ImageFactoryI getFactory(String name) {
        if (factories.containsKey(name))
            return factories.get(name);

        else
            return defaultFactory.duplicate();

    }

    public static ImageFactoryI getDefaultFactory() {
        return defaultFactory;
    }

    public static String getDefaultFactoryName() {
        return defaultFactory.getName();
    }

    public static void setDefaultFactory(ImageFactoryI factory) {
        defaultFactory = factory;
    }

    public static String[] listFactoryNames() {
        return factories.keySet().stream().toArray(s -> new String[factories.size()]);
    }
}
