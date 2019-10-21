package wbif.sjx.MIA.Module.InputOutput;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ChannelSplitter;
import ij.process.ImageProcessor;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ConvertStackToTimeseries;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack.extendRangeToEnd;

public class VideoLoaderCore {
    public static Image getVideo(String path, String outputImageName, String frameRange, String channelRange, @Nullable int[] crop) throws FrameGrabber.Exception, FileNotFoundException {
        // Initialising the video loader and converter
        Java2DFrameConverter frameConverter = new Java2DFrameConverter();
        FFmpegFrameGrabber loader = new FFmpegFrameGrabber(path);
        loader.start();

        // Getting an ordered list of frames to be imported
        int[] framesList = CommaSeparatedStringInterpreter.interpretIntegers(frameRange,true);
        if (framesList[framesList.length-1] == Integer.MAX_VALUE) framesList = extendRangeToEnd(framesList,loader.getLengthInFrames());
        TreeSet<Integer> frames = Arrays.stream(framesList).boxed().collect(Collectors.toCollection(TreeSet::new));

        int[] channelsList = CommaSeparatedStringInterpreter.interpretIntegers(channelRange,true);
        if (channelsList[channelsList.length-1] == Integer.MAX_VALUE) channelsList = extendRangeToEnd(channelsList,loader.getPixelFormat());
        TreeSet<Integer> channels = Arrays.stream(channelsList).boxed().collect(Collectors.toCollection(TreeSet::new));

        int left = 0;
        int top = 0;
        int origWidth = loader.getImageWidth();
        int origHeight = loader.getImageHeight();
        int width = origWidth;
        int height = origHeight;

        if (crop != null) {
            left = crop[0];
            top = crop[1];
            width = crop[2];
            height = crop[3];
        }

        ImagePlus ipl = IJ.createHyperStack("Image",width, height,channelsList.length,1,framesList.length,8);
        int count = 1;
        int total = frames.size();
        for (int frame:frames) {
            IJ.showProgress(((double) count)/((double) total));

            loader.setVideoFrameNumber(frame-1);

            ImagePlus frameIpl = new ImagePlus("Temporary",frameConverter.convert(loader.grabImage()));

            for (int channel:channels) {
                ipl.setPosition(channel,1,count);

                ImageProcessor ipr = ChannelSplitter.getChannel(frameIpl,channel).getProcessor(1);
                if (crop != null) {
                    ipr.setRoi(left,top,width,height);
                    ipr = ipr.crop();
                }

                ipl.setProcessor(ipr);

            }

            count++;

        }

        // This will probably load as a Z-stack rather than timeseries, so convert it to a stack
        if (((ipl.getNFrames() == 1 && ipl.getNSlices() > 1) || (ipl.getNSlices() == 1 && ipl.getNFrames() > 1) )) {
            ConvertStackToTimeseries.process(ipl);
            ipl.getCalibration().pixelDepth = 1;
        }

        return new Image(outputImageName,ipl);

    }
}
