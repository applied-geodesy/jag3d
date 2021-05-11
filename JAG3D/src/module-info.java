/***********************************************************************
* Copyright by Michael Loesler, https://software.applied-geodesy.org   *
*                                                                      *
* This program is free software; you can redistribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation; either version 3 of the License, or    *
* at your option any later version.                                    *
*                                                                      *
* This program is distributed in the hope that it will be useful,      *
* but WITHOUT ANY WARRANTY; without even the implied warranty of       *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
* GNU General Public License for more details.                         *
*                                                                      *
* You should have received a copy of the GNU General Public License    *
* along with this program; if not, see <http://www.gnu.org/licenses/>  *
* or write to the                                                      *
* Free Software Foundation, Inc.,                                      *
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.            *
*                                                                      *
***********************************************************************/

module org.applied_geodesy.jag3d {
	exports org.applied_geodesy.jag3d;
	exports org.applied_geodesy.jag3d.ui;
	
	exports org.applied_geodesy.juniform;
	exports org.applied_geodesy.juniform.ui;
	
	exports org.applied_geodesy.adjustment.cmd;
		
	requires arpack.combined.all;
	requires core;
	requires jdistlib;
	requires jlatexmath;
	requires mtj;

	requires freemarker;
	requires CoordTrans;
	requires GeoTra;

	requires org.hsqldb;
	requires org.hsqldb.sqltool;
	
	requires java.desktop;
	requires java.logging;
	requires java.sql;
	requires java.xml;

	requires transitive javafx.base;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	requires transitive javafx.swing;
}