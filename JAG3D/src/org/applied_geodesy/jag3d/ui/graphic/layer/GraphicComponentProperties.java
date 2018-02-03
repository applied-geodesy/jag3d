package org.applied_geodesy.jag3d.ui.graphic.layer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public class GraphicComponentProperties {
	private BooleanProperty enable = new SimpleBooleanProperty(Boolean.TRUE);
	private ObjectProperty<Color> color = new SimpleObjectProperty<Color>(Color.BLACK);
	
	public final BooleanProperty enableProperty() {
		return this.enable;
	}

	public final boolean isEnable() {
		return this.enableProperty().get();
	}

	public final void setEnable(final boolean enable) {
		this.enableProperty().set(enable);
	}

	public final ObjectProperty<Color> colorProperty() {
		return this.color;
	}

	public final Color getColor() {
		return this.colorProperty().get();
	}
	
	public final void setColor(final Color color) {
		this.colorProperty().set(color);
	}
}
