package io.github.mianalysis.mia.object.coordinates;

import ij.gui.PointRoi;

public class PointPair {
        private PointRoi p1;
        private PointRoi p2;
        private int ID;

        public PointPair(PointRoi p1, PointRoi p2, int ID) {
            this.p1 = p1;
            this.p2 = p2;
            this.ID = ID;
        }

        public PointRoi getPoint1() {
            return p1;
        }

        public PointRoi getPoint2() {
            return p2;
        }

        public int getID() {
            return ID;
        }

        @Override
        public String toString() {
            return "Pair "+ID;
        }
    }