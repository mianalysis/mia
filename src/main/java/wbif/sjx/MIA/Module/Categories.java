package wbif.sjx.MIA.Module;

public class Categories {
    public final static Category ROOT = new Category("Modules", "All operations are performed using dedicated modules. Typically, a module will perform one specific task, such as loading an image or measuring the shapes of objects.", null);

    public final static Category CORE = new Category("Core", "", ROOT);
    
    public final static Category DEPRECATED = new Category("Deprecated", "", ROOT);

    public final static Category IMAGE_MEASUREMENTS = new Category("Image measurements", "", ROOT);

    public final static Category IMAGE_PROCESSING = new Category("Image processing", "", ROOT);
    public final static Category IMAGE_PROCESSING_PIXEL = new Category("Pixel", "", IMAGE_PROCESSING);
    public final static Category IMAGE_PROCESSING_PIXEL_BINARY = new Category("Binary", "", IMAGE_PROCESSING_PIXEL);
    public final static Category IMAGE_PROCESSING_PIXEL_THRESHOLD = new Category("Threshold", "", IMAGE_PROCESSING_PIXEL);
    public final static Category IMAGE_PROCESSING_STACK = new Category("Stack", "", IMAGE_PROCESSING);
    public final static Category IMAGE_PROCESSING_STACK_REGISTRATION = new Category("Registration", "",
            IMAGE_PROCESSING_STACK);

    public final static Category INPUT_OUTPUT = new Category("Input output", "", ROOT);

    public final static Category MISCELLANEOUS = new Category("Miscellaneous", "", ROOT);
    public final static Category MISCELLANEOUS_MACROS = new Category("Macros", "", MISCELLANEOUS);

    public final static Category OBJECT_MEASUREMENTS = new Category("Object measurements", "", ROOT);
    public final static Category OBJECT_MEASUREMENTS_INTENSITY = new Category("Intensity", "", OBJECT_MEASUREMENTS);
    public final static Category OBJECT_MEASUREMENTS_MISCELLANEOUS = new Category("Miscellaneous", "",
            OBJECT_MEASUREMENTS);
    public final static Category OBJECT_MEASUREMENTS_SPATIAL = new Category("Spatial", "", OBJECT_MEASUREMENTS);

    public final static Category OBJECT_PROCESSING = new Category("Object processing", "", ROOT);
    public final static Category OBJECT_PROCESSING_IDENTIFICATION = new Category("Identification", "", OBJECT_PROCESSING);
    public final static Category OBJECT_PROCESSING_MISCELLANEOUS = new Category("Miscellaneous", "", OBJECT_PROCESSING);
    public final static Category OBJECT_PROCESSING_REFINEMENT = new Category("Refinement", "", OBJECT_PROCESSING);
    public final static Category OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS = new Category("Filter objects", "",
            OBJECT_PROCESSING_REFINEMENT);
    public final static Category OBJECT_PROCESSING_REFINEMENT_MERGE_OBJECTS = new Category("Merge objects", "",
            OBJECT_PROCESSING_REFINEMENT);
    public final static Category OBJECT_PROCESSING_RELATIONSHIPS = new Category("Relationships", "", OBJECT_PROCESSING);

    public final static Category VISUALISATION = new Category("Visualisation", "", ROOT);
    public final static Category VISUALISATION_IMAGE_RENDERING = new Category("ImageRendering", "", VISUALISATION);
    public final static Category VISUALISATION_OVERLAYS = new Category("Overlays", "", VISUALISATION);

    public final static Category WORKFLOW_HANDLING = new Category("Workflow handling", "", ROOT);

    public static Category getRootCategory() {
        return ROOT;
    }
}
