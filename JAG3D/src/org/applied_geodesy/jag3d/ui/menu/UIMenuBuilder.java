package org.applied_geodesy.jag3d.ui.menu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.JavaGraticule3D;
import org.applied_geodesy.jag3d.ui.dialog.OptionDialog;
import org.applied_geodesy.jag3d.ui.io.DefaultFileChooser;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.i18.I18N;
import org.applied_geodesy.util.io.BeoFileReader;
import org.applied_geodesy.util.io.DL100FileReader;
import org.applied_geodesy.util.io.DimensionType;
import org.applied_geodesy.util.io.GSIFileReader;
import org.applied_geodesy.util.io.M5FileReader;
import org.applied_geodesy.util.io.ObservationFlatFileReader;
import org.applied_geodesy.util.io.SourceFileReader;
import org.applied_geodesy.util.io.ZFileReader;
import org.applied_geodesy.util.io.xml.HeXMLFileReader;
import org.applied_geodesy.util.io.xml.JobXMLFileReader;
import org.applied_geodesy.util.sql.HSQLDB;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser.ExtensionFilter;

public class UIMenuBuilder {
	private static UIMenuBuilder menuBuilder = new UIMenuBuilder();
	private static I18N i18n = I18N.getInstance();
	private MenuEventHandler menuEventHandler = new MenuEventHandler(this);
	private MenuBar menuBar;
	
	private UIMenuBuilder() {}
	
	public static UIMenuBuilder getInstance() {
		return menuBuilder;
	}
	
	public MenuBar getMenuBar() {
		if (this.menuBar == null)
			this.init();
		return this.menuBar;
	}
	
	private void init() {
		this.menuBar = new MenuBar();

		// Create menus
        Menu projectMenu = createMenu(i18n.getString("UIMenuBuilder.menu.project.label", "_Project"), true);
        this.createProjectMenu(projectMenu);
        
        Menu importMenu  = createMenu(i18n.getString("UIMenuBuilder.menu.import.label", "_Import"), true);
        this.createImportMenu(importMenu);
        
        Menu propertyMenu  = createMenu(i18n.getString("UIMenuBuilder.menu.property.label", "_Properties"), true);
        this.createPropertyMenu(propertyMenu);
        
        Menu preprocessingMenu  = createMenu(i18n.getString("UIMenuBuilder.menu.preprocessing.label", "P_reprocessing"), true);
        this.createPreprocessingMenu(preprocessingMenu);
        
        Menu analysisMenu  = createMenu(i18n.getString("UIMenuBuilder.menu.analysis.label", "_Analysis"), true);
        this.createAnalysisMenu(analysisMenu);

        Menu reportMenu  = createMenu(i18n.getString("UIMenuBuilder.menu.report.label", "_Report"), true);
        Menu helpMenu    = createMenu(i18n.getString("UIMenuBuilder.menu.help.label", "Help"), true);
        this.createHelpMenu(helpMenu);

        this.menuBar.getMenus().addAll(
        		projectMenu, 
        		importMenu, 
        		propertyMenu,
        		preprocessingMenu,
        		analysisMenu,
        		reportMenu, 
        		helpMenu
        );
	}
	
	private void createPreprocessingMenu(Menu parentMenu) {
		MenuItem approximationValuesItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.preprocessing.approximation.label", "Aproximation _values"), true, MenuItemType.APROXIMATE_VALUES, new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		MenuItem averageItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.preprocessing.average.label", "Avera_ge observations"), true, MenuItemType.AVERAGE, new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		
		parentMenu.getItems().addAll(
				approximationValuesItem,
				averageItem
		);
	}
	
	private void createHelpMenu(Menu parentMenu) {
		MenuItem aboutItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.help.about.label", "_About JAG3D"), true, MenuItemType.ABOUT, new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		parentMenu.getItems().addAll(
				aboutItem
		);
	}
	
	private void createAnalysisMenu(Menu parentMenu) {
		MenuItem congruentPointItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.analysis.congruentpoint.label", "Congr_uent point"), true, MenuItemType.CONGRUENT_POINT, new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		
		parentMenu.getItems().addAll(
				congruentPointItem
		);
	}
	
	private void createPropertyMenu(Menu parentMenu) {
		MenuItem preferencesItem   = createMenuItem(i18n.getString("UIMenuBuilder.menu.property.preferences.label", "_Preferences"), true, MenuItemType.PREFERENCES, new KeyCodeCombination(KeyCode.ENTER, KeyCombination.ALT_DOWN), this.menuEventHandler);
		MenuItem leastSquaresItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.property.leastsquares.label", "Least s_quares"), true, MenuItemType.LEAST_SQUARES, new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		MenuItem teststatisticItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.property.teststatistic.label", "_Test statistic"), true, MenuItemType.TEST_STATISTIC, new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		MenuItem projectionItem    = createMenuItem(i18n.getString("UIMenuBuilder.menu.property.projection.label", "_Horizontal projection"), true, MenuItemType.HORIZONTAL_PROJECTION, new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		MenuItem rankDefectItem    = createMenuItem(i18n.getString("UIMenuBuilder.menu.property.projection.label", "_Rank defect"), true, MenuItemType.RANK_DEFECT, new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		
		parentMenu.getItems().addAll(
				leastSquaresItem,
				teststatisticItem,
				rankDefectItem,
				projectionItem,
				new SeparatorMenuItem(),
				preferencesItem);
	}
	
	private void createProjectMenu(Menu parentMenu) {
		MenuItem newItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.project.new.label", "_New"), true, MenuItemType.NEW, new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		MenuItem openItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.project.open.label", "_Open"), true, MenuItemType.OPEN, new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		MenuItem exitItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.project.exit.label", "_Exit"), true, MenuItemType.EXIT, new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		
		parentMenu.getItems().addAll(
				newItem, 
				openItem,
				new SeparatorMenuItem(),
				exitItem);
	}
	
	private void createImportMenu(Menu parentMenu) {
		Menu importTerrestrialFlatMenu            = createMenu(i18n.getString("UIMenuBuilder.menu.import.flat.terrestrial.label", "Terrestrial observation"), true);
		MenuItem importLevellingFlatItem          = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.terrestrial.levelling.label", "_Levelling"), true, MenuItemType.IMPORT_FLAT_LEVELLING, new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		MenuItem importDirectionFlatItem          = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.terrestrial.direction.label", "D_irection"), true, MenuItemType.IMPORT_FLAT_DIRECTION, new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		MenuItem importHorizontalDistanceFlatItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.terrestrial.horizontaldistance.label", "_Horizontal distance"), true, MenuItemType.IMPORT_FLAT_HORIZONTAL_DISTANCE, new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		MenuItem importSlopeDistanceFlatItem      = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.terrestrial.slopedistance.label", "_Slope distance"), true, MenuItemType.IMPORT_FLAT_SLOPE_DISTANCE, new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		MenuItem importZenithAngleFlatItem        = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.terrestrial.zenithangle.label", "_Zenith angle"), true, MenuItemType.IMPORT_FLAT_ZENITH_ANGLE, new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN), this.menuEventHandler);
		
		Menu importHexagonFlatMenu = createMenu(i18n.getString("UIMenuBuilder.menu.import.hexagon.label", "Hexagon/Leica"), true);
		MenuItem gsi1DFileItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.hexagon.gsi.1d.label",  "_GSI 1D"),   true, MenuItemType.IMPORT_GSI1D,  null, this.menuEventHandler);
		MenuItem gsi2DFileItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.hexagon.gsi.2d.label",  "G_SI 2D"),   true, MenuItemType.IMPORT_GSI2D,  null, this.menuEventHandler);
		MenuItem gsi2DHFileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.hexagon.gsi.2dh.label", "GSI 2D+_H"), true, MenuItemType.IMPORT_GSI2DH, null, this.menuEventHandler);
		MenuItem gsi3DFileItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.hexagon.gsi.3d.label",  "GS_I 3D"),   true, MenuItemType.IMPORT_GSI3D,  null, this.menuEventHandler);
		
		MenuItem hexml2DFileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.hexagon.landxml.2d.label", "_LandXML 1.2 2D"), true, MenuItemType.IMPORT_LAND_XML2D, null, this.menuEventHandler);
		MenuItem hexml3DFileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.hexagon.landxml.3d.label", "L_andXML 1.2 3D"), true, MenuItemType.IMPORT_LAND_XML3D, null, this.menuEventHandler);
		
		Menu importTrimbleFlatMenu = createMenu(i18n.getString("UIMenuBuilder.menu.import.trimble.label", "Trimble/Zeiss"), true);
		MenuItem m5FileItem      = createMenuItem(i18n.getString("UIMenuBuilder.menu.trimble.m5.label", "_M5 (DiNi)"), true, MenuItemType.IMPORT_M5, null, this.menuEventHandler);
		MenuItem jxml2DFileItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.trimble.jobxml.2d.label", "_JobXML 2D"),    true, MenuItemType.IMPORT_JOB_XML2D,  null, this.menuEventHandler);
		MenuItem jxml2DHFileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.trimble.jobxml.2dh.label", "JobXML 2D+_H"), true, MenuItemType.IMPORT_JOB_XML2DH, null, this.menuEventHandler);
		MenuItem jxml3DFileItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.trimble.jobxml.3d.label", "J_obXML 3D"),    true, MenuItemType.IMPORT_JOB_XML3D,  null, this.menuEventHandler);
		
		MenuItem dl100FileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.topcon.dl100.label", "_DL-100 (Topcon)"), true, MenuItemType.IMPORT_DL100, null, this.menuEventHandler);
		
		MenuItem zFileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.z.label", "_Z-File (Caplan)"), true, MenuItemType.IMPORT_Z, null, this.menuEventHandler);
		MenuItem beoFileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.beo.label", "_Beo-File (Neptan)"), true, MenuItemType.IMPORT_BEO, null, this.menuEventHandler);
		
		importHexagonFlatMenu.getItems().addAll(
				gsi1DFileItem,
				gsi2DFileItem,
				gsi2DHFileItem,
				gsi3DFileItem,
				new SeparatorMenuItem(),
				hexml2DFileItem,
				hexml3DFileItem
				);
		
		importTrimbleFlatMenu.getItems().addAll(
				m5FileItem,
				new SeparatorMenuItem(),
				jxml2DFileItem,
				jxml2DHFileItem,
				jxml3DFileItem
				);
		
		importTerrestrialFlatMenu.getItems().addAll(
				importLevellingFlatItem,
				new SeparatorMenuItem(),
				importDirectionFlatItem,
				importHorizontalDistanceFlatItem,
				new SeparatorMenuItem(),
				importSlopeDistanceFlatItem,
				importZenithAngleFlatItem
		);
		
		Menu importGNSSFlatMenu       = createMenu(i18n.getString("UIMenuBuilder.menu.import.flat.gnss.label", "GNSS baseline"), true);
		MenuItem importGNSS1DFlatItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.genss.1d.label", "GNSS1D baseline"), true, MenuItemType.IMPORT_FLAT_GNSS1D, null, this.menuEventHandler);
		MenuItem importGNSS2DFlatItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.genss.2d.label", "GNSS2D baseline"), true, MenuItemType.IMPORT_FLAT_GNSS2D, null, this.menuEventHandler);
		MenuItem importGNSS3DFlatItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.genss.3d.label", "GNSS3D baseline"), true, MenuItemType.IMPORT_FLAT_GNSS3D, null, this.menuEventHandler);
		
		importGNSSFlatMenu.getItems().addAll(
				importGNSS1DFlatItem,
				importGNSS2DFlatItem,
				importGNSS3DFlatItem
		);
				
		parentMenu.getItems().addAll(
				importTerrestrialFlatMenu,
				importGNSSFlatMenu,
				new SeparatorMenuItem(),
				importHexagonFlatMenu,
				importTrimbleFlatMenu,
				dl100FileItem,
				new SeparatorMenuItem(),
				beoFileItem,
				zFileItem
		);
	}
	
	private static Menu createMenu(String label, boolean mnemonicParsing) {
		Menu menu = new Menu(label);
		menu.setMnemonicParsing(mnemonicParsing);		
		return menu;
	}
	
	private static MenuItem createMenuItem(String label, boolean mnemonicParsing, MenuItemType menuItemType, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler) {
		MenuItem menuItem = new MenuItem(label);
		menuItem.setMnemonicParsing(mnemonicParsing);
		if (keyCodeCombination != null)
			menuItem.setAccelerator(keyCodeCombination);
		menuItem.setUserData(menuItemType);
		menuItem.setOnAction(menuEventHandler);
		return menuItem;
	}
	

	void newProject() {
		File selectedFile = DefaultFileChooser.showSaveDialog(
				i18n.getString("UIMenuBuilder.filechooser.open.title", "Open existing project"),
				"project.script",
				new ExtensionFilter("HSQLDB (*.script)", "*.script")
		);
		
		try {
			if (selectedFile != null) {
				Path path = selectedFile.toPath();
				String regex = "(?i)(.+?)(\\.)(backup$|data$|properties$|script$)";
				String project = path.toAbsolutePath().toString().replaceFirst(regex, "$1");

				if (project != null) {
					SQLManager.createNewProject(new HSQLDB(project));
					JavaGraticule3D.setTitle(path.getFileName() == null ? null : path.getFileName().toString().replaceFirst(regex, "$1"));
				}
			}
		}
		catch (Exception e) {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIMenuBuilder.message.error.new.title", "I/O Error"),
							i18n.getString("UIMenuBuilder.message.error.new.header", "Error, could not create new project."),
							i18n.getString("UIMenuBuilder.message.error.new.message", "An exception is occured during database transaction."),
							e
					);
			e.printStackTrace();
		}
	}
	
	void openProject() {
		File selectedFile = DefaultFileChooser.showOpenDialog(
				i18n.getString("UIMenuBuilder.filechooser.open.title", "Open existing project"),
				null,
				new ExtensionFilter("HSQLDB (*.script)", "*.script")
		);

		try {
			if (selectedFile != null) {
				Path path = selectedFile.toPath();
				String regex = "(?i)(.+?)(\\.)(backup$|data$|properties$|script$)";
				String project = Files.exists(path, LinkOption.NOFOLLOW_LINKS) ? path.toAbsolutePath().toString().replaceFirst(regex, "$1") : null;

				if (project != null) {
					SQLManager.openExistingProject(new HSQLDB(project));
					JavaGraticule3D.setTitle(path.getFileName() == null ? null : path.getFileName().toString().replaceFirst(regex, "$1"));
				}
			}
		}
		catch (Exception e) {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIMenuBuilder.message.error.open.title", "I/O Error"),
							i18n.getString("UIMenuBuilder.message.error.open.header", "Error, could not open selected project."),
							i18n.getString("UIMenuBuilder.message.error.open.message", "An exception occured during databse transaction."),
							e
					);
			e.printStackTrace();
		}
	}
	
	private void importFile(SourceFileReader fileReader, ExtensionFilter[] extensionFilters, String title) {
		List<File> selectedFiles = DefaultFileChooser.showOpenMultipleDialog(
				title,
				null,
				JobXMLFileReader.getExtensionFilters()
		);

		if (selectedFiles == null || selectedFiles.isEmpty())
			return;
		
		TreeItem<TreeItemValue> lastItem = null;
		for (File file : selectedFiles) {
			fileReader.setPath(file.toPath());
			try {
				lastItem = fileReader.readAndImport();
			} catch (IOException | SQLException e) {
				e.printStackTrace();
				OptionDialog.showThrowableDialog (
						i18n.getString("UIMenuBuilder.message.error.io.title", "I/O Error"),
						i18n.getString("UIMenuBuilder.message.error.io.header", "Error, could not import selected file."),
						i18n.getString("UIMenuBuilder.message.error.io.message", "An exception occured during i/o process."),
						e
				);
			}
		}
		
		if (lastItem != null) {
			UITreeBuilder.getInstance().getTree().getSelectionModel().clearSelection();
			UITreeBuilder.getInstance().getTree().getSelectionModel().select(lastItem);
		}
	}
	
	void importFile(MenuItemType menuItemType) {
		switch(menuItemType) {
		case IMPORT_FLAT_LEVELLING:
			this.importFile(new ObservationFlatFileReader(ObservationType.LEVELING), ObservationFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.leveling.title", "Import leveling data from flat files"));
			break;
		case IMPORT_FLAT_DIRECTION:
			this.importFile(new ObservationFlatFileReader(ObservationType.DIRECTION), ObservationFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.direction.title", "Import directions from flat files"));
			break;
		case IMPORT_FLAT_HORIZONTAL_DISTANCE:
			this.importFile(new ObservationFlatFileReader(ObservationType.HORIZONTAL_DISTANCE), ObservationFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.horizontal_distances.title", "Import horizontal distances from flat files"));
			break;
		case IMPORT_FLAT_SLOPE_DISTANCE:
			this.importFile(new ObservationFlatFileReader(ObservationType.SLOPE_DISTANCE), ObservationFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.slope_distances.title", "Import slope distances from flat files"));
			break;
		case IMPORT_FLAT_ZENITH_ANGLE:
			this.importFile(new ObservationFlatFileReader(ObservationType.ZENITH_ANGLE), ObservationFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.zenith_angles.title", "Import zenith angles from flat files"));
			break;
			
		case IMPORT_FLAT_GNSS1D:
			this.importFile(new ObservationFlatFileReader(ObservationType.GNSS1D), ObservationFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.gnss.1d.title", "Import 1D gnss baselines from flat files"));
			break;
		case IMPORT_FLAT_GNSS2D:
			this.importFile(new ObservationFlatFileReader(ObservationType.GNSS2D), ObservationFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.gnss.2d.title", "Import 2D gnss baselines from flat files"));
			break;
		case IMPORT_FLAT_GNSS3D:
			this.importFile(new ObservationFlatFileReader(ObservationType.GNSS3D), ObservationFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.gnss.3d.title", "Import 3D gnss baselines from flat files"));
			break;
			
		case IMPORT_BEO:
			this.importFile(new BeoFileReader(), BeoFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.beo.title", "Import data from BEO files"));
			break;
		case IMPORT_DL100:
			this.importFile(new DL100FileReader(), DL100FileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.dl100.title", "Import data from DL-100 files"));
			break;
			
		case IMPORT_GSI1D:
			this.importFile(new GSIFileReader(DimensionType.HEIGHT), GSIFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.gsi.1d.title", "Import 1D data from GSI files"));
			break;
		case IMPORT_GSI2D:
			this.importFile(new GSIFileReader(DimensionType.PLAN), GSIFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.gsi.2d.title", "Import 2D data from GSI files"));
			break;
		case IMPORT_GSI2DH:
			this.importFile(new GSIFileReader(DimensionType.PLAN_AND_HEIGHT), GSIFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.gsi.2dh.title", "Import 2D+H data from GSI files"));
			break;
		case IMPORT_GSI3D:
			this.importFile(new GSIFileReader(DimensionType.SPATIAL), GSIFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.gsi.3d.title", "Import 3D data from GSI files"));
			break;
			
		case IMPORT_JOB_XML2D:
			this.importFile(new JobXMLFileReader(DimensionType.PLAN), JobXMLFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.jobxml.2d.title", "Import 2D data from JobXML files"));
			break;
		case IMPORT_JOB_XML2DH:
			this.importFile(new JobXMLFileReader(DimensionType.PLAN_AND_HEIGHT), JobXMLFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.jobxml.2dh.title", "Import 2D+H data from JobXML files"));
			break;
		case IMPORT_JOB_XML3D:
			this.importFile(new JobXMLFileReader(DimensionType.SPATIAL), JobXMLFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.jobxml.3d.title", "Import 3D data from JobXML files"));
			break;
			
		case IMPORT_LAND_XML2D:
			this.importFile(new HeXMLFileReader(DimensionType.PLAN), HeXMLFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.landxml.2d.title", "Import 2D data from LandXML/HeXML files"));
			break;
		case IMPORT_LAND_XML3D:
			this.importFile(new HeXMLFileReader(DimensionType.SPATIAL), HeXMLFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.landxml.3d.title", "Import 3D data from LandXML/HeXML files"));
			break;
			
		case IMPORT_M5:
			this.importFile(new M5FileReader(), M5FileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.m5.title", "Import data from M5 files"));
			break;
		case IMPORT_Z:
			this.importFile(new ZFileReader(), ZFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.z.title", "Import data from Z files"));
			break;
		
		default:
			break;
		
		}
	}
}