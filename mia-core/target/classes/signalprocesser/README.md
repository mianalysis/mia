"Concave" hulls by Glenn Hudson and Matt Duckham

Source code downloaded from https://archive.md/l3Un5#selection-571.0-587.218 on 3rd November 2021.

- This software is Copyright (C) 2008 Glenn Hudson released under Gnu Public License (GPL). Under GPL you are free to use, modify, and redistribute the software. Please acknowledge Glenn Hudson and Matt Duckham as the source of this software if you do use or adapt the code in further research or other work. For full details of GPL see https://www.gnu.org/licenses/gpl-3.0.txt.
- This software comes with no warranty of any kind, expressed or implied.


The following description comes from the original webpage:

## "Concave" hulls
This page relates to work in 2005-2008 on the development of an algorithm for characterizing the shape of a set of points. The output of the algorithm, the chi-shape, differs from that of the classic alpha-shapes algorithm in that the shape generated is guaranteed to be a connected, simple polygon, a useful property in many situations.

The software below is free and open source. However, the algorithm has apparently now also been implemented as the SDO_GEOM.SDO_CONCAVEHULL_BOUNDARY operation in Oracle 11gR2. The documentation for that operation includes a very good summary of the chi-shape algorithm: "[This] function takes all coordinates from the input geometry, and uses them to compute Delaunay triangulations. But after that, it computes a convex hull, puts all boundary edges into a priority queue based on the lengths of these edges, and then removes edges one by one as long as the shape is still a single connected polygon (unless stopped by a specified length parameter value)."

Other similar functions, although apparently not related to the chi-shape, include the SDO_GEOM.SDO_CONCAVEHULL operation in Oracle 11gR2 and ST_CONCAVEHULL in PostGIS.

## Non-convex hulls software
The software on this page implements an algorithm for generating possibly non-convex simple polygons for sets of points in the plane. The algorithm aims to characterize the shape of set of points in the plane (hence "characteristic shapes"). Its behavior is governed by a single normalized length parameter. Adjusting the length parameter to its maximum (100) yields the convex hull. Adjusting the length parameter to its minimum (0) yields a uniquely defined maximally eroded shape.

After starting the software, you can manually add points or randomly generate points filling a variety of predefined shapes (letters and countries). Varying the length parameter will lead to the generation of the entire family of characteristic shapes for that set of points.

## More information
A paper with full details of the characteristic hulls algorithm is published in Pattern Recognition.

Duckham, M., Kulik, L., Worboys, M.F., Galton, A. (2008) Efficient generation of simple polygons for characterizing the shape of a set of points in the plane. Pattern Recognition v41, 3224-3236 [[pdf](https://archive.md/o/l3Un5/www.geosensor.net/papers/duckham08.PR.pdf), [doi](https://archive.md/o/l3Un5/dx.doi.org/10.1016/j.patcog.2008.03.023)].

The abstract of our forthcoming paper, "Efficient generation of simple polygons for characterizing the shape of a set of points in the plane", by Duckham, Kulik, Worboys, and Galton, is given below.

## Pattern Recognition paper abstract
"This paper presents a simple, flexible, and efficient algorithm for constructing a possibly non-convex, simple polygon that characterizes the shape of a set of input points in the plane, termed a characteristic shape. The algorithm is based on the Delaunay triangulation of the points. The shape produced by the algorithm is controlled by a single normalized parameter, which can be used to generate a finite, totally ordered family of related characteristic shapes, varying between the convex hull at one extreme and a uniquely defined shape with minimum area. An optimal O(n log n) algorithm for computing the shapes is presented. Characteristic shapes possess a number of desirable properties, and the paper includes an empirical investigation of the shapes produced by the algorithm. This investigation provides experimental evidence that with appropriate parameterization the algorithm is able to accurately characterize the shape of a wide range of different point distributions and densities. The experiments detail the effects of changing parameter values and provide an indication of some "good" parameter values to use in certain circumstances."

## Acknowledgments
The software was developed by Glenn Hudson while working with me as an RA. The characteristic shapes algorithm is collaborative work between Matt Duckham, Lars Kulik, Antony Galton, and Mike Worboys.