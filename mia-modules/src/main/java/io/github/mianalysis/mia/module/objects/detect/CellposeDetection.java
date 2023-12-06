// package io.github.mianalysis.mia.module.objects.detect;

// import java.io.File;

// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;

// import ch.epfl.biop.wrappers.cellpose.Cellpose;
// import ij.ImagePlus;
// import io.github.mianalysis.mia.module.Categories;
// import io.github.mianalysis.mia.module.Category;
// import io.github.mianalysis.mia.module.Module;
// import io.github.mianalysis.mia.module.Modules;
// import io.github.mianalysis.mia.object.Workspace;
// import io.github.mianalysis.mia.object.image.Image;
// import io.github.mianalysis.mia.object.parameters.FolderPathP;
// import io.github.mianalysis.mia.object.parameters.InputImageP;
// import io.github.mianalysis.mia.object.parameters.Parameters;
// import io.github.mianalysis.mia.object.parameters.SeparatorP;
// import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
// import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
// import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
// import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
// import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
// import io.github.mianalysis.mia.object.system.Status;

// @Plugin(type = Module.class, priority = Priority.LOW, visible = true)
// public class CellposeDetection extends Module {

//     public static final String INPUT_SEPARATOR = "Image input/object output";

//     public static final String INPUT_IMAGE = "Input image";

//     public static final String OUTPUT_OBJECTS = "Output objects";

//     public static final String CELLPOSE_SEPARATOR = "Cellpose settings";

//     public static final String CELLPOSE_PATH = "Cellpose environment path";

//     public CellposeDetection(Modules modules) {
//         super("Cellpose detection", modules);
//     }

//     @Override
//     public Category getCategory() {
//         return Categories.OBJECTS_DETECT;
//     }

//     @Override
//     public String getVersionNumber() {
//         return "1.0.0";
//     }

//     @Override
//     public String getDescription() {
//         return "";
//     }

//     @Override
//     public Status process(Workspace workspace) {
//         // Getting input image
//         String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
//         String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
//         String cellposePath = parameters.getValue(CELLPOSE_PATH, workspace);

//         Image inputImage = workspace.getImages().get(inputImageName);
//         ImagePlus ipl = inputImage.getImagePlus();

//         Cellpose.setEnvDirPath(new File(cellposePath));
//         Cellpose.setEnvType("conda");
//         Cellpose.setUseGpu(true);
//         Cellpose.setUseMxnet(false);
//         Cellpose.setUseFastMode(false);
//         Cellpose.setUseResample(false);
//         Cellpose.setVersion("2.0");

//         // Cellpose_SegmentImgPlusOwnModelAdvanced cellpose = new
//         // Cellpose_SegmentImgPlusOwnModelAdvanced();
//         // cellpose.imp = ipl
//         // cellpose.diameter = 20
//         // cellpose.cellproba_threshold = 0
//         // cellpose.flow_threshold = 0.4
//         // cellpose.anisotropy = 1
//         // cellpose.diam_threshold = 12
//         // cellpose.model_path = new File("cellpose")
//         // cellpose.model = "cyto"
//         // cellpose.nuclei_channel = -1
//         // cellpose.cyto_channel = 1
//         // cellpose.dimensionMode = "2D"
//         // cellpose.stitch_threshold = 0
//         // cellpose.omni = false
//         // cellpose.cluster = false
//         // cellpose.additional_flags = ""

//         // cellpose.run()

//         // cellsImage = ImageFactory.createImage("Objects",cellpose.cellpose_imp)
//         // outputObjects =
//         // cellsImage.convertImageToObjects(VolumeType.QUADTREE,"RedCells")

//         // workspace.addObjects(outputObjects)

//         return Status.PASS;

//     }

//     @Override
//     protected void initialiseParameters() {
//         parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
//         parameters.add(new InputImageP(INPUT_IMAGE, this));
//         parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

//         parameters.add(new SeparatorP(CELLPOSE_SEPARATOR, this));
//         parameters.add(new FolderPathP(CELLPOSE_PATH, this));

//     }

//     @Override
//     public Parameters updateAndGetParameters() {
//         Workspace workspace = null;
//         Parameters returnedParameters = new Parameters();

//         returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
//         returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
//         returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

//         returnedParameters.add(parameters.getParameter(CELLPOSE_SEPARATOR));
//         returnedParameters.add(parameters.getParameter(CELLPOSE_PATH));

//         return returnedParameters;

//     }

//     @Override
//     public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
//         return null;
//     }

//     @Override
//     public MetadataRefs updateAndGetMetadataReferences() {
//         return null;
//     }

//     @Override
//     public ParentChildRefs updateAndGetParentChildRefs() {
//         return null;
//     }

//     @Override
//     public PartnerRefs updateAndGetPartnerRefs() {
//         return null;
//     }

//     @Override
//     public boolean verify() {
//         return true;
//     }
// }
