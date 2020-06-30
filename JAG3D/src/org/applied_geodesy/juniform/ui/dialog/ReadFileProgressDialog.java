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

package org.applied_geodesy.juniform.ui.dialog;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.applied_geodesy.juniform.ui.table.UIParameterTableBuilder;
import org.applied_geodesy.juniform.ui.table.UIPointTableBuilder;
import org.applied_geodesy.juniform.ui.tree.UITreeBuilder;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.juniform.ui.i18n.I18N;
import org.applied_geodesy.util.io.FileProgressChangeListener;
import org.applied_geodesy.util.io.FileProgressEvent;
import org.applied_geodesy.util.io.SourceFileReader;
import org.applied_geodesy.util.io.FileProgressEvent.FileProgressEventType;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class ReadFileProgressDialog<T> {

	private class ReadFileTask extends Task<T> implements FileProgressChangeListener {
		
		private SourceFileReader<T> reader;
		private double processState = 0.0;
		private List<File> selectedFiles;
		private final static long DEFAULT_PARTICLE_SIZE = 1024*64;
		private long totalBytes = 0L, readedBytes = 0L, particleSize = DEFAULT_PARTICLE_SIZE;
		private int cnt = 1;
		public ReadFileTask(SourceFileReader<T> reader, List<File> selectedFiles) {
			this.reader = reader;
			this.selectedFiles = selectedFiles;
		}

		@Override
		protected T call() throws Exception {
			try {
				preventClosing = true;

				this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, ProgressIndicator.INDETERMINATE_PROGRESS);
				this.updateProgressMessage(i18n.getString("ReadFileProgressDialog.pleasewait.label", "Please wait\u2026"));
				
				this.reader.addFileProgressChangeListener(this);

				if (this.isCancelled())
					return null;
				
				this.processState = 0.0;
				this.updateProgress(this.processState, 1.0);
				
				for (File file : this.selectedFiles)
					this.totalBytes += Files.size(file.toPath());
				
				// estimate particle size of file size
				this.particleSize = DEFAULT_PARTICLE_SIZE;
				this.cnt = 1;
				
				int ratio = (int)Math.ceil(this.totalBytes / this.particleSize);
				if (ratio < 15)
					ratio = 15;
				if (ratio > 30)
					ratio = 30;
				this.particleSize = (long)(this.totalBytes / ratio);

				this.reader.reset();
				T result = null;
				for (int i = 0; i < this.selectedFiles.size(); i++) {
					File file = this.selectedFiles.get(i);
					this.updateProgressMessage(String.format(Locale.ENGLISH, i18n.getString("ReadFileProgressDialog.sourcefile.label", "File %d of %d - Please wait\u2026\r\n%s"), (i+1), this.selectedFiles.size(), file.getName()));
					this.reader.setPath(file.toPath(), Boolean.FALSE);
					result = this.reader.readAndImport();
					this.updateProgressMessage(i18n.getString("ReadFileProgressDialog.pleasewait.label", "Please wait\u2026"));
					this.readedBytes += Files.size(file.toPath());
				}

				if (this.isCancelled())
					return null;

				this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, ProgressIndicator.INDETERMINATE_PROGRESS);
				this.updateMessage(i18n.getString("ReadFileProgressDialog.save.label", "Save results\u2026"));
				this.updateProgressMessage(i18n.getString("ReadFileProgressDialog.pleasewait.label", "Please wait\u2026"));
				
				return result;
			}
			finally {
				this.destroy();
				this.updateProgressMessage(null);
				preventClosing = false;
			}
		}
		
		@Override 
		protected void succeeded() {
			preventClosing = false;
			super.succeeded();
			hideDialog();
		}

		@Override 
		protected void failed() {
			result = null;
			preventClosing = false;
			super.failed();
			hideDialog();
		}
		
		@Override
		protected void cancelled() {
			result = null;
			super.cancelled();
			if (this.reader != null) {
				this.reader.interrupt();
			}
		}
		
		private void hideDialog() {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (!preventClosing)
						dialog.hide();
				}
			});
		}

		private void updateProgressMessage(String message) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					progressLabel.setText(message);
				}
			});
		}

		@Override
		protected void updateProgress(double workDone, double max) {
			super.updateProgress(workDone, max);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					progressIndicator.setProgress(workDone);
					if (workDone >= 1.0) {
						Node node = progressIndicator.lookup(".percentage");
						if (node != null && node instanceof Text) {
							Text text = (Text)node;
							text.setText(i18n.getString("ReadFileProgressDialog.done.label", "Done"));
							progressIndicator.setPrefWidth(text.getLayoutBounds().getWidth());
						}
					}
				}
			});
		}

		private void destroy() {
			if (this.reader != null)
				this.reader.removeFileProgressChangeListener(this);
			this.reader = null;
			this.selectedFiles = null;
			this.totalBytes = 0;
			this.readedBytes = 0;
			this.particleSize = DEFAULT_PARTICLE_SIZE;
			this.cnt = 1;
			result = null;
		}

		@Override
		public void fileProgressChanged(FileProgressEvent evt) {

			if (evt.getEventType() == FileProgressEventType.READ_LINE) {
				long readedBytes = evt.getReadedBytes();
//				long totalBytes  = evt.getTotalBytes();
				
				readedBytes += this.readedBytes;
				
				if (readedBytes > this.cnt * this.particleSize) {
					this.cnt++;
					
					double frac = Math.min((double)readedBytes / this.totalBytes, 1.0);
					this.processState = Math.max(this.processState, frac);

					this.updateProgress(this.processState, 1.0);
				}
			}
		}
	}

	private static Window window;
	private ReadFileTask readFileTask;
	private boolean preventClosing = false;
	private I18N i18n = I18N.getInstance();
	private Dialog<T> dialog = null;
	private ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
	private Label progressLabel = new Label();
	private T result;
	public ReadFileProgressDialog() {}

	public Optional<T> showAndWait(SourceFileReader<T> reader, List<File> selectedFiles) {
		this.init();
		this.reset();

		this.process(reader, selectedFiles);

		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return this.dialog.showAndWait();
	}

	public static void setOwner(Window owner) {
		window = owner;
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<T>();
		this.dialog.initOwner(window);
		this.dialog.setTitle(i18n.getString("ReadFileProgressDialog.title", "File reader"));
		this.dialog.setHeaderText(i18n.getString("ReadFileProgressDialog.header", "Read source file\u2026"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		VBox vbox = new VBox();
		vbox.getChildren().addAll(this.createProgressPane());

		this.dialog.getDialogPane().getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (preventClosing) {
					event.consume();
					if (readFileTask != null)
						readFileTask.cancelled();
				}
			}
		});

		this.dialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
			@Override
			public void handle(DialogEvent event) {
				if (preventClosing) {
					event.consume();
					if (readFileTask != null)
						readFileTask.cancelled();
				}
			}
		});
		
		this.dialog.setResultConverter(new Callback<ButtonType, T>() {
			@Override
			public T call(ButtonType buttonType) {
				return result;
			}
		});

		this.dialog.getDialogPane().setContent(vbox);
	}

	private void reset() {
		this.result = null;
		this.progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		this.progressLabel.setText(null);
		this.preventClosing = false;
	}

	private Node createProgressPane() {
		GridPane gridPane = new GridPane();
		gridPane.setMinWidth(400);
		gridPane.setHgap(20);
		gridPane.setVgap(15);
		gridPane.setPadding(new Insets(5, 10, 5, 10)); // oben, recht, unten, links

		this.progressIndicator.setMinWidth(50);
		this.progressIndicator.setMinHeight(50);

		gridPane.add(this.progressIndicator, 0, 0, 1, 2); // col, row, colspan, rowspan
		gridPane.add(this.progressLabel,     1, 0);

		return gridPane;
	}

	private void process(SourceFileReader<T> reader, List<File> selectedFiles) {
		this.reset();
		this.readFileTask = new ReadFileTask(reader, selectedFiles);
		this.readFileTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				result = readFileTask.getValue();
			}
		});

		this.readFileTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				preventClosing = false;
				dialog.hide();
				Throwable throwable = readFileTask.getException(); 
				if (throwable != null) {
					Platform.runLater(new Runnable() {
						@Override public void run() {
							OptionDialog.showThrowableDialog (
									i18n.getString("ReadFileProgressDialog.message.error.import.exception.title", "I/O Error"),
									i18n.getString("ReadFileProgressDialog.message.error.import.exception.header", "Error, could not import selected file."),
									i18n.getString("ReadFileProgressDialog.message.error.import.exception.message", "An exception has occurred during file import."),
									throwable
									);
						}
					});
					throwable.printStackTrace();
				}
				UITreeBuilder.getInstance().getTree().getSelectionModel().select(0);
				UIPointTableBuilder.getInstance().getTable().refresh();
				UIParameterTableBuilder.getInstance().getTable().refresh();
			}
		});

		Thread th = new Thread(this.readFileTask);
		th.setDaemon(true);
		th.start();
	}
}
