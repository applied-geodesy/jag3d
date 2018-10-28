module org.applied_geodesy.jag3d {
	exports org.applied_geodesy.jag3d;

	requires arpack.combined.all;
	requires core;
	requires freemarker;
	requires jdistlib;
	requires mtj;
	
	requires CoordTrans;
	requires FormFittingToolbox;
	requires GeoTra;

	requires java.desktop;
	requires java.logging;
	requires java.sql;
	requires java.xml;

	requires javafx.base;
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.swing;

}