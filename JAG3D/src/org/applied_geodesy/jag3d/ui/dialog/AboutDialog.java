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

package org.applied_geodesy.jag3d.ui.dialog;

import java.util.Optional;

import org.applied_geodesy.version.jag3d.Version;
import org.applied_geodesy.version.jag3d.VersionType;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class AboutDialog {
	private static AboutDialog aboutDialog = new AboutDialog();
	private Dialog<Void> dialog = null;
	private Window window;
	private HostServices hostServices;
	private AboutDialog() {}

	public static void setOwner(Window owner) {
		aboutDialog.window = owner;
	}
	
	public static void setHostServices(HostServices hostServices) {
		aboutDialog.hostServices = hostServices;
	}

	public static Optional<Void> showAndWait() {
		aboutDialog.init();
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	try {
            		aboutDialog.dialog.getDialogPane().requestLayout();
            		Stage stage = (Stage) aboutDialog.dialog.getDialogPane().getScene().getWindow();
            		stage.sizeToScene();
            	} 
            	catch (Exception e) {
            		e.printStackTrace();
            	}
            }
		});
		return aboutDialog.dialog.showAndWait();
	}


	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<Void>();
		this.dialog.setTitle("JAG3D");
		this.dialog.setHeaderText("JAG3D \u2014 Java\u00B7Applied\u00B7Geodesy\u00B73D");
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
	}

	private Node createPane() {
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(15);
		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(7,7,7,7)); // oben, recht, unten, links
//		gridPane.setGridLinesVisible(true);
		
		Text applicationText = new Text("Least-Squares Adjustment Software for Geodetic Sciences");
		applicationText.setFont(Font.font("SansSerif", FontWeight.NORMAL, FontPosture.REGULAR, 14));
		applicationText.setTextAlignment(TextAlignment.CENTER);
		
		// left-hand labels
		Label authorLabel      = this.createLabel("Author:");	
		Label licenceLabel     = this.createLabel("Licence:");
		Label uiVersionLabel   = this.createLabel("UI version:");
		Label dbVersionLabel   = this.createLabel("DB version:");
		Label coreVersionLabel = this.createLabel("AC version:");
		Label homePageLabel    = this.createLabel("Homepage:");
		Label thirdPartyLabel  = this.createLabel("3rd Party Libraries:");
		Label iconLabel        = this.createLabel("Icon set:");

		// right-hand labels
		Label author      = this.createLabel("Michael L\u00F6sler\r\n\r\nBahnhofsplatz 3\r\nDE-61118 Bad Vilbel");
		Label licence     = this.createLabel("GNU General Public License v3.0");
		Label uiVersion   = this.createLabel("v" + Version.get(VersionType.USER_INTERFACE));
		Label dbVersion   = this.createLabel("v" + Version.get(VersionType.DATABASE));
		Label coreVersion = this.createLabel("v" + Version.get(VersionType.ADJUSTMENT_CORE));
		
		Hyperlink homePageLink = new Hyperlink("software.applied-geodesy.org");
		homePageLink.setTooltip(new Tooltip("Go to software.applied-geodesy.org"));
		homePageLink.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		homePageLink.setPadding(new Insets(0));
		homePageLink.setVisited(false);
		homePageLink.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (hostServices != null)
					hostServices.showDocument("http://software.applied-geodesy.org");
				homePageLink.setVisited(false);
			}
		});
		

		Label thirdParty = this.createLabel(""
				+ "\u2219 MTJ: GNU Lesser General Public License v3.0\r\n"
				+ "\u2219 FreeMarker: Apache License v2.0\r\n"
				+ "\u2219 netlib-java: BSD License\r\n"
				+ "\u2219 JLAPACK: BSD License\r\n"
				+ "\u2219 HSQLDB: BSD License\r\n"
				+ "\u2219 JDistlib: GNU General Public License v2.0\r\n"
				+ "\u2219 OpenCSV: Apache License v2.0\r\n"
				+ "\u2219 Launch4J: BSD/MIT License");
		

		Label icon = this.createLabel("Subway Icon Set: CC BY 4.0");

		authorLabel.setLabelFor(author);
		licenceLabel.setLabelFor(licence);
		uiVersionLabel.setLabelFor(uiVersion);
		dbVersionLabel.setLabelFor(dbVersion);
		coreVersionLabel.setLabelFor(coreVersion);
		homePageLabel.setLabelFor(homePageLink);
		thirdPartyLabel.setLabelFor(thirdParty);
		iconLabel.setLabelFor(icon);
	
		GridPane.setHalignment(applicationText, HPos.CENTER);
		int row = 0;
		gridPane.add(applicationText, 0, row++, 2, 1);
		
		gridPane.add(authorLabel, 0, row);
		gridPane.add(author, 1, row++);
		
		gridPane.add(licenceLabel, 0, row);
		gridPane.add(licence, 1, row++);
		
		gridPane.add(uiVersionLabel, 0, row);
		gridPane.add(uiVersion, 1, row++);
		
		gridPane.add(dbVersionLabel, 0, row);
		gridPane.add(dbVersion, 1, row++);
		
		gridPane.add(coreVersionLabel, 0, row);
		gridPane.add(coreVersion, 1, row++);
		
		gridPane.add(homePageLabel, 0, row);
		gridPane.add(homePageLink, 1, row++);
		
		gridPane.add(thirdPartyLabel, 0, row);
		gridPane.add(thirdParty, 1, row++);
		
		gridPane.add(iconLabel, 0, row);
		gridPane.add(icon, 1, row++);

		ScrollPane scroller = new ScrollPane(gridPane);
		scroller.setPadding(new Insets(10, 10, 10, 10));
		scroller.setFitToHeight(true);
		scroller.setFitToWidth(true);
		
		return gridPane;
	}
	
	private Label createLabel(String text) {
		Label label = new Label(text);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxHeight(Double.MAX_VALUE);
		label.setAlignment(Pos.TOP_LEFT);
		return label;
	}
}
