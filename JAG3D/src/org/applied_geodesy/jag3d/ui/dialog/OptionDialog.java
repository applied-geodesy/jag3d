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

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class OptionDialog {
	private static Window window;
	
	private OptionDialog() {}
	
	public static void setOwner(Window owner) {
		window = owner;
	}
	
	private static Optional<ButtonType> showDialog(AlertType type, String title, String header, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.initOwner(window);
		alert.setResizable(true);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		return alert.showAndWait();
	}
	
	public static Optional<ButtonType> showErrorDialog(String title, String header, String message) {
		return showDialog(AlertType.ERROR, title, header, message);
	}
	
	public static Optional<ButtonType> showWarningDialog(String title, String header, String message) {
		return showDialog(AlertType.WARNING, title, header, message);
	}
	
	public static Optional<ButtonType> showInformationDialog(String title, String header, String message) {
		return showDialog(AlertType.INFORMATION, title, header, message);
	}
	
	public static Optional<ButtonType> showConfirmationDialog(String title, String header, String message) {
		return showDialog(AlertType.CONFIRMATION, title, header, message);
	}
	
	public static Optional<ButtonType> showContentDialog(AlertType type, String title, String header, String message, Node node) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.initOwner(window);
		
		GridPane.setVgrow(node, Priority.ALWAYS);
		GridPane.setHgrow(node, Priority.ALWAYS);

		GridPane content = new GridPane();
		content.setMaxWidth(Double.MAX_VALUE);

		int row = 0;
		if (message != null) {
			Label label = new Label(message);
			content.add(label, 0, ++row);
		}
		content.add(node, 0, ++row);
		alert.getDialogPane().setContent(content);
		
		return alert.showAndWait();
    }
	
	public static Optional<ButtonType> showThrowableDialog(String title, String header, String message, Throwable throwable) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.initOwner(window);

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		throwable.printStackTrace(printWriter);
		String throwableText = stringWriter.toString();

		Label label = new Label("Stacktrace:");
		TextArea textArea = new TextArea(throwableText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);
		alert.getDialogPane().setExpandableContent(expContent);

		return alert.showAndWait();
    }
}