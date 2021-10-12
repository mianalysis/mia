package io.github.mianalysis.mia.module;

public class Categories {
        public final static Category ROOT = new Category("Modules",
                        "All operations are performed using dedicated modules. Typically, a module will perform one specific task, such as loading an image or measuring the shapes of objects.",
                        null);

        public final static Category CORE = new Category("Core",
                        "Required modules for input and output control.  Each workflow contains one copy of each module.",
                        ROOT, false);

        public final static Category IMAGES = new Category("Images",
                        "Operations making measurements on images in the workspace.  Measurements are associated with the input images for later use.",
                        ROOT);

        public final static Category IMAGES_CONFIGURE = new Category("Configure",
                        "Modules applying operations to images from the workspace.  For example, image filtering, thresholding or intensity normalisation.",
                        IMAGES);
        public final static Category IMAGES_MEASURE = new Category("Measure",
                        "General image processing operations, such as setting spatial calibration.", IMAGES);
        public final static Category IMAGES_PROCESS = new Category("Process",
                        "Image processing operations which act on a pixel level, such as image filtering or thresholding.",
                        IMAGES);
        public final static Category IMAGES_PROCESS_BINARY = new Category("Binary",
                        "Image operations which either act binary images.  Binary images are 8-bit with 255-intensity background and 0-intensity objects.",
                        IMAGES_PROCESS);
        public final static Category IMAGES_PROCESS_THRESHOLD = new Category("Threshold",
                        "Operations binarising images from the workspace.  Thresholds can be calculated automatically or applied manually. ",
                        IMAGES_PROCESS);
        public final static Category IMAGES_TRANSFORM = new Category("Transform",
                        "Image processing operations leading to changes in the whole stack layout.  For example, substack extraction, cropping or drift correction",
                        IMAGES);
        public final static Category IMAGES_TRANSFORM_REGISTRATION = new Category("Registration",
                        "Modules performing alignment of images within a stack to account for sample drift or other motion.",
                        IMAGES_TRANSFORM);

        public final static Category INPUT_OUTPUT = new Category("Input output",
                        "Modules loading or saving files to the computer filesystem.", ROOT);

        public final static Category OBJECTS = new Category("Objects",
                        "Operations making measurements of individual objects in the workspace.  Measurements are associated with the relevant input objects.",
                        ROOT);
        public final static Category OBJECTS_CONVERT = new Category("Convert",
                        "Operations which allow conversion between objects and images.", OBJECTS);
        public final static Category OBJECTS_DETECT = new Category("Detect",
                        "Modules resulting in creation of new objects to be stored in the workspace.", OBJECTS);
        public final static Category OBJECTS_FILTER = new Category("Filter",
                        "Modules which can remove objects from an object collection (or move them to a new collection) based on various metrics.",
                        OBJECTS);
        public final static Category OBJECTS_MEASURE = new Category("Measure",
                        "Operations making measurements of individual objects in the workspace.  Measurements are associated with the relevant input objects.",
                        OBJECTS);
        public final static Category OBJECTS_MEASURE_INTENSITY = new Category("Intensity",
                        "Modules performing intensity-based measurements on objects in the workspace.",
                        OBJECTS_MEASURE);
        public final static Category OBJECTS_MEASURE_MISCELLANEOUS = new Category("Miscellaneous",
                        "General object measurements, such as counting the number of child objects or binning measurements.",
                        OBJECTS_MEASURE);
        public final static Category OBJECTS_MEASURE_SPATIAL = new Category("Spatial",
                        "Modules performing spatial-based measurements on objects in the workspace.", OBJECTS_MEASURE);
        public final static Category OBJECTS_PROCESS = new Category("Process",
                        "Operations capable of creating new objects, changing existing ones or creating new object relationships.",
                        OBJECTS);
        public final static Category OBJECTS_RELATE = new Category("Relate",
                        "Modules creating relationships between objects.  Relationships can be one-to-many (parent-child) or one-to-one (partners).",
                        OBJECTS);
        public final static Category OBJECTS_RELATE_MERGE = new Category("Merge",
                        "Modules used for combining either different objects into one or combining objects from different collections into a single collection.",
                        OBJECTS_RELATE);
        public final static Category OBJECTS_TRANSFORM = new Category("Transform",
                        "Modules capable of updating object coordinates or removing objects entirely from the workspace (e.g. measurement-based filters).",
                        OBJECTS);

        public final static Category SCRIPT = new Category("Script",
                        "Miscellaneous modules which don't fit into fixed categories.  Includes global variable definitions and macro handling.",
                        ROOT);

        public final static Category SYSTEM = new Category("Macros",
                        "Modules capable of running ImageJ macros during a workflow.  Using MIA-specific macro commands, images and objects in the workspace can be accessed from each macro.",
                        ROOT);

        public final static Category VISUALISATION = new Category("Visualisation",
                        "Modules altering how images are displayed (e.g. lookup-tables) or adding visual elements (e.g. text or object overlays).",
                        ROOT);
        public final static Category VISUALISATION_OVERLAYS = new Category("Overlays",
                        "Modules adding ImageJ overlay elements to images.  For example, object outlines, object ID labels or fixed text.",
                        VISUALISATION);

        public final static Category WORKFLOW = new Category("Workflow",
                        "Modules capable of controlling the order of module execution.  For example, skipping specific modules if certain criteria are met.",
                        ROOT);

        public static Category getRootCategory() {
                return ROOT;
        }
}
