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
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.ArrowSymbolType;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.util.i18.I18N;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;

public class ArrowSymbolTypeListCell extends ListCell<ArrowSymbolType> {
	private I18N i18n = I18N.getInstance();
	
	protected void updateItem(ArrowSymbolType symbolType, boolean empty){
		super.updateItem(symbolType, empty);
	
		this.setGraphic(null);
		this.setText(null);
		
		if(symbolType != null) {
			this.setGraphic(this.createSymbolImage(symbolType));
			this.setText(this.getText(symbolType));
		}
	}
	
	private Canvas createSymbolImage(ArrowSymbolType symbolType) {
		double width  = 4.0 * SymbolBuilder.DEFAULT_SIZE;
		double height = 1.5 * SymbolBuilder.DEFAULT_SIZE;
		Canvas canvas = new Canvas(width, height);
		GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
		graphicsContext.setStroke(Color.BLACK);
		graphicsContext.setFill(Color.BLACK);
		graphicsContext.strokeLine(0.05*width, 0.5*height, 0.75*width, 0.5*height);
		SymbolBuilder.drawSymbol(graphicsContext, new PixelCoordinate(0.05*width, 0.5*height), symbolType, 1.3 * SymbolBuilder.DEFAULT_SIZE, Math.PI);
		return canvas;
	}
	
	private String getText(ArrowSymbolType symbolType) {
		switch(symbolType) {
		case FILLED_TETRAGON_ARROW:
			return i18n.getString("ArrowSymbolTypeListCell.symbol.filled_tetragon_arrow.label", "Filled tetragon arrow");
		case FILLED_TRIANGLE_ARROW:
			return i18n.getString("ArrowSymbolTypeListCell.symbol.filled_triangle_arrow.label", "Filled triangle arrow");
		case STROKED_TETRAGON_ARROW:
			return i18n.getString("ArrowSymbolTypeListCell.symbol.stroked_tetragon_arrow.label", "Stroked tetragon arrow");
		case STROKED_TRIANGLE_ARROW:
			return i18n.getString("ArrowSymbolTypeListCell.symbol.stroked_triangle_arrow.label", "Stroked triangle arrow");
		}
		return null;
	}
}