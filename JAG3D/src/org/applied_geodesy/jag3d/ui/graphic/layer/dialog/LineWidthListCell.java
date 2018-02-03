package org.applied_geodesy.jag3d.ui.graphic.layer.dialog;

import java.text.NumberFormat;
import java.util.Locale;

import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;

public class LineWidthListCell extends ListCell<Double> {

	private NumberFormat numberFormat;
	LineWidthListCell() {
		this.numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
		this.numberFormat.setMaximumFractionDigits(2);
		this.numberFormat.setMinimumFractionDigits(2);
		this.numberFormat.setGroupingUsed(false);
	}
	
	protected void updateItem(Double lineWidth, boolean empty){
		super.updateItem(lineWidth, empty);
	
		this.setGraphic(null);
		this.setText(null);
		
		if(lineWidth != null) {
			this.setGraphic(this.createSymbolImage(lineWidth));
			this.setText(this.numberFormat.format(lineWidth));
		}
	}
	
	private Canvas createSymbolImage(Double lineWidth) {
		
		double width  = 75;
		double height = 1.7 * SymbolBuilder.DEFAULT_SIZE;
		Canvas canvas = new Canvas(width, height);
		GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
		graphicsContext.setStroke(Color.BLACK);
		graphicsContext.setFill(Color.BLACK);
		graphicsContext.setLineWidth(lineWidth);
		graphicsContext.strokeLine(0.2*width, 0.5*height, 0.8*width, 0.5*height);
		return canvas;
	}
}
