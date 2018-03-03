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

package org.applied_geodesy.jag3d.ui.graphic.layer.dialog;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.PointSymbolType;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.util.i18.I18N;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;

public class PointSymbolTypeListCell extends ListCell<PointSymbolType> {
	private I18N i18n = I18N.getInstance();
	
	protected void updateItem(PointSymbolType symbolType, boolean empty){
		super.updateItem(symbolType, empty);
	
		this.setGraphic(null);
		this.setText(null);
		
		if(symbolType != null) {
			this.setGraphic(this.createSymbolImage(symbolType));
			this.setText(this.getText(symbolType));
		}
	}
	
	private Canvas createSymbolImage(PointSymbolType symbolType) {
		double width  = 1.7 * SymbolBuilder.DEFAULT_SIZE;
		double height = 1.7 * SymbolBuilder.DEFAULT_SIZE;
		Canvas canvas = new Canvas(width, height);
		GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
		graphicsContext.setStroke(Color.BLACK);
		graphicsContext.setFill(Color.BLACK);
		SymbolBuilder.drawSymbol(graphicsContext, new PixelCoordinate(0.5*width, 0.5*height), symbolType, 1.5 * SymbolBuilder.DEFAULT_SIZE);
		return canvas;
	}
	
	private String getText(PointSymbolType symbolType) {
		switch(symbolType) {
		case DOT:
			return i18n.getString("PointSymbolTypeListCell.symbol.dot.label", "Dot");
		case X_CROSS:
			return i18n.getString("PointSymbolTypeListCell.symbol.x_cross.label", "Cross (x)");
		case PLUS_CROSS:
			return i18n.getString("PointSymbolTypeListCell.symbol.plus_cross.label", "Cross (+)");
		case CROSSED_CIRCLE:
			return i18n.getString("PointSymbolTypeListCell.symbol.crossed_circle.label", "Crossed circle");
		case CROSSED_SQUARE:
			return i18n.getString("PointSymbolTypeListCell.symbol.crossed_square.label", "Crossed square");
		case FILLED_CIRCLE:
			return i18n.getString("PointSymbolTypeListCell.symbol.filled_square.label", "Filled circle");
		case FILLED_DIAMAND:
			return i18n.getString("PointSymbolTypeListCell.symbol.filled_diamand.label", "Filled diamand");
		case FILLED_HEPTAGON:
			return i18n.getString("PointSymbolTypeListCell.symbol.filled_heptagon.label", "Filled heptagon");
		case FILLED_HEXAGON:
			return i18n.getString("PointSymbolTypeListCell.symbol.filled_hexagon.label", "Filled hexagon");
		case FILLED_OCTAGON:
			return i18n.getString("PointSymbolTypeListCell.symbol.filled_octagon.label", "Filled octagon");
		case FILLED_PENTAGON:
			return i18n.getString("PointSymbolTypeListCell.symbol.filled_pentagon.label", "Filled pentagon");
		case FILLED_SQUARE:
			return i18n.getString("PointSymbolTypeListCell.symbol.filled_square.label", "Filled square");
		case FILLED_STAR:
			return i18n.getString("PointSymbolTypeListCell.symbol.filled_star.label", "Filled star");
		case FILLED_UPRIGHT_TRIANGLE:
			return i18n.getString("PointSymbolTypeListCell.symbol.filled_upright_triangle.label", "Filled upright triangle");
		case FILLED_DOWNRIGHT_TRIANGLE:
			return i18n.getString("PointSymbolTypeListCell.symbol.filled_downright_triangle.label", "Filled downright triangle");
		case STROKED_CIRCLE:
			return i18n.getString("PointSymbolTypeListCell.symbol.stroked_circle.label", "Stroked circle");
		case STROKED_DIAMAND:
			return i18n.getString("PointSymbolTypeListCell.symbol.stroked_diamand.label", "Stroked diamand");
		case STROKED_HEPTAGON:
			return i18n.getString("PointSymbolTypeListCell.symbol.stroked_heptagon.label", "Stroked heptagon");
		case STROKED_HEXAGON:
			return i18n.getString("PointSymbolTypeListCell.symbol.stroked_hexagon.label", "Stroked hexagon");
		case STROKED_OCTAGON:
			return i18n.getString("PointSymbolTypeListCell.symbol.stroked_octagon.label", "Stroked octagon");
		case STROKED_PENTAGON:
			return i18n.getString("PointSymbolTypeListCell.symbol.stroked_pentagon.label", "Stroked pentagon");
		case STROKED_SQUARE:
			return i18n.getString("PointSymbolTypeListCell.symbol.stroked_square.label", "Stroked square");
		case STROKED_STAR:
			return i18n.getString("PointSymbolTypeListCell.symbol.stroked_star.label", "Stroked star");
		case STROKED_UPRIGHT_TRIANGLE:
			return i18n.getString("PointSymbolTypeListCell.symbol.stroked_upright_triangle.label", "Stroked upright triangle");
		case STROKED_DOWNRIGHT_TRIANGLE:
			return i18n.getString("PointSymbolTypeListCell.symbol.stroked_downright_triangle.label", "Stroked downright triangle");
			
		}
		return null;
	}
}