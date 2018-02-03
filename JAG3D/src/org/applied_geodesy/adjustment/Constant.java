package org.applied_geodesy.adjustment;

public final class Constant {
	
	private Constant() {}
	
	public static final double RHO_DEG2RAD = Math.PI/180.0;

	public static final double RHO_RAD2DEG = 180.0/Math.PI;

	public static final double RHO_DEG2GRAD = 200.0/180.0;	
	
	public static final double RHO_GRAD2DEG = 180.0/200.0;	

	public static final double RHO_GRAD2RAD = Math.PI/200.0;
	
	public static final double RHO_RAD2GRAD = 200.0/Math.PI;
	
	public static final double RHO_MIL2RAD = Math.PI/3200.0;
	
	public static final double RHO_MIL2GRAD = 200.0/3200.0;
	
	public static final double RHO_MIL2DEG = 180.0/3200.0;
	
	public static final double RHO_RAD2MIL = 3200.0/Math.PI;
	
	public static final double RHO_GRAD2MIL = 3200.0/200.0;
	
	public static final double RHO_DEG2MIL = 3200.0/180.0;


	public static final double TORR2HEKTOPASCAL = 1013.25/760.0;

	public static final double HEKTOPASCAL2TORR = 760.0/1013.25;

	
	public static final double EARTH_RADIUS = 6371000.0;

	
	public static final double EPS = Constant.mashEPS();
 
	/**
	 * Methode zum Berechnen der relativen EPS-Genauigkeit.
	 * @return Relative EPS-Genauigkeit
	 * @see Gisela Engeln-Muellges und Fritz Reutter: Formelsammlung zur Numerischen Mathematik mit C-Programmen
	 */
	private static double mashEPS(){
		double eps=1.0,x=2.0,y=1.0;
		while (y<x) {
			eps *= 0.5;
			x = 1.0+eps;
		}
		return eps;
	}
}
