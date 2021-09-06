package io.github.mianalysis.mia.module.objectprocessing.identification;

import java.util.ArrayList;

import ij.ImagePlus;
import ij.gui.PolygonRoi;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.sjcross.common.exceptions.IntegerOverflowException;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.SpatCal;
import io.github.sjcross.common.object.volume.VolumeType;

public class test {
    public static void main(String[] args) {

        ArrayList<PolygonRoi> polygons = new ArrayList<>();
        Image image = new Image("test", new ImagePlus());

        ImagePlus ipl = image.getImagePlus();

        SpatCal cal = SpatCal.getFromImage(ipl);
        int nFrames = ipl.getNFrames();
        double frameInterval = ipl.getCalibration().frameInterval;
        Objs outputObjects = new Objs("Nuclei", cal, nFrames, frameInterval,
                TemporalUnit.getOMEUnit());

        for (PolygonRoi polygon : polygons) {
            Obj obj = outputObjects.createAndAddNewObject(VolumeType.POINTLIST);
            try {
                obj.addPointsFromRoi(polygon, 0);
            } catch (IntegerOverflowException  e) {
                e.printStackTrace();
            }
        }
    }
}
