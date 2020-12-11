package wbif.sjx.MIA.Module;

public class Categories {
    public final static Category ROOT = new Category("Modules", "All operations are performed using dedicated modules. Typically, a module will perform one specific task, such as loading an image or measuring the shapes of objects.", null);

    public final static Category CORE = new Category("Core", "Required modules for input and output control.  Each workflow contains one copy of each module.", ROOT);
    
    public final static Category DEPRECATED = new Category("Deprecated",
                    "Modules which will be removed in future releases of MIA.  In all cases, functionality is provided by alternative modules.  See individual module documentation for details.", ROOT);

    public final static Category IMAGE_MEASUREMENTS = new Category("Image measurements", "Operations making measurements on images in the workspace.  Measurements are associated with the input images for later use.", ROOT);

    public final static Category IMAGE_PROCESSING = new Category("Image processing", "Modules applying operations to images from the workspace.  For example, image filtering, thresholding or intensity normalisation.", ROOT);
    public final static Category IMAGE_PROCESSING_PIXEL = new Category("Pixel", "Image processing operations which act on a pixel level, such as image filtering or thresholding.", IMAGE_PROCESSING);
    public final static Category IMAGE_PROCESSING_PIXEL_BINARY = new Category("Binary", "Image operations which either act binary images.  Binary images are 8-bit with 255-intensity background and 0-intensity objects.", IMAGE_PROCESSING_PIXEL);
    public final static Category IMAGE_PROCESSING_PIXEL_THRESHOLD = new Category("Threshold", "Operations binarising images from the workspace.  Thresholds can be calculated automatically or applied manually. ", IMAGE_PROCESSING_PIXEL);
    public final static Category IMAGE_PROCESSING_STACK = new Category("Stack", "Image processing operations leading to changes in the whole stack layout.  For example, substack extraction, cropping or drift correction", IMAGE_PROCESSING);
    public final static Category IMAGE_PROCESSING_STACK_REGISTRATION = new Category("Registration", "Modules performing alignment of images within a stack to account for sample drift or other motion.",
            IMAGE_PROCESSING_STACK);

    public final static Category INPUT_OUTPUT = new Category("Input output", "Modules loading or saving files to the computer filesystem.", ROOT);

    public final static Category MISCELLANEOUS = new Category("Miscellaneous", "Miscellaneous modules which don't fit into fixed categories.  Includes global variable definitions and macro handling.", ROOT);
    public final static Category MISCELLANEOUS_MACROS = new Category("Macros", "Modules capable of running ImageJ macros during a workflow.  Using MIA-specific macro commands, images and objects in the workspace can be accessed from each macro.", MISCELLANEOUS);

    public final static Category OBJECT_MEASUREMENTS = new Category("Object measurements", "Operations making measurements of individual objects in the workspace.  Measurements are associated with the relevant input objects.", ROOT);
    public final static Category OBJECT_MEASUREMENTS_INTENSITY = new Category("Intensity", "Modules performing intensity-based measurements on objects in the workspace.", OBJECT_MEASUREMENTS);
    public final static Category OBJECT_MEASUREMENTS_MISCELLANEOUS = new Category("Miscellaneous", "General object measurements, such as counting the number of child objects or binning measurements.",
            OBJECT_MEASUREMENTS);
    public final static Category OBJECT_MEASUREMENTS_SPATIAL = new Category("Spatial", "Modules performing spatial-based measurements on objects in the workspace.", OBJECT_MEASUREMENTS);

    public final static Category OBJECT_PROCESSING = new Category("Object processing", "Operations capable of creating new objects, changing existing ones or creating new object relationships.", ROOT);
    public final static Category OBJECT_PROCESSING_IDENTIFICATION = new Category("Identification", "Modules resulting in creation of new objects to be stored in the workspace.", OBJECT_PROCESSING);
    public final static Category OBJECT_PROCESSING_MISCELLANEOUS = new Category("Miscellaneous", "Operations which allow conversion between objects and images.", OBJECT_PROCESSING);
    public final static Category OBJECT_PROCESSING_REFINEMENT = new Category("Refinement", "Modules capable of updating object coordinates or removing objects entirely from the workspace (e.g. measurement-based filters).", OBJECT_PROCESSING);
    public final static Category OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS = new Category("Filter objects", "Modules which can remove objects from an object collection (or move them to a new collection) based on various metrics.",
            OBJECT_PROCESSING_REFINEMENT);
    public final static Category OBJECT_PROCESSING_REFINEMENT_MERGE_OBJECTS = new Category("Merge objects", "Modules used for combining either different objects into one or combining objects from different collections into a single collection.",
            OBJECT_PROCESSING_REFINEMENT);
    public final static Category OBJECT_PROCESSING_RELATIONSHIPS = new Category("Relationships", "Modules creating relationships between objects.  Relationships can be one-to-many (parent-child) or one-to-one (partners).", OBJECT_PROCESSING);

    public final static Category VISUALISATION = new Category("Visualisation", "Modules altering how images are displayed (e.g. lookup-tables) or adding visual elements (e.g. text or object overlays).", ROOT);
    public final static Category VISUALISATION_IMAGE_RENDERING = new Category("ImageRendering", "Operations altering how ImageJ displays images.  These operations don't result in fundamental changes to pixel values.", VISUALISATION);
    public final static Category VISUALISATION_OVERLAYS = new Category("Overlays", "Modules adding ImageJ overlay elements to images.  For example, object outlines, object ID labels or fixed text.", VISUALISATION);

    public final static Category WORKFLOW_HANDLING = new Category("Workflow handling", "Modules capable of controlling the order of module execution.  For example, skipping specific modules if certain criteria are met.", ROOT);

    public static Category getRootCategory() {
        return ROOT;
    }
}
