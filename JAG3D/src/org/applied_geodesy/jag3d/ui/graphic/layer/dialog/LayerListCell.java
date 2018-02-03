package org.applied_geodesy.jag3d.ui.graphic.layer.dialog;

import org.applied_geodesy.jag3d.ui.graphic.layer.Layer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class LayerListCell extends ListCell<Layer> {
	private CheckBox visibleCheckBox = new CheckBox();
	private Rectangle rect = new Rectangle(25, 15);

	public LayerListCell() {
		this.rect.setStroke(Color.BLACK);
		this.visibleCheckBox.setText(null);
		this.visibleCheckBox.setGraphic(this.rect);

		// bind/unbind properties
		this.itemProperty().addListener(new ChangeListener<Layer>() {
			@Override
			public void changed(ObservableValue<? extends Layer> observable, Layer oldValue, Layer newValue) {				
				if (oldValue != null) {
					visibleCheckBox.selectedProperty().unbindBidirectional(oldValue.visibleProperty());
					rect.fillProperty().unbind();
				}
				
				if (newValue != null) {
					visibleCheckBox.selectedProperty().bindBidirectional(newValue.visibleProperty());
					rect.fillProperty().bind(newValue.colorProperty());
				}
			}
		});
	}

	@Override
	public void updateItem(Layer layer, boolean empty) {
		super.updateItem(layer, empty);
		if (!empty && layer != null) {
			this.setGraphic(this.visibleCheckBox);
			this.setText(layer.toString());
		}
	}
}