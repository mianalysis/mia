package io.github.mianalysis.mia.expectedobjects;

import io.github.mianalysis.mia.object.Objs;
import io.github.sjcross.common.exceptions.IntegerOverflowException;
import io.github.sjcross.common.object.volume.VolumeType;

import java.util.HashMap;
import java.util.List;

import ome.units.UNITS;

/**
 * Created by Stephen Cross on 29/08/2017.
 */
public class Objects3D extends ExpectedObjects {
    public Objects3D(VolumeType volumeType) {
        super(volumeType, 64, 76, 12, 1, 0.02, UNITS.SECOND);
    }

    public enum Measures {
        EXP_ID_8BIT, EXP_ID_16BIT, EXP_X_MIN, EXP_X_MEAN, EXP_X_MEDIAN, EXP_X_MAX, EXP_Y_MIN, EXP_Y_MEAN, EXP_Y_MEDIAN, 
        EXP_Y_MAX, EXP_Z_MIN, EXP_Z_MEAN, EXP_Z_MEDIAN, EXP_Z_MAX, EXP_C, EXP_F, EXP_N_VOXELS, EXP_N_VOXELS_PROJ,
        EXP_I_MEAN_8BIT, EXP_I_MIN_8BIT, EXP_I_MAX_8BIT, EXP_I_STD_8BIT, EXP_I_SUM_8BIT, EXP_I_MEAN_16BIT,
        EXP_I_MIN_16BIT, EXP_I_MAX_16BIT, EXP_I_STD_16BIT, EXP_I_SUM_16BIT, EXP_I_MEAN_32BIT, EXP_I_MIN_32BIT,
        EXP_I_MAX_32BIT, EXP_I_STD_32BIT, EXP_I_SUM_32BIT, EXP_SPOT_ID_X, EXP_SPOT_ID_Y, EXP_SPOT_ID_Z,
        EXP_SPOT_PROX_CENT_X, EXP_SPOT_PROX_CENT_Y, EXP_SPOT_PROX_CENT_Z, EXP_SPOT_PROX_CENT_DIST,
        EXP_SPOT_PROX_CENT_20PX_X, EXP_SPOT_PROX_CENT_20PX_Y, EXP_SPOT_PROX_CENT_20PX_Z, EXP_SPOT_PROX_CENT_20PX_DIST,
        EXP_PROJ_DIA_PX, EXP_PROJ_DIA_CAL, EXP_BIN_N_VOXELS_4BINS_INRANGE, EXP_BIN_N_VOXELS_4BINS_SHORTRANGE,
        EXP_BIN_N_VOXELS_4BINS_HIGHRANGE
    }

    public Objs getObjects(String objectName, Mode mode, double dppXY, double dppZ, String calibratedUnits, boolean includeMeasurements) throws IntegerOverflowException {
        return super.getObjects(objectName, mode, dppXY, dppZ, calibratedUnits, includeMeasurements);
    }

    public HashMap<Integer,HashMap<String,Double>> getMeasurements() {
        HashMap<Integer,HashMap<String,Double>> expectedValues = new HashMap<>();

        HashMap<String,Double> obj = new HashMap<>();
        obj.put(Measures.EXP_ID_8BIT.name(),3d);
        obj.put(Measures.EXP_ID_16BIT.name(),3d);
        obj.put(Measures.EXP_X_MIN.name(),7d);
        obj.put(Measures.EXP_X_MEAN.name(),11.5d);
        obj.put(Measures.EXP_X_MEDIAN.name(),11.5d);
        obj.put(Measures.EXP_X_MAX.name(),16d);
        obj.put(Measures.EXP_Y_MIN.name(),8d);
        obj.put(Measures.EXP_Y_MEAN.name(),15.40d);
        obj.put(Measures.EXP_Y_MEDIAN.name(),15.0d);
        obj.put(Measures.EXP_Y_MAX.name(),23d);
        obj.put(Measures.EXP_Z_MIN.name(),0d);
        obj.put(Measures.EXP_Z_MEAN.name(),1.80d);
        obj.put(Measures.EXP_Z_MEDIAN.name(),2.0d);
        obj.put(Measures.EXP_Z_MAX.name(),4d);
        obj.put(Measures.EXP_C.name(),0d);
        obj.put(Measures.EXP_F.name(),0d);
        obj.put(Measures.EXP_N_VOXELS.name(),272d);
        obj.put(Measures.EXP_N_VOXELS_PROJ.name(),130d);
        obj.put(Measures.EXP_I_MEAN_8BIT.name(),46.23d);
        obj.put(Measures.EXP_I_MIN_8BIT.name(),14d);
        obj.put(Measures.EXP_I_MAX_8BIT.name(),76d);
        obj.put(Measures.EXP_I_STD_8BIT.name(),11.92d);
        obj.put(Measures.EXP_I_SUM_8BIT.name(),12575d);
        obj.put(Measures.EXP_I_MEAN_16BIT.name(),9337.54d);
        obj.put(Measures.EXP_I_MIN_16BIT.name(),3330d);
        obj.put(Measures.EXP_I_MAX_16BIT.name(),16290d);
        obj.put(Measures.EXP_I_STD_16BIT.name(),2572.40d);
        obj.put(Measures.EXP_I_SUM_16BIT.name(),2539812d);
        obj.put(Measures.EXP_I_MEAN_32BIT.name(),0.1437d);
        obj.put(Measures.EXP_I_MIN_32BIT.name(),0.051d);
        obj.put(Measures.EXP_I_MAX_32BIT.name(),0.251d);
        obj.put(Measures.EXP_I_STD_32BIT.name(),0.040d);
        obj.put(Measures.EXP_I_SUM_32BIT.name(),39.075d);
        obj.put(Measures.EXP_SPOT_ID_X.name(),24d);
        obj.put(Measures.EXP_SPOT_ID_Y.name(),42d);
        obj.put(Measures.EXP_SPOT_ID_Z.name(),1d);
        obj.put(Measures.EXP_PROJ_DIA_PX.name(),15.2971d);
        obj.put(Measures.EXP_PROJ_DIA_CAL.name(),0.3059d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_INRANGE.name(),300d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_SHORTRANGE.name(),200d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_HIGHRANGE.name(),150d);
        expectedValues.put(3,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_ID_8BIT.name(),4d);
        obj.put(Measures.EXP_ID_16BIT.name(),4d);
        obj.put(Measures.EXP_X_MIN.name(),27d);
        obj.put(Measures.EXP_X_MEAN.name(),27.0d);
        obj.put(Measures.EXP_X_MEDIAN.name(),27.0d);
        obj.put(Measures.EXP_X_MAX.name(),27d);
        obj.put(Measures.EXP_Y_MIN.name(),16d);
        obj.put(Measures.EXP_Y_MEAN.name(),16.0d);
        obj.put(Measures.EXP_Y_MEDIAN.name(),16.0d);
        obj.put(Measures.EXP_Y_MAX.name(),16d);
        obj.put(Measures.EXP_Z_MIN.name(),5d);
        obj.put(Measures.EXP_Z_MEAN.name(),5.5d);
        obj.put(Measures.EXP_Z_MEDIAN.name(),5.5d);
        obj.put(Measures.EXP_Z_MAX.name(),6d);
        obj.put(Measures.EXP_C.name(),0d);
        obj.put(Measures.EXP_F.name(),0d);
        obj.put(Measures.EXP_N_VOXELS.name(),2d);
        obj.put(Measures.EXP_N_VOXELS_PROJ.name(),1d);
        obj.put(Measures.EXP_I_MEAN_8BIT.name(),106.0d);
        obj.put(Measures.EXP_I_MIN_8BIT.name(),98d);
        obj.put(Measures.EXP_I_MAX_8BIT.name(),114d);
        obj.put(Measures.EXP_I_STD_8BIT.name(),11.31d);
        obj.put(Measures.EXP_I_SUM_8BIT.name(),212d);
        obj.put(Measures.EXP_I_MEAN_16BIT.name(),22377.5d);
        obj.put(Measures.EXP_I_MIN_16BIT.name(),20432d);
        obj.put(Measures.EXP_I_MAX_16BIT.name(),24323d);
        obj.put(Measures.EXP_I_STD_16BIT.name(),2751.35d);
        obj.put(Measures.EXP_I_SUM_16BIT.name(),44755d);
        obj.put(Measures.EXP_I_MEAN_32BIT.name(),0.344d);
        obj.put(Measures.EXP_I_MIN_32BIT.name(),0.314d);
        obj.put(Measures.EXP_I_MAX_32BIT.name(),0.374d);
        obj.put(Measures.EXP_I_STD_32BIT.name(),0.0424d);
        obj.put(Measures.EXP_I_SUM_32BIT.name(),0.688d);
        obj.put(Measures.EXP_SPOT_ID_X.name(),24d);
        obj.put(Measures.EXP_SPOT_ID_Y.name(),44d);
        obj.put(Measures.EXP_SPOT_ID_Z.name(),1d);
        obj.put(Measures.EXP_PROJ_DIA_PX.name(),0d);
        obj.put(Measures.EXP_PROJ_DIA_CAL.name(),0d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_INRANGE.name(),100d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_SHORTRANGE.name(),200d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_HIGHRANGE.name(),0d);
        expectedValues.put(4,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_ID_8BIT.name(),7d);
        obj.put(Measures.EXP_ID_16BIT.name(),1155d);
        obj.put(Measures.EXP_X_MIN.name(),42d);
        obj.put(Measures.EXP_X_MEAN.name(),49.81d);
        obj.put(Measures.EXP_X_MEDIAN.name(),50.0d);
        obj.put(Measures.EXP_X_MAX.name(),57d);
        obj.put(Measures.EXP_Y_MIN.name(),3d);
        obj.put(Measures.EXP_Y_MEAN.name(),11.73d);
        obj.put(Measures.EXP_Y_MEDIAN.name(),12.0d);
        obj.put(Measures.EXP_Y_MAX.name(),20d);
        obj.put(Measures.EXP_Z_MIN.name(),3d);
        obj.put(Measures.EXP_Z_MEAN.name(),6.31d);
        obj.put(Measures.EXP_Z_MEDIAN.name(),7.0d);
        obj.put(Measures.EXP_Z_MAX.name(),9d);
        obj.put(Measures.EXP_C.name(),0d);
        obj.put(Measures.EXP_F.name(),0d);
        obj.put(Measures.EXP_N_VOXELS.name(),742d);
        obj.put(Measures.EXP_N_VOXELS_PROJ.name(),234d);
        obj.put(Measures.EXP_I_MEAN_8BIT.name(),199.43d);
        obj.put(Measures.EXP_I_MIN_8BIT.name(),149d);
        obj.put(Measures.EXP_I_MAX_8BIT.name(),244d);
        obj.put(Measures.EXP_I_STD_8BIT.name(),17.23d);
        obj.put(Measures.EXP_I_SUM_8BIT.name(),147979d);
        obj.put(Measures.EXP_I_MEAN_16BIT.name(),39871.31d);
        obj.put(Measures.EXP_I_MIN_16BIT.name(),28151d);
        obj.put(Measures.EXP_I_MAX_16BIT.name(),52362d);
        obj.put(Measures.EXP_I_STD_16BIT.name(),3625.46d);
        obj.put(Measures.EXP_I_SUM_16BIT.name(),29584511d);
        obj.put(Measures.EXP_I_MEAN_32BIT.name(),0.6134d);
        obj.put(Measures.EXP_I_MIN_32BIT.name(),0.433d);
        obj.put(Measures.EXP_I_MAX_32BIT.name(),0.806d);
        obj.put(Measures.EXP_I_STD_32BIT.name(),0.0558d);
        obj.put(Measures.EXP_I_SUM_32BIT.name(),455.15d);
        obj.put(Measures.EXP_SPOT_ID_X.name(),19d);
        obj.put(Measures.EXP_SPOT_ID_Y.name(),13d);
        obj.put(Measures.EXP_SPOT_ID_Z.name(),0d);
        obj.put(Measures.EXP_PROJ_DIA_PX.name(),18.3848d);
        obj.put(Measures.EXP_PROJ_DIA_CAL.name(),0.3677d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_INRANGE.name(),700d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_SHORTRANGE.name(),800d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_HIGHRANGE.name(),150d);
        expectedValues.put(7,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_ID_8BIT.name(),8d);
        obj.put(Measures.EXP_ID_16BIT.name(),8d);
        obj.put(Measures.EXP_X_MIN.name(),28d);
        obj.put(Measures.EXP_X_MEAN.name(),34.8d);
        obj.put(Measures.EXP_X_MEDIAN.name(),35.0d);
        obj.put(Measures.EXP_X_MAX.name(),41d);
        obj.put(Measures.EXP_Y_MIN.name(),72d);
        obj.put(Measures.EXP_Y_MEAN.name(),73.57d);
        obj.put(Measures.EXP_Y_MEDIAN.name(),74.0d);
        obj.put(Measures.EXP_Y_MAX.name(),75d);
        obj.put(Measures.EXP_Z_MIN.name(),1d);
        obj.put(Measures.EXP_Z_MEAN.name(),2.0d);
        obj.put(Measures.EXP_Z_MEDIAN.name(),2.0d);
        obj.put(Measures.EXP_Z_MAX.name(),3d);
        obj.put(Measures.EXP_C.name(),0d);
        obj.put(Measures.EXP_F.name(),0d);
        obj.put(Measures.EXP_N_VOXELS.name(),90d);
        obj.put(Measures.EXP_N_VOXELS_PROJ.name(),48d);
        obj.put(Measures.EXP_I_MEAN_8BIT.name(),139.42d);
        obj.put(Measures.EXP_I_MIN_8BIT.name(),94d);
        obj.put(Measures.EXP_I_MAX_8BIT.name(),172d);
        obj.put(Measures.EXP_I_STD_8BIT.name(),16.40d);
        obj.put(Measures.EXP_I_SUM_8BIT.name(),12548d);
        obj.put(Measures.EXP_I_MEAN_16BIT.name(),28101.38d);
        obj.put(Measures.EXP_I_MIN_16BIT.name(),20448d);
        obj.put(Measures.EXP_I_MAX_16BIT.name(),34928d);
        obj.put(Measures.EXP_I_STD_16BIT.name(),3317.57d);
        obj.put(Measures.EXP_I_SUM_16BIT.name(),2529124d);
        obj.put(Measures.EXP_I_MEAN_32BIT.name(),0.432d);
        obj.put(Measures.EXP_I_MIN_32BIT.name(),0.315d);
        obj.put(Measures.EXP_I_MAX_32BIT.name(),0.537d);
        obj.put(Measures.EXP_I_STD_32BIT.name(),0.051d);
        obj.put(Measures.EXP_I_SUM_32BIT.name(),38.911d);
        obj.put(Measures.EXP_SPOT_ID_X.name(),0d);
        obj.put(Measures.EXP_SPOT_ID_Y.name(),31d);
        obj.put(Measures.EXP_SPOT_ID_Z.name(),2d);
        obj.put(Measures.EXP_PROJ_DIA_PX.name(),13.1529d);
        obj.put(Measures.EXP_PROJ_DIA_CAL.name(),0.2631d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_INRANGE.name(),100d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_SHORTRANGE.name(),200d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_HIGHRANGE.name(),100d);
        expectedValues.put(8,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_ID_8BIT.name(),9d);
        obj.put(Measures.EXP_ID_16BIT.name(),9d);
        obj.put(Measures.EXP_X_MIN.name(),17d);
        obj.put(Measures.EXP_X_MEAN.name(),18.0d);
        obj.put(Measures.EXP_X_MEDIAN.name(),18.0d);
        obj.put(Measures.EXP_X_MAX.name(),19d);
        obj.put(Measures.EXP_Y_MIN.name(),60d);
        obj.put(Measures.EXP_Y_MEAN.name(),61.0d);
        obj.put(Measures.EXP_Y_MEDIAN.name(),61.0d);
        obj.put(Measures.EXP_Y_MAX.name(),62d);
        obj.put(Measures.EXP_Z_MIN.name(),2d);
        obj.put(Measures.EXP_Z_MEAN.name(),2.0d);
        obj.put(Measures.EXP_Z_MEDIAN.name(),2.0d);
        obj.put(Measures.EXP_Z_MAX.name(),2d);
        obj.put(Measures.EXP_C.name(),0d);
        obj.put(Measures.EXP_F.name(),0d);
        obj.put(Measures.EXP_N_VOXELS.name(),9d);
        obj.put(Measures.EXP_N_VOXELS_PROJ.name(),9d);
        obj.put(Measures.EXP_I_MEAN_8BIT.name(),71.56d);
        obj.put(Measures.EXP_I_MIN_8BIT.name(),64d);
        obj.put(Measures.EXP_I_MAX_8BIT.name(),83d);
        obj.put(Measures.EXP_I_STD_8BIT.name(),6.06d);
        obj.put(Measures.EXP_I_SUM_8BIT.name(),644d);
        obj.put(Measures.EXP_I_MEAN_16BIT.name(),14648.11d);
        obj.put(Measures.EXP_I_MIN_16BIT.name(),11653d);
        obj.put(Measures.EXP_I_MAX_16BIT.name(),17281d);
        obj.put(Measures.EXP_I_STD_16BIT.name(),1823.12d);
        obj.put(Measures.EXP_I_SUM_16BIT.name(),131833d);
        obj.put(Measures.EXP_I_MEAN_32BIT.name(),0.225d);
        obj.put(Measures.EXP_I_MIN_32BIT.name(),0.179d);
        obj.put(Measures.EXP_I_MAX_32BIT.name(),0.266d);
        obj.put(Measures.EXP_I_STD_32BIT.name(),0.028d);
        obj.put(Measures.EXP_I_SUM_32BIT.name(),2.028d);
        obj.put(Measures.EXP_SPOT_ID_X.name(),0d);
        obj.put(Measures.EXP_SPOT_ID_Y.name(),75d);
        obj.put(Measures.EXP_SPOT_ID_Z.name(),5d);
        obj.put(Measures.EXP_PROJ_DIA_PX.name(),2.8284d);
        obj.put(Measures.EXP_PROJ_DIA_CAL.name(),0.0566d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_INRANGE.name(),100d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_SHORTRANGE.name(),200d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_HIGHRANGE.name(),0d);
        expectedValues.put(9,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_ID_8BIT.name(),13d);
        obj.put(Measures.EXP_ID_16BIT.name(),13d);
        obj.put(Measures.EXP_X_MIN.name(),34d);
        obj.put(Measures.EXP_X_MEAN.name(),40.57d);
        obj.put(Measures.EXP_X_MEDIAN.name(),40.0d);
        obj.put(Measures.EXP_X_MAX.name(),48d);
        obj.put(Measures.EXP_Y_MIN.name(),35d);
        obj.put(Measures.EXP_Y_MEAN.name(),40.26d);
        obj.put(Measures.EXP_Y_MEDIAN.name(),40.0d);
        obj.put(Measures.EXP_Y_MAX.name(),46d);
        obj.put(Measures.EXP_Z_MIN.name(),6d);
        obj.put(Measures.EXP_Z_MEAN.name(),8.39d);
        obj.put(Measures.EXP_Z_MEDIAN.name(),9.0d);
        obj.put(Measures.EXP_Z_MAX.name(),11d);
        obj.put(Measures.EXP_C.name(),0d);
        obj.put(Measures.EXP_F.name(),0d);
        obj.put(Measures.EXP_N_VOXELS.name(),375d);
        obj.put(Measures.EXP_N_VOXELS_PROJ.name(),137d);
        obj.put(Measures.EXP_I_MEAN_8BIT.name(),161.93d);
        obj.put(Measures.EXP_I_MIN_8BIT.name(),119d);
        obj.put(Measures.EXP_I_MAX_8BIT.name(),218d);
        obj.put(Measures.EXP_I_STD_8BIT.name(),17.51d);
        obj.put(Measures.EXP_I_SUM_8BIT.name(),60724d);
        obj.put(Measures.EXP_I_MEAN_16BIT.name(),32363.46d);
        obj.put(Measures.EXP_I_MIN_16BIT.name(),23471d);
        obj.put(Measures.EXP_I_MAX_16BIT.name(),43399d);
        obj.put(Measures.EXP_I_STD_16BIT.name(),3629.69d);
        obj.put(Measures.EXP_I_SUM_16BIT.name(),12136296d);
        obj.put(Measures.EXP_I_MEAN_32BIT.name(),0.498d);
        obj.put(Measures.EXP_I_MIN_32BIT.name(),0.361d);
        obj.put(Measures.EXP_I_MAX_32BIT.name(),0.668d);
        obj.put(Measures.EXP_I_STD_32BIT.name(),0.0558d);
        obj.put(Measures.EXP_I_SUM_32BIT.name(),186.716d);
        obj.put(Measures.EXP_SPOT_ID_X.name(),39d);
        obj.put(Measures.EXP_SPOT_ID_Y.name(),27d);
        obj.put(Measures.EXP_SPOT_ID_Z.name(),3d);
        obj.put(Measures.EXP_PROJ_DIA_PX.name(),14.4222d);
        obj.put(Measures.EXP_PROJ_DIA_CAL.name(),0.2884d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_INRANGE.name(),300d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_SHORTRANGE.name(),400d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_HIGHRANGE.name(),150d);
        expectedValues.put(13,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_ID_8BIT.name(),20d);
        obj.put(Measures.EXP_ID_16BIT.name(),20d);
        obj.put(Measures.EXP_X_MIN.name(),29d);
        obj.put(Measures.EXP_X_MEAN.name(),32.38d);
        obj.put(Measures.EXP_X_MEDIAN.name(),32.5d);
        obj.put(Measures.EXP_X_MAX.name(),35d);
        obj.put(Measures.EXP_Y_MIN.name(),51d);
        obj.put(Measures.EXP_Y_MEAN.name(),54.5d);
        obj.put(Measures.EXP_Y_MEDIAN.name(),54.5d);
        obj.put(Measures.EXP_Y_MAX.name(),58d);
        obj.put(Measures.EXP_Z_MIN.name(),4d);
        obj.put(Measures.EXP_Z_MEAN.name(),7.38d);
        obj.put(Measures.EXP_Z_MEDIAN.name(),7.5d);
        obj.put(Measures.EXP_Z_MAX.name(),10d);
        obj.put(Measures.EXP_C.name(),0d);
        obj.put(Measures.EXP_F.name(),0d);
        obj.put(Measures.EXP_N_VOXELS.name(),8d);
        obj.put(Measures.EXP_N_VOXELS_PROJ.name(),8d);
        obj.put(Measures.EXP_I_MEAN_8BIT.name(),130.88d);
        obj.put(Measures.EXP_I_MIN_8BIT.name(),124d);
        obj.put(Measures.EXP_I_MAX_8BIT.name(),139d);
        obj.put(Measures.EXP_I_STD_8BIT.name(),4.94d);
        obj.put(Measures.EXP_I_SUM_8BIT.name(),1047d);
        obj.put(Measures.EXP_I_MEAN_16BIT.name(),25965.75d);
        obj.put(Measures.EXP_I_MIN_16BIT.name(),23278d);
        obj.put(Measures.EXP_I_MAX_16BIT.name(),27482d);
        obj.put(Measures.EXP_I_STD_16BIT.name(),1381.49d);
        obj.put(Measures.EXP_I_SUM_16BIT.name(),207726d);
        obj.put(Measures.EXP_I_MEAN_32BIT.name(),0.3995d);
        obj.put(Measures.EXP_I_MIN_32BIT.name(),0.358d);
        obj.put(Measures.EXP_I_MAX_32BIT.name(),0.423d);
        obj.put(Measures.EXP_I_STD_32BIT.name(),0.021d);
        obj.put(Measures.EXP_I_SUM_32BIT.name(),3.196d);
        obj.put(Measures.EXP_SPOT_ID_X.name(),15d);
        obj.put(Measures.EXP_SPOT_ID_Y.name(),57d);
        obj.put(Measures.EXP_SPOT_ID_Z.name(),2d);
        obj.put(Measures.EXP_PROJ_DIA_PX.name(),9.2195d);
        obj.put(Measures.EXP_PROJ_DIA_CAL.name(),0.1844d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_INRANGE.name(),100d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_SHORTRANGE.name(),200d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_HIGHRANGE.name(),0d);
        expectedValues.put(20,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_ID_8BIT.name(),23d);
        obj.put(Measures.EXP_ID_16BIT.name(),23d);
        obj.put(Measures.EXP_X_MIN.name(),55d);
        obj.put(Measures.EXP_X_MEAN.name(),55.0d);
        obj.put(Measures.EXP_X_MEDIAN.name(),55.0d);
        obj.put(Measures.EXP_X_MAX.name(),55d);
        obj.put(Measures.EXP_Y_MIN.name(),61d);
        obj.put(Measures.EXP_Y_MEAN.name(),61.0d);
        obj.put(Measures.EXP_Y_MEDIAN.name(),61.0d);
        obj.put(Measures.EXP_Y_MAX.name(),61d);
        obj.put(Measures.EXP_Z_MIN.name(),11d);
        obj.put(Measures.EXP_Z_MEAN.name(),11.0d);
        obj.put(Measures.EXP_Z_MEDIAN.name(),11.0d);
        obj.put(Measures.EXP_Z_MAX.name(),11d);
        obj.put(Measures.EXP_C.name(),0d);
        obj.put(Measures.EXP_F.name(),0d);
        obj.put(Measures.EXP_N_VOXELS.name(),1d);
        obj.put(Measures.EXP_N_VOXELS_PROJ.name(),1d);
        obj.put(Measures.EXP_I_MEAN_8BIT.name(),237.0d);
        obj.put(Measures.EXP_I_MIN_8BIT.name(),237d);
        obj.put(Measures.EXP_I_MAX_8BIT.name(),237d);
        obj.put(Measures.EXP_I_STD_8BIT.name(),Double.NaN);
        obj.put(Measures.EXP_I_SUM_8BIT.name(),237d);
        obj.put(Measures.EXP_I_MEAN_16BIT.name(),48606.0d);
        obj.put(Measures.EXP_I_MIN_16BIT.name(),48606d);
        obj.put(Measures.EXP_I_MAX_16BIT.name(),48606d);
        obj.put(Measures.EXP_I_STD_16BIT.name(),Double.NaN);
        obj.put(Measures.EXP_I_SUM_16BIT.name(),48606d);
        obj.put(Measures.EXP_I_MEAN_32BIT.name(),0.748d);
        obj.put(Measures.EXP_I_MIN_32BIT.name(),0.748d);
        obj.put(Measures.EXP_I_MAX_32BIT.name(),0.748d);
        obj.put(Measures.EXP_I_STD_32BIT.name(),Double.NaN);
        obj.put(Measures.EXP_I_SUM_32BIT.name(),0.748d);
        obj.put(Measures.EXP_SPOT_ID_X.name(),36d);
        obj.put(Measures.EXP_SPOT_ID_Y.name(),21d);
        obj.put(Measures.EXP_SPOT_ID_Z.name(),7d);
        obj.put(Measures.EXP_PROJ_DIA_PX.name(),0d);
        obj.put(Measures.EXP_PROJ_DIA_CAL.name(),0d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_INRANGE.name(),100d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_SHORTRANGE.name(),200d);
        obj.put(Measures.EXP_BIN_N_VOXELS_4BINS_HIGHRANGE.name(),0d);
        expectedValues.put(23,obj);

        return expectedValues;

    }

    public HashMap<Integer,HashMap<String,Object>> getOtherValues() {
        HashMap<Integer,HashMap<String,Object>> expectedValues = new HashMap<>();

        HashMap<String,Object> obj = new HashMap<>();
        obj.put(Measures.EXP_SPOT_PROX_CENT_X.name(),new int[]{19,32,8,0});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Y.name(),new int[]{13,18,17,31});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Z.name(),new int[]{0,1,2,2});
        obj.put(Measures.EXP_SPOT_PROX_CENT_DIST.name(),new double[]{11.96,21.05,3.98,19.41});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_X.name(),new int[]{19,8,0});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Y.name(),new int[]{13,17,31});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Z.name(),new int[]{0,2,2});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_DIST.name(),new double[]{11.96,3.98,19.41});
        expectedValues.put(272,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_SPOT_PROX_CENT_X.name(),new int[]{39,15,21,36,17});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Y.name(),new int[]{27,36,5,21,10});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Z.name(),new int[]{3,5,6,7,8});
        obj.put(Measures.EXP_SPOT_PROX_CENT_DIST.name(),new double[]{20.52,23.46,12.78,12.74,17.10});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_X.name(),new int[]{21,36,17});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Y.name(),new int[]{5,21,10});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Z.name(),new int[]{6,7,8});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_DIST.name(),new double[]{12.78,12.74,17.10});
        expectedValues.put(2,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_SPOT_PROX_CENT_X.name(),new int[]{52,44,44});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Y.name(),new int[]{4,14,14});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Z.name(),new int[]{0,2,7});
        obj.put(Measures.EXP_SPOT_PROX_CENT_DIST.name(),new double[]{32.56,22.43,7.13});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_X.name(),new int[]{44});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Y.name(),new int[]{14});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Z.name(),new int[]{7});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_DIST.name(),new double[]{7.13});
        expectedValues.put(742,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_SPOT_PROX_CENT_X.name(),new int[]{});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Y.name(),new int[]{});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Z.name(),new int[]{});
        obj.put(Measures.EXP_SPOT_PROX_CENT_DIST.name(),new double[]{});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_X.name(),new int[]{});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Y.name(),new int[]{});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Z.name(),new int[]{});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_DIST.name(),new double[]{});
        expectedValues.put(90,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_SPOT_PROX_CENT_X.name(),new int[]{24,24,15,0});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Y.name(),new int[]{42,44,57,75});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Z.name(),new int[]{1,1,2,5});
        obj.put(Measures.EXP_SPOT_PROX_CENT_DIST.name(),new double[]{20.54,18.71,5,27.29});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_X.name(),new int[]{24,15});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Y.name(),new int[]{44,57});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Z.name(),new int[]{1,2});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_DIST.name(),new double[]{18.71,5});
        expectedValues.put(9,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_SPOT_PROX_CENT_X.name(),new int[]{46,51,56});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Y.name(),new int[]{44,36,40});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Z.name(),new int[]{6,10,10});
        obj.put(Measures.EXP_SPOT_PROX_CENT_DIST.name(),new double[]{13.63,13.86,17.42});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_X.name(),new int[]{46,51,56});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Y.name(),new int[]{44,36,40});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Z.name(),new int[]{6,10,10});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_DIST.name(),new double[]{13.63,13.86,17.42});
        expectedValues.put(375,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_SPOT_PROX_CENT_X.name(),new int[]{35,35,45,21});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Y.name(),new int[]{41,45,71,70});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Z.name(),new int[]{3,5,7,8});
        obj.put(Measures.EXP_SPOT_PROX_CENT_DIST.name(),new double[]{25.84,15.43,20.86,19.48});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_X.name(),new int[]{35,21});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Y.name(),new int[]{45,70});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Z.name(),new int[]{5,8});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_DIST.name(),new double[]{15.43,19.48});
        expectedValues.put(8,obj);

        obj = new HashMap<>();
        obj.put(Measures.EXP_SPOT_PROX_CENT_X.name(),new int[]{51,63});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Y.name(),new int[]{65,64});
        obj.put(Measures.EXP_SPOT_PROX_CENT_Z.name(),new int[]{10,11});
        obj.put(Measures.EXP_SPOT_PROX_CENT_DIST.name(),new double[]{7.55,8.54});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_X.name(),new int[]{51,63});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Y.name(),new int[]{65,64});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_Z.name(),new int[]{10,11});
        obj.put(Measures.EXP_SPOT_PROX_CENT_20PX_DIST.name(),new double[]{7.55,8.54});
        expectedValues.put(1,obj);

        return expectedValues;

    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/Objects3D.csv");
    }
}

