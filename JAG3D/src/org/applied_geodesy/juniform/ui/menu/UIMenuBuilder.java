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

package org.applied_geodesy.juniform.ui.menu;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.applied_geodesy.adjustment.geometry.FeatureChangeListener;
import org.applied_geodesy.adjustment.geometry.FeatureEvent;
import org.applied_geodesy.adjustment.geometry.FeatureType;
import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.FeatureEvent.FeatureEventType;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.juniform.io.FeaturePointFileReader;
import org.applied_geodesy.juniform.io.InitialGuessFileReader;
import org.applied_geodesy.juniform.io.report.FTLReport;
import org.applied_geodesy.juniform.ui.JUniForm;
import org.applied_geodesy.juniform.ui.dialog.ReadFileProgressDialog;
import org.applied_geodesy.juniform.ui.table.UIPointTableBuilder;
import org.applied_geodesy.juniform.ui.tree.UITreeBuilder;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.ui.io.DefaultFileChooser;
import org.applied_geodesy.util.ObservableUniqueList;
import org.applied_geodesy.juniform.ui.i18n.I18N;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser.ExtensionFilter;

public class UIMenuBuilder implements FeatureChangeListener {
	private class GeometricPrimitiveListChangeListener implements ListChangeListener<GeometricPrimitive> {

		@Override
		public void onChanged(Change<? extends GeometricPrimitive> change) {
			while (change.next()) {
				if (change.wasAdded()) {
					for (GeometricPrimitive geometry : change.getAddedSubList()) {
						addGeometricPrimitiveItem(geometry);
					}
				}
				else if (change.wasRemoved()) {
					for (GeometricPrimitive geometry : change.getRemoved()) {
						removeGeometricPrimitiveItem(geometry);
					}
				}
			}
		}
	}
	enum DisableStateType {
		CURVE_TYPE, SURFACE_TYPE, FEATURE
	}
	private static UIMenuBuilder menuBuilder = new UIMenuBuilder();
	private I18N i18n = I18N.getInstance();
	private MenuEventHandler menuEventHandler = new MenuEventHandler(this);
	private MenuBar menuBar;
	private ToggleGroup featureToggleGroup;
	private GeometricPrimitiveListChangeListener geometricPrimitiveListChangeListener = new GeometricPrimitiveListChangeListener();
	private UIMenuBuilder() {}
	private Menu initialGuessMenu, reportMenu;

	public static UIMenuBuilder getInstance() {
		return menuBuilder;
	}
	
	public MenuBar getMenuBar() {
		if (this.menuBar == null)
			this.init();
		return this.menuBar;
	}
	
	private void init() {
		this.featureToggleGroup = new ToggleGroup();
		
		this.menuBar = new MenuBar();
		Menu fileMenu       = createMenu(i18n.getString("UIMenuBuilder.menu.file.label", "_File"),  true);
		Menu curveMenu      = createMenu(i18n.getString("UIMenuBuilder.menu.feature.curve.label", "_Curves"),  true);
		Menu surfaceMenu    = createMenu(i18n.getString("UIMenuBuilder.menu.feature.surface.label", "S_urfaces"),  true);
		Menu propertiesMenu = createMenu(i18n.getString("UIMenuBuilder.menu.properties.label", "Propert_ies"), true);
		Menu adjustmentMenu = createMenu(i18n.getString("UIMenuBuilder.menu.adjustment.label", "Adjustment"), true);
		this.reportMenu     = createMenu(i18n.getString("UIMenuBuilder.menu.report.label", "Repor_t"), true);
		Menu helpMenu       = createMenu(i18n.getString("UIMenuBuilder.menu.help.label", "_?"), true);
		
		this.createFileMenu(fileMenu);
		this.createCurveFeatureMenu(curveMenu, this.featureToggleGroup);
		this.createSurfaceFeatureMenu(surfaceMenu, this.featureToggleGroup);
		this.createPropertiesMenu(propertiesMenu);
		this.createAdjustmentMenu(adjustmentMenu);
		this.createReportMenu(this.reportMenu);
		this.createHelpMenu(helpMenu);
		
		this.menuBar.getMenus().addAll(
				fileMenu,
				curveMenu,
				surfaceMenu,
				propertiesMenu,
				adjustmentMenu,
				reportMenu,
				helpMenu
		);
	}
	
	private void createFileMenu(Menu parentMenu) {
		MenuItem importCurvePointsItem   = createMenuItem(i18n.getString("UIMenuBuilder.menu.file.import.points.curve.label", "Impo_rt curve points"), true, MenuItemType.IMPORT_CURVE_POINTS, new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		MenuItem importSurfacePointsItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.file.import.points.surface.label", "Imp_ort surface points"), true, MenuItemType.IMPORT_SURFACE_POINTS, new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		MenuItem exitItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.file.exit.label", "_Exit"), true, MenuItemType.EXIT, new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);

		this.initialGuessMenu = createMenu(i18n.getString("UIMenuBuilder.menu.file.import.initial.label", "Initial _guess"), true);
		this.initialGuessMenu.setDisable(true);
		
		parentMenu.getItems().addAll(
				importCurvePointsItem,
				importSurfacePointsItem,
				new SeparatorMenuItem(),
				this.initialGuessMenu,
				new SeparatorMenuItem(),
				exitItem
		);
	}
	
	private void createAdjustmentMenu(Menu parentMenu) {
		MenuItem leastSquaresItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.adjustment.leastsquares.label", "Least-squares"), true, MenuItemType.LEAST_SQUARES, new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem teststatisticItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.adjustment.teststatistic.label", "Test statistic"), true, MenuItemType.TEST_STATISTIC, new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem preferencesItem   = createMenuItem(i18n.getString("UIMenuBuilder.menu.adjustment.preferences.label", "Preferences"), true, MenuItemType.PREFERENCES, new KeyCodeCombination(KeyCode.ENTER, KeyCombination.ALT_DOWN), this.menuEventHandler, false);
		parentMenu.getItems().addAll(
				leastSquaresItem,
				teststatisticItem,
				new SeparatorMenuItem(),
				preferencesItem
		);
	}
	
	private void createPropertiesMenu(Menu parentMenu) {

		MenuItem featurePropertiesItem        = createMenuItem(i18n.getString("UIMenuBuilder.menu.setting.feature.label", "_Feature"), true, MenuItemType.FEATURE_PROPERTIES, new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem paramameterPropertiesItem    = createMenuItem(i18n.getString("UIMenuBuilder.menu.setting.parameter.label", "_Parameter"), true, MenuItemType.PARAMETER_PROPERTIES, new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem restrictionPropertiesItem    = createMenuItem(i18n.getString("UIMenuBuilder.menu.setting.restriction.label", "_Restriction"), true, MenuItemType.RESTRICTION_PROPERTIES, new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem postprocessingPropertiesItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.setting.postprocessing.label", "_Post processing"), true, MenuItemType.POSTPROCESSING_PROPERTIES, new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
			
		parentMenu.getItems().addAll(
				featurePropertiesItem,
				paramameterPropertiesItem,
				restrictionPropertiesItem,
				postprocessingPropertiesItem
		);
	}
	
	private void createCurveFeatureMenu(Menu parentMenu, ToggleGroup toggleGroup) {
		// Curves
		RadioMenuItem lineItem          = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.curve.line.label", "Line"), true, MenuItemType.LINE, null, this.menuEventHandler, true);
		RadioMenuItem circleItem        = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.curve.circle.label", "Circle"), true, MenuItemType.CIRCLE, null, this.menuEventHandler, true);
		RadioMenuItem ellipseItem       = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.curve.ellipse.label", "Ellipse"), true, MenuItemType.ELLIPSE, null, this.menuEventHandler, true);

		RadioMenuItem quadraticCurveItem = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.curve.quadric.label", "Quadratic curve"), true, MenuItemType.QUADRATIC_CURVE, null, this.menuEventHandler, true);
		RadioMenuItem modifiedCurveItem  = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.curve.userdefined.label", "User defined curve"), true, MenuItemType.MODIFIABLE_CURVE, null, this.menuEventHandler, true);

		toggleGroup.getToggles().addAll(
				lineItem,
				circleItem,
				ellipseItem,
				quadraticCurveItem,
				modifiedCurveItem
		);

		parentMenu.getItems().addAll(
				lineItem,
				circleItem,
				ellipseItem,
				quadraticCurveItem,
				new SeparatorMenuItem(),
				modifiedCurveItem
		);
	}
	
	private void createSurfaceFeatureMenu(Menu parentMenu, ToggleGroup toggleGroup) {
		// Surfaces
		RadioMenuItem planeItem     = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.plane.label", "Plane"), true, MenuItemType.PLANE, null, this.menuEventHandler, true);
		RadioMenuItem sphereItem    = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.sphere.label", "Sphere"), true, MenuItemType.SPHERE, null, this.menuEventHandler, true);
		RadioMenuItem ellipsoidItem = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.ellipsoid.label", "Ellipsoid"), true, MenuItemType.ELLIPSOID, null, this.menuEventHandler, true);

		RadioMenuItem spatialLineItem    = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.line.label", "Spatial line"), true, MenuItemType.SPATIAL_LINE, null, this.menuEventHandler, true);
		RadioMenuItem spatialCircleItem  = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.circle.label", "Spatial circle"), true, MenuItemType.SPATIAL_CIRCLE, null, this.menuEventHandler, true);
		RadioMenuItem spatialEllipseItem = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.ellipse.label", "Spatial ellipse"), true, MenuItemType.SPATIAL_ELLIPSE, null, this.menuEventHandler, true);

		RadioMenuItem cicularConeItem = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.cone.circular.label", "Circular cone"), true, MenuItemType.CIRCULAR_CONE, null, this.menuEventHandler, true);
		RadioMenuItem coneItem        = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.cone.elliptic.label", "Cone"), true, MenuItemType.CONE, null, this.menuEventHandler, true);

		RadioMenuItem circularParaboloidItem = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.paraboloid.circular.label", "Circular paraboloid"), true, MenuItemType.CIRCULAR_PARABOLOID, null, this.menuEventHandler, true);
		RadioMenuItem paraboloidItem         = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.paraboloid.elliptic.label", "Paraboloid"), true, MenuItemType.PARABOLOID, null, this.menuEventHandler, true);
		
		RadioMenuItem cicularCylinderItem = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.cylinder.circular.label", "Circular cylinder"), true, MenuItemType.CIRCULAR_CYLINDER, null, this.menuEventHandler, true);
		RadioMenuItem cylinderItem        = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.cylinder.elliptic.label", "Cylinder"), true, MenuItemType.CYLINDER, null, this.menuEventHandler, true);
		
		RadioMenuItem quadraticSurfaceItem = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.quadric.label", "Quadratic surface"), true, MenuItemType.QUADRATIC_SURFACE, null, this.menuEventHandler, true);
		
		RadioMenuItem modifiedSurfaceItem = createRadioMenuItem(i18n.getString("UIMenuBuilder.menu.feature.surface.userdefined.label", "User defined surface"), true, MenuItemType.MODIFIABLE_SURFACE, null, this.menuEventHandler, true);

		toggleGroup.getToggles().addAll(
				planeItem,
				sphereItem,
				ellipsoidItem,
				spatialLineItem,
				spatialCircleItem,
				spatialEllipseItem,
				cicularCylinderItem,
				cylinderItem,
				cicularConeItem,
				coneItem,
				circularParaboloidItem,
				paraboloidItem,
				quadraticSurfaceItem,
				modifiedSurfaceItem
		);

		parentMenu.getItems().addAll(
				spatialLineItem,
				spatialCircleItem,
				spatialEllipseItem,
				new SeparatorMenuItem(),
				planeItem,
				sphereItem,
				ellipsoidItem,
				cicularCylinderItem,
				cylinderItem,
				cicularConeItem,
				coneItem,
				circularParaboloidItem,
				paraboloidItem,
				quadraticSurfaceItem,
				new SeparatorMenuItem(),
				modifiedSurfaceItem
		);
	}
	
	private void createReportMenu(Menu parentMenu) {
		List<File> templateFiles = FTLReport.getTemplates();
		if (templateFiles != null && !templateFiles.isEmpty()) {
			for (File templateFile : templateFiles) {
				MenuItem templateFileItem = createMenuItem(templateFile.getName(), false, MenuItemType.REPORT, templateFile, null, this.menuEventHandler, true);

				parentMenu.getItems().add(templateFileItem);
			}
		}
	}
	
	private void createHelpMenu(Menu parentMenu) {
		MenuItem aboutItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.help.about.label", "_About JAG3D"), true, MenuItemType.ABOUT, new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		parentMenu.getItems().addAll(
				aboutItem
		);
	}
	
	void setFeatureType(FeatureType featureType) {
		DisableStateType disableStateType = null;
		if (featureType == FeatureType.CURVE)
			disableStateType = DisableStateType.CURVE_TYPE;
		else if (featureType == FeatureType.SURFACE)
			disableStateType = DisableStateType.SURFACE_TYPE;
		else
			return;
		
		this.disableMenu(disableStateType);
		
		for (Toggle toggle : this.featureToggleGroup.getToggles()) {
			toggle.setSelected(false);
		}
	}
	
	void disableMenu(DisableStateType disableStateType) {
		List<Menu> menus = this.menuBar.getMenus();
		for (Menu menu : menus)
			this.disableMenu(menu, disableStateType);
	}
	
	private void disableMenu(Menu menu, DisableStateType disableStateType) {
		List<MenuItem> items = menu.getItems();
		for (MenuItem item : items) {
			if (item instanceof Menu)
				this.disableMenu((Menu)item, disableStateType);
			else if (item.getUserData() != null && item.getUserData() instanceof MenuItemType) { 
				MenuItemType itemType = (MenuItemType)item.getUserData();
				switch(itemType) {
				case FEATURE_PROPERTIES:
				case PARAMETER_PROPERTIES:
				case POSTPROCESSING_PROPERTIES:
				case RESTRICTION_PROPERTIES:
					if (disableStateType == DisableStateType.FEATURE)
						item.setDisable(false);
					break;
					
				case TEST_STATISTIC:
				case LEAST_SQUARES:
					item.setDisable(disableStateType == DisableStateType.CURVE_TYPE || disableStateType == DisableStateType.SURFACE_TYPE);
					break;
				
				case LINE:
				case CIRCLE:
				case ELLIPSE:
				case QUADRATIC_CURVE:
				case MODIFIABLE_CURVE:
					if (disableStateType == DisableStateType.CURVE_TYPE || disableStateType == DisableStateType.SURFACE_TYPE)
						item.setDisable(disableStateType != DisableStateType.CURVE_TYPE);
					break;
				case PLANE:
				case SPHERE:
				case ELLIPSOID:
				case SPATIAL_LINE:
				case SPATIAL_CIRCLE:
				case SPATIAL_ELLIPSE:
				case CIRCULAR_CYLINDER:
				case CYLINDER:
				case CIRCULAR_CONE:
				case CONE:
				case CIRCULAR_PARABOLOID:
				case PARABOLOID:
				case QUADRATIC_SURFACE:
				case MODIFIABLE_SURFACE:
					if (disableStateType == DisableStateType.CURVE_TYPE || disableStateType == DisableStateType.SURFACE_TYPE)
						item.setDisable(disableStateType != DisableStateType.SURFACE_TYPE);
					break;
					
				case REPORT:
					this.setReportMenuDisable(true);
					break;
					
				case EXIT:
				case ABOUT:
				case PREFERENCES:
				case IMPORT_CURVE_POINTS:
				case IMPORT_SURFACE_POINTS:
					// do nothing
					break;
				}
			}
		}
	}
	
	public ToggleGroup getToggleGroup() {
		return this.featureToggleGroup;
	}
	
	private static Menu createMenu(String label, boolean mnemonicParsing) {
		Menu menu = new Menu(label);
		menu.setMnemonicParsing(mnemonicParsing);		
		return menu;
	}
	
	private static MenuItem createMenuItem(MenuItem menuItem, boolean mnemonicParsing, Object userDate, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		menuItem.setMnemonicParsing(mnemonicParsing);
		if (keyCodeCombination != null)
			menuItem.setAccelerator(keyCodeCombination);
		menuItem.setOnAction(menuEventHandler);
		menuItem.setDisable(disable);
		menuItem.setUserData(userDate);
		return menuItem;
	}

	private static MenuItem createMenuItem(String label, boolean mnemonicParsing, MenuItemType menuItemType, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		MenuItem menuItem = createMenuItem(new MenuItem(label), mnemonicParsing, menuItemType, keyCodeCombination, menuEventHandler, disable);

		return menuItem;
	}
	
	private static MenuItem createMenuItem(String label, boolean mnemonicParsing, GeometricPrimitive geometry, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		MenuItem menuItem = createMenuItem(new MenuItem(label), mnemonicParsing, geometry, keyCodeCombination, menuEventHandler, disable);

		return menuItem;
	}
	
	private static RadioMenuItem createRadioMenuItem(String label, boolean mnemonicParsing, MenuItemType menuItemType, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		RadioMenuItem menuItem = (RadioMenuItem)createMenuItem(new RadioMenuItem(label), mnemonicParsing, menuItemType, keyCodeCombination, menuEventHandler, disable);

		return menuItem;
	}

	private static MenuItem createMenuItem(String label, boolean mnemonicParsing, MenuItemType menuItemType, File file, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		FileMenuItem menuItem = (FileMenuItem)createMenuItem(new FileMenuItem(label), mnemonicParsing, menuItemType, keyCodeCombination, menuEventHandler, disable);
		menuItem.setFile(file);
		return menuItem;
	}
	
	private void addGeometricPrimitiveItem(GeometricPrimitive geometry) {
		MenuItem geometryItem = createMenuItem(geometry.getName(), true, geometry, null, this.menuEventHandler, false);
		geometryItem.textProperty().bind(geometry.nameProperty());
		this.initialGuessMenu.getItems().add(geometryItem);
		this.initialGuessMenu.setDisable(false);
	}
	
	private void removeGeometricPrimitiveItem(GeometricPrimitive geometry) {
		MenuItem geometryItem = null;
		for (MenuItem item : this.initialGuessMenu.getItems()) {
			if (item.getUserData() == geometry) {
				geometryItem = item;
				break;
			}
		}
		if (geometryItem != null) {
			geometryItem.textProperty().unbind();
			this.initialGuessMenu.getItems().remove(geometryItem);
		}
		this.initialGuessMenu.setDisable(this.initialGuessMenu.getItems().isEmpty());
	}
	
	@Override
	public void featureChanged(FeatureEvent evt) {
		if (evt.getEventType() == FeatureEventType.FEATURE_ADDED) {
			// add geometries to initial guess menu
			for (GeometricPrimitive geometry : evt.getSource().getGeometricPrimitives())
				this.addGeometricPrimitiveItem(geometry);
			// add listener to handle new feature
			evt.getSource().getGeometricPrimitives().addListener(this.geometricPrimitiveListChangeListener);
		}
		else if (evt.getEventType() == FeatureEventType.FEATURE_REMOVED) {
			// add geometries to initial guess menu
			for (GeometricPrimitive geometry : evt.getSource().getGeometricPrimitives())
				this.removeGeometricPrimitiveItem(geometry);
			// remove listener from old feature
			evt.getSource().getGeometricPrimitives().removeListener(this.geometricPrimitiveListChangeListener);	
		}
	}
	
	
	
	void importFile(FeaturePointFileReader fileReader, ExtensionFilter[] extensionFilters, String title) {
		List<File> selectedFiles = DefaultFileChooser.showOpenMultipleDialog(
				JUniForm.getStage(),
				title,
				null,
				extensionFilters
				);

		if (selectedFiles == null || selectedFiles.isEmpty())
			return;

		this.importFile(fileReader, selectedFiles);
	}
	
	void importFile(FeaturePointFileReader fileReader, List<File> selectedFiles) {
		if (selectedFiles == null || selectedFiles.isEmpty())
			return;

		ObservableUniqueList<FeaturePoint> points = null;
		ReadFileProgressDialog<ObservableUniqueList<FeaturePoint>> dialog = new ReadFileProgressDialog<ObservableUniqueList<FeaturePoint>>();
		Optional<ObservableUniqueList<FeaturePoint>> optional = dialog.showAndWait(fileReader, selectedFiles);
		if (optional.isPresent()) {
			points = optional.get();
		}

		if (points != null) {
			FeatureType prevFeatureType = UITreeBuilder.getInstance().getFeatureAdjustment().getFeature() != null ? UITreeBuilder.getInstance().getFeatureAdjustment().getFeature().getFeatureType() : null;
				
			Toggle toggle = this.featureToggleGroup.getSelectedToggle();
			
			this.fireFeatureTypeChanged(fileReader.getFeatureType());
			UIPointTableBuilder.getInstance().getTable().setItems(points);
			
			// re-select last feature (create a new instance because file has changed)
			if (prevFeatureType == fileReader.getFeatureType() && toggle != null && toggle instanceof MenuItem) {
				toggle.setSelected(true);
				this.menuEventHandler.handleAction((MenuItem)toggle);
			}
			
			UITreeBuilder.getInstance().handleTreeSelections();
		}
	}
	
	void importInitialGuess(GeometricPrimitive geometry) {
		File selectedFile = DefaultFileChooser.showOpenDialog(
				JUniForm.getStage(),
				i18n.getString("UIMenuBuilder.filechooser.import.initialguess.title", "Import initial guess from flat files"),
				null,
				InitialGuessFileReader.getExtensionFilters()
				);

		if (selectedFile == null)
			return;

		InitialGuessFileReader fileReader = new InitialGuessFileReader(geometry);
		ReadFileProgressDialog<GeometricPrimitive> dialog = new ReadFileProgressDialog<GeometricPrimitive>();
		Optional<GeometricPrimitive> optional = dialog.showAndWait(fileReader, List.of(selectedFile));
		if (optional.isPresent()) {
			optional.get();
		}
	}
	
	void createReport(File templateFile) {
		try {
			if (UITreeBuilder.getInstance().getFeatureAdjustment().getFeature() == null)
				return;

			Pattern pattern = Pattern.compile(".*?\\.(\\w+)\\.ftlh$", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(templateFile.getName().toLowerCase());
			String extension = "html";
			ExtensionFilter extensionFilter = new ExtensionFilter(i18n.getString("UIMenuBuilder.report.extension.html", "Hypertext Markup Language"), "*.html", "*.htm");
			if (matcher.find() && matcher.groupCount() == 1) {
				extension = matcher.group(1);
				extensionFilter = new ExtensionFilter(String.format(Locale.ENGLISH, i18n.getString("UIMenuBuilder.report.extension.template", "%s-File"), extension), "*." + extension); 
			}
			
			String fileNameSuggestion = "report." + extension;

			FTLReport ftl = new FTLReport(UITreeBuilder.getInstance().getFeatureAdjustment());
			File reportFile = DefaultFileChooser.showSaveDialog(
					JUniForm.getStage(),
					i18n.getString("UIMenuBuilder.filechooser.report.title", "Save adjustment report"), 
					fileNameSuggestion,
					extensionFilter
			);
			if (reportFile != null && ftl != null) {
				ftl.setTemplate(templateFile.getName());
				ftl.toFile(reportFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
			OptionDialog.showThrowableDialog (
					i18n.getString("UIMenuBuilder.message.error.report.exception.title", "I/O Error"),
					i18n.getString("UIMenuBuilder.message.error.report.exception.header", "Error, could not create adjustment report."),
					i18n.getString("UIMenuBuilder.message.error.report.exception.message", "An exception has occurred during report creation."),
					e
					);
		}
	}
	
	private void fireFeatureTypeChanged(FeatureType featureType) {
		this.setFeatureType(featureType);
		UIPointTableBuilder.getInstance().setFeatureType(featureType);
		UITreeBuilder.getInstance().setFeatureType(featureType);
	}
	
	public void setReportMenuDisable(boolean disable) {
		for (MenuItem item : this.reportMenu.getItems())
			item.setDisable(disable);
	}

}
