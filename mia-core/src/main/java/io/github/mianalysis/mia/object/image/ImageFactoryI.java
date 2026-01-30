package io.github.mianalysis.mia.object.image;

public interface ImageFactoryI {
    public String getName();

    public ImageFactoryI duplicate();

    public ImageI create(String name, Object rawImage);

}