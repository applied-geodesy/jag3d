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

package org.applied_geodesy.jag3d.ui.graphic.layer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateChangedListener;
import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateEvent;
import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dialog.OptionDialog;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.Layer;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerType;
import org.applied_geodesy.jag3d.ui.graphic.layer.ToolbarType;
import org.applied_geodesy.jag3d.ui.graphic.layer.dialog.LayerManagerDialog;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;
import org.applied_geodesy.jag3d.ui.graphic.sql.SQLGraphicManager;
import org.applied_geodesy.jag3d.ui.io.DefaultFileChooser;
import org.applied_geodesy.util.ImageUtils;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;

public class LayerManager {
	private class DatabaseStateChangedListener implements ProjectDatabaseStateChangedListener {
		@Override
		public void projectDatabaseStateChanged(ProjectDatabaseStateEvent evt) {
			if (layerToolbar != null) {
				boolean disable = evt.getEventType() != ProjectDatabaseStateType.OPENED;
				layerToolbar.setDisable(disable);
				
				if (evt.getEventType() == ProjectDatabaseStateType.CLOSED) {
					currentGraphicExtent.set(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
					clearAllLayers();
					draw();
				}
			}
		}
	}

	private class ToolbarActionEventHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			ToolbarType type = ToolbarType.NONE;
			if (event.getSource() instanceof Button && ((Button)event.getSource()).getUserData() instanceof ToolbarType) {
				Button button = (Button)event.getSource();
				type = (ToolbarType)button.getUserData();
			}
			else if (event.getSource() instanceof ToggleButton && ((ToggleButton)event.getSource()).getUserData() instanceof ToolbarType) {
				ToggleButton button = (ToggleButton)event.getSource();
				type = button.isSelected() ? (ToolbarType)button.getUserData() : ToolbarType.NONE;
			}
			else
				return;
			
			action(type);
		}
	}
	
	private class ScaleChangeListener implements ChangeListener<Double> {
		private Layer[] layers;
		ScaleChangeListener(Layer... layer) {
			this.layers = layer;
		}
		@Override
		public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
			if (newValue != null && !Double.isInfinite(newValue) && !Double.isNaN(newValue)) {
				for (Layer layer : layers) {
					if (layer.getLayerType() == LayerType.POINT_SHIFT || 
							layer.getLayerType() == LayerType.PRINCIPAL_COMPONENT_HORIZONTAL || 
							layer.getLayerType() == LayerType.PRINCIPAL_COMPONENT_VERTICAL)
						((ArrowLayer)layer).setVectorScale(newValue);
					else 
						if (layer.getLayerType() == LayerType.ABSOLUTE_CONFIDENCE)
							((AbsoluteConfidenceLayer)layer).setConfidenceScale(newValue);
				}
				save(newValue);
				if (!ignoreChangeEvent)
					draw();
			}			
		}
	}
	
	private class VisiblePropertyChangeListener implements ChangeListener<Boolean> {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (!ignoreChangeEvent)
				draw();
		}
	}

	private class ToggleChangeListener implements ChangeListener<Toggle> {
		@Override
		public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
        	if (newValue == null && oldValue != null)
        		oldValue.setSelected(true);
        }
	}
	
	private class GraphicExtentChangeListener implements ChangeListener<Boolean> {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			save(currentGraphicExtent);
		}
	}
	
	private Label coordinatePanel = new Label();
	private I18N i18n = I18N.getInstance();
	private ToolBar layerToolbar = new ToolBar();
	private final GraphicExtent currentGraphicExtent = new GraphicExtent();
	private final ResizableCanvas resizableGraphicCanvas = new ResizableCanvas(this.currentGraphicExtent);
	private ObservableList<Layer> layers = this.resizableGraphicCanvas.getLayers();
	private StackPane stackPane = new StackPane(); // Mouse- and Plotlayers
	private final MouseNavigationCanvas mouseNavigationCanvas = new MouseNavigationCanvas(this);
//	private ObjectProperty<Color> color = new SimpleObjectProperty<Color>(Color.rgb(255, 255, 255, 1.0)); //0-255
	private VisiblePropertyChangeListener visibleChangeListener = new VisiblePropertyChangeListener();
	private boolean ignoreChangeEvent = false;
	
	private Spinner<Double> scaleSpinner;
	public LayerManager() {
		this.init();
	}
	
	private void init() {
		this.initPane();
		this.initToolBar();
		this.initCoordinateLabel();
		
		this.initLayers();

//		this.color.addListener(new ColorChangeListener());
		this.currentGraphicExtent.drawingBoardWidthProperty().bind(this.stackPane.widthProperty());
		this.currentGraphicExtent.drawingBoardHeightProperty().bind(this.stackPane.heightProperty());

		GraphicExtentChangeListener graphicExtentChangeListener = new GraphicExtentChangeListener();
//		this.currentGraphicExtent.minXProperty().addListener(graphicExtentChangeListener);
//		this.currentGraphicExtent.maxXProperty().addListener(graphicExtentChangeListener);
//		this.currentGraphicExtent.minYProperty().addListener(graphicExtentChangeListener);
//		this.currentGraphicExtent.maxYProperty().addListener(graphicExtentChangeListener);
		this.currentGraphicExtent.extendedProperty().addListener(graphicExtentChangeListener);
		
		SQLManager.getInstance().addProjectDatabaseStateChangedListener(new DatabaseStateChangedListener());
	}
	
	private void initLayers() {
		LayerType layerTypes[] = LayerType.values();

		for (LayerType layerType : layerTypes) {
			Layer layer = null; 

			// create/add layer
			switch(layerType) {
			case REFERENCE_POINT_APRIORI:
			case REFERENCE_POINT_APOSTERIORI:				
			case STOCHASTIC_POINT_APRIORI:
			case STOCHASTIC_POINT_APOSTERIORI:				
			case DATUM_POINT_APRIORI:	
			case DATUM_POINT_APOSTERIORI:
			case NEW_POINT_APRIORI:
			case NEW_POINT_APOSTERIORI:
				layer = new PointLayer(layerType);
				break;

			case OBSERVATION_APRIORI:			
			case OBSERVATION_APOSTERIORI:
				layer = new ObservationLayer(layerType);
				break;

			case PRINCIPAL_COMPONENT_HORIZONTAL:
			case PRINCIPAL_COMPONENT_VERTICAL:
				layer = new PrincipalComponentArrowLayer(layerType);;
				break;

			case POINT_SHIFT:
				layer = new PointShiftArrowLayer(layerType);;
				break;

			case ABSOLUTE_CONFIDENCE:
				layer = new AbsoluteConfidenceLayer(layerType);
				break;

			case RELATIVE_CONFIDENCE:
				layer = new RelativeConfidenceLayer(layerType);
				break;

			}
			
			if (layer != null)
				this.add(layer);
		}
		
		AbsoluteConfidenceLayer absoluteConfidenceLayer = (AbsoluteConfidenceLayer)this.getLayer(LayerType.ABSOLUTE_CONFIDENCE);
		absoluteConfidenceLayer.addAll(
				(PointLayer)this.getLayer(LayerType.STOCHASTIC_POINT_APOSTERIORI),
				(PointLayer)this.getLayer(LayerType.DATUM_POINT_APOSTERIORI),
				(PointLayer)this.getLayer(LayerType.NEW_POINT_APOSTERIORI)
				);
		
		
		PrincipalComponentArrowLayer principalComponentHorizontalArrowLayer = (PrincipalComponentArrowLayer)this.getLayer(LayerType.PRINCIPAL_COMPONENT_HORIZONTAL);
		principalComponentHorizontalArrowLayer.addAll(
				(PointLayer)this.getLayer(LayerType.STOCHASTIC_POINT_APOSTERIORI),
				(PointLayer)this.getLayer(LayerType.DATUM_POINT_APOSTERIORI),
				(PointLayer)this.getLayer(LayerType.NEW_POINT_APOSTERIORI)
				);
		
		PrincipalComponentArrowLayer principalComponentVerticalArrowLayer = (PrincipalComponentArrowLayer)this.getLayer(LayerType.PRINCIPAL_COMPONENT_VERTICAL);
		principalComponentVerticalArrowLayer.addAll(
				(PointLayer)this.getLayer(LayerType.STOCHASTIC_POINT_APOSTERIORI),
				(PointLayer)this.getLayer(LayerType.DATUM_POINT_APOSTERIORI),
				(PointLayer)this.getLayer(LayerType.NEW_POINT_APOSTERIORI)
				);
		
		PointShiftArrowLayer pointShiftArrowLayer = (PointShiftArrowLayer)this.getLayer(LayerType.POINT_SHIFT);
		RelativeConfidenceLayer relativeConfidenceLayer = (RelativeConfidenceLayer)this.getLayer(LayerType.RELATIVE_CONFIDENCE);
		relativeConfidenceLayer.add(pointShiftArrowLayer);
		
		// add scale change listener
		pointShiftArrowLayer.setVectorScale(this.scaleSpinner.getValueFactory().getValue());
		principalComponentHorizontalArrowLayer.setVectorScale(this.scaleSpinner.getValueFactory().getValue());
		principalComponentVerticalArrowLayer.setVectorScale(this.scaleSpinner.getValueFactory().getValue());
		absoluteConfidenceLayer.setConfidenceScale(this.scaleSpinner.getValueFactory().getValue());
		this.scaleSpinner.valueProperty().addListener(new ScaleChangeListener(pointShiftArrowLayer, principalComponentHorizontalArrowLayer, principalComponentVerticalArrowLayer, absoluteConfidenceLayer));
		
		// Bind apriori symbol size of point to observation layer
		((ObservationLayer)this.getLayer(LayerType.OBSERVATION_APRIORI)).pointSymbolSizeProperty().bind(
				Bindings.max(
						Bindings.max(
								Bindings.max(
										this.getLayer(LayerType.REFERENCE_POINT_APRIORI).symbolSizeProperty(), 
										this.getLayer(LayerType.STOCHASTIC_POINT_APRIORI).symbolSizeProperty()),
								this.getLayer(LayerType.DATUM_POINT_APRIORI).symbolSizeProperty()),
						this.getLayer(LayerType.NEW_POINT_APRIORI).symbolSizeProperty())
				);
		
		// Bind aposteriori symbol size of point to observation layer
		((ObservationLayer)this.getLayer(LayerType.OBSERVATION_APOSTERIORI)).pointSymbolSizeProperty().bind(
				Bindings.max(
						Bindings.max(
								Bindings.max(
										this.getLayer(LayerType.REFERENCE_POINT_APOSTERIORI).symbolSizeProperty(), 
										this.getLayer(LayerType.STOCHASTIC_POINT_APOSTERIORI).symbolSizeProperty()),
								this.getLayer(LayerType.DATUM_POINT_APOSTERIORI).symbolSizeProperty()),
						this.getLayer(LayerType.NEW_POINT_APOSTERIORI).symbolSizeProperty())
				);
	}
	
	private void add(Layer layer) {
		this.currentGraphicExtent.merge(layer.getMaximumGraphicExtent());
		layer.visibleProperty().addListener(this.visibleChangeListener);
		// add to List
		this.layers.add(layer);
	}
	
	public GraphicExtent getCurrentGraphicExtent() {
		return this.currentGraphicExtent;
	}
	
	public void clearAllLayers() {
		for (Layer layer : this.layers) {
			layer.clearLayer();
		}
	}
	
	public Layer getLayer(LayerType layerType) {
		for (Layer layer : this.layers) {
			if (layer.getLayerType() == layerType)
				return layer;
		}
		return null;
	}
	
	public void draw() {
		this.resizableGraphicCanvas.draw();
		this.mouseNavigationCanvas.draw();
	}
	
	public void reorderLayer(List<Layer> layers) {
		this.layers.clear();
		
		for (Layer layer : layers) {
			if (layer != null && layer.getLayerType() != null)
				this.layers.add(layer);
		}
	}
	
	public Pane getPane() {
		return this.stackPane;
	}

	public ToolBar getToolBar() {
		return this.layerToolbar;
	}
	
	public Label getCoordinateLabel() {
		return this.coordinatePanel;
	}
	
	public void setEllipseScale(double scale) {
		this.scaleSpinner.getValueFactory().setValue(scale);
	}
	
	private void initPane() {
		//BackgroundFill fill = new BackgroundFill(this.getColor(), CornerRadii.EMPTY, Insets.EMPTY);
		BackgroundFill fill = new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY);
		Background background = new Background(fill);
		
		this.stackPane.setBackground(background);
		this.stackPane.setMinSize(0, 0);
		this.stackPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		this.stackPane.getChildren().addAll(this.resizableGraphicCanvas, this.mouseNavigationCanvas);
	}

	private void initCoordinateLabel() {
		this.coordinatePanel.setTextAlignment(TextAlignment.RIGHT);
	}
	
	private void initToolBar() {
		ToolbarActionEventHandler toolbarActionEventHandler = new ToolbarActionEventHandler();
		ToggleGroup group = new ToggleGroup();

		ToggleButton moveButton = this.createToggleButton(
				"graphic_move_32x32.png", 
				i18n.getString("LayerManager.toolbar.button.move.tooltip", "Move graphic"),
				ToolbarType.MOVE,
				toolbarActionEventHandler
				);

		ToggleButton windowZoomButton = this.createToggleButton(
				"graphic_window_zoom_32x32.png", 
				i18n.getString("LayerManager.toolbar.button.window_zoom.tooltip", "Window zoom"),
				ToolbarType.WINDOW_ZOOM,
				toolbarActionEventHandler
				);

		Button zoomInButton  = this.createButton(
				"graphic_zoom_in_32x32.png", 
				i18n.getString("LayerManager.toolbar.button.zoom_in.tooltip", "Zoom in"),
				ToolbarType.ZOOM_IN,
				toolbarActionEventHandler
				);

		Button zoomOutButton = this.createButton(
				"graphic_zoom_out_32x32.png", 
				i18n.getString("LayerManager.toolbar.button.zoom_out.tooltip", "Zoom out"),
				ToolbarType.ZOOM_OUT,
				toolbarActionEventHandler
				);

		Button expandButton  = this.createButton(
				"graphic_expand_32x32.png", 
				i18n.getString("LayerManager.toolbar.button.expand.tooltip", "Expand graphic"),
				ToolbarType.EXPAND,
				toolbarActionEventHandler
				);
		
		Button redrawButton  = this.createButton(
				"graphic_refresh_32x32.png", 
				i18n.getString("LayerManager.toolbar.button.refresh.tooltip", "Refresh graphic from database"),
				ToolbarType.REDRAW,
				toolbarActionEventHandler
				);
		
		Button layerButton  = this.createButton(
				"graphic_layer_32x32.png", 
				i18n.getString("LayerManager.toolbar.button.layers.tooltip", "Layer properties"),
				ToolbarType.LAYER_PROPERTIES,
				toolbarActionEventHandler
				);
		
		Button exportButton  = this.createButton(
				"graphic_export_32x32.png", 
				i18n.getString("LayerManager.toolbar.button.export.tooltip", "Export graphic"),
				ToolbarType.EXPORT,
				toolbarActionEventHandler
				);
				
		this.scaleSpinner = this.createDoubleSpinner(1, Double.MAX_VALUE, 5000, 250, i18n.getString("LayerManager.toolbar.scale.tooltip", "Scale of arrows and confidences"), 0);
		this.scaleSpinner.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		Label scaleLabel = new Label(" : 1");
		scaleLabel.setGraphic(this.scaleSpinner);
		scaleLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		scaleLabel.setContentDisplay(ContentDisplay.LEFT);
		
		group.getToggles().addAll(
				moveButton,
				windowZoomButton
				);
		
		group.selectedToggleProperty().addListener(new ToggleChangeListener());

		this.layerToolbar.getItems().addAll(
				moveButton,
				windowZoomButton,
				new Separator(),
				zoomInButton,
				zoomOutButton,
				new Separator(),
				expandButton,
				redrawButton,
				exportButton,
				new Separator(),
				layerButton,
				new Separator(),
				scaleLabel
				);
		
		boolean disable = !SQLManager.getInstance().hasDatabase();
		windowZoomButton.setSelected(true); // pre-selection
		action(ToolbarType.WINDOW_ZOOM);
		this.layerToolbar.setDisable(disable);
	}
	
	private void action(ToolbarType type) {	
		switch(type) {
		case EXPAND:
			this.expand();
			break;
			
		case REDRAW:
			this.redraw();
			break;
			
		case ZOOM_IN:
			this.zoomIn();
			break;
			
		case ZOOM_OUT:
			this.zoomOut();
			break;
			
		case EXPORT:
			saveSnapshot();
			break;
			
		case LAYER_PROPERTIES:
			layerProperties();
			break;
			
		case NONE:
		case MOVE:
		case WINDOW_ZOOM:
			this.mouseNavigationCanvas.setToolbarType(type);
			break;
			
		}
	}
	
	public void redraw() {
		SQLGraphicManager sqlGraphicManager = SQLManager.getInstance().getSQLGraphicManager();
		if (sqlGraphicManager == null) 
			return;

		try {
			this.ignoreChangeEvent = true;
			sqlGraphicManager.load(this);
			sqlGraphicManager.loadEllipseScale(this);
			if (!sqlGraphicManager.load(this.getCurrentGraphicExtent()))
				this.expand();
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("LayerManager.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("LayerManager.message.error.load.exception.header", "Error, could not create plot from database."),
							i18n.getString("LayerManager.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		} finally {
			this.ignoreChangeEvent = false;
			//this.expand();
			this.draw();
		}
	}
	
	public void layerProperties() {
		LayerManagerDialog.showAndWait(this, this.layers);
	}
	
	public void expand() {
		GraphicExtent maxGraphicExtent = new GraphicExtent();
		for (Layer layer : this.layers) {
			if (layer.isVisible())
				maxGraphicExtent.merge(layer.getMaximumGraphicExtent());
		}
		
		if (maxGraphicExtent.getMinX() == Double.MAX_VALUE && 
				maxGraphicExtent.getMinY() == Double.MAX_VALUE && 
				maxGraphicExtent.getMaxX() == Double.MIN_VALUE && 
				maxGraphicExtent.getMaxY() == Double.MIN_VALUE) {
			//this.redraw(); 
		}
		else {
			this.currentGraphicExtent.set(maxGraphicExtent);
			//this.draw();
			this.zoomOut();
		}
	}
	
	public void zoomIn() {
		double scale = this.currentGraphicExtent.getScale();
		this.currentGraphicExtent.setScale(scale / 1.15);
		this.draw();
	}
	
	public void zoomOut() {
		double scale = this.currentGraphicExtent.getScale();
		this.currentGraphicExtent.setScale(scale * 1.15);
		this.draw();
	}
	
	public void saveSnapshot() {
		double pixelScale = 1.5;
		double width  = this.stackPane.getWidth();
		double height = this.stackPane.getHeight();

		if (height > 0 && width > 0) {
			ExtensionFilter extensionFilters[] = new ExtensionFilter[] {
					new ExtensionFilter(i18n.getString("LayerManager.extension.filled.png",      "Portable Network Graphics (PNG)"), "*.png"),
					new ExtensionFilter(i18n.getString("LayerManager.extension.transparent.png", "Transparent Portable Network Graphics (PNG)"), "*.png")
			};
			File outputFile = DefaultFileChooser.showSaveDialog(
					i18n.getString("LayerManager.export.title", "Save current graphic"), 
					null, 
					extensionFilters);
			
			if (outputFile != null) {				
				try {
					boolean transparentImage = DefaultFileChooser.getSelectedExtensionFilter() == extensionFilters[1];
					SnapshotParameters snapshotParameters = new SnapshotParameters();
					snapshotParameters.setFill(transparentImage ? Color.TRANSPARENT : null);
					snapshotParameters.setTransform(Transform.scale(pixelScale, pixelScale));
					WritableImage writableImage = new WritableImage((int)Math.rint(pixelScale*width), (int)Math.rint(pixelScale*height));
					this.stackPane.snapshot(snapshotParameters, writableImage);
					
					BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
					ImageIO.write(bufferedImage, "PNG", outputFile);
				} catch (Exception e) {
					e.printStackTrace();
					Platform.runLater(new Runnable() {
						@Override public void run() {
							OptionDialog.showThrowableDialog (
									i18n.getString("LayerManager.message.error.save.image.exception.title", "Unexpected Error"),
									i18n.getString("LayerManager.message.error.save.image.exception.header", "Error, could not save plot as image."),
									i18n.getString("LayerManager.message.error.save.image.exception.message", "An exception has occurred during image export."),
									e
									);
						}
					});
				}
			}
		}
	}

	private ToggleButton createToggleButton(String iconName, String tooltip, ToolbarType type, ToolbarActionEventHandler toolbarActionEventHandler) {
		ToggleButton toggleButton = new ToggleButton();
		toggleButton.setTooltip(new Tooltip(tooltip));
		try {
			toggleButton.setGraphic(new ImageView(ImageUtils.getImage(iconName)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		toggleButton.setPadding(new Insets(0, 0, 0, 0));
		toggleButton.setUserData(type);
		toggleButton.setOnAction(toolbarActionEventHandler);
		return toggleButton;
	}

	private Button createButton(String iconName, String tooltip, ToolbarType type, ToolbarActionEventHandler toolbarActionEventHandler) {
		Button button = new Button();
		button.setTooltip(new Tooltip(tooltip));
		try {
			button.setGraphic(new ImageView(ImageUtils.getImage(iconName)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		button.setPadding(new Insets(0, 0, 0, 0));
		button.setUserData(type);
		button.setOnAction(toolbarActionEventHandler);
		return button;
	}
	private Spinner<Double> createDoubleSpinner(double min, double max, double initValue, double amountToStepBy, String tooltip, int digits) {
		NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
		numberFormat.setMaximumFractionDigits(digits);
		numberFormat.setMinimumFractionDigits(digits);
		numberFormat.setGroupingUsed(false);

		StringConverter<Double> converter = new StringConverter<Double>() {
			@Override
			public Double fromString(String s) {
				if (s == null || s.trim().isEmpty())
					return null;
				else {
					try {
						return numberFormat.parse(s).doubleValue();
					}catch (Exception nfe) {
						nfe.printStackTrace();
					}
				}
				return null;
			}

			@Override
			public String toString(Double d) {
				return d == null ? "" : numberFormat.format(d);
			}
		};

		SpinnerValueFactory.DoubleSpinnerValueFactory doubleFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max);
		Spinner<Double> doubleSpinner = new Spinner<Double>();
		doubleSpinner.setEditable(true);
		doubleSpinner.setValueFactory(doubleFactory);

		doubleFactory.setConverter(converter);
		doubleFactory.setAmountToStepBy(amountToStepBy);

		TextFormatter<Double> formatter = new TextFormatter<Double>(doubleFactory.getConverter(), doubleFactory.getValue());
		doubleSpinner.getEditor().setTextFormatter(formatter);
		doubleSpinner.getEditor().setAlignment(Pos.BOTTOM_RIGHT);
		doubleFactory.valueProperty().bindBidirectional(formatter.valueProperty());

		doubleSpinner.setPrefWidth(100);
		doubleSpinner.setMaxWidth(Double.MAX_VALUE);
		doubleSpinner.setTooltip(new Tooltip(tooltip));
		
		doubleFactory.setValue(initValue);
		
		doubleFactory.valueProperty().addListener(new ChangeListener<Double>() {
			@Override
			public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
				if (newValue == null)
					doubleFactory.setValue(oldValue);
			}
		});
		return doubleSpinner;
	}
	
	private void save(double newScale) {
		SQLGraphicManager sqlGraphicManager = SQLManager.getInstance().getSQLGraphicManager();
		if (sqlGraphicManager == null || Double.isNaN(newScale) || Double.isInfinite(newScale))
			return;
		
		try {
			sqlGraphicManager.saveEllipseScale(newScale);
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("LayerManager.message.error.save.scale.exception.title", "Unexpected SQL-Error"),
							i18n.getString("LayerManager.message.error.save.scale.exception.header", "Error, could not save scale parameter to database."),
							i18n.getString("LayerManager.message.error.save.scale.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
	
	private void save(GraphicExtent currentGraphicExtent) {
		SQLGraphicManager sqlGraphicManager = SQLManager.getInstance().getSQLGraphicManager();
		if (Double.isNaN(currentGraphicExtent.getMinX()) || Double.isInfinite(currentGraphicExtent.getMinX()) ||
				Double.isNaN(currentGraphicExtent.getMinY()) || Double.isInfinite(currentGraphicExtent.getMinY()) ||
				Double.isNaN(currentGraphicExtent.getMaxX()) || Double.isInfinite(currentGraphicExtent.getMaxX()) ||
				Double.isNaN(currentGraphicExtent.getMaxY()) || Double.isInfinite(currentGraphicExtent.getMaxY()) ||
				sqlGraphicManager == null) 
			return;

		try {
			sqlGraphicManager.save(currentGraphicExtent);
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("LayerManager.message.error.save.extent.exception.title", "Unexpected SQL-Error"),
							i18n.getString("LayerManager.message.error.save.extent.exception.header", "Error, could not save graphic extent to database."),
							i18n.getString("LayerManager.message.error.save.extent.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
}
