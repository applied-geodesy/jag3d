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

import org.applied_geodesy.jag3d.ui.graphic.layer.Layer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class LayerListCell extends ListCell<Layer> {
	private CheckBox visibleCheckBox = new CheckBox();
	private Rectangle rect = new Rectangle(25, 15);

	public LayerListCell() {
		HBox box = new HBox(this.rect);
		box.setPadding(new Insets(0,0,0,3));
		this.rect.setStroke(Color.BLACK);
		this.visibleCheckBox.setText(null);
		this.visibleCheckBox.setGraphic(box); // this.rect

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