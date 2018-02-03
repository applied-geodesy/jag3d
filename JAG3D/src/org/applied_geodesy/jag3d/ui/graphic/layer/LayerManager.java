package org.applied_geodesy.jag3d.ui.graphic.layer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dialog.OptionDialog;
import org.applied_geodesy.jag3d.ui.graphic.layer.dialog.LayerManagerDialog;
import org.applied_geodesy.jag3d.ui.graphic.sql.SQLGraphicManager;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;
import org.applied_geodesy.jag3d.ui.io.DefaultFileChooser;
import org.applied_geodesy.util.ImageUtils;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
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

	private class ColorChangeListener implements ChangeListener<Color> {
		@Override
		public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
			BackgroundFill fill = new BackgroundFill(getColor(), CornerRadii.EMPTY, Insets.EMPTY);
			Background background = new Background(fill);
			stackPane.setBackground(background);
		}
	}
	
	private class DrawDependentLayerChangeListener implements ChangeListener<Object> {
		private Layer layer;
		private DrawDependentLayerChangeListener(Layer layer) {
			this.layer = layer;
		}
		@Override
		public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
			switch(this.layer.getLayerType()) {
			case DATUM_POINT_APRIORI:
			case NEW_POINT_APRIORI:
			case REFERENCE_POINT_APRIORI:
			case STOCHASTIC_POINT_APRIORI:
				this.layer.draw(getCurrentGraphicExtent());
				getLayer(LayerType.OBSERVATION_APRIORI).draw(getCurrentGraphicExtent());
				break;

			case DATUM_POINT_APOSTERIORI:
			case NEW_POINT_APOSTERIORI:
			case REFERENCE_POINT_APOSTERIORI:
			case STOCHASTIC_POINT_APOSTERIORI:
				this.layer.draw(getCurrentGraphicExtent());
				getLayer(LayerType.OBSERVATION_APOSTERIORI).draw(getCurrentGraphicExtent());
				getLayer(LayerType.ABSOLUTE_CONFIDENCE).draw(getCurrentGraphicExtent());
				getLayer(LayerType.RELATIVE_CONFIDENCE).draw(getCurrentGraphicExtent());
				getLayer(LayerType.ARROW).draw(getCurrentGraphicExtent());
				break;
				
			case ARROW:
				this.layer.draw(getCurrentGraphicExtent());
				getLayer(LayerType.RELATIVE_CONFIDENCE).draw(getCurrentGraphicExtent());
				break;
			
			default:
				draw();
				break;
			}
		}
	}

	private Label coordinatePanel = new Label();
	private static I18N i18n = I18N.getInstance();
	private ToolBar layerToolbar = new ToolBar();
	private StackPane stackPane = new StackPane();
	private final GraphicExtent currentGraphicExtent = new GraphicExtent();
	private ObjectProperty<Color> color = new SimpleObjectProperty<Color>(Color.rgb(255, 255, 255, 1.0)); //0-255
	private final MouseLayer mouseLayer = new MouseLayer(this);
	private Spinner<Double> scaleSpinner;
	public LayerManager() {
		this.init();
	}

	private void init() {
		this.initPane();
		this.initToolBar();
		this.initCoordinateLabel();
		
		this.initLayers();

		this.color.addListener(new ColorChangeListener());
		this.currentGraphicExtent.drawingBoardWidthProperty().bind(this.stackPane.widthProperty());
		this.currentGraphicExtent.drawingBoardHeightProperty().bind(this.stackPane.heightProperty());
	}
	
	private void initLayers() {
		LayerType layerTypes[] = LayerType.values();

		for (LayerType layerType : layerTypes) {
			Layer layer = null; 

			// create/add layer
			switch(layerType) {
			case MOUSE:
				layer = this.mouseLayer;
				break;
				
			case REFERENCE_POINT_APRIORI:
			case REFERENCE_POINT_APOSTERIORI:				
			case STOCHASTIC_POINT_APRIORI:
			case STOCHASTIC_POINT_APOSTERIORI:				
			case DATUM_POINT_APRIORI:	
			case DATUM_POINT_APOSTERIORI:
			case NEW_POINT_APRIORI:
			case NEW_POINT_APOSTERIORI:
				PointLayer pointLayer = new PointLayer(layerType, this.getCurrentGraphicExtent());
				pointLayer.point1DVisibleProperty().addListener(new DrawDependentLayerChangeListener(pointLayer));
				pointLayer.point2DVisibleProperty().addListener(new DrawDependentLayerChangeListener(pointLayer));
				pointLayer.point3DVisibleProperty().addListener(new DrawDependentLayerChangeListener(pointLayer));
				pointLayer.visibleProperty().addListener(new DrawDependentLayerChangeListener(pointLayer));
				layer = pointLayer;
				break;
			
			case OBSERVATION_APRIORI:			
			case OBSERVATION_APOSTERIORI:
				layer = new ObservationLayer(layerType, this.getCurrentGraphicExtent());
				break;

			case ARROW:
				ArrowLayer arrowLayer = new ArrowLayer(layerType, this.getCurrentGraphicExtent());
				arrowLayer.vectorScaleProperty().addListener(new DrawDependentLayerChangeListener(arrowLayer));
				layer = arrowLayer;
				break;
				
			case ABSOLUTE_CONFIDENCE:
				layer = new AbsoluteConfidenceLayer(layerType, this.getCurrentGraphicExtent());
				break;
				
			case RELATIVE_CONFIDENCE:
				layer = new RelativeConfidenceLayer(layerType, this.getCurrentGraphicExtent());
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
		
		ArrowLayer arrowLayer = (ArrowLayer)this.getLayer(LayerType.ARROW);
		RelativeConfidenceLayer relativeConfidenceLayer = (RelativeConfidenceLayer)this.getLayer(LayerType.RELATIVE_CONFIDENCE);
		relativeConfidenceLayer.add(arrowLayer);
		
		// binding scale property
		arrowLayer.vectorScaleProperty().bind(this.scaleSpinner.valueProperty());
		absoluteConfidenceLayer.confidenceScaleProperty().bind(this.scaleSpinner.valueProperty());
		
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

	private void initPane() {
		BackgroundFill fill = new BackgroundFill(this.getColor(), CornerRadii.EMPTY, Insets.EMPTY);
		Background background = new Background(fill);

		this.stackPane.setBackground(background);
		this.stackPane.setMinSize(0, 0);
		this.stackPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
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
				i18n.getString("LayerManager.toolbar.button.refresh.tooltip", "Refresh graphic"),
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
				
		this.scaleSpinner = this.createDoubleSpinner(1, Double.MAX_VALUE, 5000, 250, i18n.getString("LayerManager.toolbar.scale.tooltip", "Selected arrow and confidence scale"), 0);
		this.scaleSpinner.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		Label scaleLabel = new Label(" : 1");
		scaleLabel.setGraphic(this.scaleSpinner);
		scaleLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		scaleLabel.setContentDisplay(ContentDisplay.LEFT);
		
		group.getToggles().addAll(
				moveButton,
				windowZoomButton
				);

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
	}
	
	public void clearAllLayers() {
		for (Node node : this.stackPane.getChildren()) {
			if (node instanceof Layer) {
				Layer layer = (Layer)node;
				layer.clearLayer();
			}
		}
	}

	public void draw() {
		for (Node node : this.stackPane.getChildren()) {
			if (node instanceof Layer) {
				Layer layer = (Layer)node;
				if (layer.isVisible()) 
					layer.draw(this.currentGraphicExtent);
			}
		}
	}
	
	public void reorderLayer(List<Layer> layers) {
		this.stackPane.getChildren().setAll(layers);
		this.stackPane.getChildren().add(this.mouseLayer);
		this.mouseToFront();
	}
	
	private void mouseToFront() {
		int mouseLayerIndex = this.stackPane.getChildren().indexOf(this.mouseLayer);
		if (mouseLayerIndex != -1 && mouseLayerIndex != this.stackPane.getChildren().size()-1)
			this.stackPane.getChildren().get(mouseLayerIndex).toFront();
	}

	private void add(Layer layer) {
		this.currentGraphicExtent.merge(layer.getMaximumGraphicExtent());

		layer.widthProperty().bind(this.stackPane.widthProperty());
		layer.heightProperty().bind(this.stackPane.heightProperty());

		this.stackPane.getChildren().add(layer);
		this.mouseToFront();
	}

	public GraphicExtent getCurrentGraphicExtent() {
		return this.currentGraphicExtent;
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
			this.mouseLayer.setToolbarType(type);
		
		}
	}
	
	public void redraw() {
		SQLGraphicManager sqlGraphicManager = SQLManager.getInstance().getSQLGraphicManager();
		if (sqlGraphicManager == null) 
			return;

		try {
			sqlGraphicManager.load(this); 
		} catch (SQLException e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("LayerManager.message.error.sql.load.title", "SQL-Error"),
							i18n.getString("LayerManager.message.error.sql.load.header", "Error, could not load data."),
							i18n.getString("LayerManager.message.error.sql.load.message", "An exception occure during saving dataset to database."),
							e
							);
				}
			});
		} finally {
			// TODO get Extent from DB
			this.expand();
			this.draw();
		}
	}
	
	public void layerProperties() {
		LayerManagerDialog.showAndWait(this);
	}
	
	public void expand() {
		GraphicExtent maxGraphicExtent = new GraphicExtent();
		for (Node node : this.stackPane.getChildren()) {
			if (node instanceof Layer) {
				Layer layer = (Layer)node;
				if (layer.isVisible())
					maxGraphicExtent.merge(layer.getMaximumGraphicExtent());
			}
		}
		this.currentGraphicExtent.set(maxGraphicExtent);
		this.draw();
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

			SnapshotParameters snapshotParameters = new SnapshotParameters();
			snapshotParameters.setFill(Color.TRANSPARENT);
			snapshotParameters.setTransform(Transform.scale(pixelScale, pixelScale));
			WritableImage writableImage = new WritableImage((int)Math.rint(pixelScale*width), (int)Math.rint(pixelScale*height));
			this.stackPane.snapshot(snapshotParameters, writableImage);
			
			ExtensionFilter extensionFilter = new ExtensionFilter(i18n.getString("LayerManager.export.png", "PNG-Image"), "*.png");
			File outputFile = DefaultFileChooser.showSaveDialog(
					i18n.getString("LayerManager.export.title", "Save current graphic"), 
					null, 
					extensionFilter);

			if (outputFile != null) {				
				try {
					BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
					ImageIO.write(bufferedImage, "PNG", outputFile);
				} catch (Exception e) {
					// TODO
					e.printStackTrace();
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

	public ObjectProperty<Color> colorProperty() {
		return this.color;
	}

	public Color getColor() {
		return this.colorProperty().get();
	}

	public void setColor(final Color color) {
		this.colorProperty().set(color);
	}
	
	public Layer getLayer(LayerType layerType) {
		List<Node> nodes = this.stackPane.getChildren();
		for (Node node : nodes) {
			if (node instanceof Layer && ((Layer)node).getLayerType() == layerType)
				return (Layer)node;
		}
		return null;
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
		//doubleSpinner.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);

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
}
