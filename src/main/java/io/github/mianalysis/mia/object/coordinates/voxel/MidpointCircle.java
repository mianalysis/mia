package io.github.mianalysis.mia.object.coordinates.voxel;

// Implementation of the midpoint circle algorithm
// Described at https://en.wikipedia.org/wiki/Midpoint_circle_algorithm (accessed 12-07-2016)

import java.util.ArrayList;

import io.github.mianalysis.mia.process.math.ArrayFunc;

public class MidpointCircle {
	int[] x_oct;
	int[] y_oct;

	public MidpointCircle(int r) {
		int x = r;
		int y = 0;
		int dec = 1-x;

		ArrayList<Integer> x_temp = new ArrayList<Integer>();
		ArrayList<Integer> y_temp = new ArrayList<Integer>();

		while (x >= y) {
			x_temp.add(x);
			y_temp.add(y);

			y++;

			if (dec <= 0) {
				dec += 2*y+1;
			} else {
				x--;
				dec += 2*(y-x)+1;
			}			
		}
		
		if (r==1) { //For consistency with the r=1 result from imglib2 (and thus TrackMate)
			x_oct = new int[]{0, 1};
			y_oct = new int[]{1, 0};
		} else {
			x_oct = new int[x_temp.size()];
			y_oct = new int[y_temp.size()];

			for (int i=0;i<x_temp.size();i++) {
				x_oct[i] = x_temp.get(i);
				y_oct[i] = y_temp.get(i);
			}
		}
	}

	public int[] getXOct(){
		return x_oct;
	}

	public int[] getYOct(){
		return y_oct;
	}

	public int[] getXQuad(){
		int st = 0;
		int l = x_oct.length*2;
		if (x_oct[x_oct.length-1]==y_oct[y_oct.length-1]) {
			st = 1;
			l = x_oct.length*2-1;
		}
		int[] x_quad = new int[l];

		for (int i=0;i<x_oct.length;i++) 
			x_quad[i] = x_oct[i];
		
		for (int i=st;i<x_oct.length;i++) 
			x_quad[i-st+x_oct.length] = y_oct[y_oct.length-1-i];
		
		return x_quad;
	}

	public int[] getYQuad(){

		int st = 0;
		int l = y_oct.length*2;
		if (x_oct[x_oct.length-1]==y_oct[y_oct.length-1]) {
			st = 1;
			l = y_oct.length*2-1;
		}
		int[] y_quad = new int[l];

		for (int i=0;i<y_oct.length;i++)
			y_quad[i] = y_oct[i];
		
		for (int i=st;i<y_oct.length;i++) 
			y_quad[i-st+y_oct.length] = x_oct[x_oct.length-1-i];

		return y_quad;
	}

	public int[] getXQuadFill() {
		
		int[] x_quad = getXQuad();
		int[] y_quad = getYQuad();
		int[] un = ArrayFunc.uniqueRows(y_quad);
		ArrayList<Integer> x_quad_f_al = new ArrayList<Integer>();

		for (int i=0;i<un.length;i++)
			for (int j=0;j<=x_quad[un[i]];j++)
				x_quad_f_al.add(j);

		int[] x_quad_f = new int[x_quad_f_al.size()];
		for (int i=0;i<x_quad_f_al.size();i++)
			x_quad_f[i] = x_quad_f_al.get(i);

		return x_quad_f;
	}

	public int[] getYQuadFill() {
		int[] x_quad = getXQuad();
		int[] y_quad = getYQuad();
		int[] un = ArrayFunc.uniqueRows(y_quad);
		ArrayList<Integer> y_quad_f_al = new ArrayList<Integer>();

		for (int i=0;i<un.length;i++)
			for (int j=0;j<=x_quad[un[i]];j++)
				y_quad_f_al.add(y_quad[un[i]]);
			
		int[] y_quad_f = new int[y_quad_f_al.size()];
		for (int i=0;i<y_quad_f_al.size();i++)
			y_quad_f[i] = y_quad_f_al.get(i);

		return y_quad_f;
	}

	public int[] getXHalf() {
		int[] x_quad = getXQuad();
		int[] x_half = new int[x_quad.length*2-1];

		for (int i=0;i<x_quad.length;i++)
			x_half[i] = x_quad[x_quad.length-1-i];
		
		for (int i=1;i<x_quad.length;i++)
			x_half[i-1+x_quad.length] = x_quad[i];

		return x_half;
	}

	public int[] getYHalf() {
		int[] y_quad = getYQuad();
		int[] y_half = new int[y_quad.length*2-1];

		for (int i=0;i<y_quad.length;i++)
			y_half[i] = y_quad[y_quad.length-1-i];
		
		for (int i=1;i<y_quad.length;i++)
			y_half[i-1+y_quad.length] = -y_quad[i];

		return y_half;
	}

	public int[] getXHalfFill() {
		int[] x_quad_f = getXQuadFill();
		int[] y_quad_f = getYQuadFill();

		ArrayList<Integer> x_half_f_al = new ArrayList<Integer>();
		for (int i=0;i<x_quad_f.length;i++)
			x_half_f_al.add(x_quad_f[i]);
		
		for (int i=0;i<x_quad_f.length;i++)
			if (y_quad_f[i]!=0)
				x_half_f_al.add(x_quad_f[i]);	

		int[] x_half_f = new int[x_half_f_al.size()];
		for (int i=0;i<x_half_f_al.size();i++)
			x_half_f[i] = x_half_f_al.get(i);

		return x_half_f;
	}

	public int[] getYHalfFill() {
		int[] y_quad_f = getYQuadFill();

		ArrayList<Integer> y_half_f_al = new ArrayList<Integer>();
		for (int i=0;i<y_quad_f.length;i++)
			y_half_f_al.add(y_quad_f[i]);
		
		for (int i=0;i<y_quad_f.length;i++)
			if (y_quad_f[i]!=0)
				y_half_f_al.add(-y_quad_f[i]);	
			
		int[] y_half_f = new int[y_half_f_al.size()];
		for (int i=0;i<y_half_f_al.size();i++)
			y_half_f[i] = y_half_f_al.get(i);

		return y_half_f;
	}

	public int[] getXCircle() {
		int[] x_half = getXHalf();
		int[] x_circ = new int[x_half.length*2-2];

		for (int i=0;i<x_half.length;i++)
			x_circ[i] = x_half[x_half.length-1-i];
		
		for (int i=1;i<x_half.length-1;i++)
			x_circ[i-1+x_half.length] = -x_half[i];

		return x_circ;
	}

	public int[] getYCircle() {
		int[] y_half = getYHalf();
		int[] y_circ = new int[y_half.length*2-2];

		for (int i=0;i<y_half.length;i++)
			y_circ[i] = y_half[y_half.length-1-i];
		
		for (int i=1;i<y_half.length-1;i++)
			y_circ[i-1+y_half.length] = y_half[i];

		return y_circ;

	}

	public int[][] getCircle() {
		int[] x_circ = getXCircle();
		int[] y_circ = getYCircle();

		int[][] circ = new int[x_circ.length][2];
		for (int i=0;i<x_circ.length;i++) {
			circ[i][0] = x_circ[i];
			circ[i][1] = y_circ[i];
		}

		return circ;

	}

	public int[] getXCircleFill(){
		int[] x_half_f = getXHalfFill();

		ArrayList<Integer> x_circ_f_al = new ArrayList<Integer>();
		for (int i=0;i<x_half_f.length;i++)
			x_circ_f_al.add(x_half_f[i]);
		
		for (int i=0;i<x_half_f.length;i++)
			if (x_half_f[i]!=0)
				x_circ_f_al.add(-x_half_f[i]);	

		int[] x_circ_f = new int[x_circ_f_al.size()];
		for (int i=0;i<x_circ_f_al.size();i++)
			x_circ_f[i] = x_circ_f_al.get(i);
		
		return x_circ_f;
	}

	public int[] getYCircleFill(){
		int[] x_half_f = getXHalfFill();
		int[] y_half_f = getYHalfFill();

		ArrayList<Integer> y_circ_f_al = new ArrayList<Integer>();
		for (int i=0;i<y_half_f.length;i++)
			y_circ_f_al.add(y_half_f[i]);
		
		for (int i=0;i<y_half_f.length;i++)
			if (x_half_f[i]!=0)
				y_circ_f_al.add(y_half_f[i]);	

		int[] y_circ_f = new int[y_circ_f_al.size()];
		for (int i=0;i<y_circ_f_al.size();i++)
			y_circ_f[i] = y_circ_f_al.get(i);
		
		return y_circ_f;
	}
}