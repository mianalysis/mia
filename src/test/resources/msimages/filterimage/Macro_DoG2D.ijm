im1 = getTitle();

run("32-bit");

run("Duplicate...", "duplicate");
im2 = getTitle();
run("Gaussian Blur...", "sigma=4.8 stack");

selectWindow(im1);
run("Gaussian Blur...", "sigma=3 stack");

imageCalculator("Subtract create stack", im1,im2);
