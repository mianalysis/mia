// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package io.github.mianalysis.mia.thirdparty;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import ABSnake.Point2d;
import ABSnake.SnakeConfig;
import ij.IJ;
import ij.Prefs;
import ij.gui.Line;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import mcib3d.image3d.processing.CannyDeriche1D;

public class ABSnakeMod {
   Point2d[] points;
   Point2d[] normale;
   Point2d[] deplace;
   double dataDistance;
   double[] lambda;
   int[] etat;
   int NPT;
   int NMAX = 50000;
   int block;
   int elimination;
   int ARRET;
   boolean closed;
   SnakeConfig config;
   ImageProcessor gradImage;
   ImageProcessor originalImage;
   double previousAlpha = (double)0.0F;

   public ABSnakeMod() {
   }

   public void kill() {
      this.points = null;
      this.normale = null;
      this.deplace = null;
      this.lambda = null;
      this.etat = null;
      System.gc();
   }

   public void killImages() {
      this.originalImage = null;
      this.gradImage = null;
      System.gc();
   }

   public void setConfig(SnakeConfig sc) {
      this.config = sc;
   }

   public int getNbPoints() {
      return this.NPT;
   }

   public Point2d getPoint(int i) {
      return this.points[i];
   }

   public Point2d[] getPoints() {
      return this.points;
   }

   public SnakeConfig getConfig() {
      return this.config;
   }

   public double[] getLambda() {
      return this.lambda;
   }

   public Point2d[] getDisplacement() {
      return this.deplace;
   }

   public boolean closed() {
      return this.closed;
   }

   public void setOriginalImage(ImageProcessor originalImage) {
      this.originalImage = originalImage;
   }

   public void DrawSnake(ImageProcessor A, Color col, int linewidth) {
      A.setColor(col);
      A.setLineWidth(linewidth);

      for(int i = 0; i < this.NPT - 1; ++i) {
         int x = (int)this.points[i].x;
         int y = (int)this.points[i].y;
         int xx = (int)this.points[i + 1].x;
         int yy = (int)this.points[i + 1].y;
         A.drawLine(x, y, xx, yy);
      }

      if (this.closed()) {
         int x = (int)this.points[this.NPT - 1].x;
         int y = (int)this.points[this.NPT - 1].y;
         int xx = (int)this.points[0].x;
         int yy = (int)this.points[0].y;
         A.drawLine(x, y, xx, yy);
      }

   }

   public void EcritFreeD(int nb) {
      try {
         File fichier = new File("freed" + (nb + 1) + ".txt");
         FileWriter fw = new FileWriter(fichier);
         BufferedWriter bw = new BufferedWriter(fw);
         bw.write("MODELITEM cervo \"section " + (nb + 1) + "\";");
         bw.write("\nMODELITEMDATA  \n");

         for(int i = 0; i < this.NPT; ++i) {
            bw.write("" + (int)this.points[i].x + "," + (int)this.points[i].y + "  \n");
         }

         bw.write(";\n");
         bw.close();
         fw.close();
      } catch (IOException var6) {
      }

   }

   public void writeCoordinates(String nom, int nb, double resXY) {
      NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
      nf.setMaximumFractionDigits(3);

      try {
         File fichier = new File(nom + nb + ".txt");
         FileWriter fw = new FileWriter(fichier);
         BufferedWriter bw = new BufferedWriter(fw);
         bw.write("nb\tX\tY\tZ\tXcal\tYcal\n");

         for(int i = 0; i < this.NPT; ++i) {
            bw.write(i + "\t" + nf.format(this.points[i].x) + "\t" + nf.format(this.points[i].y) + "\t" + nb + "\t" + nf.format(this.points[i].x * resXY) + "\t" + nf.format(this.points[i].y) + "\n");
         }

         bw.close();
         fw.close();
      } catch (IOException var10) {
      }

   }

   public PolygonRoi createRoi() {
      int[] xx = new int[this.NPT];
      int[] yy = new int[this.NPT];

      for(int i = 0; i < this.NPT; ++i) {
         xx[i] = (int)this.points[i].x;
         yy[i] = (int)this.points[i].y;
      }

      PolygonRoi rr = new PolygonRoi(xx, yy, this.NPT - 1, 3);
      return rr;
   }

   public void Init(Roi R) {
      int i = 1;
      this.NPT = 0;
      this.points = new Point2d[this.NMAX];
      this.normale = new Point2d[this.NMAX];
      this.deplace = new Point2d[this.NMAX];
      this.dataDistance = (double)0.0F;
      this.etat = new int[this.NMAX];
      this.lambda = new double[this.NMAX];

      for(int var20 = 0; var20 < this.NMAX; ++var20) {
         this.points[var20] = new Point2d();
         this.normale[var20] = new Point2d();
         this.deplace[var20] = new Point2d();
      }

      if (R.getType() != 1 && R.getType() != 0) {
         if (R.getType() == 5) {
            this.closed = false;
            Line l = (Line)R;
            double Rx = (double)(l.x2 - l.x1);
            double Ry = (double)(l.y2 - l.y1);
            double a = Math.sqrt(Rx * Rx + Ry * Ry);
            Rx /= a;
            Ry /= a;
            int ind = 0;

            for(int var22 = 0; (double)var22 <= l.getLength(); ++var22) {
               this.points[ind].x = (double)l.x1 + Rx * (double)var22;
               this.points[ind].y = (double)l.y1 + Ry * (double)var22;
               this.etat[ind] = 0;
               ++ind;
            }

            this.NPT = ind;
         } else if (R.getType() != 3 && R.getType() != 2) {
            if (R.getType() == 4) {
               this.closed = true;
               Polygon polygon = R.getPolygon();
               PolygonRoi p = new PolygonRoi(polygon, 2);
               Rectangle rectBound = p.getBounds();
               int NBPT = p.getNCoordinates();
               int[] pointsX = p.getXCoordinates();
               int[] pointsY = p.getYCoordinates();

               for(int var24 = 0; var24 < NBPT; ++var24) {
                  this.points[var24].x = (double)(pointsX[var24] + rectBound.x);
                  this.points[var24].y = (double)(pointsY[var24] + rectBound.y);
               }

               this.NPT = NBPT;
               if (R.getType() == 2) {
                  this.resample(true);
               }
            } else {
               IJ.showStatus("Selection type not supported");
            }
         } else {
            this.closed = true;
            PolygonRoi p = (PolygonRoi)R;
            Rectangle rectBound = p.getBounds();
            int NBPT = p.getNCoordinates();
            int[] pointsX = p.getXCoordinates();
            int[] pointsY = p.getYCoordinates();

            for(int var23 = 0; var23 < NBPT; ++var23) {
               this.points[var23].x = (double)(pointsX[var23] + rectBound.x);
               this.points[var23].y = (double)(pointsY[var23] + rectBound.y);
            }

            this.NPT = NBPT;
            if (R.getType() == 2) {
               this.resample(true);
            }
         }
      } else {
         this.closed = true;
         Rectangle Rect = R.getBounds();
         int xc = Rect.x + Rect.width / 2;
         int yc = Rect.y + Rect.height / 2;
         double Rx = (double)Rect.width / (double)2.0F;
         double Ry = (double)Rect.height / (double)2.0F;
         double theta = (double)4.0F / (Rx + Ry);
         i = 0;

         for(double a = (Math.PI * 2D); a > (double)0.0F; a -= theta) {
            this.points[i].x = (double)((int)((double)xc + Rx * Math.cos(a)));
            this.points[i].y = (double)((int)((double)yc + Ry * Math.sin(a)));
            this.etat[i] = 0;
            ++i;
         }

         this.NPT = i;
      }

      this.block = 0;
      this.elimination = 0;
      this.ARRET = 0;
   }

   void resample(boolean init) {
      Point2d[] temp = new Point2d[this.NMAX];
      Point2d Ta = new Point2d();
      double Dtot = (double)0.0F;
      double Dmin = (double)1000.0F;
      double Dmax = (double)0.0F;

      for(int i = 1; i < this.NPT; ++i) {
         double Di = this.distance(i, i - 1);
         Dtot += Di;
         if (Di < Dmin) {
            Dmin = Di;
         }

         if (Di > Dmax) {
            Dmax = Di;
         }
      }

      if (Dmax / Dmin > (double)3.0F || init) {
         double Dmoyg = (double)1.0F;
         temp[0] = new Point2d();
         temp[0].x = this.points[0].x;
         temp[0].y = this.points[0].y;
         int var29 = 1;
         int ii = 1;
         temp[ii] = new Point2d();

         while(var29 < this.NPT) {
            double Dmoy = Dmoyg;
            double DD = this.distance(var29, var29 - 1);
            if (DD > Dmoyg) {
               int aj = (int)(DD / Dmoyg);
               Ta.x = this.points[var29].x - this.points[var29 - 1].x;
               Ta.y = this.points[var29].y - this.points[var29 - 1].y;
               double normtan = Math.sqrt(Ta.x * Ta.x + Ta.y * Ta.y);
               Ta.x /= normtan;
               Ta.y /= normtan;

               for(int k = 1; k <= aj; ++k) {
                  temp[ii].x = this.points[var29 - 1].x + (double)k * Dmoy * Ta.x;
                  temp[ii].y = this.points[var29 - 1].y + (double)k * Dmoy * Ta.y;
                  ++ii;
                  temp[ii] = new Point2d();
               }
            }

            ++var29;
            if (DD <= Dmoy && var29 < this.NPT - 1) {
               int j = var29 - 1;

               for(double D = (double)0.0F; D < Dmoy && j < this.NPT - 1; ++j) {
                  D += this.distance(j, j + 1);
               }

               temp[ii].x = this.points[j].x;
               temp[ii].y = this.points[j].y;
               ++ii;
               temp[ii] = new Point2d();
               var29 = j + 1;
            }

            if (var29 == this.NPT - 1) {
               var29 = this.NPT;
            }
         }

         temp[ii].x = this.points[this.NPT - 1].x;
         temp[ii].y = this.points[this.NPT - 1].y;
         this.NPT = ii + 1;

         for(int var30 = 0; var30 < this.NPT; ++var30) {
            this.points[var30].x = temp[var30].x;
            this.points[var30].y = temp[var30].y;
         }
      }

   }

   public void calculus(int deb, int fin) {
      double omega = 1.8;
      Point2d bi = new Point2d();
      Point2d temp = new Point2d();
      Point2d debtemp = new Point2d();
      debtemp.x = this.points[deb].x;
      debtemp.y = this.points[deb].y;

      for(int i = deb; i < fin; ++i) {
         bi.x = this.points[i].x + this.deplace[i].x;
         bi.y = this.points[i].y + this.deplace[i].y;
         double gi = -this.lambda[i];
         double di = -this.lambda[i + 1];
         double mi = this.lambda[i] + this.lambda[i + 1] + (double)1.0F;
         if (i > deb) {
            temp.x = mi * this.points[i].x + omega * (-gi * this.points[i - 1].x - mi * this.points[i].x - di * this.points[i + 1].x + bi.x);
            temp.y = mi * this.points[i].y + omega * (-gi * this.points[i - 1].y - mi * this.points[i].y - di * this.points[i + 1].y + bi.y);
         }

         if (i == deb && this.closed) {
            temp.x = mi * this.points[i].x + omega * (-gi * this.points[fin].x - mi * this.points[i].x - di * this.points[i + 1].x + bi.x);
            temp.y = mi * this.points[i].y + omega * (-gi * this.points[fin].y - mi * this.points[i].y - di * this.points[i + 1].y + bi.y);
         }

         if (i == deb && !this.closed) {
            temp.x = this.points[deb].x * mi;
            temp.y = this.points[deb].y * mi;
         }

         this.points[i].x = temp.x / mi;
         this.points[i].y = temp.y / mi;
      }

      if (this.closed) {
         bi.x = this.points[fin].x + this.deplace[fin].x;
         bi.y = this.points[fin].y + this.deplace[fin].y;
         double gi = -this.lambda[fin];
         double di = -this.lambda[deb];
         double mi = this.lambda[fin] + this.lambda[deb] + (double)1.0F;
         temp.x = mi * this.points[fin].x + omega * (-gi * this.points[fin - 1].x - mi * this.points[fin].x - di * debtemp.x + bi.x);
         temp.y = mi * this.points[fin].y + omega * (-gi * this.points[fin - 1].y - mi * this.points[fin].y - di * debtemp.y + bi.y);
         this.points[fin].x = temp.x / mi;
         this.points[fin].y = temp.y / mi;
      }

   }

   public double compute_displacements() {
      double som = (double)0.0F;
      double seuil = this.config.getGradThreshold();
      double DivForce = this.config.getMaxDisplacement();
      Point2d displ = new Point2d();
      som = (double)0.0F;

      for(int i = 0; i < this.NPT; ++i) {
         displ.x = (double)0.0F;
         displ.y = (double)0.0F;
         displ = this.compute_displ(i, seuil, (double)1000.0F, (double)1000.0F, 0);
         double force = Math.sqrt(displ.x * displ.x + displ.y * displ.y);
         if (force > DivForce) {
            this.deplace[i].x = DivForce * (displ.x / force);
            this.deplace[i].y = DivForce * (displ.y / force);
         } else {
            this.deplace[i].x = displ.x;
            this.deplace[i].y = displ.y;
         }

         force = Math.sqrt(this.deplace[i].x * this.deplace[i].x + this.deplace[i].y * this.deplace[i].y);
         som += force;
      }

      return som;
   }

   public void computeGrad(ImageProcessor image) {
      if (this.previousAlpha != this.config.getAlpha()) {
         this.gradImage = this.grad2d_deriche(image, this.config.getAlpha());
         this.previousAlpha = this.config.getAlpha();
      }

   }

   Point2d compute_displ1D(int num, double Edge_Threshold, double dist_plus, double dist_minus, int dir) {
      double scaleint = this.config.getMaxSearch();
      double crp = Double.NaN;
      double crm = Double.NaN;
      int scale = 10;
      double[] image_line = new double[(int)((double)(2 * scale) * scaleint + (double)1.0F)];
      Point2d pos = this.points[num];
      Point2d norm = this.normale[num];
      Point2d displacement = new Point2d();
      int index = 0;
      double step = (double)1.0F / (double)scale;
      double deb = -scaleint;

      for(double ii = deb; ii < scaleint; ii += step) {
         double iy = pos.y + norm.y * ii;
         double ix = pos.x + norm.x * ii;
         if (ix < (double)0.0F) {
            ix = (double)0.0F;
         }

         if (iy < (double)0.0F) {
            iy = (double)0.0F;
         }

         if (ix >= (double)this.originalImage.getWidth()) {
            ix = (double)(this.originalImage.getWidth() - 1);
         }

         if (iy >= (double)this.originalImage.getHeight()) {
            iy = (double)(this.originalImage.getHeight() - 1);
         }

         image_line[index] = this.originalImage.getInterpolatedPixel(ix, iy);
         ++index;
      }

      CannyDeriche1D canny = new CannyDeriche1D(image_line, this.config.getAlpha());
      image_line = canny.getCannyDeriche();
      double deplus = Double.POSITIVE_INFINITY;
      double demoins = Double.NEGATIVE_INFINITY;
      boolean edge_found = false;

      for(int var49 = 1; (double)var49 < (double)(2 * scale) * scaleint - (double)1.0F; ++var49) {
         if (image_line[var49] >= Edge_Threshold && image_line[var49] >= image_line[var49 - 1] && image_line[var49] >= image_line[var49 + 1]) {
            double Dist = (double)var49 * step + deb;
            if (Dist < (double)0.0F && Dist > demoins) {
               demoins = Dist;
               edge_found = true;
            }

            if (Dist >= (double)0.0F && Dist < deplus) {
               deplus = Dist;
               edge_found = true;
            }
         }
      }

      this.etat[num] = 0;
      if (!edge_found) {
         displacement.x = (double)0.0F;
         displacement.y = (double)0.0F;
         return displacement;
      } else {
         if (deplus > dist_plus) {
            deplus = (double)2.0F * scaleint;
         }

         if (demoins < -dist_minus) {
            demoins = (double)-2.0F * scaleint;
         }

         if (Double.isInfinite(deplus) && Double.isInfinite(demoins)) {
            displacement.x = (double)0.0F;
            displacement.y = (double)0.0F;
            return displacement;
         } else {
            if (-demoins < deplus) {
               displacement.x = norm.x * demoins;
               displacement.y = norm.y * demoins;
               int direction = -1;
            } else {
               displacement.x = norm.x * deplus;
               displacement.y = norm.y * deplus;
               int direction = 1;
            }

            return displacement;
         }
      }
   }

   Point2d compute_displ(int num, double Edge_Threshold, double dist_plus, double dist_minus, int dir) {
      double scaleint = this.config.getMaxSearch();
      double crp = Double.NaN;
      double crm = Double.NaN;
      int scale = 10;
      double[] image_line = new double[(int)((double)(2 * scale) * scaleint + (double)1.0F)];
      Point2d pos = this.points[num];
      Point2d norm = this.normale[num];
      Point2d displacement = new Point2d();
      int index = 0;
      double step = (double)1.0F / (double)scale;
      double deb = -scaleint;

      for(double ii = deb; ii < scaleint; ii += step) {
         double iy = pos.y + norm.y * ii;
         double ix = pos.x + norm.x * ii;
         if (ix < (double)0.0F) {
            ix = (double)0.0F;
         }

         if (iy < (double)0.0F) {
            iy = (double)0.0F;
         }

         if (ix >= (double)this.gradImage.getWidth()) {
            ix = (double)(this.gradImage.getWidth() - 1);
         }

         if (iy >= (double)this.gradImage.getHeight()) {
            iy = (double)(this.gradImage.getHeight() - 1);
         }

         image_line[index] = this.gradImage.getInterpolatedPixel(ix, iy);
         ++index;
      }

      for(int i = 0; i < this.NPT - 1; ++i) {
         if (i != num && i != num - 1) {
            double bden = -norm.x * this.points[i + 1].y + norm.x * this.points[i].y + norm.y * this.points[i + 1].x - norm.y * this.points[i].x;
            double bnum = -norm.x * pos.y + norm.x * this.points[i].y + norm.y * pos.x - norm.y * this.points[i].x;
            double bres;
            if (bden != (double)0.0F) {
               bres = bnum / bden;
            } else {
               bres = (double)5.0F;
            }

            if (bres >= (double)0.0F && bres <= (double)1.0F) {
               double ares = (double)((float)(-(-this.points[i + 1].y * pos.x + this.points[i + 1].y * this.points[i].x + this.points[i].y * pos.x + pos.y * this.points[i + 1].x - pos.y * this.points[i].x - this.points[i].y * this.points[i + 1].x) / (-norm.x * this.points[i + 1].y + norm.x * this.points[i].y + norm.y * this.points[i + 1].x - norm.y * this.points[i].x)));
               if (ares > (double)0.0F && ares < crp) {
                  crp = ares;
               }

               if (ares < (double)0.0F && ares > crm) {
                  crm = ares;
               }
            }
         }
      }

      double coeff_crossing = 0.9;
      double var10000 = crp * coeff_crossing;
      var10000 = crm * coeff_crossing;
      double deplus = Double.POSITIVE_INFINITY;
      double demoins = Double.NEGATIVE_INFINITY;
      boolean edge_found = false;

      for(int var49 = 1; (double)var49 < (double)(2 * scale) * scaleint - (double)1.0F; ++var49) {
         if (image_line[var49] >= Edge_Threshold && image_line[var49] >= image_line[var49 - 1] && image_line[var49] >= image_line[var49 + 1]) {
            double Dist = (double)var49 * step + deb;
            if (Dist < (double)0.0F && Dist > demoins) {
               demoins = Dist;
               edge_found = true;
            }

            if (Dist >= (double)0.0F && Dist < deplus) {
               deplus = Dist;
               edge_found = true;
            }
         }
      }

      this.etat[num] = 0;
      if (!edge_found) {
         displacement.x = (double)0.0F;
         displacement.y = (double)0.0F;
         return displacement;
      } else {
         if (deplus > dist_plus) {
            deplus = (double)2.0F * scaleint;
         }

         if (demoins < -dist_minus) {
            demoins = (double)-2.0F * scaleint;
         }

         if (Double.isInfinite(deplus) && Double.isInfinite(demoins)) {
            displacement.x = (double)0.0F;
            displacement.y = (double)0.0F;
            return displacement;
         } else {
            if (-demoins < deplus) {
               displacement.x = norm.x * demoins;
               displacement.y = norm.y * demoins;
               int direction = -1;
            } else {
               displacement.x = norm.x * deplus;
               displacement.y = norm.y * deplus;
               int direction = 1;
            }

            return displacement;
         }
      }
   }

   public void compute_normales() {
      for(int i = 0; i < this.NPT; ++i) {
         this.normale[i] = this.compute_normale(i);
      }

   }

   public void compute_lambdas() {
      double maxforce = (double)0.0F;
      double minr = this.config.getRegMin();
      double maxr = this.config.getRegMax();

      for(int i = 0; i < this.NPT; ++i) {
         double force = Math.sqrt(this.deplace[i].x * this.deplace[i].x + this.deplace[i].y * this.deplace[i].y);
         if (force > maxforce) {
            maxforce = force;
         }
      }

      for(int i = 0; i < this.NPT; ++i) {
         double force = Math.sqrt(this.deplace[i].x * this.deplace[i].x + this.deplace[i].y * this.deplace[i].y);
         this.lambda[i] = maxr / ((double)1.0F + (maxr - minr) / minr * (force / maxforce));
      }

   }

   Point2d compute_normale(int np) {
      Point2d tan = new Point2d();
      Point2d norma = new Point2d();
      if (np == 0) {
         if (this.closed) {
            tan.x = this.points[1].x - this.points[this.NPT - 1].x;
            tan.y = this.points[1].y - this.points[this.NPT - 1].y;
         } else {
            tan.x = this.points[1].x - this.points[0].x;
            tan.y = this.points[1].y - this.points[0].y;
         }
      }

      if (np == this.NPT - 1) {
         if (this.closed) {
            tan.x = this.points[0].x - this.points[this.NPT - 2].x;
            tan.y = this.points[0].y - this.points[this.NPT - 2].y;
         } else {
            tan.x = this.points[this.NPT - 1].x - this.points[this.NPT - 2].x;
            tan.y = this.points[this.NPT - 1].y - this.points[this.NPT - 2].y;
         }
      }

      if (np > 0 && np < this.NPT - 1) {
         tan.x = this.points[np + 1].x - this.points[np - 1].x;
         tan.y = this.points[np + 1].y - this.points[np - 1].y;
      }

      double normtan = Math.sqrt(tan.x * tan.x + tan.y * tan.y);
      if (normtan > (double)0.0F) {
         tan.x /= normtan;
         tan.y /= normtan;
         norma.x = -tan.y;
         norma.y = tan.x;
      }

      return norma;
   }

   void destroysnake() {
      Point2d[] temp = new Point2d[this.NPT];
      Point2d[] fo = new Point2d[this.NPT];
      double[] lan = new double[this.NPT];
      int[] state = new int[this.NPT];
      int j = 0;

      for(int i = 0; i < this.NPT; ++i) {
         if (this.etat[i] != 1) {
            temp[j] = new Point2d();
            temp[j].x = this.points[i].x;
            temp[j].y = this.points[i].y;
            state[j] = this.etat[i];
            fo[j] = new Point2d();
            fo[j].x = this.deplace[i].x;
            fo[j].y = this.deplace[i].y;
            lan[j] = this.lambda[i];
            ++j;
         }
      }

      this.NPT = j;

      for(int var7 = 0; var7 < this.NPT; ++var7) {
         this.points[var7].x = temp[var7].x;
         this.points[var7].y = temp[var7].y;
         this.etat[var7] = state[var7];
         this.deplace[var7].x = fo[var7].x;
         this.deplace[var7].y = fo[var7].y;
         this.lambda[var7] = lan[var7];
      }

   }

   double distance(int a, int b) {
      return Math.sqrt(Math.pow(this.points[a].x - this.points[b].x, (double)2.0F) + Math.pow(this.points[a].y - this.points[b].y, (double)2.0F));
   }

   void new_positions() {
      this.calculus(0, this.NPT - 1);
   }

   private ImageProcessor grad2d_deriche(ImageProcessor iDep, double alphaD) {
      ImageProcessor iGrad = new ByteProcessor(iDep.getWidth(), iDep.getHeight());
      float[] nf_grx = null;
      float[] nf_gry = null;
      float[] a1 = null;
      float[] a2 = null;
      float[] a3 = null;
      float[] a4 = null;
      byte[] result_array = null;
      int icolonnes = 0;
      int icoll = 0;
      int lignes = iDep.getHeight();
      int colonnes = iDep.getWidth();
      int nmem = lignes * colonnes;
      int lig_1 = lignes - 1;
      int lig_2 = lignes - 2;
      int lig_3 = lignes - 3;
      int col_1 = colonnes - 1;
      int col_2 = colonnes - 2;
      int col_3 = colonnes - 3;
      nf_grx = new float[nmem];
      nf_gry = new float[nmem];
      a1 = new float[nmem];
      a2 = new float[nmem];
      a3 = new float[nmem];
      a4 = new float[nmem];
      float ad1 = (float)(-Math.exp(-alphaD));
      float ad2 = 0.0F;
      float an1 = 1.0F;
      float an2 = 0.0F;
      float an3 = (float)Math.exp(-alphaD);
      float an4 = 0.0F;
      float an11 = 1.0F;

      for(int i = 0; i < lignes; ++i) {
         for(int j = 0; j < colonnes; ++j) {
            a1[i * colonnes + j] = iDep.getPixelValue(j, i);
         }
      }

      for(int var67 = 0; var67 < lignes; ++var67) {
         icolonnes = var67 * colonnes;
         int icol_1 = icolonnes - 1;
         int icol_2 = icolonnes - 2;
         a2[icolonnes] = an1 * a1[icolonnes];
         a2[icolonnes + 1] = an1 * a1[icolonnes + 1] + an2 * a1[icolonnes] - ad1 * a2[icolonnes];

         for(int j = 2; j < colonnes; ++j) {
            a2[icolonnes + j] = an1 * a1[icolonnes + j] + an2 * a1[icol_1 + j] - ad1 * a2[icol_1 + j] - ad2 * a2[icol_2 + j];
         }
      }

      for(int var68 = 0; var68 < lignes; ++var68) {
         icolonnes = var68 * colonnes;
         int icol_1 = icolonnes + 1;
         int icol_2 = icolonnes + 2;
         a3[icolonnes + col_1] = 0.0F;
         a3[icolonnes + col_2] = an3 * a1[icolonnes + col_1];

         for(int j = col_3; j >= 0; --j) {
            a3[icolonnes + j] = an3 * a1[icol_1 + j] + an4 * a1[icol_2 + j] - ad1 * a3[icol_1 + j] - ad2 * a3[icol_2 + j];
         }
      }

      int icol_1 = lignes * colonnes;

      for(int var69 = 0; var69 < icol_1; ++var69) {
         a2[var69] += a3[var69];
      }

      for(int j = 0; j < colonnes; ++j) {
         a3[j] = 0.0F;
         a3[colonnes + j] = an11 * a2[j] - ad1 * a3[j];

         for(int var70 = 2; var70 < lignes; ++var70) {
            a3[var70 * colonnes + j] = an11 * a2[(var70 - 1) * colonnes + j] - ad1 * a3[(var70 - 1) * colonnes + j] - ad2 * a3[(var70 - 2) * colonnes + j];
         }
      }

      for(int var89 = 0; var89 < colonnes; ++var89) {
         a4[lig_1 * colonnes + var89] = 0.0F;
         a4[lig_2 * colonnes + var89] = -an11 * a2[lig_1 * colonnes + var89] - ad1 * a4[lig_1 * colonnes + var89];

         for(int var71 = lig_3; var71 >= 0; --var71) {
            a4[var71 * colonnes + var89] = -an11 * a2[(var71 + 1) * colonnes + var89] - ad1 * a4[(var71 + 1) * colonnes + var89] - ad2 * a4[(var71 + 2) * colonnes + var89];
         }
      }

      icol_1 = colonnes * lignes;

      for(int var72 = 0; var72 < icol_1; ++var72) {
         a3[var72] += a4[var72];
      }

      for(int var73 = 0; var73 < lignes; ++var73) {
         for(int var90 = 0; var90 < colonnes; ++var90) {
            nf_gry[var73 * colonnes + var90] = a3[var73 * colonnes + var90];
         }
      }

      for(int var74 = 0; var74 < lignes; ++var74) {
         for(int var91 = 0; var91 < colonnes; ++var91) {
            a1[var74 * colonnes + var91] = (float)iDep.getPixel(var91, var74);
         }
      }

      for(int var75 = 0; var75 < lignes; ++var75) {
         icolonnes = var75 * colonnes;
         icol_1 = icolonnes - 1;
         int icol_2 = icolonnes - 2;
         a2[icolonnes] = 0.0F;
         a2[icolonnes + 1] = an11 * a1[icolonnes];

         for(int var92 = 2; var92 < colonnes; ++var92) {
            a2[icolonnes + var92] = an11 * a1[icol_1 + var92] - ad1 * a2[icol_1 + var92] - ad2 * a2[icol_2 + var92];
         }
      }

      for(int var76 = 0; var76 < lignes; ++var76) {
         icolonnes = var76 * colonnes;
         icol_1 = icolonnes + 1;
         int icol_2 = icolonnes + 2;
         a3[icolonnes + col_1] = 0.0F;
         a3[icolonnes + col_2] = -an11 * a1[icolonnes + col_1];

         for(int var93 = col_3; var93 >= 0; --var93) {
            a3[icolonnes + var93] = -an11 * a1[icol_1 + var93] - ad1 * a3[icol_1 + var93] - ad2 * a3[icol_2 + var93];
         }
      }

      icol_1 = lignes * colonnes;

      for(int var77 = 0; var77 < icol_1; ++var77) {
         a2[var77] += a3[var77];
      }

      for(int var94 = 0; var94 < colonnes; ++var94) {
         a3[var94] = an1 * a2[var94];
         a3[colonnes + var94] = an1 * a2[colonnes + var94] + an2 * a2[var94] - ad1 * a3[var94];

         for(int var78 = 2; var78 < lignes; ++var78) {
            a3[var78 * colonnes + var94] = an1 * a2[var78 * colonnes + var94] + an2 * a2[(var78 - 1) * colonnes + var94] - ad1 * a3[(var78 - 1) * colonnes + var94] - ad2 * a3[(var78 - 2) * colonnes + var94];
         }
      }

      for(int var95 = 0; var95 < colonnes; ++var95) {
         a4[lig_1 * colonnes + var95] = 0.0F;
         a4[lig_2 * colonnes + var95] = an3 * a2[lig_1 * colonnes + var95] - ad1 * a4[lig_1 * colonnes + var95];

         for(int var79 = lig_3; var79 >= 0; --var79) {
            a4[var79 * colonnes + var95] = an3 * a2[(var79 + 1) * colonnes + var95] + an4 * a2[(var79 + 2) * colonnes + var95] - ad1 * a4[(var79 + 1) * colonnes + var95] - ad2 * a4[(var79 + 2) * colonnes + var95];
         }
      }

      icol_1 = colonnes * lignes;

      for(int var80 = 0; var80 < icol_1; ++var80) {
         a3[var80] += a4[var80];
      }

      for(int var81 = 0; var81 < lignes; ++var81) {
         for(int var96 = 0; var96 < colonnes; ++var96) {
            nf_grx[var81 * colonnes + var96] = a3[var81 * colonnes + var96];
         }
      }

      for(int var82 = 0; var82 < lignes; ++var82) {
         for(int var97 = 0; var97 < colonnes; ++var97) {
            a2[var82 * colonnes + var97] = nf_gry[var82 * colonnes + var97];
         }
      }

      icol_1 = colonnes * lignes;

      for(int var83 = 0; var83 < icol_1; ++var83) {
         a2[var83] = (float)Math.sqrt((double)(a2[var83] * a2[var83] + a3[var83] * a3[var83]));
      }

      result_array = new byte[nmem];
      double min = (double)a2[0];
      double max = (double)a2[0];

      for(int var84 = 1; var84 < nmem; ++var84) {
         if (min > (double)a2[var84]) {
            min = (double)a2[var84];
         }

         if (max < (double)a2[var84]) {
            max = (double)a2[var84];
         }
      }

      for(int var85 = 0; var85 < nmem; ++var85) {
         result_array[var85] = (byte)((int)((double)255.0F * ((double)a2[var85] / (max - min))));
      }

      iGrad.setPixels(result_array);
      return iGrad;
   }

   public double process() {
      Point2d displ = new Point2d();
      double maxforce = (double)0.0F;
      double som = (double)0.0F;
      double seuil = this.config.getGradThreshold();
      double DivForce = this.config.getMaxDisplacement();
      double minr = this.config.getRegMin();
      double maxr = this.config.getRegMax();
      double alpha = this.config.getAlpha();
      double dist_plus = Prefs.get("ABSnake_ThreshDistPos.double", (double)100.0F);
      double dist_minus = Prefs.get("ABSnake_ThreshDistNeg.double", (double)100.0F);

      for(int i = 0; i < this.NPT; ++i) {
         this.normale[i] = this.compute_normale(i);
      }

      this.block = 0;
      this.elimination = 0;

      for(int var23 = 0; var23 < this.NPT; ++var23) {
         displ.x = (double)0.0F;
         displ.y = (double)0.0F;
         displ = this.compute_displ(var23, seuil, dist_plus, dist_minus, -1);
         double force = Math.sqrt(Math.pow(displ.x, (double)2.0F) + Math.pow(displ.y, (double)2.0F));
         if (force > DivForce) {
            this.deplace[var23].x = DivForce * (displ.x / force);
            this.deplace[var23].y = DivForce * (displ.y / force);
         } else {
            this.deplace[var23].x = displ.x;
            this.deplace[var23].y = displ.y;
         }

         force = Math.sqrt(this.deplace[var23].x * this.deplace[var23].x + this.deplace[var23].y * this.deplace[var23].y);
         if (force > maxforce) {
            maxforce = force;
         }

         som += force;
      }

      this.dataDistance = som / (double)this.NPT;

      for(int var24 = 0; var24 < this.NPT; ++var24) {
         double force = Math.sqrt(Math.pow(this.deplace[var24].x, (double)2.0F) + Math.pow(this.deplace[var24].y, (double)2.0F));
         this.lambda[var24] = maxr / ((double)1.0F + (maxr - minr) / minr * (force / maxforce));
      }

      if (this.elimination == 1) {
         this.destroysnake();
      }

      this.new_positions();
      this.resample(false);
      return this.dataDistance;
   }

   public ByteProcessor segmentation(int wi, int he, int col) {
      Point2d pos = new Point2d();
      new Point2d();
      Point2d ref = new Point2d();
      ByteProcessor res = new ByteProcessor(wi, he);
      int top = 0;
      int bottom = 100000;
      int left = 100000;
      int right = 0;

      for(int i = 0; i < this.NPT; ++i) {
         if (this.points[i].y > (double)top) {
            top = (int)this.points[i].y;
         }

         if (this.points[i].y < (double)bottom) {
            bottom = (int)this.points[i].y;
         }

         if (this.points[i].x > (double)right) {
            right = (int)this.points[i].x;
         }

         if (this.points[i].x < (double)left) {
            left = (int)this.points[i].x;
         }
      }

      ref.x = (double)0.0F;
      ref.y = (double)0.0F;

      for(int x = left; x < right; ++x) {
         for(int y = bottom; y < top; ++y) {
            pos.x = (double)x;
            pos.y = (double)y;
            if (this.inside(pos)) {
               res.putPixel(x, y, col);
            } else {
               res.putPixel(x, y, 0);
            }
         }
      }

      return res;
   }

   boolean inside(Point2d pos) {
      Point2d norm = new Point2d();
      Point2d ref = new Point2d();
      ref.x = (double)0.0F;
      ref.y = (double)0.0F;
      norm.x = ref.x - pos.x;
      norm.y = ref.y - pos.y;
      double lnorm = Math.sqrt(norm.x * norm.x + norm.y * norm.y);
      norm.x /= lnorm;
      norm.y /= lnorm;
      int count = 0;

      for(int i = 0; i < this.NPT - 1; ++i) {
         double bden = -norm.x * this.points[i + 1].y + norm.x * this.points[i].y + norm.y * this.points[i + 1].x - norm.y * this.points[i].x;
         double bnum = -norm.x * pos.y + norm.x * this.points[i].y + norm.y * pos.x - norm.y * this.points[i].x;
         double bres;
         if (bden != (double)0.0F) {
            bres = bnum / bden;
         } else {
            bres = (double)5.0F;
         }

         if (bres >= (double)0.0F && bres <= (double)1.0F) {
            double ares = -(-this.points[i + 1].y * pos.x + this.points[i + 1].y * this.points[i].x + this.points[i].y * pos.x + pos.y * this.points[i + 1].x - pos.y * this.points[i].x - this.points[i].y * this.points[i + 1].x) / (-norm.x * this.points[i + 1].y + norm.x * this.points[i].y + norm.y * this.points[i + 1].x - norm.y * this.points[i].x);
            if (ares >= (double)0.0F && ares <= lnorm) {
               ++count;
            }
         }
      }

      int var16 = this.NPT - 1;
      double bden = -norm.x * this.points[0].y + norm.x * this.points[var16].y + norm.y * this.points[0].x - norm.y * this.points[var16].x;
      double bnum = -norm.x * pos.y + norm.x * this.points[var16].y + norm.y * pos.x - norm.y * this.points[var16].x;
      double bres;
      if (bden != (double)0.0F) {
         bres = bnum / bden;
      } else {
         bres = (double)5.0F;
      }

      if (bres >= (double)0.0F && bres <= (double)1.0F) {
         double ares = -(-this.points[0].y * pos.x + this.points[0].y * this.points[var16].x + this.points[var16].y * pos.x + pos.y * this.points[0].x - pos.y * this.points[var16].x - this.points[var16].y * this.points[0].x) / (-norm.x * this.points[0].y + norm.x * this.points[var16].y + norm.y * this.points[0].x - norm.y * this.points[var16].x);
         if (ares >= (double)0.0F && ares <= lnorm) {
            ++count;
         }
      }

      return count % 2 == 1;
   }
}
