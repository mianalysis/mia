package wbif.sjx.MIA.Plugins;

import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import org.apache.commons.io.FilenameUtils;
import org.bytedeco.javacv.FrameGrabber;
import wbif.sjx.MIA.Module.InputOutput.VideoLoaderCore;
import wbif.sjx.MIA.Object.Image;

import java.io.File;
import java.io.FileNotFoundException;

public class Video_Loader implements PlugIn {
    @Override
    public void run(String path) {
        int[] crop = null;

        // Checking if file exists; if not, opening a file selection dialog
        if (!new File(path).exists()) {
            OpenDialog openDialog = new OpenDialog("Select video to load",null);
            path = openDialog.getDirectory()+openDialog.getFileName();
        }

        // Get output name
        String outputName = new File(path).getName();

        // Show parameter selection dialog
        GenericDialog genericDialog = new GenericDialog("SVG dimensions");
        genericDialog.addStringField("Frame range","1-end");
        genericDialog.addStringField("Channel range","1-end");
        genericDialog.showDialog();

        if (genericDialog.wasCanceled()) return;

        String frameRange = genericDialog.getNextString();
        String channelRange = genericDialog.getNextString();

        // Do file loading
        try {
            Image image = VideoLoaderCore.getVideo(path,outputName,frameRange,channelRange,crop);

            // Displaying the image
            image.getImagePlus().show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
