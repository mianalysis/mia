package io.github.mianalysis.mia.object.coordinates.volume.quadtree;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Stack;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetI;
import io.github.mianalysis.mia.object.coordinates.volume.PointCoordinates;

/**
 * Created by JDJFisher on 9/07/2019.
 */
public class Quadtree extends AbstractSet<Point<Integer>> {
    private QuadtreeNode root;
    private int rootSize;
    private int rootMinX;
    private int rootMinY;
    private int points;
    private int nodes;

    // default constructor
    public Quadtree() {
        root = new QuadtreeNode();
        rootSize = 1;
        rootMinX = 0;
        rootMinY = 0;
        points = 0;
        nodes = 1;
    }

    // copy constructor
    public Quadtree(Quadtree qt) {
        root = new QuadtreeNode(qt.root);
        rootSize = qt.rootSize;
        rootMinX = qt.rootMinX;
        rootMinY = qt.rootMinY;
        points = qt.points;
        nodes = qt.nodes;
    }

    // determine whether there is a point stored at the specified coordinates
    public boolean contains(Point<Integer> point) {
        return contains(point.getX(), point.getY());
    }

    public boolean contains(int x, int y) {
        // if the coordinates are out of the root nodes span return false
        if (x < rootMinX || x >= rootMinX + rootSize ||
                y < rootMinY || y >= rootMinY + rootSize)
            return false;

        return contains(root, x, y, rootSize, rootMinX, rootMinY);
    }

    private boolean contains(QuadtreeNode node, int x, int y, int size, int minX, int minY) {
        // recursively select the sub-node that contains the coordinates until a leaf
        // node is found
        if (node.isDivided()) {
            final int halfSize = size / 2;
            final int midX = minX + halfSize;
            final int midY = minY + halfSize;

            if (x < midX && y < midY) {
                return contains(node.nw, x, y, halfSize, minX, minY);
            } else if (x >= midX && y < midY) {
                return contains(node.ne, x, y, halfSize, midX, minY);
            } else if (x < midX && y >= midY) {
                return contains(node.sw, x, y, halfSize, minX, midY);
            } else {
                return contains(node.se, x, y, halfSize, midX, midY);
            }
        }

        // return the value of the leaf
        return node.coloured;
    }

    // add a point to the structure
    public boolean add(Point<Integer> point) {
        return add(point.getX(), point.getY());
    }

    public boolean add(int x, int y) {
        // while the coordinates are out of the root nodes span, increase the size of
        // the root appropriately
        while (x < rootMinX || y < rootMinY) {
            QuadtreeNode newRoot = new QuadtreeNode();
            newRoot.subDivide();
            newRoot.se = root;

            rootMinX -= rootSize;
            rootMinY -= rootSize;
            rootSize *= 2;
            root = newRoot;
        }

        while (x >= rootMinX + rootSize || y >= rootMinY + rootSize) {
            QuadtreeNode newRoot = new QuadtreeNode();
            newRoot.subDivide();
            newRoot.nw = root;

            rootSize *= 2;
            root = newRoot;
        }

        return set(x, y, true);
    }

    // remove a point from the structure
    public boolean remove(Point<Integer> point) {
        return remove(point.getX(), point.getY());
    }

    public boolean remove(int x, int y) {
        // if the coordinates are out of the root nodes span return false
        if (x < rootMinX || x >= rootMinX + rootSize ||
                y < rootMinY || y >= rootMinY + rootSize)
            return false;

        return set(x, y, false);
    }

    // set the point state for a given coordinate pair
    public boolean set(int x, int y, boolean b) {
        return set(root, x, y, b, rootSize, rootMinX, rootMinY);
    }

    private boolean set(QuadtreeNode node, int x, int y, boolean b, int size, int minX, int minY) {
        if (node.isLeaf()) {
            if (node.coloured == b)
                return false;

            if (size == 1) {
                node.coloured = b;
                points += b ? 1 : -1;
                return true;
            }

            node.subDivide();
            nodes += 4;
        }

        final int halfSize = size / 2;
        final int midX = minX + halfSize;
        final int midY = minY + halfSize;

        if (x < midX && y < midY) {
            return set(node.nw, x, y, b, halfSize, minX, minY);
        } else if (x >= midX && y < midY) {
            return set(node.ne, x, y, b, halfSize, midX, minY);
        } else if (x < midX && y >= midY) {
            return set(node.sw, x, y, b, halfSize, minX, midY);
        } else {
            return set(node.se, x, y, b, halfSize, midX, midY);
        }
    }

    // optimise the Quadtree by merging sub-nodes encoding a uniform value
    public void optimise() {
        optimiseNode(root);
    }

    private void optimiseNode(QuadtreeNode node) {
        if (node.isDivided()) {
            // attempt to optimise sub-nodes first
            optimiseNode(node.nw);
            optimiseNode(node.ne);
            optimiseNode(node.sw);
            optimiseNode(node.se);

            // if all the sub-nodes are equivalent, dispose of them
            if (node.nw.equals(node.ne) && node.ne.equals(node.sw) && node.sw.equals(node.se)) {
                node.coloured = node.nw.coloured;

                // destroy the redundant sub-nodes
                node.nw = node.ne = node.sw = node.se = null;
                nodes -= 4;
            }
        }
    }

    public CoordinateSetI getEdgePoints() {
        CoordinateSetI points = new PointCoordinates();

        getEdgePoints(root, points, rootSize, rootMinX, rootMinY);

        return points;
    }

    private void getEdgePoints(QuadtreeNode node, CoordinateSetI points, int size, int minX, int minY) {
        if (node.isDivided()) {
            final int halfSize = size / 2;
            final int midX = minX + halfSize;
            final int midY = minY + halfSize;

            getEdgePoints(node.nw, points, halfSize, minX, minY);
            getEdgePoints(node.ne, points, halfSize, midX, minY);
            getEdgePoints(node.sw, points, halfSize, minX, midY);
            getEdgePoints(node.se, points, halfSize, midX, midY);
        } else if (node.coloured) {
            final int maxX = minX + size - 1;
            final int maxY = minY + size - 1;

            for (int x = minX; x <= maxX; x++) {
                if (!contains(x, minY - 1)) {
                    points.add(new Point<>(x, minY, 0));
                }

                if (!contains(x, maxY + 1)) {
                    points.add(new Point<>(x, maxY, 0));
                }
            }

            for (int y = minY; y <= maxY; y++) {
                if (!contains(minX - 1, y)) {
                    points.add(new Point<>(minX, y, 0));
                }

                if (!contains(maxX + 1, y)) {
                    points.add(new Point<>(maxX, y, 0));
                }
            }
        }
    }

    public void getEdgePoints3D(CoordinateSetI points, @Nullable Quadtree above, @Nullable Quadtree below, int z) {
        getEdgePoints3D(root, points, above, below, z, rootSize, rootMinX, rootMinY);
    }

    private void getEdgePoints3D(QuadtreeNode node, CoordinateSetI points, Quadtree a, Quadtree b, int z, int size,
            int minX,
            int minY) {
        if (node.isDivided()) {
            final int halfSize = size / 2;
            final int midX = minX + halfSize;
            final int midY = minY + halfSize;

            getEdgePoints3D(node.nw, points, a, b, z, halfSize, minX, minY);
            getEdgePoints3D(node.ne, points, a, b, z, halfSize, midX, minY);
            getEdgePoints3D(node.sw, points, a, b, z, halfSize, minX, midY);
            getEdgePoints3D(node.se, points, a, b, z, halfSize, midX, midY);
        } else if (node.coloured) {
            final int maxX = minX + size - 1;
            final int maxY = minY + size - 1;

            if (a == null || b == null) {
                for (int y = minY; y <= maxY; y++) {
                    for (int x = minX; x <= maxX; x++) {
                        points.add(new Point<>(x, y, z));
                    }
                }
            } else {
                for (int x = minX; x <= maxX; x++) {
                    if (!contains(x, minY - 1) || !a.contains(x, maxY) || !b.contains(x, minY)) {
                        points.add(new Point<>(x, minY, z));
                    }

                    if (!contains(x, maxY + 1) || !a.contains(x, maxY) || !b.contains(x, maxY)) {
                        points.add(new Point<>(x, maxY, z));
                    }
                }

                for (int y = minY; y <= maxY; y++) {
                    if (!contains(minX - 1, y) || !a.contains(minX, y) || !b.contains(minX, y)) {
                        points.add(new Point<>(minX, y, z));
                    }

                    if (!contains(maxX + 1, y) || !a.contains(maxX, y) || !b.contains(maxX, y)) {
                        points.add(new Point<>(maxX, y, z));
                    }
                }
            }
        }
    }

    private void recountNodes() {
        nodes = 1;
        recountNodes(root);
    }

    private void recountNodes(QuadtreeNode node) {
        if (node.isDivided()) {
            nodes += 4;
            recountNodes(node.nw);
            recountNodes(node.ne);
            recountNodes(node.sw);
            recountNodes(node.se);
        }
    }

    private void recountPoints() {
        points = 0;
        recountPoints(root, rootSize);
    }

    private void recountPoints(QuadtreeNode node, int size) {
        if (node.isDivided()) {
            final int halfSize = size / 2;

            recountPoints(node.nw, halfSize);
            recountPoints(node.ne, halfSize);
            recountPoints(node.sw, halfSize);
            recountPoints(node.se, halfSize);
        } else if (node.coloured) {
            points += size * size;
        }
    }

    @Override
    public boolean isEmpty() {
        return points == 0;
    }

    @Override
    public void clear() {
        root = new QuadtreeNode();
        rootSize = 1;
        rootMinX = 0;
        rootMinY = 0;
        points = 0;
        nodes = 1;
    }

    @Override
    public int size() {
        return points;
    }

    public QuadtreeNode getRoot() {
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

    public int getPointCount() {
        return points;
    }

    public int getNodeCount() {
        return nodes;
    }

    @Override
    public Iterator<Point<Integer>> iterator() {
        return new QuadtreeIterator();
    }

    private class QuadtreeIterator implements Iterator<Point<Integer>> {
        private final Stack<QuadtreeNode> nodeStack;
        private final Stack<Integer> sizeStack;
        private final Stack<Integer> minXStack;
        private final Stack<Integer> minYStack;

        private int x, y;
        private int minX, minY;
        private int maxX, maxY;

        public QuadtreeIterator() {
            nodeStack = new Stack<>();
            sizeStack = new Stack<>();
            minXStack = new Stack<>();
            minYStack = new Stack<>();

            nodeStack.push(root);
            sizeStack.push(rootSize);
            minXStack.push(rootMinX);
            minYStack.push(rootMinY);

            maxX = maxY = Integer.MIN_VALUE;

            findNextColouredLeaf();
        }

        @Override
        public boolean hasNext() {
            return !nodeStack.empty() || y <= maxY;
        }

        @Override
        public Point<Integer> next() {
            Point<Integer> currentPoint = new Point<>(x, y, 0);

            findNextPoint();

            return currentPoint;
        }

        private void findNextPoint() {
            x++;

            if (x > maxX) {
                x = minX;
                y++;

                if (y > maxY) {
                    findNextColouredLeaf();
                }
            }
        }

        private void findNextColouredLeaf() {
            while (!nodeStack.empty()) {
                final QuadtreeNode node = nodeStack.pop();
                final int size = sizeStack.pop();
                minX = minXStack.pop();
                minY = minYStack.pop();

                if (node.isDivided()) {
                    final int halfSize = size / 2;
                    final int midX = minX + halfSize;
                    final int midY = minY + halfSize;

                    nodeStack.push(node.nw);
                    sizeStack.push(halfSize);
                    minXStack.push(minX);
                    minYStack.push(minY);

                    nodeStack.push(node.ne);
                    sizeStack.push(halfSize);
                    minXStack.push(midX);
                    minYStack.push(minY);

                    nodeStack.push(node.sw);
                    sizeStack.push(halfSize);
                    minXStack.push(minX);
                    minYStack.push(midY);

                    nodeStack.push(node.se);
                    sizeStack.push(halfSize);
                    minXStack.push(midX);
                    minYStack.push(midY);
                } else if (node.coloured) {
                    maxX = minX + size - 1;
                    maxY = minY + size - 1;

                    x = minX;
                    y = minY;

                    return;
                }
            }
        }
    }
}