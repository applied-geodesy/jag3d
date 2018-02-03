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
	private static I18N i18n = I18N.getInstance();
	
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