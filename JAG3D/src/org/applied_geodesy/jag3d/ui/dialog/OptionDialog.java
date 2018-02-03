package org.applied_geodesy.jag3d.ui.dialog;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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
//		alert.initStyle(StageStyle.UTILITY);
		alert.setResizable(true);
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
//		alert.initStyle(StageStyle.UTILITY);
		
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
//		alert.initStyle(StageStyle.UTILITY);

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

//    public static void showInformation(String title, String message) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.initStyle(StageStyle.UTILITY);
//        alert.setTitle("Information");
//        alert.setHeaderText(title);
//        alert.setContentText(message);
//
//        alert.showAndWait();
//    }
//
//    public static void showWarning(String title, String message) {
//        Alert alert = new Alert(Alert.AlertType.WARNING);
//        alert.initStyle(StageStyle.UTILITY);
//        alert.setTitle("Warning");
//        alert.setHeaderText(title);
//        alert.setContentText(message);
//
//        alert.showAndWait();
//    }
//
//    public static void showError(String title, String message) {
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.initStyle(StageStyle.UTILITY);
//        alert.setTitle("Error");
//        alert.setHeaderText(title);
//        alert.setContentText(message);
//
//        alert.showAndWait();
//    }
//
//    public static void showException(String title, String message, Exception exception) {
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.initStyle(StageStyle.UTILITY);
//        alert.setTitle("Exception");
//        alert.setHeaderText(title);
//        alert.setContentText(message);
//
//        StringWriter sw = new StringWriter();
//        PrintWriter pw = new PrintWriter(sw);
//        exception.printStackTrace(pw);
//        String exceptionText = sw.toString();
//
//        Label label = new Label("Details:");
//
//        TextArea textArea = new TextArea(exceptionText);
//        textArea.setEditable(false);
//        textArea.setWrapText(true);
//
//        textArea.setMaxWidth(Double.MAX_VALUE);
//        textArea.setMaxHeight(Double.MAX_VALUE);
//        GridPane.setVgrow(textArea, Priority.ALWAYS);
//        GridPane.setHgrow(textArea, Priority.ALWAYS);
//
//        GridPane expContent = new GridPane();
//        expContent.setMaxWidth(Double.MAX_VALUE);
//        expContent.add(label, 0, 0);
//        expContent.add(textArea, 0, 1);
//
//        alert.getDialogPane().setExpandableContent(expContent);
//
//        alert.showAndWait();
//    }
//
//    public static final String YES = "Yes";
//    public static final String NO = "No";
//    public static final String OK = "OK";
//    public static final String CANCEL = "Cancel";
//
//    public static String showConfirm(String title, String message, String... options) {
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.initStyle(StageStyle.UTILITY);
//        alert.setTitle("Choose an option");
//        alert.setHeaderText(title);
//        alert.setContentText(message);
//
//        //To make enter key press the actual focused button, not the first one. Just like pressing "space".
//        alert.getDialogPane().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//            if (event.getCode().equals(KeyCode.ENTER)) {
//                event.consume();
//                try {
//                    Robot r = new Robot();
//                    r.keyPress(java.awt.event.KeyEvent.VK_SPACE);
//                    r.keyRelease(java.awt.event.KeyEvent.VK_SPACE);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        if (options == null || options.length == 0) {
//            options = new String[]{OK, CANCEL};
//        }
//
//        List<ButtonType> buttons = new ArrayList<>();
//        for (String option : options) {
//            buttons.add(new ButtonType(option));
//        }
//
//        alert.getButtonTypes().setAll(buttons);
//
//        Optional<ButtonType> result = alert.showAndWait();
//        if (!result.isPresent()) {
//            return CANCEL;
//        } else {
//            return result.get().getText();
//        }
//    }
//
//    public static String showTextInput(String title, String message, String defaultValue) {
//        TextInputDialog dialog = new TextInputDialog(defaultValue);
//        dialog.initStyle(StageStyle.UTILITY);
//        dialog.setTitle("Input");
//        dialog.setHeaderText(title);
//        dialog.setContentText(message);
//
//        Optional<String> result = dialog.showAndWait();
//        if (result.isPresent()) {
//            return result.get();
//        } else {
//            return null;
//        }
//
//    }

}