package wbif.sjx.MIA.Object;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;
import wbif.sjx.MIA.Module.ImageMeasurements.MeasureImageIntensity;
import wbif.sjx.MIA.Module.ImageMeasurements.MeasureImageTexture;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.RemoveImage;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.ObjectMeasurements.Intensity.MeasureObjectTexture;
import wbif.sjx.MIA.Module.ObjectMeasurements.Spatial.MeasureObjectCentroid;
import wbif.sjx.MIA.Module.ObjectMeasurements.Spatial.MeasureObjectShape;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.TrackObjects;
import wbif.sjx.MIA.Module.ObjectProcessing.Refinement.ObjectClusterer;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;

import java.util.LinkedHashSet;

import static org.junit.Assert.*;

public class ModuleCollectionTest < T extends RealType< T > & NativeType< T >> {
    @Test
    public void testGetAvailableImages() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String im1Name = "Im 1";
        String im2Name = "New_image";

        // Populating some modules (no need to populate all parameters as these should be initialised)
        ImageLoader imageLoader= new ImageLoader();
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,im1Name);
        modules.add(imageLoader);

        FilterImage filterImage = new FilterImage();
        filterImage.updateParameterValue(FilterImage.INPUT_IMAGE,im1Name);
        filterImage.updateParameterValue(FilterImage.APPLY_TO_INPUT,false);
        filterImage.updateParameterValue(FilterImage.OUTPUT_IMAGE,im2Name);
        modules.add(filterImage);

        RemoveImage removeImage = new RemoveImage();
        removeImage.updateParameterValue(RemoveImage.INPUT_IMAGE,im1Name);
        modules.add(removeImage);

        LinkedHashSet<OutputImageP> availableImages = modules.getAvailableImages(null);

        assertEquals(1,availableImages.size());

    }

    @Test
    public void testGetImageMeasurementRefsNoCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String im1Name = "Im 1";
        String im2Name = "New_image";

        // Populating some modules (no need to populate all parameters as these should be initialised)
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,im1Name);
        modules.add(measureImageIntensity);

        MeasureImageIntensity measureImageIntensity2 = new MeasureImageIntensity();
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,im2Name);
        modules.add(measureImageIntensity2);

        MeasureImageTexture measureImageTexture = new MeasureImageTexture();
        measureImageTexture.updateParameterValue(MeasureImageTexture.INPUT_IMAGE,im2Name);
        modules.add(measureImageTexture);

        // Checking the values for "Im1"
        MeasurementRefCollection references1 = modules.getImageMeasurementRefs(im1Name);
        assertEquals(5,references1.size());

        String[] expectedNames1 = new String[]{MeasureImageIntensity.Measurements.MEAN,
                MeasureImageIntensity.Measurements.STDEV,
                MeasureImageIntensity.Measurements.MIN,
                MeasureImageIntensity.Measurements.MAX,
                MeasureImageIntensity.Measurements.SUM};

        for (String expectedName1:expectedNames1) {
            assertTrue(references1.containsKey(expectedName1));
        }

        // Checking the values for "New_image"
        MeasurementRefCollection references2 = modules.getImageMeasurementRefs(im2Name);
        assertEquals(9,references2.size());

        String[] expectedNames2 = new String[]{MeasureImageIntensity.Measurements.MEAN,
                MeasureImageIntensity.Measurements.STDEV,
                MeasureImageIntensity.Measurements.MIN,
                MeasureImageIntensity.Measurements.MAX,
                MeasureImageIntensity.Measurements.SUM,
                MeasureImageTexture.Measurements.ASM,
                MeasureImageTexture.Measurements.CONTRAST,
                MeasureImageTexture.Measurements.CORRELATION,
                MeasureImageTexture.Measurements.ENTROPY};

        for (String expectedName2:expectedNames2) {
            assertTrue(references2.containsKey(expectedName2));
        }
    }

    @Test
    public void testGetImageMeasurementRefsWithCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String im1Name = "Im 1";
        String im2Name = "New_image";

        // Populating some modules (no need to populate all parameters as these should be initialised)
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity();
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,im1Name);
        modules.add(measureImageIntensity);

        MeasureImageIntensity measureImageIntensity2 = new MeasureImageIntensity();
        measureImageIntensity2.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,im2Name);
        modules.add(measureImageIntensity2);

        MeasureImageTexture measureImageTexture = new MeasureImageTexture();
        measureImageTexture.updateParameterValue(MeasureImageTexture.INPUT_IMAGE,im2Name);
        modules.add(measureImageTexture);

        // Checking the values for "Im1"
        MeasurementRefCollection references1 = modules.getImageMeasurementRefs(im1Name,measureImageIntensity2);
        assertEquals(5,references1.size());

        String[] expectedNames1 = new String[]{MeasureImageIntensity.Measurements.MEAN,
                MeasureImageIntensity.Measurements.STDEV,
                MeasureImageIntensity.Measurements.MIN,
                MeasureImageIntensity.Measurements.MAX,
                MeasureImageIntensity.Measurements.SUM};

        for (String expectedName1:expectedNames1) {
            assertTrue(references1.containsKey(expectedName1));
        }

        // Checking the values for "New_image"
        MeasurementRefCollection references2 = modules.getImageMeasurementRefs(im2Name,measureImageIntensity2);
        assertEquals(0,references2.size());

    }

    @Test
    public void testGetObjectMeasurementRefsNoCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String obj1Name = "First obj set";
        String obj2Name = "Second";

        // Populating some modules (no need to populate all parameters as these should be initialised)
        MeasureObjectCentroid measureObjectCentroid = new MeasureObjectCentroid();
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,obj1Name);
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.CENTROID_METHOD,MeasureObjectCentroid.Methods.MEAN);
        modules.add(measureObjectCentroid);

        MeasureObjectShape measureObjectShape = new MeasureObjectShape();
        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,obj2Name);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_VOLUME,true);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_AREA,false);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_DIA,true);
        modules.add(measureObjectShape);

        MeasureObjectTexture measureObjectTexture = new MeasureObjectTexture();
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_OBJECTS,obj1Name);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_IMAGE,"");
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.POINT_MEASUREMENT,true);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.CALIBRATED_RADIUS,false);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.MEASUREMENT_RADIUS,1d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.X_OFFSET,1d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Y_OFFSET,0d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Z_OFFSET,0d);
        modules.add(measureObjectTexture);

        // Checking the values for "Im1"
        MeasurementRefCollection references1 = modules.getObjectMeasurementRefs(obj1Name);
        assertEquals(10,references1.size());

        double[] offs = new double[]{1,0,0};
        String[] expectedNames1 = new String[]{MeasureObjectCentroid.Measurements.MEAN_X_PX,
                MeasureObjectCentroid.Measurements.MEAN_Y_PX,
                MeasureObjectCentroid.Measurements.MEAN_Z_SLICE,
                Units.replace(MeasureObjectCentroid.Measurements.MEAN_X_CAL),
                Units.replace(MeasureObjectCentroid.Measurements.MEAN_Y_CAL),
                Units.replace(MeasureObjectCentroid.Measurements.MEAN_Z_CAL),
                MeasureObjectTexture.getFullName("",MeasureObjectTexture.Measurements.ASM,offs,false),
                MeasureObjectTexture.getFullName("",MeasureObjectTexture.Measurements.CONTRAST,offs,false),
                MeasureObjectTexture.getFullName("",MeasureObjectTexture.Measurements.CORRELATION,offs,false),
                MeasureObjectTexture.getFullName("",MeasureObjectTexture.Measurements.ENTROPY,offs,false)};

        for (String expectedName1:expectedNames1) {
            assertTrue(references1.containsKey(expectedName1));
        }

        // Checking the values for the second object set
        MeasurementRefCollection references2 = modules.getObjectMeasurementRefs(obj2Name);
        assertEquals(5,references2.size());

        String[] expectedNames2 = new String[]{
                Units.replace(MeasureObjectShape.Measurements.PROJ_DIA_CAL),
                MeasureObjectShape.Measurements.PROJ_DIA_PX,
                Units.replace(MeasureObjectShape.Measurements.VOLUME_CAL),
                Units.replace(MeasureObjectShape.Measurements.VOLUME_CAL)};

        for (String expectedName2:expectedNames2) {
            assertTrue(references2.containsKey(expectedName2));
        }
    }

    @Test
    public void testGetObjectMeasurementRefsWithCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String obj1Name = "First obj set";
        String obj2Name = "Second";

        // Populating some modules (no need to populate all parameters as these should be initialised)
        MeasureObjectCentroid measureObjectCentroid = new MeasureObjectCentroid();
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,obj1Name);
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.CENTROID_METHOD,MeasureObjectCentroid.Methods.MEAN);
        modules.add(measureObjectCentroid);

        MeasureObjectShape measureObjectShape = new MeasureObjectShape();
                measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,obj2Name);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_VOLUME,true);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_AREA,false);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_DIA,true);
        modules.add(measureObjectShape);

        MeasureObjectTexture measureObjectTexture = new MeasureObjectTexture();
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_OBJECTS,obj1Name);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_IMAGE,"");
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.POINT_MEASUREMENT,true);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.CALIBRATED_RADIUS,false);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.MEASUREMENT_RADIUS,1d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.X_OFFSET,1d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Y_OFFSET,0d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Z_OFFSET,0d);
        modules.add(measureObjectTexture);

        // Checking the values for "Im1"
        MeasurementRefCollection references1 = modules.getObjectMeasurementRefs(obj1Name,measureObjectShape);
        assertEquals(6,references1.size());

        String[] expectedNames1 = new String[]{MeasureObjectCentroid.Measurements.MEAN_X_PX,
                MeasureObjectCentroid.Measurements.MEAN_Y_PX,
                MeasureObjectCentroid.Measurements.MEAN_Z_SLICE,
                Units.replace(MeasureObjectCentroid.Measurements.MEAN_X_CAL),
                Units.replace(MeasureObjectCentroid.Measurements.MEAN_Y_CAL),
                Units.replace(MeasureObjectCentroid.Measurements.MEAN_Z_CAL)};

        for (String expectedName1:expectedNames1) {
            assertTrue(references1.containsKey(expectedName1));
        }

        // Checking the values for the second object set
        MeasurementRefCollection references2 = modules.getObjectMeasurementRefs(obj2Name,measureObjectShape);
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
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,obj1Name);
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.CENTROID_METHOD,MeasureObjectCentroid.Methods.MEAN);
        modules.add(measureObjectCentroid);

        MeasureObjectShape measureObjectShape = new MeasureObjectShape();
        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,obj2Name);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_VOLUME,true);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_AREA,false);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_DIA,true);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_PERIM,false);
        modules.add(measureObjectShape);

        MeasureObjectTexture measureObjectTexture = new MeasureObjectTexture();
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_OBJECTS,obj1Name);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_IMAGE,"");
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.POINT_MEASUREMENT,true);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.CALIBRATED_RADIUS,false);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.MEASUREMENT_RADIUS,1d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.X_OFFSET,1d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Y_OFFSET,0d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Z_OFFSET,0d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.CALIBRATED_OFFSET,false);
        modules.add(measureObjectTexture);

        // Checking for booleans
        LinkedHashSet<BooleanP> actualParams = modules.getParametersMatchingType(BooleanP.class);

        // Getting expected values
        LinkedHashSet<Parameter> expectedParams = new LinkedHashSet<>();
        expectedParams.add(new BooleanP(MeasureObjectShape.MEASURE_VOLUME,measureObjectShape,true));
        expectedParams.add(new BooleanP(MeasureObjectShape.MEASURE_PROJECTED_DIA,measureObjectShape,true));
        expectedParams.add(new BooleanP(MeasureObjectShape.MEASURE_PROJECTED_AREA,measureObjectShape,false));
        expectedParams.add(new BooleanP(MeasureObjectShape.MEASURE_PROJECTED_PERIM,measureObjectShape,false));
        expectedParams.add(new BooleanP(MeasureObjectTexture.POINT_MEASUREMENT,measureObjectShape,true));
        expectedParams.add(new BooleanP(MeasureObjectTexture.CALIBRATED_RADIUS,measureObjectShape,false));
        expectedParams.add(new BooleanP(MeasureObjectTexture.CALIBRATED_OFFSET,measureObjectShape,false));

        for (BooleanP param:actualParams) {
            System.err.println(param.getName()+"_"+param.getModule().getTitle());
        }


        // Checking the parameters are what are expected
        assertEquals(7,actualParams.size());

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
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,obj1Name);
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.CENTROID_METHOD,MeasureObjectCentroid.Methods.MEAN);
        modules.add(measureObjectCentroid);

        MeasureObjectShape measureObjectShape = new MeasureObjectShape();
        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,obj2Name);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_VOLUME,true);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_DIA,true);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_AREA,false);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_PERIM,false);
        modules.add(measureObjectShape);

        MeasureObjectTexture measureObjectTexture = new MeasureObjectTexture();
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_OBJECTS,obj1Name);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_IMAGE,"");
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.POINT_MEASUREMENT,true);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.CALIBRATED_RADIUS,false);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.MEASUREMENT_RADIUS,1d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.X_OFFSET,1d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Y_OFFSET,0d);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Z_OFFSET,0d);
        modules.add(measureObjectTexture);

        // Checking for booleans
        LinkedHashSet<BooleanP> actualParams = modules.getParametersMatchingType(BooleanP.class,measureObjectTexture);

        // Getting expected values
        LinkedHashSet<Parameter> expectedParams = new LinkedHashSet<>();
        expectedParams.add(new BooleanP(MeasureObjectShape.MEASURE_VOLUME,measureObjectShape,true));
        expectedParams.add(new BooleanP(MeasureObjectShape.MEASURE_PROJECTED_DIA,measureObjectShape,true));
        expectedParams.add(new BooleanP(MeasureObjectShape.MEASURE_PROJECTED_AREA,measureObjectShape,false));
        expectedParams.add(new BooleanP(MeasureObjectShape.MEASURE_PROJECTED_PERIM,measureObjectShape,false));

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
    public void testGetRelationshipsNoCutoff() {
        // Creating a ModuleCollection to hold the Modules
        ModuleCollection modules = new ModuleCollection();

        String spotsName = "Spot objects";
        String tracksName = "Tracks";
        String clustersName = "Clusters";

        TrackObjects trackObjects = new TrackObjects();
        trackObjects.updateParameterValue(TrackObjects.INPUT_OBJECTS,spotsName);
        trackObjects.updateParameterValue(TrackObjects.TRACK_OBJECTS,tracksName);
        trackObjects.updateParameterValue(TrackObjects.IDENTIFY_LEADING_POINT,false);
        trackObjects.updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID);
        modules.add(trackObjects);

        ObjectClusterer objectClusterer = new ObjectClusterer();
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
        String[] actualSpotChildren = actualRelationships.getChildNames(spotsName,false);
        String[] actualSpotParents = actualRelationships.getParentNames(spotsName,false);

        // Getting expected relationships for spots
        String[] expectedSpotChildren = new String[]{""};
        String[] expectedSpotParents = new String[]{tracksName,clustersName};

        assertEquals(0,actualSpotChildren.length);
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
        String[] actualTrackChildren = actualRelationships.getChildNames(tracksName,false);
        String[] actualTrackParents = actualRelationships.getParentNames(tracksName,false);

        // Getting expected relationships for spots
        String[] expectedTrackChildren = new String[]{spotsName};
        String[] expectedTrackParents = new String[]{""};

        assertEquals(1,actualTrackChildren.length);
        assertEquals(0,actualTrackParents.length);

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
        String[] actualClusterChildren = actualRelationships.getChildNames(clustersName,false);
        String[] actualClusterParents = actualRelationships.getParentNames(clustersName,false);

        // Getting expected relationships for spots
        String[] expectedClusterChildren = new String[]{spotsName};
        String[] expectedClusterParents = new String[]{""};

        assertEquals(1,actualClusterChildren.length);
        assertEquals(0,actualClusterParents.length);

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
        trackObjects.updateParameterValue(TrackObjects.INPUT_OBJECTS,spotsName);
        trackObjects.updateParameterValue(TrackObjects.TRACK_OBJECTS,tracksName);
        trackObjects.updateParameterValue(TrackObjects.IDENTIFY_LEADING_POINT,false);
        trackObjects.updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID);
        modules.add(trackObjects);

        ObjectClusterer objectClusterer = new ObjectClusterer();
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
        String[] actualSpotChildren = actualRelationships.getChildNames(spotsName,false);
        String[] actualSpotParents = actualRelationships.getParentNames(spotsName,false);

        // Getting expected relationships for spots
        String[] expectedSpotChildren = new String[]{""};
        String[] expectedSpotParents = new String[]{tracksName};

        assertEquals(0,actualSpotChildren.length);
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
        String[] actualTrackChildren = actualRelationships.getChildNames(tracksName,false);
        String[] actualTrackParents = actualRelationships.getParentNames(tracksName,false);

        // Getting expected relationships for spots
        String[] expectedTrackChildren = new String[]{spotsName};
        String[] expectedTrackParents = new String[]{""};

        assertEquals(1,actualTrackChildren.length);
        assertEquals(0,actualTrackParents.length);

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
        String[] actualClusterChildren = actualRelationships.getChildNames(clustersName,false);
        String[] actualClusterParents = actualRelationships.getParentNames(clustersName,false);

        // Getting expected relationships for spots
        String[] expectedClusterChildren = new String[]{""};
        String[] expectedClusterParents = new String[]{""};

        assertEquals(0,actualClusterChildren.length);
        assertEquals(0,actualClusterParents.length);

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