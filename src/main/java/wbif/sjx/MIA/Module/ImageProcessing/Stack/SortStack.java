// package wbif.sjx.MIA.Module.ImageProcessing.Stack;

// import ij.IJ;
// import ij.ImagePlus;
// import ij.process.ImageProcessor;
// import mpicbg.models.AbstractAffineModel2D;
// import wbif.sjx.MIA.MIA;
// import wbif.sjx.MIA.Module.Module;
// import wbif.sjx.MIA.Module.ModuleCollection;
// import wbif.sjx.MIA.Module.PackageNames;
// import wbif.sjx.MIA.Object.Image;
// import wbif.sjx.MIA.Object.Status;
// import wbif.sjx.MIA.Object.Workspace;
// import wbif.sjx.MIA.Object.Parameters.BooleanP;
// import wbif.sjx.MIA.Object.Parameters.InputImageP;
// import wbif.sjx.MIA.Object.Parameters.OutputImageP;
// import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
// import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
// import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
// import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
// import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
// import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
// import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

// /**
//  * Created by sc13967 on 30/06/2017.
//  */
// public class SortStack extends Module {
//     public static final String INPUT_SEPARATOR = "Image input/output";
//     public static final String INPUT_IMAGE = "Input image";
//     public static final String APPLY_TO_INPUT = "Apply to input image";
//     public static final String OUTPUT_IMAGE = "Output image";

//     public SortStack(ModuleCollection modules) {
//         super("Sort stack", modules);
//     }

//     @Override
//     public String getPackageName() {
//         return PackageNames.IMAGE_PROCESSING_STACK;
//     }

//     @Override
//     public String getDescription() {
//         return "";
//     }

//     public int getClosestMatchingSlice(Image image, int sliceIndex, RegisterImages.Param param) {
//         double maxSimilarity = 0;
//         int matchingSliceIndex = 0;

        
//         // Current image
//         ImagePlus ipl = image.getImagePlus();
//         ImagePlus costs = IJ.createImage("Costs", ipl.getNFrames(), ipl.getNFrames(), 1, 32);
//         ImageProcessor costsIpr = costs.getProcessor();

//         for (int tCurr = 0; tCurr < ipl.getNFrames(); tCurr++) {
//             // ipl.setPosition(sliceIndex + 1);
//             ipl.setPosition(tCurr + 1);
//             ImageProcessor currIpr = ipl.getProcessor().duplicate();

//             for (int tTest = tCurr + 1; tTest < ipl.getNFrames(); tTest++) {
//                 if (ipl.isHyperStack()) {
//                     int[] pos = new int[] { 1, 1, tTest + 1 };
//                     ipl.setPosition(pos[0], pos[1], pos[2]);
//                 } else {
//                     ipl.setPosition(tTest + 1);
//                 }
//                 ImageProcessor testIpr = ipl.getProcessor().duplicate();

//                 AbstractAffineModel2D<?> model = RegisterImages.getAffineModel2D(currIpr, testIpr, param);
//                 costsIpr.setf(tTest, tCurr, (float) model.getCost());
//                 MIA.log.writeDebug(tCurr + "_" + tTest + "_" + model.getCost());

//             }
//         }

//         costs.show();
//         // ImagePlus referenceIpl = referenceImage.getImagePlus();
//         // ImagePlus warpedIpl = warpedImage.getImagePlus();

//         // // Initialising SIFT feature extractor
//         // FloatArray2DSIFT sift = new FloatArray2DSIFT(param);
//         // SIFT ijSIFT = new SIFT(sift);

//         // // Extracting features
//         // ArrayList<Feature> featureList1 = new ArrayList<Feature>();
//         // ijSIFT.extractFeatures(referenceIpl.getProcessor(), featureList1);
//         // ArrayList<Feature> featureList2 = new ArrayList<Feature>();
//         // ijSIFT.extractFeatures(warpedIpl.getProcessor(), featureList2);

//         // // Running registration
//         // AbstractAffineModel2D model = getModel(param.transformationMode);
//         // InverseTransformMapping mapping = new
//         // InverseTransformMapping<AbstractAffineModel2D<?>>(model);
//         // Vector<PointMatch> candidates = FloatArray2DSIFT.createMatches(featureList2,
//         // featureList1, 1.5f, null,
//         // Float.MAX_VALUE, param.rod);
//         // Vector<PointMatch> inliers = new Vector<PointMatch>();

//         // try {
//         // model.filterRansac(candidates, inliers, 1000, param.maxEpsilon,
//         // param.minInlierRatio);
//         // } catch (NotEnoughDataPointsException e) {
//         // e.printStackTrace();
//         // return null;
//         // }

//         return matchingSliceIndex;

//     }

//     @Override
//     public Status process(Workspace workspace) {
//         // Getting input image
//         String inputImageName = parameters.getValue(INPUT_IMAGE);
//         boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
//         String outputImageName = parameters.getValue(OUTPUT_IMAGE);

//         Image inputImage = workspace.getImage(inputImageName);

//         RegisterImages.Param param = new RegisterImages.Param();
//         param.transformationMode = RegisterImages.TransformationModes.TRANSLATION;
//         param.initialSigma = 1.6f;
//         param.steps = 3;
//         param.minOctaveSize = 64;
//         param.maxOctaveSize = 1024;
//         param.fdSize = 4;
//         param.fdBins = 8;
//         param.rod = 0.92f;
//         param.maxEpsilon = 25.0f;
//         param.minInlierRatio = 0.05f;

//         getClosestMatchingSlice(inputImage, 0, param);

//         // Create forward similarity map

//         // Get slice order based on minimum costs

//         // Updating stack order

//         return Status.PASS;

//     }

//     @Override
//     protected void initialiseParameters() {
//         parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
//         parameters.add(new InputImageP(INPUT_IMAGE, this));
//         parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
//         parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

//         addParameterDescriptions();

//     }

//     @Override
//     public ParameterCollection updateAndGetParameters() {
//         ParameterCollection returnedParameters = new ParameterCollection();

//         returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
//         returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
//         returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
//         if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
//             returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
//         }

//         return returnedParameters;

//     }

//     @Override
//     public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public MetadataRefCollection updateAndGetMetadataReferences() {
//         return null;
//     }

//     @Override
//     public ParentChildRefCollection updateAndGetParentChildRefs() {
//         return null;
//     }

//     @Override
//     public PartnerRefCollection updateAndGetPartnerRefs() {
//         return null;
//     }

//     @Override
//     public boolean verify() {
//         return true;
//     }

//     void addParameterDescriptions() {

//     }
// }
