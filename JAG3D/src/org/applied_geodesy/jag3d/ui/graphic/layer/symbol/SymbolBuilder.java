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

package org.applied_geodesy.jag3d.ui.graphic.layer.symbol;

import java.util.Map;
import java.util.Set;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationSymbolProperties;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationSymbolProperties.ObservationType;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

public class SymbolBuilder {

	public final static double DEFAULT_SIZE = 12.0;
	
	public static void drawEllipse(GraphicsContext graphicsContext, PixelCoordinate coordinate, double majorAxis, double minorAxis, double angle) {
		double x0 = coordinate.getX() - majorAxis;
		double y0 = coordinate.getY() - minorAxis;

		graphicsContext.save();
		graphicsContext.transform(new Affine(
				new Rotate(angle * Constant.RHO_RAD2DEG, coordinate.getX(), coordinate.getY())
		));
		graphicsContext.fillOval(x0, y0, 2.0*majorAxis, 2.0*minorAxis);
		graphicsContext.strokeOval(x0, y0, 2.0*majorAxis, 2.0*minorAxis);
		graphicsContext.restore();
	}
	
	public static void drawSymbol(GraphicsContext graphicsContext, PixelCoordinate coordinate, Map<ObservationType, ObservationSymbolProperties> symbolPropertiesMap, Set<ObservationType> observationTypes, double size) {
		// determin enabled observation typs
		int n = 0;
		for (ObservationType type : observationTypes) {
			if (symbolPropertiesMap.get(type).isVisible())
				n++;
		}

		if (n > 0) {
			Paint currentPaint = graphicsContext.getStroke();
			double angle = 360.0 / n;
			int cnt = 0;
			graphicsContext.setLineDashes(null);
			//graphicsContext.setLineWidth(1); // take value from observation properties
			for (ObservationType type : observationTypes) {
				if (!symbolPropertiesMap.get(type).isVisible())
					continue;
				
				graphicsContext.setStroke(symbolPropertiesMap.get(type).getColor());
				graphicsContext.setFill(symbolPropertiesMap.get(type).getColor());
				
				graphicsContext.fillArc(
						coordinate.getX(), 
						coordinate.getY(),
						size, 
						size, 
						cnt*angle, 
						angle, 
						ArcType.ROUND
						);
				cnt++;
			}
			graphicsContext.setStroke(currentPaint);
			graphicsContext.strokeOval(coordinate.getX(), coordinate.getY(), size, size);
		}
	}
	
	public static void drawSymbol(GraphicsContext graphicsContext, PixelCoordinate coordinate, ArrowSymbolType symbolType, double size, double angle) {
		switch(symbolType) {
		case STROKED_TETRAGON_ARROW:
		case FILLED_TETRAGON_ARROW:
			drawTetragonArrow(graphicsContext, coordinate, size, angle, symbolType == ArrowSymbolType.STROKED_TETRAGON_ARROW);
			break;
			
		case STROKED_TRIANGLE_ARROW:	
		case FILLED_TRIANGLE_ARROW:
			drawTriangleArrow(graphicsContext, coordinate, size, angle, symbolType == ArrowSymbolType.STROKED_TRIANGLE_ARROW);
			break;
		}
		
	}
	
	public static void drawSymbol(GraphicsContext graphicsContext, PixelCoordinate coordinate, PointSymbolType symbolType, double size) {
		switch(symbolType) {
		case FILLED_CIRCLE:
		case STROKED_CIRCLE:
			drawCircle(graphicsContext, coordinate, size, symbolType == PointSymbolType.STROKED_CIRCLE);
			break;

		case FILLED_SQUARE:
		case STROKED_SQUARE:
			drawSquare(graphicsContext, coordinate, size, symbolType == PointSymbolType.STROKED_SQUARE);
			break;
			
		case FILLED_UPRIGHT_TRIANGLE:
		case STROKED_UPRIGHT_TRIANGLE:
			drawUprightTriangle(graphicsContext, coordinate, size, symbolType == PointSymbolType.STROKED_UPRIGHT_TRIANGLE);
			break;
			
		case FILLED_DOWNRIGHT_TRIANGLE:
		case STROKED_DOWNRIGHT_TRIANGLE:
			drawDownrightTriangle(graphicsContext, coordinate, size, symbolType == PointSymbolType.STROKED_DOWNRIGHT_TRIANGLE);
			break;
			
		case FILLED_PENTAGON:
		case STROKED_PENTAGON:
			drawNEdges(graphicsContext, coordinate, size, symbolType == PointSymbolType.STROKED_PENTAGON, 5);
			break;
			
		case FILLED_HEXAGON:
		case STROKED_HEXAGON:
			drawNEdges(graphicsContext, coordinate, size, symbolType == PointSymbolType.STROKED_HEXAGON, 6);
			break;
			
		case FILLED_HEPTAGON:
		case STROKED_HEPTAGON:
			drawNEdges(graphicsContext, coordinate, size, symbolType == PointSymbolType.STROKED_HEPTAGON, 7);
			break;
			
		case FILLED_OCTAGON:
		case STROKED_OCTAGON:
			drawNEdges(graphicsContext, coordinate, size, symbolType == PointSymbolType.STROKED_OCTAGON, 8);
			break;
			
		case FILLED_STAR:
		case STROKED_STAR:
			drawStar(graphicsContext, coordinate, size, symbolType == PointSymbolType.STROKED_STAR);
			break;
			
		case FILLED_DIAMAND:
		case STROKED_DIAMAND:
			drawDiamand(graphicsContext, coordinate, size, symbolType == PointSymbolType.STROKED_DIAMAND);
			break;
			
		case X_CROSS:
			drawXCross(graphicsContext, coordinate, size);
			break;
			
		case PLUS_CROSS:
			drawPlusCross(graphicsContext, coordinate, size);
			break;
			
		case CROSSED_CIRCLE:
			drawCrossedCircle(graphicsContext, coordinate, size);
			break;
			
		case CROSSED_SQUARE:
			drawCrossedSquare(graphicsContext, coordinate, size);
			break;

		case DOT:
			drawCircle(graphicsContext, coordinate, 1, false);
			break;
		}
		
		drawCircle(graphicsContext, coordinate, 1, false);
	}
	
	private static void drawCircle(GraphicsContext gc, PixelCoordinate coordinate, double size, boolean stroke) {
		double x = coordinate.getX() - 0.5 * size;
		double y = coordinate.getY() - 0.5 * size;

		if (stroke)
			gc.strokeOval(x, y, size, size);
		else
			gc.fillOval(x, y, size, size);
	}
	
	private static void drawSquare(GraphicsContext gc, PixelCoordinate coordinate, double size, boolean stroke) {	
		double x = coordinate.getX() - 0.5 * size;
		double y = coordinate.getY() - 0.5 * size;
		
		if (stroke) 
			gc.strokeRect(x, y, size, size);
		else
			gc.fillRect(x, y, size, size);
	}
	
	private static void drawUprightTriangle(GraphicsContext gc, PixelCoordinate coordinate, double size, boolean stroke) {
		double x = coordinate.getX();
		double y = coordinate.getY();
		
		double cos30 = Math.cos(Math.toRadians(30));
		double sin30 = Math.sin(Math.toRadians(30));
		
		double[] xi = new double[] {
				x,
				x + cos30 * 0.5 * size,
				x - cos30 * 0.5 * size 
		};
		
		double[] yi = new double[] {
				y - 0.5*size,
				y + sin30 * 0.5 * size,
				y + sin30 * 0.5 * size,
		};

		if (stroke)
			gc.strokePolygon(xi, yi, xi.length);
		else
			gc.fillPolygon(xi, yi, xi.length);
	}
	
	private static void drawDownrightTriangle(GraphicsContext gc, PixelCoordinate coordinate, double size, boolean stroke) {
		double x = coordinate.getX();
		double y = coordinate.getY();
		
		double cos30 = Math.cos(Math.toRadians(30));
		double sin30 = Math.sin(Math.toRadians(30));
		
		double[] xi = new double[] {
				x,
				x + cos30 * 0.5 * size,
				x - cos30 * 0.5 * size 
		};
		
		double[] yi = new double[] {
				y + 0.5*size,
				y - sin30 * 0.5 * size,
				y - sin30 * 0.5 * size,
		};

		if (stroke)
			gc.strokePolygon(xi, yi, xi.length);
		else
			gc.fillPolygon(xi, yi, xi.length);
	}
	
	private static void drawDiamand(GraphicsContext gc, PixelCoordinate coordinate, double size, boolean stroke) {
		double x = coordinate.getX();
		double y = coordinate.getY();
		
		double[] xi = new double[] {
				x,
				x + 0.5 * size,
				x,
				x - 0.5 * size
		};
		
		double[] yi = new double[] {
				y - 0.5*size,
				y,
				y + 0.5*size,
				y
		};

		if (stroke)
			gc.strokePolygon(xi, yi, xi.length);
		else
			gc.fillPolygon(xi, yi, xi.length);
	}
	
	private static void drawNEdges(GraphicsContext gc, PixelCoordinate coordinate, double size, boolean stroke, int n) {
		double x = coordinate.getX();
		double y = coordinate.getY();
		
		double xi[] = new double[n];
		double yi[] = new double[n];
		
		double angle = 2.0*Math.PI / (double)n;
		for (int i=0; i < n; i++) {
			xi[i] = x + 0.5*size * Math.cos(i*angle);
			yi[i] = y + 0.5*size * Math.sin(i*angle);
		}

		if (stroke)
			gc.strokePolygon(xi, yi, n);
		else
			gc.fillPolygon(xi, yi, n);
	}
	
	private static void drawStar(GraphicsContext gc, PixelCoordinate coordinate, double size, boolean stroke) {
		double x = coordinate.getX();
		double y = coordinate.getY();
		
		int n = 5;
		
		double xi[] = new double[2*n];
		double yi[] = new double[2*n];
		
		double angle = 2.0*Math.PI / (double)(2*n);
		for (int i=0; i < 2*n; i++) {
			double s = (i%2 == 0) ? 0.5*size : 0.5/2.5*size;

			xi[i] = x - s * Math.sin(i*angle);
			yi[i] = y - s * Math.cos(i*angle);
		}

		if (stroke)
			gc.strokePolygon(xi, yi, 2*n);
		else
			gc.fillPolygon(xi, yi, 2*n);
	}
	
	private static void drawCrossedCircle(GraphicsContext gc, PixelCoordinate coordinate, double size) {
		double x = coordinate.getX();
		double y = coordinate.getY();

		gc.strokeLine(x - 0.5/Math.sqrt(2.0)*size, y - 0.5/Math.sqrt(2.0)*size, x + 0.5/Math.sqrt(2.0)*size, y + 0.5/Math.sqrt(2.0)*size);
		gc.strokeLine(x + 0.5/Math.sqrt(2.0)*size, y - 0.5/Math.sqrt(2.0)*size, x - 0.5/Math.sqrt(2.0)*size, y + 0.5/Math.sqrt(2.0)*size);
		
		drawCircle(gc, coordinate, size, true);
	}
	
	private static void drawCrossedSquare(GraphicsContext gc, PixelCoordinate coordinate, double size) {
		drawXCross(gc, coordinate, size);
		drawSquare(gc, coordinate, size, true);
	}
	
	private static void drawXCross(GraphicsContext gc, PixelCoordinate coordinate, double size) {
		double x = coordinate.getX();
		double y = coordinate.getY();

		gc.strokeLine(x - 0.5*size, y - 0.5*size, x + 0.5*size, y + 0.5*size);
		gc.strokeLine(x + 0.5*size, y - 0.5*size, x - 0.5*size, y + 0.5*size);
	}
	
	private static void drawPlusCross(GraphicsContext gc, PixelCoordinate coordinate, double size) {
		double x = coordinate.getX();
		double y = coordinate.getY();

		gc.strokeLine(x, y - 0.5*size, x, y + 0.5*size);
		gc.strokeLine(x + 0.5*size, y, x - 0.5*size, y);
	}
	
	
	private static void drawTriangleArrow(GraphicsContext gc, PixelCoordinate coordinate, double size, double angle, boolean stroke) {
		double x = coordinate.getX(); // tip of a vector
		double y = coordinate.getY();
		
		double[] xi = new double[] {
				x,
				x - size,
				x - size,
		};
		
		double[] yi = new double[] {
				y,
				y + 0.375 * size,
				y - 0.375 * size 
		};

		gc.save();
		gc.transform(new Affine(
				new Rotate(angle * Constant.RHO_RAD2DEG, x, y)
		));	

		if (stroke)
			gc.strokePolygon(xi, yi, xi.length);
		else
			gc.fillPolygon(xi, yi, xi.length);
		
		gc.restore();
	}
	
	private static void drawTetragonArrow(GraphicsContext gc, PixelCoordinate coordinate, double size, double angle, boolean stroke) {
		double x = coordinate.getX(); // tip of a vector
		double y = coordinate.getY();
		
		double[] xi = new double[] {
				x,
				x - size,
				x - 0.75*size,
				x - size,
		};
		
		double[] yi = new double[] {
				y,
				y + 0.375 * size,
				y,
				y - 0.375 * size 
		};

		gc.save();
		gc.transform(new Affine(
				new Rotate(angle * Constant.RHO_RAD2DEG, x, y)
		));	

		if (stroke)
			gc.strokePolygon(xi, yi, xi.length);
		else
			gc.fillPolygon(xi, yi, xi.length);
		
		gc.restore();
	}
	
}
