package io.github.mianalysis.mia;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactories;

public class TestUtils {
    public static void addImageToWorkspace(WorkspaceI workspace, String path, String imageName)
            throws UnsupportedEncodingException {
        String pathToImage = URLDecoder.decode(TestUtils.class.getResource(path).getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create(imageName, ipl);
        
        workspace.addImage(image);

    }

    public static ImageI loadImage(String path, String imageName) throws UnsupportedEncodingException {
        String pathToImage = URLDecoder.decode(TestUtils.class.getResource(path).getPath(), "UTF-8");

        return ImageFactories.getDefaultFactory().create(imageName, IJ.openImage(pathToImage));

    }
    
    // public static Stream<Arguments> dimensionLogicInputProvider() {
    //     Stream.Builder<Arguments> argumentBuilder = Stream.builder();
    //     for (Dimension dimension : Dimension.values())
    //         for (Logic logic : Logic.values())
    //             argumentBuilder.add(Arguments.of(dimension, logic));
            
    //     return argumentBuilder.build();
    // }
}
