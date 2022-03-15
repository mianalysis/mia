package io.github.mianalysis;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Workspace;

public class TestUtils {
    public static void addImageToWorkspace(Workspace workspace, String path, String imageName)
            throws UnsupportedEncodingException {
        String pathToImage = URLDecoder.decode(TestUtils.class.getResource(path).getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image(imageName, ipl);
        
        workspace.addImage(image);

    }

    public static Image loadImage(String path, String imageName) throws UnsupportedEncodingException {
        String pathToImage = URLDecoder.decode(TestUtils.class.getResource(path).getPath(), "UTF-8");

        return new Image(imageName, IJ.openImage(pathToImage));

    }
    
    // public static Stream<Arguments> dimensionLogicInputProvider() {
    //     Stream.Builder<Arguments> argumentBuilder = Stream.builder();
    //     for (Dimension dimension : Dimension.values())
    //         for (Logic logic : Logic.values())
    //             argumentBuilder.add(Arguments.of(dimension, logic));
            
    //     return argumentBuilder.build();
    // }
}
