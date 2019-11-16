package wbif.sjx.MIA.GUI.Icons;

import wbif.sjx.MIA.GUI.Colours;

import javax.swing.*;

public class IconFactory {
    private int width = 13;
    private int height = 13;

    private double strokeWidth = 0.8;


    // CONSTRUCTORS

    public IconFactory() {

    }

    public IconFactory(int width, int height) {
        this.width = width;
        this.height = height;
    }


    // ICON CREATORS

    public ImageIcon getArrowDown() {
        String url = IconFactory.class.getResource("/Icons/Arrow_Down.svg").toString();
        IconTranscoder transcoder = new IconTranscoder(url,width,height);

        TranscodeOperation operation = new TranscodeOperation("arrowpath","stroke", Colours.DARK_BLUE_HEX);
        transcoder.addTranscodeOperation(operation);

        operation = new TranscodeOperation("arrowpath","stroke-width", String.valueOf(strokeWidth));
        transcoder.addTranscodeOperation(operation);

        return transcoder.transcode();

    }

    public ImageIcon getArrowLeft() {
        String url = IconFactory.class.getResource("/Icons/Arrow_Left.svg").toString();
        IconTranscoder transcoder = new IconTranscoder(url,width,height);

        TranscodeOperation operation = new TranscodeOperation("arrowpath","stroke", Colours.DARK_BLUE_HEX);
        transcoder.addTranscodeOperation(operation);

        operation = new TranscodeOperation("arrowpath","stroke-width", String.valueOf(strokeWidth));
        transcoder.addTranscodeOperation(operation);

        return transcoder.transcode();

    }

    public ImageIcon getArrowRight() {
        String url = IconFactory.class.getResource("/Icons/Arrow_Right.svg").toString();
        IconTranscoder transcoder = new IconTranscoder(url,width,height);

        TranscodeOperation operation = new TranscodeOperation("arrowpath","stroke", Colours.DARK_BLUE_HEX);
        transcoder.addTranscodeOperation(operation);

        operation = new TranscodeOperation("arrowpath","stroke-width", String.valueOf(strokeWidth));
        transcoder.addTranscodeOperation(operation);

        return transcoder.transcode();

    }

    public ImageIcon getArrowUp() {
        String url = IconFactory.class.getResource("/Icons/Arrow_Up.svg").toString();
        IconTranscoder transcoder = new IconTranscoder(url,width,height);

        TranscodeOperation operation = new TranscodeOperation("arrowpath","stroke", Colours.DARK_BLUE_HEX);
        transcoder.addTranscodeOperation(operation);

        operation = new TranscodeOperation("arrowpath","stroke-width", String.valueOf(strokeWidth));
        transcoder.addTranscodeOperation(operation);

        return transcoder.transcode();

    }

    public ImageIcon getEyeClosed(String colourHex) {
        String url = IconFactory.class.getResource("/Icons/Eye_Closed.svg").toString();
        IconTranscoder transcoder = new IconTranscoder(url,width,height);

        TranscodeOperation operation = new TranscodeOperation("eyefill","stroke", colourHex);
        transcoder.addTranscodeOperation(operation);


        return transcoder.transcode();

    }

    public ImageIcon getEyeOpen(String colourHex) {
        String url = IconFactory.class.getResource("/Icons/Eye_Open.svg").toString();
        IconTranscoder transcoder = new IconTranscoder(url,width,height);

        TranscodeOperation operation = new TranscodeOperation("eyefill","stroke", colourHex);
        transcoder.addTranscodeOperation(operation);


        return transcoder.transcode();

    }

    public ImageIcon getPowerOff(String colourHex) {
        String url = IconFactory.class.getResource("/Icons/Power_Off.svg").toString();
        IconTranscoder transcoder = new IconTranscoder(url,width,height);

        TranscodeOperation operation = new TranscodeOperation("powerpath","stroke", colourHex);
        transcoder.addTranscodeOperation(operation);

        operation = new TranscodeOperation("powerpath","stroke-width", String.valueOf(strokeWidth));
        transcoder.addTranscodeOperation(operation);

        return transcoder.transcode();

    }

    public ImageIcon getPowerOn(String colourHex) {
        String url = IconFactory.class.getResource("/Icons/Power_On.svg").toString();
        IconTranscoder transcoder = new IconTranscoder(url,width,height);

        TranscodeOperation operation = new TranscodeOperation("powerpath","stroke", colourHex);
        transcoder.addTranscodeOperation(operation);

        operation = new TranscodeOperation("powerpath","stroke-width", String.valueOf(strokeWidth));
        transcoder.addTranscodeOperation(operation);

        return transcoder.transcode();

    }


    // GETTERS AND SETTERS

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
}
