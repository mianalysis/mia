package wbif.sjx.MIA.Object;

import ij.ImagePlus;
import ij.measure.Calibration;
import wbif.sjx.common.Object.Volume.SpatCal;

public class TSpatCal extends SpatCal {
    public final int nFrames;

    public TSpatCal(double dppXY, double dppZ, String units, int width, int height, int nSlices, int nFrames) {
        super(dppXY, dppZ, units, width, height, nSlices);
        this.nFrames = nFrames;

    }

    public TSpatCal duplicate() {
        return new TSpatCal(dppXY,dppZ, units,width,height,nSlices,nFrames);
    }

    public static TSpatCal getFromImage(ImagePlus ipl) {
        Calibration calibration = ipl.getCalibration();

        int width = ipl.getWidth();
        int height = ipl.getHeight();
        int nSlices = ipl.getNSlices();
        int nFrames = ipl.getNFrames();
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String units = calibration.getUnits();

        return new TSpatCal(dppXY,dppZ,units,width,height,nSlices,nFrames);

    }

    public int getnFrames() {
        return nFrames;
    }
}
