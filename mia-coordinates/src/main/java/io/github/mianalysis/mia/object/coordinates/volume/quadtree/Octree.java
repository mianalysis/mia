package io.github.mianalysis.mia.object.coordinates.volume.quadtree;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Stack;

import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetI;
import io.github.mianalysis.mia.object.coordinates.volume.PointCoordinates;

/**
 * Created by JDJFisher on 19/07/2019.
 */
public class Octree extends AbstractSet<Point<Integer>> {
    private OctreeNode root;
    private int rootSize;
    private int rootMinX;
    private int rootMinY;
    private int rootMinZ;
    private int points;
    private int nodes;

    // default constructor
    public Octree() {
        root = new OctreeNode();
        rootSize = 1;
        rootMinX = 0;
        rootMinY = 0;
        rootMinZ = 0;
        points = 0;
        nodes = 1;
    }

    // copy constructor
    public Octree(Octree ot) {
        root = new OctreeNode(ot.root);
        rootSize = ot.rootSize;
        rootMinX = ot.rootMinX;
        rootMinY = ot.rootMinY;
        rootMinZ = ot.rootMinZ;
        points = ot.points;
        nodes = ot.nodes;
    }

    // determine whether there is a point stored at the specified coordinates
    public boolean contains(Point<Integer> point) {
        return contains(point.getX(), point.getY(), point.getZ());
    }

    public boolean contains(int x, int y, int z) {
        // if the coordinates are out of the root nodes span return false
        if (x < rootMinX || x >= rootMinX + rootSize || y < rootMinY || y >= rootMinY + rootSize || z < rootMinZ
                || z >= rootMinZ + rootSize)
            return false;

        return contains(root, x, y, z, rootSize, rootMinX, rootMinY, rootMinZ);
    }

    private boolean contains(OctreeNode node, int x, int y, int z, int size, int minX, int minY, int minZ) {
        // recursively select the sub-node that contains the coordinates until a leaf
        // node is found
        if (node.isDivided()) {
            final int halfSize = size / 2;
            final int midX = minX + halfSize;
            final int midY = minY + halfSize;
            final int midZ = minZ + halfSize;

            if (x < midX && y < midY && z < midZ) {
                return contains(node.lnw, x, y, z, halfSize, minX, minY, minZ);
            } else if (x >= midX && y < midY && z < midZ) {
                return contains(node.lne, x, y, z, halfSize, midX, minY, minZ);
            } else if (x < midX && y >= midY && z < midZ) {
                return contains(node.lsw, x, y, z, halfSize, minX, midY, minZ);
            } else if (x >= midX && y >= midY && z < midZ) {
                return contains(node.lse, x, y, z, halfSize, midX, midY, minZ);
            } else if (x < midX && y < midY && z >= midZ) {
                return contains(node.unw, x, y, z, halfSize, minX, minY, midZ);
            } else if (x >= midX && y < midY && z >= midZ) {
                return contains(node.une, x, y, z, halfSize, midX, minY, midZ);
            } else if (x < midX && y >= midY && z >= midZ) {
                return contains(node.usw, x, y, z, halfSize, minX, midY, midZ);
            } else {
                return contains(node.use, x, y, z, halfSize, midX, midY, midZ);
            }
        }

        // return the value of the leaf
        return node.coloured;
    }

    // add a point to the structure
    public boolean add(Point<Integer> point) {
        return add(point.getX(), point.getY(), point.getZ());
    }

    public boolean add(int x, int y, int z) {
        // while the coordinates are out of the root nodes span, increase the size of
        // the root appropriately
        while (x < rootMinX || y < rootMinY || z < rootMinZ) {
            OctreeNode newRoot = new OctreeNode();
            newRoot.subDivide();
            newRoot.use = root;

            rootMinX -= rootSize;
            rootMinY -= rootSize;
            rootMinZ -= rootSize;
            rootSize *= 2;
            root = newRoot;
        }

        while (x >= rootMinX + rootSize || y >= rootMinY + rootSize || z >= rootMinZ + rootSize) {
            OctreeNode newRoot = new OctreeNode();
            newRoot.subDivide();
            newRoot.lnw = root;

            rootSize *= 2;
            root = newRoot;
        }

        return set(x, y, z, true);
    }

    // remove a point from the structure
    public boolean remove(Point<Integer> point) {
        return remove(point.getX(), point.getY(), point.getZ());
    }

    public boolean remove(int x, int y, int z) {
        // if the coordinates are out of the root nodes span return false
        if (x < rootMinX || x >= rootMinX + rootSize || y < rootMinY || y >= rootMinY + rootSize || z < rootMinZ
                || z >= rootMinZ + rootSize)
            return false;

        return set(x, y, z, false);
    }

    // set the point state for a given coordinate pair
    public boolean set(int x, int y, int z, boolean b) {
        return set(root, x, y, z, b, rootSize, rootMinX, rootMinY, rootMinZ);
    }

    private boolean set(OctreeNode node, int x, int y, int z, boolean b, int size, int minX, int minY, int minZ) {
        if (node.isLeaf()) {
            if (node.coloured == b)
                return false;

            if (size == 1) {
                node.coloured = b;
                points += b ? 1 : -1;
                return true;
            }

            node.subDivide();
            nodes += 8;
        }

        final int halfSize = size / 2;
        final int midX = minX + halfSize;
        final int midY = minY + halfSize;
        final int midZ = minZ + halfSize;

        if (x < midX && y < midY && z < midZ) {
            return set(node.lnw, x, y, z, b, halfSize, minX, minY, minZ);
        } else if (x >= midX && y < midY && z < midZ) {
            return set(node.lne, x, y, z, b, halfSize, midX, minY, minZ);
        } else if (x < midX && y >= midY && z < midZ) {
            return set(node.lsw, x, y, z, b, halfSize, minX, midY, minZ);
        } else if (x >= midX && y >= midY && z < midZ) {
            return set(node.lse, x, y, z, b, halfSize, midX, midY, minZ);
        } else if (x < midX && y < midY && z >= midZ) {
            return set(node.unw, x, y, z, b, halfSize, minX, minY, midZ);
        } else if (x >= midX && y < midY && z >= midZ) {
            return set(node.une, x, y, z, b, halfSize, midX, minY, midZ);
        } else if (x < midX && y >= midY && z >= midZ) {
            return set(node.usw, x, y, z, b, halfSize, minX, midY, midZ);
        } else {
            return set(node.use, x, y, z, b, halfSize, midX, midY, midZ);
        }
    }

    // optimise the Octree by merging sub-nodes encoding a uniform value
    public void optimise() {
        optimise(root);
    }

    private void optimise(OctreeNode node) {
        if (node.isDivided()) {
            // attempt to optimise sub-nodes first
            optimise(node.lnw);
            optimise(node.lne);
            optimise(node.lsw);
            optimise(node.lse);
            optimise(node.unw);
            optimise(node.une);
            optimise(node.usw);
            optimise(node.use);

            // if all the sub-nodes are equivalent, dispose of them
            if (node.lnw.equals(node.lne) && node.lne.equals(node.lsw) && node.lsw.equals(node.lse)
                    && node.lse.equals(node.unw) && node.unw.equals(node.une) && node.une.equals(node.usw)
                    && node.usw.equals(node.use)) {
                node.coloured = node.unw.coloured;

                // destroy the redundant sub-nodes
                node.lnw = node.lne = node.lsw = node.lse = node.unw = node.une = node.usw = node.use = null;
                nodes -= 8;
            }
        }
    }

    public CoordinateSetI getEdgePoints(boolean is2D) {
        CoordinateSetI points = new PointCoordinates();

        getEdgePoints(root, points, is2D, rootSize, rootMinX, rootMinY, rootMinZ);

        return points;
    }

    private void getEdgePoints(OctreeNode node, CoordinateSetI points, boolean is2d, int size, int minX, int minY,
            int minZ) {
        if (node.isDivided()) {
            final int halfSize = size / 2;
            final int midX = minX + halfSize;
            final int midY = minY + halfSize;
            final int midZ = minZ + halfSize;

            getEdgePoints(node.lnw, points, is2d, halfSize, minX, minY, minZ);
            getEdgePoints(node.lne, points, is2d, halfSize, midX, minY, minZ);
            getEdgePoints(node.lsw, points, is2d, halfSize, minX, midY, minZ);
            getEdgePoints(node.lse, points, is2d, halfSize, midX, midY, minZ);

            if (is2d)
                return;

            getEdgePoints(node.unw, points, false, halfSize, minX, minY, midZ);
            getEdgePoints(node.une, points, false, halfSize, midX, minY, midZ);
            getEdgePoints(node.usw, points, false, halfSize, minX, midY, midZ);
            getEdgePoints(node.use, points, false, halfSize, midX, midY, midZ);
        } else if (node.coloured) {
            final int maxX = minX + size - 1;
            final int maxY = minY + size - 1;
            final int maxZ = minZ + size - 1;

            for (int z = minZ; z <= maxZ; z++) {
                if (!contains(minX - 1, minY, z) || !contains(minX, minY - 1, z)) {
                    points.add(new Point<>(minX, minY, z));
                }

                if (!contains(maxX + 1, minY, z) || !contains(maxX, minY - 1, z)) {
                    points.add(new Point<>(maxX, minY, z));
                }

                if (!contains(minX - 1, maxY, z) || !contains(minX, maxY + 1, z)) {
                    points.add(new Point<>(minX, maxY, z));
                }

                if (!contains(maxX + 1, maxY, z) || !contains(maxX, maxY + 1, z)) {
                    points.add(new Point<>(maxX, maxY, z));
                }
            }

            if (is2d)
                return;

            for (int x = minX; x <= maxX; x++) {
                if (!contains(x, minY - 1, minZ) || !contains(x, minY, minZ - 1)) {
                    points.add(new Point<>(x, minY, minZ));
                }

                if (!contains(x, maxY + 1, minZ) || !contains(x, maxY, minZ - 1)) {
                    points.add(new Point<>(x, maxY, minZ));
                }

                if (!contains(x, minY - 1, maxZ) || !contains(x, minY, maxZ + 1)) {
                    points.add(new Point<>(x, minY, maxZ));
                }

                if (!contains(x, maxY + 1, maxZ) || !contains(x, maxY, maxZ + 1)) {
                    points.add(new Point<>(x, maxY, maxZ));
                }
            }

            for (int y = minY; y <= maxY; y++) {
                if (!contains(minX - 1, y, minZ) || !contains(minX, y, minZ - 1)) {
                    points.add(new Point<>(minX, y, minZ));
                }

                if (!contains(maxX + 1, y, minZ) || !contains(maxX, y, minZ - 1)) {
                    points.add(new Point<>(maxX, y, minZ));
                }

                if (!contains(minX - 1, y, maxZ) || !contains(minX, y, maxZ + 1)) {
                    points.add(new Point<>(minX, y, maxZ));
                }

                if (!contains(maxX + 1, y, maxZ) || !contains(maxX, y, maxZ + 1)) {
                    points.add(new Point<>(maxX, y, maxZ));
                }
            }
        }
    }

    private void recountNodes() {
        nodes = 1;
        recountNodes(root);
    }

    private void recountNodes(OctreeNode node) {
        if (node.isDivided()) {
            nodes += 8;
            recountNodes(node.lnw);
            recountNodes(node.lne);
            recountNodes(node.lsw);
            recountNodes(node.lse);
            recountNodes(node.unw);
            recountNodes(node.une);
            recountNodes(node.usw);
            recountNodes(node.use);
        }
    }

    private void recountPoints() {
        points = 0;
        recountPoints(root, rootSize);
    }

    private void recountPoints(OctreeNode node, int size) {
        if (node.isDivided()) {
            final int halfSize = size / 2;

            recountPoints(node.lnw, halfSize);
            recountPoints(node.lne, halfSize);
            recountPoints(node.lsw, halfSize);
            recountPoints(node.lse, halfSize);
            recountPoints(node.unw, halfSize);
            recountPoints(node.une, halfSize);
            recountPoints(node.usw, halfSize);
            recountPoints(node.use, halfSize);
        } else if (node.coloured) {
            points += size * size * size;
        }
    }

    @Override
    public boolean isEmpty() {
        return points == 0;
    }

    @Override
    public void clear() {
        root = new OctreeNode();
        rootSize = 1;
        rootMinX = 0;
        rootMinY = 0;
        rootMinZ = 0;
        points = 0;
        nodes = 1;
    }

    @Override
    public int size() {
        return points;
    }

    public OctreeNode getRoot() {
        return root;
    }

    public int getRootSize() {
        return rootSize;
    }

    public int getRootMinX() {
        return rootMinX;
    }

    public int getRootMinY() {
        return rootMinY;
    }

    public int getRootMinZ() {
        return rootMinZ;
    }

    public int getPointCount() {
        return points;
    }

    public int getNodeCount() {
        return nodes;
    }

    @Override
    public Iterator<Point<Integer>> iterator() {
        return new OctreeIterator();
    }

    private class OctreeIterator implements Iterator<Point<Integer>> {
        private final Stack<OctreeNode> nodeStack;
        private final Stack<Integer> sizeStack;
        private final Stack<Integer> minXStack;
        private final Stack<Integer> minYStack;
        private final Stack<Integer> minZStack;

        private int x, y, z;
        private int minX, minY, minZ;
        private int maxX, maxY, maxZ;

        public OctreeIterator() {
            nodeStack = new Stack<>();
            sizeStack = new Stack<>();
            minXStack = new Stack<>();
            minYStack = new Stack<>();
            minZStack = new Stack<>();

            nodeStack.push(root);
            sizeStack.push(rootSize);
            minXStack.push(rootMinX);
            minYStack.push(rootMinY);
            minZStack.push(rootMinZ);

            maxX = maxY = maxZ = Integer.MIN_VALUE;

            findNextColouredLeaf();

        }

        @Override
        public boolean hasNext() {
            return !nodeStack.empty() || z <= maxZ;
        }

        @Override
        public Point<Integer> next() {
            Point<Integer> currentPoint = new Point<>(x, y, z);

            findNextPoint();

            return currentPoint;
            
        }

        private void findNextPoint() {
            x++;

            if (x > maxX) {
                x = minX;
                y++;

                if (y > maxY) {
                    x = minX;
                    y = minY;
                    z++;

                    if (z > maxZ) {
                        findNextColouredLeaf();
                    }
                }
            }
        }

        private void findNextColouredLeaf() {
            while (!nodeStack.empty()) {
                final OctreeNode node = nodeStack.pop();
                final int size = sizeStack.pop();
                minX = minXStack.pop();
                minY = minYStack.pop();
                minZ = minZStack.pop();

                if (node.isDivided()) {
                    final int halfSize = size / 2;
                    final int midX = minX + halfSize;
                    final int midY = minY + halfSize;
                    final int midZ = minZ + halfSize;

                    nodeStack.push(node.lnw);
                    sizeStack.push(halfSize);
                    minXStack.push(minX);
                    minYStack.push(minY);
                    minZStack.push(minZ);

                    nodeStack.push(node.lne);
                    sizeStack.push(halfSize);
                    minXStack.push(midX);
                    minYStack.push(minY);
                    minZStack.push(minZ);

                    nodeStack.push(node.lsw);
                    sizeStack.push(halfSize);
                    minXStack.push(minX);
                    minYStack.push(midY);
                    minZStack.push(minZ);

                    nodeStack.push(node.lse);
                    sizeStack.push(halfSize);
                    minXStack.push(midX);
                    minYStack.push(midY);
                    minZStack.push(minZ);

                    nodeStack.push(node.unw);
                    sizeStack.push(halfSize);
                    minXStack.push(minX);
                    minYStack.push(minY);
                    minZStack.push(midZ);

                    nodeStack.push(node.une);
                    sizeStack.push(halfSize);
                    minXStack.push(midX);
                    minYStack.push(minY);
                    minZStack.push(midZ);

                    nodeStack.push(node.usw);
                    sizeStack.push(halfSize);
                    minXStack.push(minX);
                    minYStack.push(midY);
                    minZStack.push(midZ);

                    nodeStack.push(node.use);
                    sizeStack.push(halfSize);
                    minXStack.push(midX);
                    minYStack.push(midY);
                    minZStack.push(midZ);
                } else if (node.coloured) {
                    maxX = minX + size - 1;
                    maxY = minY + size - 1;
                    maxZ = minZ + size - 1;

                    x = minX;
                    y = minY;
                    z = minZ;

                    return;
                }
            }
        }
    }
}