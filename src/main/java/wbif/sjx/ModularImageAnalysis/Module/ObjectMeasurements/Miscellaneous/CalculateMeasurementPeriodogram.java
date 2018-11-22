package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Miscellaneous;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Analysis.PeriodogramCalculator;
import wbif.sjx.common.MathFunc.CumStat;

import java.text.DecimalFormat;
import java.util.TreeMap;

/**
 * Created by sc13967 on 22/05/2018.
 */
public class CalculateMeasurementPeriodogram extends Module {
    public static final String TRACK_OBJECTS = "Track objects";
    public static final String SPOT_OBJECTS = "Spot objects";
    public static final String MEASUREMENT = "Measurement";
    public static final String REPORTING_MODE = "Reporting mode";
    public static final String NUMBER_OF_PEAKS_TO_REPORT = "Number of peaks to report";
    public static final String NUMBER_OF_BINS = "Number of bins";
    public static final String MISSING_POINT_HANDLING = "Missing point handling";

    public interface Measurements {
        String FREQUENCY = "FREQUENCY (FR^-1)";
        String POWER = "POWER";
    }

    public interface ReportingModes {
        String KEY_FREQUENCIES = "Key frequencies";
        String WHOLE_SPECTRUM = "Whole spectrum";

        String[] ALL = new String[]{KEY_FREQUENCIES,WHOLE_SPECTRUM};

    }

    public interface MissingPointModes {
        String SKIP_MEASUREMENT = "Skip measurement";
        String SET_POINTS_TO_MEAN = "Set points to mean";
        String SET_POINTS_TO_ZERO = "Set points to zero";

        String[] ALL = new String[]{SKIP_MEASUREMENT,SET_POINTS_TO_MEAN,SET_POINTS_TO_ZERO};

    }


    public static String getKeyFrequenciesFullName(String measurementSource, String measurementType, int peakNumber) {
        return "PERIODOGRAM // ("+measurementSource+")_PEAK"+peakNumber+"_"+measurementType;
    }

    public static String getWholeSpectrumFullName(String measurementSource, double frequency) {
        DecimalFormat df = new DecimalFormat("0.000");
        return "PERIODOGRAM // ("+measurementSource+")_POWER AT "+df.format(frequency)+" FR^-1";
    }


    public double[] getSignal(ObjCollection spotObjects, String measurementName, String missingMode) {
        int[] tLimits = spotObjects.getTemporalLimits();
        double[] signal = new double[tLimits[1]-tLimits[0]+1];

        // Populating the array with Double.NaN
        for (int i=0;i<signal.length;i++) signal[i] = Double.NaN;

        // Adding values to array
        for (Obj spotObject:spotObjects.values()) {
            signal[spotObject.getT()-tLimits[0]] = spotObject.getMeasurement(measurementName).getValue();
        }

        // If skipping measurement the presence of Double.NaN is sufficient
        if (missingMode.equals(MissingPointModes.SKIP_MEASUREMENT)) return signal;

        // Determining the appropriate replacement value for missing points
        double replacementVal = 0;
        if (missingMode.equals(MissingPointModes.SET_POINTS_TO_MEAN)) {
            CumStat cs = new CumStat();
            for (Obj spotObject:spotObjects.values()) {
                cs.addMeasure(spotObject.getMeasurement(measurementName).getValue());
            }
            replacementVal = cs.getMean();
        }

        // Replacing missing points
        for (int i=0;i<signal.length;i++) {
            if (Double.isNaN(signal[i])) signal[i] = replacementVal;
        }

        return signal;

    }

    private void addKeyFrequencyMeasures(Obj obj, TreeMap<Double,Double> psd, String measurement, int nPeaks) {
        double[][] peaks = PeriodogramCalculator.getKeyFrequencies(psd,nPeaks);

        // Adding peaks as Measurements
        for (int i = 0; i < nPeaks; i++) {
            if (peaks == null || psd.size() == 1) {
                String name = getKeyFrequenciesFullName(measurement, Measurements.FREQUENCY, i + 1);
                obj.addMeasurement(new Measurement(name, Double.NaN));

                name = getKeyFrequenciesFullName(measurement, Measurements.POWER, i + 1);
                obj.addMeasurement(new Measurement(name, Double.NaN));
            } else {
                String name = getKeyFrequenciesFullName(measurement, Measurements.FREQUENCY, i + 1);
                obj.addMeasurement(new Measurement(name, peaks[i][0]));

                name = getKeyFrequenciesFullName(measurement, Measurements.POWER, i + 1);
                obj.addMeasurement(new Measurement(name, peaks[i][1]));
            }
        }
    }

    private void addWholeSpectrumMeasures(Obj obj, TreeMap<Double,Double> psd, String measurement, int nBins) {
        // Creating a linear interpolator for the PSD
        double[] freqs = new double[psd.size()];
        double[] powers = new double[psd.size()];

        int i = 0;
        for (double freq:psd.keySet()) {
            freqs[i] = freq;
            powers[i++] = psd.get(freq);
        }
        // Getting frequencies for this number of bins
        double[] targetFreq = PeriodogramCalculator.calculateFrequency(1,nBins);

        if (psd.size() == 1) {
            for (int j=0;j<nBins;j++) {
                String name = getWholeSpectrumFullName(measurement, targetFreq[j]);
                obj.addMeasurement(new Measurement(name,Double.NaN));
            }
            return;
        }

        UnivariateFunction powerFn = new LinearInterpolator().interpolate(freqs,powers);

        // Interpolating function and adding as measurements
        for (int j=0;j<nBins;j++) {
            double freq = powerFn.value(targetFreq[j]);
            String name = getWholeSpectrumFullName(measurement, targetFreq[j]);
            obj.addMeasurement(new Measurement(name,freq));
        }
    }


    @Override
    public String getTitle() {
        return "Calculate measurement periodogram";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_MISCELLANEOUS;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting parameters
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);
        String spotObjectsName = parameters.getValue(SPOT_OBJECTS);
        String measurement = parameters.getValue(MEASUREMENT);
        String reportingMode = parameters.getValue(REPORTING_MODE);
        int nPeaks = parameters.getValue(NUMBER_OF_PEAKS_TO_REPORT);
        int nBins = parameters.getValue(NUMBER_OF_BINS);
        String missingMode = parameters.getValue(MISSING_POINT_HANDLING);

        // Loading objects
        ObjCollection trackObjects = workspace.getObjectSet(trackObjectsName);

        // Running through each track object, calculating the periodicity
        int count = 1;
        int nTracks = trackObjects.size();
        for (Obj trackObject:trackObjects.values()) {
            writeMessage("Processing object "+(count++)+" of "+nTracks);

            ObjCollection spotObjects = trackObject.getChildren(spotObjectsName);
            double[] signal = getSignal(spotObjects,measurement,missingMode);
            TreeMap<Double,Double> psd = PeriodogramCalculator.calculate(signal,1);

            switch (reportingMode) {
                case ReportingModes.KEY_FREQUENCIES:
                    addKeyFrequencyMeasures(trackObject,psd,measurement,nPeaks);
                    break;

                case ReportingModes.WHOLE_SPECTRUM:
                    addWholeSpectrumMeasures(trackObject,psd,measurement,nBins);
                    break;
            }
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(TRACK_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(SPOT_OBJECTS,Parameter.CHILD_OBJECTS,null));
        parameters.add(new Parameter(MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(REPORTING_MODE,Parameter.CHOICE_ARRAY,ReportingModes.KEY_FREQUENCIES,ReportingModes.ALL));
        parameters.add(new Parameter(NUMBER_OF_PEAKS_TO_REPORT,Parameter.INTEGER,3));
        parameters.add(new Parameter(NUMBER_OF_BINS,Parameter.INTEGER,256));
        parameters.add(new Parameter(MISSING_POINT_HANDLING,Parameter.CHOICE_ARRAY,MissingPointModes.SKIP_MEASUREMENT,MissingPointModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);
        parameters.updateValueSource(SPOT_OBJECTS,trackObjectsName);

        String spotObjectsName = parameters.getValue(SPOT_OBJECTS);
        parameters.updateValueSource(MEASUREMENT,spotObjectsName);

        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(TRACK_OBJECTS));
        returnedParameters.add(parameters.getParameter(SPOT_OBJECTS));
        returnedParameters.add(parameters.getParameter(MEASUREMENT));
        returnedParameters.add(parameters.getParameter(REPORTING_MODE));

        switch ((String) parameters.getValue(REPORTING_MODE)) {
            case ReportingModes.KEY_FREQUENCIES:
                returnedParameters.add(parameters.getParameter(NUMBER_OF_PEAKS_TO_REPORT));
                break;

            case ReportingModes.WHOLE_SPECTRUM:
                returnedParameters.add(parameters.getParameter(NUMBER_OF_BINS));
                break;

        }

        returnedParameters.add(parameters.getParameter(MISSING_POINT_HANDLING));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        objectMeasurementReferences.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(TRACK_OBJECTS);
        String measurement = parameters.getValue(MEASUREMENT);

        switch ((String) parameters.getValue(REPORTING_MODE)) {
            case ReportingModes.KEY_FREQUENCIES:
                int numberOfPeaks = parameters.getValue(NUMBER_OF_PEAKS_TO_REPORT);
                for (int i = 0; i < numberOfPeaks; i++) {
                    String name = getKeyFrequenciesFullName(measurement, Measurements.FREQUENCY, i + 1);
                    MeasurementReference reference = objectMeasurementReferences.getOrPut(name);
                    reference.setImageObjName(inputObjectsName);
                    reference.setCalculated(true);

                    name = getKeyFrequenciesFullName(measurement, Measurements.POWER, i + 1);
                    reference = objectMeasurementReferences.getOrPut(name);
                    reference.setImageObjName(inputObjectsName);
                    reference.setCalculated(true);

                }
                break;

            case ReportingModes.WHOLE_SPECTRUM:
                int numberOfBins = parameters.getValue(NUMBER_OF_BINS);
                double[] freq = PeriodogramCalculator.calculateFrequency(1,numberOfBins);
                for (int i=0;i<numberOfBins;i++) {
                    String name = getWholeSpectrumFullName(measurement, freq[i]);
                    MeasurementReference reference = objectMeasurementReferences.getOrPut(name);
                    reference.setImageObjName(inputObjectsName);
                    reference.setCalculated(true);
                }
                break;
        }

        return objectMeasurementReferences;

    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
