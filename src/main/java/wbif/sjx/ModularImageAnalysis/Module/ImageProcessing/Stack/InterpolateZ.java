package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import fiji.stacks.Dynamic_Reslice;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Resizer;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementReferenceCollection;
import wbif.sjx.ModularImageAnalysis.Object.ParameterCollection;
import wbif.sjx.ModularImageAnalysis.Object.RelationshipCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

/**
 * Created by sc13967 on 23/03/2018.
 */
public class InterpolateZ extends Module {
    public static void main(String[] args) {
        try {
            new InterpolateZ().run(new Workspace(0,null));
        } catch (GenericMIAException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTitle() {
        return "Interpolate Z axis";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        ImagePlus ipl = IJ.openImage("C:\\Users\\sc13967\\Documents\\Java_Projects\\ModularImageAnalysis\\src\\test\\resources\\images\\BinarySphere3D_8bit.tif");
        Resizer resizer = new Resizer();
        resizer.setAverageWhenDownsizing(true);
        resizer.zScale(ipl,50,Resizer.IN_PLACE).show();

    }

    @Override
    protected void initialiseParameters() {

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
