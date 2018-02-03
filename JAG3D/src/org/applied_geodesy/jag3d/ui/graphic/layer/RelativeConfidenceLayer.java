package org.applied_geodesy.jag3d.ui.graphic.layer;

import java.util.List;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.sql.GraphicPoint;
import org.applied_geodesy.jag3d.ui.graphic.sql.RelativeConfidence;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RelativeConfidenceLayer extends ConfidenceLayer<ArrowLayer> {

	RelativeConfidenceLayer(LayerType layerType, GraphicExtent currentGraphicExtent) {
		super(layerType, currentGraphicExtent);
		this.setColor(Color.LIGHTYELLOW);
	}

	@Override
	public void draw(GraphicExtent graphicExtent) {
		this.clearDrawingBoard();

		if (!this.isVisible() || this.getCurrentGraphicExtent().getScale() <= 0)
			return;

		GraphicsContext graphicsContext = this.getGraphicsContext2D();	
		List<ArrowLayer> referenceLayers = this.getReferenceLayers();
		double scale = this.getCurrentGraphicExtent().getScale();
		// double ellipseScale = this.getConfidenceScale()/scale;
		double lineWidth  = this.getLineWidth();
		graphicsContext.setLineWidth(lineWidth);
		graphicsContext.setStroke(this.getStrokeColor());
		graphicsContext.setFill(this.getColor());

		for (ArrowLayer layer : referenceLayers) {
			if (layer.isVisible()) {
				double ellipseScale = layer.getVectorScale()/scale;
				for (RelativeConfidence relativeConfidence : layer.getRelativeConfidences()) { 
					GraphicPoint startPoint = relativeConfidence.getStartPoint();
					GraphicPoint endPoint   = relativeConfidence.getEndPoint();
					
					if (!startPoint.isVisible() || !endPoint.isVisible())
						continue;
					
					PixelCoordinate pixelCoordinateStartPoint = GraphicExtent.toPixelCoordinate(startPoint.getCoordinate(), graphicExtent);
					PixelCoordinate pixelCoordinateEndPoint   = GraphicExtent.toPixelCoordinate(endPoint.getCoordinate(), graphicExtent);
					
					if (!this.contains(pixelCoordinateStartPoint) && !this.contains(pixelCoordinateEndPoint))
						continue;

					double xs = pixelCoordinateStartPoint.getX();
					double ys = pixelCoordinateStartPoint.getY();
					
					double xe = pixelCoordinateEndPoint.getX();
					double ye = pixelCoordinateEndPoint.getY();
					
					double avgX = 0.5 * (xs + xe);
					double avgY = 0.5 * (ys + ye);

					PixelCoordinate vectorStartCoordinate = new PixelCoordinate(avgX, avgY);
					
					if (this.contains(vectorStartCoordinate) && relativeConfidence.getMajorAxis() > 0) {
						double majorAxis = ellipseScale*relativeConfidence.getMajorAxis();
						double minorAxis = ellipseScale*relativeConfidence.getMinorAxis();
						double angle     = relativeConfidence.getAngle();

						SymbolBuilder.drawEllipse(graphicsContext, vectorStartCoordinate, majorAxis, minorAxis, angle);
					}
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return i18n.getString("AbsoluteConfidenceLayer.type", "Relative confidences");
	}
}
