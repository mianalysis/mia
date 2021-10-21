package io.github.mianalysis.mia.module;

public class Categories {
        public final static Category ROOT = new Category("Modules",
                        "All operations are performed using dedicated modules. Typically, a module will perform one specific task, such as loading an image or measuring the shapes of objects.",
                        null);

        public final static Category CORE = new Category("Core",
                        "Required modules for input and output control.  Each workflow contains one copy of each module.",
                        ROOT, false);

        public final static Category IMAGES = new Category("Images",
                        "Modules dealing primarily with images.  These operations typically take images as inputs and may output new images or update existing ones.",
                        ROOT);

        public final static Category IMAGES_CONFIGURE = new Category("Configure",
                        "Non-pixel operations such as setting display ranges or spatial calibrations.",
                        IMAGES);
        public final static Category IMAGES_MEASURE = new Category("Measure",
                        "Operations making measurements on images. Measurements are associated with the input images for later use.", IMAGES);
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
                        "Image processing operations leading to changes in the whole stack layout.  For example, substack extraction, cropping or drift correction.",
                        IMAGES);
        public final static Category IMAGES_TRANSFORM_REGISTRATION = new Category("Registration",
                        "Modules performing alignment of images within a stack to account for sample drift or other motion.",
                        IMAGES_TRANSFORM);

        public final static Category INPUT_OUTPUT = new Category("Input output",
                        "Modules loading or saving files to the filesystem.", ROOT);

        public final static Category OBJECTS = new Category("Objects",
                        "Modules dealing primarily with objects.  These include object detection, measurements and operations leading to changes in existing objects.",
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
                        "Operations capable of creating new objects from existing ones.  For example, fitting ellipsoids or creating projections along an axis.",
                        OBJECTS);
        public final static Category OBJECTS_RELATE = new Category("Relate",
                        "Modules creating relationships between objects.  Relationships can be one-to-many (parent-child) or one-to-one (partners).",
                        OBJECTS);
        public final static Category OBJECTS_RELATE_MERGE = new Category("Merge",
                        "Modules used for combining either different objects into one or combining objects from different collections into a single collection.",
                        OBJECTS_RELATE);
        public final static Category OBJECTS_TRANSFORM = new Category("Transform",
                        "Modules capable of updating coordinates of existing objects.  These operations can include hole filling and masking.",
                        OBJECTS);

        public final static Category SCRIPT = new Category("Script",
                        "Modules for running code directly witin a workflow.  Both single commands and full scripts (e.g. macro, Jython and Groovy) can be implemented.",
                        ROOT);

        public final static Category SYSTEM = new Category("System",
                        "Operations handling MIA properties such as memory and global variables.",
                        ROOT);

        public final static Category VISUALISATION = new Category("Visualisation",
                        "Modules primarily concerned with generating and displaying images.  These include adding overlay elements and plotting data.",
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
