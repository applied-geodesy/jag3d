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

package org.applied_geodesy.jag3d.ui.graphic;

import org.applied_geodesy.jag3d.ui.graphic.layer.LayerManager;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

public class UIGraphicPaneBuilder {
	private static UIGraphicPaneBuilder graphicPaneBuilder = new UIGraphicPaneBuilder();
	private BorderPane borderPane = null;
	private final LayerManager layerManager = new LayerManager();
	
	private UIGraphicPaneBuilder() {}
	
	public static UIGraphicPaneBuilder getInstance() {
		return graphicPaneBuilder;
	}
	
	public Pane getPane() {
		this.init();
		return this.borderPane;
	}
	
	private void init() {
		if (this.borderPane != null)
			return;
	
		this.borderPane = new BorderPane();
		this.borderPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		this.borderPane.setTop(this.layerManager.getToolBar());
		this.borderPane.setCenter(this.layerManager.getPane());
		
		Label coordinateLabel = this.layerManager.getCoordinateLabel();
		coordinateLabel.setFont(new Font(10));
		coordinateLabel.setPadding(new Insets(10, 10, 10, 10));
		
		Region spacer = new Region();
		HBox hbox = new HBox(10);
		HBox.setHgrow(spacer, Priority.ALWAYS);
		hbox.getChildren().addAll(spacer, coordinateLabel);
		
		this.borderPane.setBottom(hbox);
	}
	
	public LayerManager getLayerManager() {
		return this.layerManager;
	}
}
