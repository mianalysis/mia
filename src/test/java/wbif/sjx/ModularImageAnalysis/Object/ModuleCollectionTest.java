package wbif.sjx.ModularImageAnalysis.Object;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.GUI.GUIAnalysis;
import wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements.MeasureImageIntensity;
import wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements.MeasureImageTexture;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.ThresholdImage;
import wbif.sjx.ModularImageAnalysis.Module.InputOutput.ImageLoader;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity.MeasureObjectTexture;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial.MeasureObjectCentroid;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial.MeasureObjectShape;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.IdentifyObjects;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.RunTrackMate;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.TrackObjects;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement.ObjectClusterer;

import java.util.LinkedHashSet;

import static org.junit.Assert.*;

public class ModuleCollectionTest < T extends RealType< T > & NativeType< T >> {

    @Test
    public void testGetImageMeasurementReferencesNoCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String im1Name = "Im 1";
        String im2Name = "New_image";

        // Populating some modules (no need to populate all parameters as these should be initialised)
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity();
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,im1Name);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MEAN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_STDEV,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MIN,false);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MAX,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_SUM,false);
        modules.add(measureImageIntensity);

        MeasureImageIntensity measureImageIntensity2 = new MeasureImageIntensity();
        measureImageIntensity2.initialiseParameters();
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,im2Name);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_MEAN,false);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_STDEV,true);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_MIN,true);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_MAX,true);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_SUM,false);
        modules.add(measureImageIntensity2);

        MeasureImageTexture measureImageTexture = new MeasureImageTexture();
        measureImageTexture.initialiseParameters();
        measureImageTexture.updateParameterValue(MeasureImageTexture.INPUT_IMAGE,im2Name);
        modules.add(measureImageTexture);

        // Checking the values for "Im1"
        MeasurementReferenceCollection references1 = modules.getImageMeasurementReferences(im1Name);
        assertEquals(3,references1.size());

        String[] expectedNames1 = new String[]{MeasureImageIntensity.Measurements.MEAN,
                MeasureImageIntensity.Measurements.STDEV,
                MeasureImageIntensity.Measurements.MAX};

        for (String expectedName1:expectedNames1) {
            assertTrue(references1.containsKey(expectedName1));
        }

        // Checking the values for "New_image"
        MeasurementReferenceCollection references2 = modules.getImageMeasurementReferences(im2Name);
        assertEquals(7,references2.size());

        String[] expectedNames2 = new String[]{MeasureImageIntensity.Measurements.MIN,
                MeasureImageIntensity.Measurements.STDEV,
                MeasureImageIntensity.Measurements.MAX,
                MeasureImageTexture.Measurements.ASM,
                MeasureImageTexture.Measurements.CONTRAST,
                MeasureImageTexture.Measurements.CORRELATION,
                MeasureImageTexture.Measurements.ENTROPY};

        for (String expectedName2:expectedNames2) {
            assertTrue(references2.containsKey(expectedName2));
        }
    }

    @Test
    public void testGetImageMeasurementReferencesWithCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String im1Name = "Im 1";
        String im2Name = "New_image";

        // Populating some modules (no need to populate all parameters as these should be initialised)
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity();
        measureImageIntensity.initialiseParameters();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,im1Name);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MEAN,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_STDEV,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MIN,false);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MAX,true);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_SUM,false);
        modules.add(measureImageIntensity);

        MeasureImageIntensity measureImageIntensity2 = new MeasureImageIntensity();
        measureImageIntensity2.initialiseParameters();
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,im2Name);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_MEAN,false);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_STDEV,true);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_MIN,true);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_MAX,true);
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.MEASURE_SUM,false);
        modules.add(measureImageIntensity2);

        MeasureImageTexture measureImageTexture = new MeasureImageTexture();
        measureImageTexture.initialiseParameters();
        measureImageTexture.updateParameterValue(MeasureImageTexture.INPUT_IMAGE,im2Name);
        modules.add(measureImageTexture);

        // Checking the values for "Im1"
        MeasurementReferenceCollection references1 = modules.getImageMeasurementReferences(im1Name,measureImageIntensity2);
        assertEquals(3,references1.size());

        String[] expectedNames1 = new String[]{MeasureImageIntensity.Measurements.MEAN,
                MeasureImageIntensity.Measurements.STDEV,
                MeasureImageIntensity.Measurements.MAX};

        for (String expectedName1:expectedNames1) {
            assertTrue(references1.containsKey(expectedName1));
        }

        // Checking the values for "New_image"
        MeasurementReferenceCollection references2 = modules.getImageMeasurementReferences(im2Name,measureImageIntensity2);
        assertEquals(0,references2.size());

    }

    @Test
    public void testGetObjectMeasurementReferencesNoCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String obj1Name = "First obj set";
        String obj2Name = "Second";

        // Populating some modules (no need to populate all parameters as these should be initialised)
        MeasureObjectCentroid measureObjectCentroid = new MeasureObjectCentroid();
        measureObjectCentroid.initialiseParameters();
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,obj1Name);
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.CENTROID_METHOD,MeasureObjectCentroid.Methods.MEAN);
        modules.add(measureObjectCentroid);

        MeasureObjectShape measureObjectShape = new MeasureObjectShape();
        measureObjectShape.initialiseParameters();
        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,obj2Name);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_VOLUME,true);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_DIA,true);
        modules.add(measureObjectShape);

        MeasureObjectTexture measureObjectTexture = new MeasureObjectTexture();
        measureObjectTexture.initialiseParameters();
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_OBJECTS,obj1Name);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_IMAGE,"");
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.POINT_MEASUREMENT,true);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.CALIBRATED_RADIUS,false);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.MEASUREMENT_RADIUS,1d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.X_OFFSET,1);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Y_OFFSET,0);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Z_OFFSET,0);
        modules.add(measureObjectTexture);

        // Checking the values for "Im1"
        MeasurementReferenceCollection references1 = modules.getObjectMeasurementReferences(obj1Name);
        assertEquals(10,references1.size());

        String[] expectedNames1 = new String[]{MeasureObjectCentroid.Measurements.MEAN_X_PX,
                MeasureObjectCentroid.Measurements.MEAN_Y_PX,
                MeasureObjectCentroid.Measurements.MEAN_Z_SLICE,
                MeasureObjectCentroid.Measurements.MEAN_X_CAL,
                MeasureObjectCentroid.Measurements.MEAN_Y_CAL,
                MeasureObjectCentroid.Measurements.MEAN_Z_CAL,
                MeasureObjectTexture.getFullName("",MeasureObjectTexture.Measurements.ASM),
                MeasureObjectTexture.getFullName("",MeasureObjectTexture.Measurements.CONTRAST),
                MeasureObjectTexture.getFullName("",MeasureObjectTexture.Measurements.CORRELATION),
                MeasureObjectTexture.getFullName("",MeasureObjectTexture.Measurements.ENTROPY)};

        for (String expectedName1:expectedNames1) {
            assertTrue(references1.containsKey(expectedName1));
        }

        // Checking the values for the second object set
        MeasurementReferenceCollection references2 = modules.getObjectMeasurementReferences(obj2Name);
        assertEquals(4,references2.size());

        String[] expectedNames2 = new String[]{MeasureObjectShape.Measurements.PROJ_DIA_CAL,
                MeasureObjectShape.Measurements.PROJ_DIA_PX,
                MeasureObjectShape.Measurements.VOLUME_CAL,
                MeasureObjectShape.Measurements.VOLUME_CAL};

        for (String expectedName2:expectedNames2) {
            assertTrue(references2.containsKey(expectedName2));
        }
    }

    @Test
    public void testGetObjectMeasurementReferencesWithCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String obj1Name = "First obj set";
        String obj2Name = "Second";

        // Populating some modules (no need to populate all parameters as these should be initialised)
        MeasureObjectCentroid measureObjectCentroid = new MeasureObjectCentroid();
        measureObjectCentroid.initialiseParameters();
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,obj1Name);
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.CENTROID_METHOD,MeasureObjectCentroid.Methods.MEAN);
        modules.add(measureObjectCentroid);

        MeasureObjectShape measureObjectShape = new MeasureObjectShape();
        measureObjectShape.initialiseParameters();
        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,obj2Name);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_VOLUME,true);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_DIA,true);
        modules.add(measureObjectShape);

        MeasureObjectTexture measureObjectTexture = new MeasureObjectTexture();
        measureObjectTexture.initialiseParameters();
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_OBJECTS,obj1Name);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_IMAGE,"");
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.POINT_MEASUREMENT,true);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.CALIBRATED_RADIUS,false);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.MEASUREMENT_RADIUS,1d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.X_OFFSET,1);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Y_OFFSET,0);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Z_OFFSET,0);
        modules.add(measureObjectTexture);

        // Checking the values for "Im1"
        MeasurementReferenceCollection references1 = modules.getObjectMeasurementReferences(obj1Name,measureObjectShape);
        assertEquals(6,references1.size());

        String[] expectedNames1 = new String[]{MeasureObjectCentroid.Measurements.MEAN_X_PX,
                MeasureObjectCentroid.Measurements.MEAN_Y_PX,
                MeasureObjectCentroid.Measurements.MEAN_Z_SLICE,
                MeasureObjectCentroid.Measurements.MEAN_X_CAL,
                MeasureObjectCentroid.Measurements.MEAN_Y_CAL,
                MeasureObjectCentroid.Measurements.MEAN_Z_CAL};

        for (String expectedName1:expectedNames1) {
            assertTrue(references1.containsKey(expectedName1));
        }

        // Checking the values for the second object set
        MeasurementReferenceCollection references2 = modules.getObjectMeasurementReferences(obj2Name,measureObjectShape);
        assertEquals(0,references2.size());

    }

    @Test
    public void testGetParametersMatchingTypeNoCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String obj1Name = "First obj set";
        String obj2Name = "Second";

        // Populating some modules (no need to populate all parameters as these should be initialised)
        MeasureObjectCentroid measureObjectCentroid = new MeasureObjectCentroid();
        measureObjectCentroid.initialiseParameters();
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,obj1Name);
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.CENTROID_METHOD,MeasureObjectCentroid.Methods.MEAN);
        modules.add(measureObjectCentroid);

        MeasureObjectShape measureObjectShape = new MeasureObjectShape();
        measureObjectShape.initialiseParameters();
        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,obj2Name);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_VOLUME,true);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_DIA,true);
        modules.add(measureObjectShape);

        MeasureObjectTexture measureObjectTexture = new MeasureObjectTexture();
        measureObjectTexture.initialiseParameters();
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_OBJECTS,obj1Name);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_IMAGE,"");
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.POINT_MEASUREMENT,true);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.CALIBRATED_RADIUS,false);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.MEASUREMENT_RADIUS,1d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.X_OFFSET,1);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Y_OFFSET,0);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Z_OFFSET,0);
        modules.add(measureObjectTexture);

        // Checking for booleans
        LinkedHashSet<Parameter> actualParams = modules.getParametersMatchingType(Parameter.BOOLEAN);

        // Getting expected values
        LinkedHashSet<Parameter> expectedParams = new LinkedHashSet<>();
        expectedParams.add(new Parameter(MeasureObjectShape.MEASURE_VOLUME,Parameter.BOOLEAN,true));
        expectedParams.add(new Parameter(MeasureObjectShape.MEASURE_PROJECTED_DIA,Parameter.BOOLEAN,true));
        expectedParams.add(new Parameter(MeasureObjectTexture.POINT_MEASUREMENT,Parameter.BOOLEAN,true));
        expectedParams.add(new Parameter(MeasureObjectTexture.CALIBRATED_RADIUS,Parameter.BOOLEAN,false));

        // Checking the parameters are what are expected
        assertEquals(4,actualParams.size());

        for (Parameter actualParam:actualParams) {
            boolean found = false;
            for (Parameter expectedParam:expectedParams){
                if (expectedParam.getName().equals(actualParam.getName())
                        && expectedParam.getValue().equals(actualParam.getValue())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

    @Test
    public void testGetParametersMatchingTypeWithCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String obj1Name = "First obj set";
        String obj2Name = "Second";

        // Populating some modules (no need to populate all parameters as these should be initialised)
        MeasureObjectCentroid measureObjectCentroid = new MeasureObjectCentroid();
        measureObjectCentroid.initialiseParameters();
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,obj1Name);
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.CENTROID_METHOD,MeasureObjectCentroid.Methods.MEAN);
        modules.add(measureObjectCentroid);

        MeasureObjectShape measureObjectShape = new MeasureObjectShape();
        measureObjectShape.initialiseParameters();
        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,obj2Name);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_VOLUME,true);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_DIA,true);
        modules.add(measureObjectShape);

        MeasureObjectTexture measureObjectTexture = new MeasureObjectTexture();
        measureObjectTexture.initialiseParameters();
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_OBJECTS,obj1Name);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_IMAGE,"");
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.POINT_MEASUREMENT,true);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.CALIBRATED_RADIUS,false);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.MEASUREMENT_RADIUS,1d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.X_OFFSET,1);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Y_OFFSET,0);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Z_OFFSET,0);
        modules.add(measureObjectTexture);

        // Checking for booleans
        LinkedHashSet<Parameter> actualParams = modules.getParametersMatchingType(Parameter.BOOLEAN,measureObjectTexture);

        // Getting expected values
        LinkedHashSet<Parameter> expectedParams = new LinkedHashSet<>();
        expectedParams.add(new Parameter(MeasureObjectShape.MEASURE_VOLUME,Parameter.BOOLEAN,true));
        expectedParams.add(new Parameter(MeasureObjectShape.MEASURE_PROJECTED_DIA,Parameter.BOOLEAN,true));

        // Checking the parameters are what are expected
        assertEquals(2,actualParams.size());

        for (Parameter actualParam:actualParams) {
            boolean found = false;
            for (Parameter expectedParam:expectedParams){
                if (expectedParam.getName().equals(actualParam.getName())
                        && expectedParam.getValue().equals(actualParam.getValue())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

    @Test
    public void testGetRelationshipsNoCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String spotsName = "Spot objects";
        String tracksName = "Tracks";
        String clustersName = "Clusters";

        TrackObjects trackObjects = new TrackObjects();
        trackObjects.initialiseParameters();
        trackObjects.updateParameterValue(TrackObjects.INPUT_OBJECTS,spotsName);
        trackObjects.updateParameterValue(TrackObjects.TRACK_OBJECTS,tracksName);
        trackObjects.updateParameterValue(TrackObjects.IDENTIFY_LEADING_POINT,false);
        trackObjects.updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID);
        modules.add(trackObjects);

        ObjectClusterer objectClusterer = new ObjectClusterer();
        objectClusterer.initialiseParameters();
        objectClusterer.updateParameterValue(ObjectClusterer.INPUT_OBJECTS,spotsName);
        objectClusterer.updateParameterValue(ObjectClusterer.CLUSTER_OBJECTS,clustersName);
        objectClusterer.updateParameterValue(ObjectClusterer.CLUSTERING_ALGORITHM,ObjectClusterer.ClusteringAlgorithms.DBSCAN);
        objectClusterer.updateParameterValue(ObjectClusterer.INPUT_OBJECTS,spotsName);
        objectClusterer.updateParameterValue(ObjectClusterer.EPS,1d);
        objectClusterer.updateParameterValue(ObjectClusterer.MIN_POINTS,3);
        modules.add(objectClusterer);

        // Getting actual relationships
        RelationshipCollection actualRelationships = modules.getRelationships();

        // Getting actual relationships for spots
        String[] actualSpotChildren = actualRelationships.getChildNames(spotsName);
        String[] actualSpotParents = actualRelationships.getParentNames(spotsName);

        // Getting expected relationships for spots
        String[] expectedSpotChildren = new String[]{""};
        String[] expectedSpotParents = new String[]{tracksName,clustersName};

        assertEquals(1,actualSpotChildren.length);
        assertEquals(2,actualSpotParents.length);

        for (String actualChild:actualSpotChildren) {
            boolean found = false;
            for (String expectedChild:expectedSpotChildren){
                if (expectedChild.equals(actualChild)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        for (String actualParent:actualSpotParents) {
            boolean found = false;
            for (String expectedParent:expectedSpotParents){
                if (expectedParent.equals(actualParent)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        // Getting actual relationships for spots
        String[] actualTrackChildren = actualRelationships.getChildNames(tracksName);
        String[] actualTrackParents = actualRelationships.getParentNames(tracksName);

        // Getting expected relationships for spots
        String[] expectedTrackChildren = new String[]{spotsName};
        String[] expectedTrackParents = new String[]{""};

        assertEquals(1,actualTrackChildren.length);
        assertEquals(1,actualTrackParents.length);

        for (String actualChild:actualTrackChildren) {
            boolean found = false;
            for (String expectedChild:expectedTrackChildren){
                if (expectedChild.equals(actualChild)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        for (String actualParent:actualTrackParents) {
            boolean found = false;
            for (String expectedParent:expectedTrackParents){
                if (expectedParent.equals(actualParent)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        // Getting actual relationships for spots
        String[] actualClusterChildren = actualRelationships.getChildNames(clustersName);
        String[] actualClusterParents = actualRelationships.getParentNames(clustersName);

        // Getting expected relationships for spots
        String[] expectedClusterChildren = new String[]{spotsName};
        String[] expectedClusterParents = new String[]{""};

        assertEquals(1,actualClusterChildren.length);
        assertEquals(1,actualClusterParents.length);

        for (String actualChild:actualClusterChildren) {
            boolean found = false;
            for (String expectedChild:expectedClusterChildren){
                if (expectedChild.equals(actualChild)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        for (String actualParent:actualClusterParents) {
            boolean found = false;
            for (String expectedParent:expectedClusterParents){
                if (expectedParent.equals(actualParent)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

    @Test
    public void testGetRelationshipsWithCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String spotsName = "Spot objects";
        String tracksName = "Tracks";
        String clustersName = "Clusters";

        TrackObjects trackObjects = new TrackObjects();
        trackObjects.initialiseParameters();
        trackObjects.updateParameterValue(TrackObjects.INPUT_OBJECTS,spotsName);
        trackObjects.updateParameterValue(TrackObjects.TRACK_OBJECTS,tracksName);
        trackObjects.updateParameterValue(TrackObjects.IDENTIFY_LEADING_POINT,false);
        trackObjects.updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID);
        modules.add(trackObjects);

        ObjectClusterer objectClusterer = new ObjectClusterer();
        objectClusterer.initialiseParameters();
        objectClusterer.updateParameterValue(ObjectClusterer.INPUT_OBJECTS,spotsName);
        objectClusterer.updateParameterValue(ObjectClusterer.CLUSTER_OBJECTS,clustersName);
        objectClusterer.updateParameterValue(ObjectClusterer.CLUSTERING_ALGORITHM,ObjectClusterer.ClusteringAlgorithms.DBSCAN);
        objectClusterer.updateParameterValue(ObjectClusterer.INPUT_OBJECTS,spotsName);
        objectClusterer.updateParameterValue(ObjectClusterer.EPS,1d);
        objectClusterer.updateParameterValue(ObjectClusterer.MIN_POINTS,3);
        modules.add(objectClusterer);

        // Getting actual relationships
        RelationshipCollection actualRelationships = modules.getRelationships(objectClusterer);

        // Getting actual relationships for spots
        String[] actualSpotChildren = actualRelationships.getChildNames(spotsName);
        String[] actualSpotParents = actualRelationships.getParentNames(spotsName);

        // Getting expected relationships for spots
        String[] expectedSpotChildren = new String[]{""};
        String[] expectedSpotParents = new String[]{tracksName};

        assertEquals(1,actualSpotChildren.length);
        assertEquals(1,actualSpotParents.length);

        for (String actualChild:actualSpotChildren) {
            boolean found = false;
            for (String expectedChild:expectedSpotChildren){
                if (expectedChild.equals(actualChild)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        for (String actualParent:actualSpotParents) {
            boolean found = false;
            for (String expectedParent:expectedSpotParents){
                if (expectedParent.equals(actualParent)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        // Getting actual relationships for spots
        String[] actualTrackChildren = actualRelationships.getChildNames(tracksName);
        String[] actualTrackParents = actualRelationships.getParentNames(tracksName);

        // Getting expected relationships for spots
        String[] expectedTrackChildren = new String[]{spotsName};
        String[] expectedTrackParents = new String[]{""};

        assertEquals(1,actualTrackChildren.length);
        assertEquals(1,actualTrackParents.length);

        for (String actualChild:actualTrackChildren) {
            boolean found = false;
            for (String expectedChild:expectedTrackChildren){
                if (expectedChild.equals(actualChild)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        for (String actualParent:actualTrackParents) {
            boolean found = false;
            for (String expectedParent:expectedTrackParents){
                if (expectedParent.equals(actualParent)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        // Getting actual relationships for spots
        String[] actualClusterChildren = actualRelationships.getChildNames(clustersName);
        String[] actualClusterParents = actualRelationships.getParentNames(clustersName);

        // Getting expected relationships for spots
        String[] expectedClusterChildren = new String[]{""};
        String[] expectedClusterParents = new String[]{""};

        assertEquals(1,actualClusterChildren.length);
        assertEquals(1,actualClusterParents.length);

        for (String actualChild:actualClusterChildren) {
            boolean found = false;
            for (String expectedChild:expectedClusterChildren){
                if (expectedChild.equals(actualChild)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        for (String actualParent:actualClusterParents) {
            boolean found = false;
            for (String expectedParent:expectedClusterParents){
                if (expectedParent.equals(actualParent)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }
}