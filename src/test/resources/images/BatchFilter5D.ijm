mainWin = getTitle();

for (i=0;i<4;i++) {
	selectWindow(mainWin);
	run("Make Substack...", "channels=1-2 slices=1-12 frames="+(i+1));
	rename((i+1));
	run("Mean 3D...", "x=2 y=2 z=2");
}

run("Concatenate...", "  title=[Concatenated Stacks] image1=1 image2=2 image3=3 image4=4 image5=[-- None --]");
run("Stack to Hyperstack...", "order=xyczt(default) channels=2 slices=12 frames=4 display=Color");
run("Properties...", "channels=2 slices=12 frames=4 unit=µm pixel_width=0.02 pixel_height=0.02 voxel_depth=0.1");
