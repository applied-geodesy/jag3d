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

package org.applied_geodesy.jag3d.ui.menu;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateChangedListener;
import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateEvent;
import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.JAG3D;
import org.applied_geodesy.jag3d.ui.dialog.ColumnImportDialog;
import org.applied_geodesy.jag3d.ui.dialog.OptionDialog;
import org.applied_geodesy.jag3d.ui.io.DefaultFileChooser;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.i18.I18N;
import org.applied_geodesy.util.io.BeoFileReader;
import org.applied_geodesy.util.io.CongruenceAnalysisFlatFileReader;
import org.applied_geodesy.util.io.DL100FileReader;
import org.applied_geodesy.util.io.DimensionType;
import org.applied_geodesy.util.io.GSIFileReader;
import org.applied_geodesy.util.io.LockFileReader;
import org.applied_geodesy.util.io.M5FileReader;
import org.applied_geodesy.util.io.ObservationFlatFileReader;
import org.applied_geodesy.util.io.PointFlatFileReader;
import org.applied_geodesy.util.io.SourceFileReader;
import org.applied_geodesy.util.io.ZFileReader;
import org.applied_geodesy.util.io.properties.HTTPPropertiesLoader;
import org.applied_geodesy.util.io.properties.URLParameter;
import org.applied_geodesy.util.io.report.FTLReport;
import org.applied_geodesy.util.io.xml.HeXMLFileReader;
import org.applied_geodesy.util.io.xml.JobXMLFileReader;
import org.applied_geodesy.util.sql.HSQLDB;
import org.applied_geodesy.version.jag3d.DatabaseVersionMismatchException;
import org.applied_geodesy.version.jag3d.Version;

import com.derletztekick.geodesy.coordtrans.v2.gui.CoordTrans;
import com.derletztekick.geodesy.formFittingToolbox.v2.gui.FormFittingToolbox;
import com.derletztekick.geodesy.geotra.gui.GeoTra;

import javafx.application.HostServices;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser.ExtensionFilter;

public class UIMenuBuilder {
	private class DatabaseStateChangedListener implements ProjectDatabaseStateChangedListener {
		final Set<MenuItemType> unDisableItemTypes = new HashSet<MenuItemType>(
				Arrays.asList(
						MenuItemType.NEW,
						MenuItemType.OPEN,
						MenuItemType.EXIT,
						MenuItemType.RECENTLY_USED,
						MenuItemType.ABOUT,
						MenuItemType.CHECK_UPDATES
						)
				);

		@Override
		public void projectDatabaseStateChanged(ProjectDatabaseStateEvent evt) {
			if (menuBar == null || menuBar.getChildrenUnmodifiable().isEmpty())
				return;

			boolean disable = evt.getEventType() != ProjectDatabaseStateType.OPENED;
			List<Menu> menus = menuBar.getMenus();
			for (Menu menu : menus)
				this.disableMenu(menu, disable);
		}

		private void disableMenu(Menu menu, boolean disable) {
			List<MenuItem> items = menu.getItems();
			for (MenuItem item : items) {

				if (item instanceof Menu)
					this.disableMenu((Menu)item, disable);
				else if (!this.unDisableItemTypes.contains(item.getUserData())) 
					item.setDisable(disable);
			}
		}
	}

	private File historyFile = new File(System.getProperty("user.home") + File.separator + ".jag3d_history");
	private Menu historyMenu;
	private HostServices hostServices;
	private static UIMenuBuilder menuBuilder = new UIMenuBuilder();
	private I18N i18n = I18N.getInstance();
	private MenuEventHandler menuEventHandler = new MenuEventHandler(this);
	private MenuBar menuBar;

	private UIMenuBuilder() {}

	public static UIMenuBuilder getInstance() {
		return menuBuilder;
	}

	public static void setHostServices(HostServices hostServices) {
		menuBuilder.hostServices = hostServices;
	}

	public MenuBar getMenuBar() {
		if (this.menuBar == null)
			this.init();
		return this.menuBar;
	}

	private void init() {
		SQLManager.getInstance().addProjectDatabaseStateChangedListener(new DatabaseStateChangedListener());

		this.initHistoryPathFromProperties();

		this.menuBar = new MenuBar();

		// Create menus
		Menu projectMenu = createMenu(i18n.getString("UIMenuBuilder.menu.project.label", "_Project"), true);
		this.createProjectMenu(projectMenu);

		Menu importMenu = createMenu(i18n.getString("UIMenuBuilder.menu.import.label", "_Import"), true);
		this.createImportMenu(importMenu);

		Menu propertyMenu = createMenu(i18n.getString("UIMenuBuilder.menu.property.label", "Propert_ies"), true);
		this.createPropertyMenu(propertyMenu);

		Menu preprocessingMenu = createMenu(i18n.getString("UIMenuBuilder.menu.preprocessing.label", "P_reprocessing"), true);
		this.createPreprocessingMenu(preprocessingMenu);

		Menu analysisMenu = createMenu(i18n.getString("UIMenuBuilder.menu.analysis.label", "_Analysis"), true);
		this.createAnalysisMenu(analysisMenu);

		Menu reportMenu = createMenu(i18n.getString("UIMenuBuilder.menu.report.label", "Repor_t"), true);
		this.createReportMenu(reportMenu);

		Menu moduleMenu = createMenu(i18n.getString("UIMenuBuilder.menu.module.label", "_Module"), true);
		this.createModuleMenu(moduleMenu);

		Menu helpMenu = createMenu(i18n.getString("UIMenuBuilder.menu.help.label", "_Help"), true);
		this.createHelpMenu(helpMenu);

		this.menuBar.getMenus().addAll(
				projectMenu, 
				importMenu, 
				propertyMenu,
				preprocessingMenu,
				analysisMenu,
				reportMenu,
				moduleMenu,
				helpMenu
				);
	}

	private void createPreprocessingMenu(Menu parentMenu) {
		MenuItem approximationValuesItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.preprocessing.approximation.label", "Aproximation _values"), true, MenuItemType.APROXIMATE_VALUES, new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem averageItem             = createMenuItem(i18n.getString("UIMenuBuilder.menu.preprocessing.average.label", "Avera_ge observations"), true, MenuItemType.AVERAGE, new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);

		parentMenu.getItems().addAll(
				approximationValuesItem,
				averageItem
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

	private void createModuleMenu(Menu parentMenu) {
		MenuItem moduleGEOTRAItem             = createMenuItem(i18n.getString("UIMenuBuilder.menu.module.geotra.label", "Coordinate converter"), true, MenuItemType.MODULE_GEOTRA, null, this.menuEventHandler, false);
		MenuItem moduleCOORDTRANSItem         = createMenuItem(i18n.getString("UIMenuBuilder.menu.module.coordtrans.label", "Coordinate transformation"), true, MenuItemType.MODULE_COORDTRANS, null, this.menuEventHandler, false);
		MenuItem moduleFORMFITTINGTOOLBOXItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.module.formfittingtoolbox.label", "Geometry and form analysis"), true, MenuItemType.MODULE_FORMFITTINGTOOLBOX, null, this.menuEventHandler, false);

		parentMenu.getItems().addAll(
				moduleGEOTRAItem,
				moduleCOORDTRANSItem,
				moduleFORMFITTINGTOOLBOXItem
				);
	}

	private void createHelpMenu(Menu parentMenu) {
		MenuItem aboutItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.help.about.label", "_About JAG3D"), true, MenuItemType.ABOUT, new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		MenuItem updateItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.help.check_update.label", "Check for _updates\u2026"), true, MenuItemType.CHECK_UPDATES, new KeyCodeCombination(KeyCode.U, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		parentMenu.getItems().addAll(
				aboutItem,
				updateItem
				);
	}

	private void createAnalysisMenu(Menu parentMenu) {
		MenuItem congruentPointItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.analysis.congruentpoint.label", "Congr_uent points"), true, MenuItemType.CONGRUENT_POINT, new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem rowHighlightItem   = createMenuItem(i18n.getString("UIMenuBuilder.menu.analysis.highlight.label", "Ro_w highlighting"), true, MenuItemType.HIGHLIGHT_TABLE_ROWS, new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
				
		parentMenu.getItems().addAll(
				congruentPointItem,
				rowHighlightItem
				);
	}

	private void createPropertyMenu(Menu parentMenu) {
		MenuItem preferencesItem   = createMenuItem(i18n.getString("UIMenuBuilder.menu.property.preferences.label", "Preferences"), true, MenuItemType.PREFERENCES, new KeyCodeCombination(KeyCode.ENTER, KeyCombination.ALT_DOWN), this.menuEventHandler, true);
		MenuItem leastSquaresItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.property.leastsquares.label", "Least-squares"), true, MenuItemType.LEAST_SQUARES, new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem teststatisticItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.property.teststatistic.label", "Test statistic"), true, MenuItemType.TEST_STATISTIC, new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem projectionItem    = createMenuItem(i18n.getString("UIMenuBuilder.menu.property.projection.label", "Horizontal projection"), true, MenuItemType.HORIZONTAL_PROJECTION, new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem rankDefectItem    = createMenuItem(i18n.getString("UIMenuBuilder.menu.property.rankdefect.label", "Rank defect"), true, MenuItemType.RANK_DEFECT, new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);

		parentMenu.getItems().addAll(
				leastSquaresItem,
				teststatisticItem,
				rankDefectItem,
				projectionItem,
				new SeparatorMenuItem(),
				preferencesItem);
	}

	private void createProjectMenu(Menu parentMenu) {
		MenuItem newItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.project.new.label", "Create _new project"), true, MenuItemType.NEW, new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		MenuItem openItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.project.open.label", "_Open existing project"), true, MenuItemType.OPEN, new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		MenuItem copyItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.project.copy.label", "_Copy current project and open"), true, MenuItemType.COPY, new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN), this.menuEventHandler, true);
		MenuItem closeItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.project.close.label", "C_lose current project"), true, MenuItemType.CLOSE, new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem exitItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.project.exit.label", "_Exit"), true, MenuItemType.EXIT, new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		Menu historyMenu = this.createHistoryMenu();
		
		parentMenu.getItems().addAll(
				newItem, 
				openItem,
				closeItem,
				copyItem,
				new SeparatorMenuItem(),
				historyMenu,
				new SeparatorMenuItem(),
				exitItem);
	}

	private void writeProjectHistory() {
		if (this.historyFile == null)
			return;
		
		List<MenuItem> items = this.historyMenu.getItems();
		if (items.isEmpty())
			return;

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter( this.historyFile )));
			for (int i = 0 ; i < items.size(); i++) {
				if (items.get(i) instanceof FileMenuItem) {
					FileMenuItem fileMenuItem = (FileMenuItem)items.get(i);
					pw.printf(Locale.ENGLISH, "%s\r\n", fileMenuItem.getFile());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pw != null)
				pw.close();
		}
	}

	private void addHistoryFile(File file) { 
		try {
			List<MenuItem> items = new ArrayList<MenuItem>(this.historyMenu.getItems());
			Collections.reverse(items);
			int removeIndex = -1;
			for (int i = 0 ; i < items.size(); i++) {
				if (items.get(i) instanceof FileMenuItem) {
					FileMenuItem fileMenuItem = (FileMenuItem)items.get(i);
					if (file.getCanonicalPath().equals(fileMenuItem.getFile().getCanonicalPath())) {
						removeIndex = i;
						break;
					}
				}
			}
			if (removeIndex >= 0)
				items.remove(removeIndex);


			String itemName = file.toString();
			String parent   = file.getParent();
			if (parent.length() > 60)
				itemName = parent.substring(0, 50) + "\u2026" + File.separator + file.getName();

			MenuItem newFileItem = createMenuItem(itemName, false, MenuItemType.RECENTLY_USED, file, null, this.menuEventHandler, false);
			items.add(newFileItem);

			while (items.size() >= 10)
				items.remove(0);

			Collections.reverse(items);
			this.historyMenu.getItems().setAll(items);
			this.historyMenu.setDisable(items.isEmpty());
			this.writeProjectHistory();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Menu createHistoryMenu() {
		if (this.historyMenu == null) {
			this.historyMenu = createMenu(i18n.getString("UIMenuBuilder.menu.project.history.label", "_Recently used projects"), true);
			List<MenuItem> newItems = this.historyMenu.getItems();
			newItems.clear();
			
			if (this.historyFile == null) {
				this.historyMenu.setDisable(newItems.isEmpty());
				return this.historyMenu;
			}

			String regex = "(?i)(.+?)(\\.)(backup$|data$|properties$|script$)";
			Scanner scanner = null;
			try {
				Path path = this.historyFile.toPath();
				if (Files.exists(path) && Files.isRegularFile(path)) {
					scanner = new Scanner(this.historyFile);
					while (scanner.hasNext()) {
						String string = scanner.nextLine().trim();
						File file = new File(string);
						if (!string.isEmpty() && string.matches(regex) && Files.exists(file.toPath()) && Files.isRegularFile(file.toPath())) {
							String itemName = file.toString();
							String parent   = file.getParent();
							if (parent.length() > 60)
								itemName = parent.substring(0, 50) + "\u2026" + File.separator + file.getName();

							MenuItem fileItem = createMenuItem(itemName, false, MenuItemType.RECENTLY_USED, file, null, this.menuEventHandler, false);
							newItems.add(fileItem);
						}
						if (newItems.size() == 10)
							break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				newItems.clear();
			} finally {
				if (scanner != null)
					scanner.close();
				this.historyMenu.setDisable(newItems.isEmpty());
			}
		}
		return this.historyMenu;
	}

	private void createImportMenu(Menu parentMenu) {
		Menu importReferencePointFlatMenu        = createMenu(i18n.getString("UIMenuBuilder.menu.import.flat.point.reference.label", "Reference points"), true);
		MenuItem importReferencePoint1DFlatItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.point.reference.1d.label", "Reference points 1D"), true, MenuItemType.IMPORT_FLAT_REFERENCE_POINT_1D, null, this.menuEventHandler, true);
		MenuItem importReferencePoint2DFlatItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.point.reference.2d.label", "Reference points 2D"), true, MenuItemType.IMPORT_FLAT_REFERENCE_POINT_2D, null, this.menuEventHandler, true);
		MenuItem importReferencePoint3DFlatItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.point.reference.3d.label", "Reference points 3D"), true, MenuItemType.IMPORT_FLAT_REFERENCE_POINT_3D, null, this.menuEventHandler, true);

		Menu importStochasticPointFlatMenu        = createMenu(i18n.getString("UIMenuBuilder.menu.import.flat.point.stochastic.label", "Stochastic points"), true);
		MenuItem importStochasticPoint1DFlatItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.point.stochastic.1d.label", "Stochastic points 1D"), true, MenuItemType.IMPORT_FLAT_STOCHASTIC_POINT_1D, null, this.menuEventHandler, true);
		MenuItem importStochasticPoint2DFlatItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.point.stochastic.2d.label", "Stochastic points 2D"), true, MenuItemType.IMPORT_FLAT_STOCHASTIC_POINT_2D, null, this.menuEventHandler, true);
		MenuItem importStochasticPoint3DFlatItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.point.stochastic.3d.label", "Stochastic points 3D"), true, MenuItemType.IMPORT_FLAT_STOCHASTIC_POINT_3D, null, this.menuEventHandler, true);

		Menu importDatumPointFlatMenu        = createMenu(i18n.getString("UIMenuBuilder.menu.import.flat.point.datum.label", "Datum points"), true);
		MenuItem importDatumPoint1DFlatItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.point.datum.1d.label", "Datum points 1D"), true, MenuItemType.IMPORT_FLAT_DATUM_POINT_1D, null, this.menuEventHandler, true);
		MenuItem importDatumPoint2DFlatItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.point.datum.2d.label", "Datum points 2D"), true, MenuItemType.IMPORT_FLAT_DATUM_POINT_2D, null, this.menuEventHandler, true);
		MenuItem importDatumPoint3DFlatItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.point.datum.3d.label", "Datum points 3D"), true, MenuItemType.IMPORT_FLAT_DATUM_POINT_3D, null, this.menuEventHandler, true);

		Menu importNewPointFlatMenu        = createMenu(i18n.getString("UIMenuBuilder.menu.import.flat.point.new.label", "New points"), true);
		MenuItem importNewPoint1DFlatItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.point.new.1d.label", "New points 1D"), true, MenuItemType.IMPORT_FLAT_NEW_POINT_1D, null, this.menuEventHandler, true);
		MenuItem importNewPoint2DFlatItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.point.new.2d.label", "New points 2D"), true, MenuItemType.IMPORT_FLAT_NEW_POINT_2D, null, this.menuEventHandler, true);
		MenuItem importNewPoint3DFlatItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.point.new.3d.label", "New points 3D"), true, MenuItemType.IMPORT_FLAT_NEW_POINT_3D, null, this.menuEventHandler, true);

		Menu importTerrestrialFlatMenu            = createMenu(i18n.getString("UIMenuBuilder.menu.import.flat.terrestrial.label", "Terrestrial observations"), true);
		MenuItem importLevelingFlatItem           = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.terrestrial.leveling.label", "Leveling data"), true, MenuItemType.IMPORT_FLAT_LEVELING, null, this.menuEventHandler, true);
		MenuItem importDirectionFlatItem          = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.terrestrial.direction.label", "Direction sets"), true, MenuItemType.IMPORT_FLAT_DIRECTION, null, this.menuEventHandler, true);
		MenuItem importHorizontalDistanceFlatItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.terrestrial.horizontal_distance.label", "Horizontal distances"), true, MenuItemType.IMPORT_FLAT_HORIZONTAL_DISTANCE, null, this.menuEventHandler, true);
		MenuItem importSlopeDistanceFlatItem      = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.terrestrial.slope_distance.label", "Slope distances"), true, MenuItemType.IMPORT_FLAT_SLOPE_DISTANCE, null, this.menuEventHandler, true);
		MenuItem importZenithAngleFlatItem        = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.terrestrial.zenith_angle.label", "Zenith angles"), true, MenuItemType.IMPORT_FLAT_ZENITH_ANGLE, null, this.menuEventHandler, true);

		Menu importCongruenceAnalysisPairFlatMenu       = createMenu(i18n.getString("UIMenuBuilder.menu.import.flat.congruence_analysis.label", "Congruence Analysis"), true);
		MenuItem importCongruenceAnalysisPair1DFlatMenu = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.congruence_analysis.1d.label", "Point nexus 1D"), true, MenuItemType.IMPORT_FLAT_CONGRUENCE_ANALYSIS_PAIR_1D, null, this.menuEventHandler, true);
		MenuItem importCongruenceAnalysisPair2DFlatMenu = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.congruence_analysis.2d.label", "Point nexus 2D"), true, MenuItemType.IMPORT_FLAT_CONGRUENCE_ANALYSIS_PAIR_2D, null, this.menuEventHandler, true);
		MenuItem importCongruenceAnalysisPair3DFlatMenu = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.congruence_analysis.3d.label", "Point nexus 3D"), true, MenuItemType.IMPORT_FLAT_CONGRUENCE_ANALYSIS_PAIR_3D, null, this.menuEventHandler, true);


		Menu importHexagonFlatMenu = createMenu(i18n.getString("UIMenuBuilder.menu.import.hexagon.label", "Hexagon/Leica"), true);
		MenuItem gsi1DFileItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.hexagon.gsi.1d.label",  "GSI 1D"),   true, MenuItemType.IMPORT_GSI1D,  null, this.menuEventHandler, true);
		MenuItem gsi2DFileItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.hexagon.gsi.2d.label",  "GSI 2D"),   true, MenuItemType.IMPORT_GSI2D,  null, this.menuEventHandler, true);
		MenuItem gsi2DHFileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.hexagon.gsi.2dh.label", "GSI 2D+H"), true, MenuItemType.IMPORT_GSI2DH, null, this.menuEventHandler, true);
		MenuItem gsi3DFileItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.hexagon.gsi.3d.label",  "GSI 3D"),   true, MenuItemType.IMPORT_GSI3D,  null, this.menuEventHandler, true);

		MenuItem hexml2DFileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.hexagon.landxml.2d.label", "LandXML 1.2 2D"), true, MenuItemType.IMPORT_LAND_XML2D, null, this.menuEventHandler, true);
		MenuItem hexml3DFileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.hexagon.landxml.3d.label", "LandXML 1.2 3D"), true, MenuItemType.IMPORT_LAND_XML3D, null, this.menuEventHandler, true);

		Menu importTrimbleFlatMenu = createMenu(i18n.getString("UIMenuBuilder.menu.import.trimble.label", "Trimble/Zeiss"), true);
		MenuItem m5FileItem        = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.trimble.m5.label", "M5 (DiNi)"), true, MenuItemType.IMPORT_M5, null, this.menuEventHandler, true);
		MenuItem jxml2DFileItem    = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.trimble.jobxml.2d.label", "JobXML 2D"),    true, MenuItemType.IMPORT_JOB_XML2D,  null, this.menuEventHandler, true);
		MenuItem jxml2DHFileItem   = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.trimble.jobxml.2dh.label", "JobXML 2D+H"), true, MenuItemType.IMPORT_JOB_XML2DH, null, this.menuEventHandler, true);
		MenuItem jxml3DFileItem    = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.trimble.jobxml.3d.label", "JobXML 3D"),    true, MenuItemType.IMPORT_JOB_XML3D,  null, this.menuEventHandler, true);

		MenuItem dl100FileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.topcon.dl100.label", "DL-100 (Topcon)"), true, MenuItemType.IMPORT_DL100, null, this.menuEventHandler, true);

		MenuItem zFileItem   = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.z.label", "Z-File (Caplan)"), true, MenuItemType.IMPORT_Z, null, this.menuEventHandler, true);
		MenuItem beoFileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.beo.label", "Beo-File (Neptan)"), true, MenuItemType.IMPORT_BEO, null, this.menuEventHandler, true);

		MenuItem columnBasedFileItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.column_based.label", "Column-based data"), true, MenuItemType.IMPORT_COLUMN_BASED_FILES, null, this.menuEventHandler, true);

		
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
				importLevelingFlatItem,
				new SeparatorMenuItem(),
				importDirectionFlatItem,
				importHorizontalDistanceFlatItem,
				new SeparatorMenuItem(),
				importSlopeDistanceFlatItem,
				importZenithAngleFlatItem
				);

		importReferencePointFlatMenu.getItems().addAll(
				importReferencePoint1DFlatItem,
				importReferencePoint2DFlatItem,
				importReferencePoint3DFlatItem
				);

		importStochasticPointFlatMenu.getItems().addAll(
				importStochasticPoint1DFlatItem,
				importStochasticPoint2DFlatItem,
				importStochasticPoint3DFlatItem
				);

		importDatumPointFlatMenu.getItems().addAll(
				importDatumPoint1DFlatItem,
				importDatumPoint2DFlatItem,
				importDatumPoint3DFlatItem
				);

		importNewPointFlatMenu.getItems().addAll(
				importNewPoint1DFlatItem,
				importNewPoint2DFlatItem,
				importNewPoint3DFlatItem
				);

		importCongruenceAnalysisPairFlatMenu.getItems().addAll(
				importCongruenceAnalysisPair1DFlatMenu,
				importCongruenceAnalysisPair2DFlatMenu,
				importCongruenceAnalysisPair3DFlatMenu
				);

		Menu importGNSSFlatMenu       = createMenu(i18n.getString("UIMenuBuilder.menu.import.flat.gnss.label", "GNSS baselines"), true);
		MenuItem importGNSS1DFlatItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.gnss.1d.label", "GNSS baselines 1D"), true, MenuItemType.IMPORT_FLAT_GNSS1D, null, this.menuEventHandler, true);
		MenuItem importGNSS2DFlatItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.gnss.2d.label", "GNSS baselines 2D"), true, MenuItemType.IMPORT_FLAT_GNSS2D, null, this.menuEventHandler, true);
		MenuItem importGNSS3DFlatItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.import.flat.gnss.3d.label", "GNSS baselines 3D"), true, MenuItemType.IMPORT_FLAT_GNSS3D, null, this.menuEventHandler, true);

		importGNSSFlatMenu.getItems().addAll(
				importGNSS1DFlatItem,
				importGNSS2DFlatItem,
				importGNSS3DFlatItem
				);

		parentMenu.getItems().addAll(
				importReferencePointFlatMenu,
				importStochasticPointFlatMenu,
				importDatumPointFlatMenu,
				importNewPointFlatMenu,
				new SeparatorMenuItem(),
				importTerrestrialFlatMenu,
				importGNSSFlatMenu,
				new SeparatorMenuItem(),
				importCongruenceAnalysisPairFlatMenu,
				new SeparatorMenuItem(),
				importHexagonFlatMenu,
				importTrimbleFlatMenu,
				dl100FileItem,
				new SeparatorMenuItem(),
				beoFileItem,
				zFileItem,
				new SeparatorMenuItem(),
				columnBasedFileItem
				);
	}

	private static Menu createMenu(String label, boolean mnemonicParsing) {
		Menu menu = new Menu(label);
		menu.setMnemonicParsing(mnemonicParsing);		
		return menu;
	}

//	private static RadioMenuItem createRadioMenuItem(String label, boolean mnemonicParsing, MenuItemType menuItemType, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
//		RadioMenuItem menuItem = new RadioMenuItem(label);
//		menuItem.setMnemonicParsing(mnemonicParsing);
//		if (keyCodeCombination != null)
//			menuItem.setAccelerator(keyCodeCombination);
//		menuItem.setOnAction(menuEventHandler);
//		menuItem.setDisable(disable);
//		menuItem.setUserData(menuItemType);
//		return menuItem;
//	}
	
	private static MenuItem createMenuItem(MenuItem menuItem, boolean mnemonicParsing, MenuItemType menuItemType, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		menuItem.setMnemonicParsing(mnemonicParsing);
		if (keyCodeCombination != null)
			menuItem.setAccelerator(keyCodeCombination);
		menuItem.setOnAction(menuEventHandler);
		menuItem.setDisable(disable);
		menuItem.setUserData(menuItemType);
		return menuItem;
	}

	private static MenuItem createMenuItem(String label, boolean mnemonicParsing, MenuItemType menuItemType, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		MenuItem menuItem = createMenuItem(new MenuItem(label), mnemonicParsing, menuItemType, keyCodeCombination, menuEventHandler, disable);

		return menuItem;
	}

	private static MenuItem createMenuItem(String label, boolean mnemonicParsing, MenuItemType menuItemType, File file, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		FileMenuItem menuItem = (FileMenuItem)createMenuItem(new FileMenuItem(label), mnemonicParsing, menuItemType, keyCodeCombination, menuEventHandler, disable);
		menuItem.setFile(file);
		return menuItem;
	}

	void newProject() {
		File selectedFile = DefaultFileChooser.showSaveDialog(
				i18n.getString("UIMenuBuilder.filechooser.new.title", "Create new project"),
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
					JAG3D.setTitle(path.getFileName() == null ? null : path.getFileName().toString().replaceFirst(regex, "$1"));
					this.addHistoryFile(selectedFile);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			OptionDialog.showThrowableDialog (
					i18n.getString("UIMenuBuilder.message.error.new.exception.title", "I/O Error"),
					i18n.getString("UIMenuBuilder.message.error.new.exception.header", "Error, could not create new project."),
					i18n.getString("UIMenuBuilder.message.error.new.exception.message", "An exception has occurred during project creation."),
					e
					);
			SQLManager.getInstance().closeDataBase();
		}
	}

	void copyProject() {
		try {
			// check for loaded db
			if (!SQLManager.getInstance().hasDatabase())
				return;

			String dataBaseFileExtensions[] = new String[] { "data", "properties", "script", "backup" };
			String currentDataBaseName = SQLManager.getInstance().getDataBase() != null && SQLManager.getInstance().getDataBase() instanceof HSQLDB ? ((HSQLDB)SQLManager.getInstance().getDataBase()).getDataBaseFileName() : null;

			// check for HSQLDB type database
			if (currentDataBaseName == null)
				return;

			File selectedFile = DefaultFileChooser.showSaveDialog(
					i18n.getString("UIMenuBuilder.filechooser.copy.title", "Create deep copy of current project"),
					null,
					new ExtensionFilter("HSQLDB (*.script)", "*.script")
					);

			if (selectedFile == null)
				return;

			String regex = "(.+?)(\\.)(backup$|data$|properties$|script$)";
			String copyDataBaseName = selectedFile.getAbsolutePath().replaceAll(regex, "$1");

			// check if selected file equals to loaded file
			if (currentDataBaseName.equals(copyDataBaseName))
				return;
			
			// close current database
			SQLManager.getInstance().closeDataBase();

			// copy files
			for (int i=0; i<dataBaseFileExtensions.length; i++) {
				Path src = Paths.get(currentDataBaseName + "." + dataBaseFileExtensions[i]);
				if (!Files.exists(src, LinkOption.NOFOLLOW_LINKS))
					continue;
				
				Path trg = Paths.get(copyDataBaseName + "." + dataBaseFileExtensions[i]);
				Files.copy(src, trg, StandardCopyOption.REPLACE_EXISTING);
			}
			
			// open copied database
			if (Files.exists(selectedFile.toPath(), LinkOption.NOFOLLOW_LINKS) && Files.isRegularFile(selectedFile.toPath(), LinkOption.NOFOLLOW_LINKS))
				this.openProject(selectedFile);
			
		} 
		catch (Exception e) {
			e.printStackTrace();
			OptionDialog.showThrowableDialog (
					i18n.getString("UIMenuBuilder.message.error.copy.exception.title", "I/O Error"),
					i18n.getString("UIMenuBuilder.message.error.copy.exception.header", "Error, could not create a deep copy of the current database."),
					i18n.getString("UIMenuBuilder.message.error.copy.exception.message", "An exception has occurred during project copying."),
					e
					);
		}
	}

	void openProject() {
		File selectedFile = DefaultFileChooser.showOpenDialog(
				i18n.getString("UIMenuBuilder.filechooser.open.title", "Open existing project"),
				null,
				new ExtensionFilter("HSQLDB (*.script)", "*.script")
				);

		if (selectedFile != null)
			this.openProject(selectedFile);
	}

	void openProject(File selectedFile) {
		try {
			if (selectedFile != null) {
				Path path = selectedFile.toPath();
				String regex = "(?i)(.+?)(\\.)(backup$|data$|properties$|script$)";
				String project = Files.exists(path, LinkOption.NOFOLLOW_LINKS) ? path.toAbsolutePath().toString().replaceFirst(regex, "$1") : null;

				if (project != null) {
					SQLManager.openExistingProject(new HSQLDB(project));
					JAG3D.setTitle(path.getFileName() == null ? null : path.getFileName().toString().replaceFirst(regex, "$1"));
					this.addHistoryFile(selectedFile);
					this.historyMenu.getParentMenu().hide();
					DefaultFileChooser.setLastSelectedDirectory(selectedFile);
				}
				else {
					throw new FileNotFoundException(selectedFile.toString());
				}
			}
		}
		catch(DatabaseVersionMismatchException e) {
			e.printStackTrace();
			OptionDialog.showThrowableDialog (
					i18n.getString("UIMenuBuilder.message.error.version.exception.title", "Version error"),
					i18n.getString("UIMenuBuilder.message.error.version.exception.header", "Error, database version of the stored project is greater\r\nthan accepted database version of the application."),
					i18n.getString("UIMenuBuilder.message.error.version.exception.message", "An exception has occurred during project opening."),
					e
					);
			SQLManager.getInstance().closeDataBase();
		}
		catch (Exception e) {
			e.printStackTrace();
			OptionDialog.showThrowableDialog (
					i18n.getString("UIMenuBuilder.message.error.open.exception.title", "I/O Error"),
					i18n.getString("UIMenuBuilder.message.error.open.exception.header", "Error, could not open selected project."),
					i18n.getString("UIMenuBuilder.message.error.open.exception.message", "An exception has occurred during project opening."),
					e
					);
			SQLManager.getInstance().closeDataBase();
		}
	}

	private void importFile(SourceFileReader fileReader, ExtensionFilter[] extensionFilters, String title) {
		List<File> selectedFiles = DefaultFileChooser.showOpenMultipleDialog(
				title,
				null,
				extensionFilters
				);

		if (selectedFiles == null || selectedFiles.isEmpty())
			return;

		this.importFile(fileReader, selectedFiles);
	}
	
	private void importFile(SourceFileReader fileReader, List<File> selectedFiles) {
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
						i18n.getString("UIMenuBuilder.message.error.import.exception.title", "I/O Error"),
						i18n.getString("UIMenuBuilder.message.error.import.exception.header", "Error, could not import selected file."),
						i18n.getString("UIMenuBuilder.message.error.import.exception.message", "An exception has occurred during file import."),
						e
						);
			}
		}

		if (lastItem != null) {
			TreeView<TreeItemValue> treeView = UITreeBuilder.getInstance().getTree();
			treeView.getSelectionModel().clearSelection();
			treeView.getSelectionModel().select(lastItem);
			int index = treeView.getRow(lastItem);
			treeView.scrollTo(index);
		}
	}

	void importFile(MenuItemType menuItemType) {
		switch(menuItemType) {
		case IMPORT_FLAT_REFERENCE_POINT_1D:
			this.importFile(new PointFlatFileReader(PointType.REFERENCE_POINT, 1), PointFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.point.reference.1d.title", "Import 1D reference point data from flat files"));
			break;
		case IMPORT_FLAT_REFERENCE_POINT_2D:
			this.importFile(new PointFlatFileReader(PointType.REFERENCE_POINT, 2), PointFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.point.reference.2d.title", "Import 2D reference point data from flat files"));
			break;
		case IMPORT_FLAT_REFERENCE_POINT_3D:
			this.importFile(new PointFlatFileReader(PointType.REFERENCE_POINT, 3), PointFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.point.reference.3d.title", "Import 3D reference point data from flat files"));
			break;

		case IMPORT_FLAT_STOCHASTIC_POINT_1D:
			this.importFile(new PointFlatFileReader(PointType.STOCHASTIC_POINT, 1), PointFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.point.stochastic.1d.title", "Import 1D stochastic point data from flat files"));
			break;
		case IMPORT_FLAT_STOCHASTIC_POINT_2D:
			this.importFile(new PointFlatFileReader(PointType.STOCHASTIC_POINT, 2), PointFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.point.stochastic.2d.title", "Import 2D stochastic point data from flat files"));
			break;
		case IMPORT_FLAT_STOCHASTIC_POINT_3D:
			this.importFile(new PointFlatFileReader(PointType.STOCHASTIC_POINT, 3), PointFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.point.stochastic.3d.title", "Import 3D stochastic point data from flat files"));
			break;

		case IMPORT_FLAT_DATUM_POINT_1D:
			this.importFile(new PointFlatFileReader(PointType.DATUM_POINT, 1), PointFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.point.datum.1d.title", "Import 1D datum point data from flat files"));
			break;
		case IMPORT_FLAT_DATUM_POINT_2D:
			this.importFile(new PointFlatFileReader(PointType.DATUM_POINT, 2), PointFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.point.datum.2d.title", "Import 2D datum point data from flat files"));
			break;
		case IMPORT_FLAT_DATUM_POINT_3D:
			this.importFile(new PointFlatFileReader(PointType.DATUM_POINT, 3), PointFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.point.datum.3d.title", "Import 3D datum point data from flat files"));
			break;

		case IMPORT_FLAT_NEW_POINT_1D:
			this.importFile(new PointFlatFileReader(PointType.NEW_POINT, 1), PointFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.point.new.1d.title", "Import 1D new point data from flat files"));
			break;
		case IMPORT_FLAT_NEW_POINT_2D:
			this.importFile(new PointFlatFileReader(PointType.NEW_POINT, 2), PointFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.point.new.2d.title", "Import 2D new point data from flat files"));
			break;
		case IMPORT_FLAT_NEW_POINT_3D:
			this.importFile(new PointFlatFileReader(PointType.NEW_POINT, 3), PointFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.point.new.3d.title", "Import 3D new point data from flat files"));
			break;

		case IMPORT_FLAT_LEVELING:
			this.importFile(new ObservationFlatFileReader(ObservationType.LEVELING), ObservationFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.leveling.title", "Import leveling data from flat files"));
			break;
		case IMPORT_FLAT_DIRECTION:
			this.importFile(new ObservationFlatFileReader(ObservationType.DIRECTION), ObservationFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.direction.title", "Import direction sets from flat files"));
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

		case IMPORT_FLAT_CONGRUENCE_ANALYSIS_PAIR_1D:
			this.importFile(new CongruenceAnalysisFlatFileReader(1), CongruenceAnalysisFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.congruence_analysis.1d.title", "Import congruence analysis point nexus 1D from flat files"));
			break;
		case IMPORT_FLAT_CONGRUENCE_ANALYSIS_PAIR_2D:
			this.importFile(new CongruenceAnalysisFlatFileReader(2), CongruenceAnalysisFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.congruence_analysis.2d.title", "Import congruence analysis point nexus 2D from flat files"));
			break;
		case IMPORT_FLAT_CONGRUENCE_ANALYSIS_PAIR_3D:
			this.importFile(new CongruenceAnalysisFlatFileReader(3), CongruenceAnalysisFlatFileReader.getExtensionFilters(), i18n.getString("UIMenuBuilder.filechooser.import.flat.congruence_analysis.3d.title", "Import congruence analysis point nexus 3D from flat files"));
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
			
		case IMPORT_COLUMN_BASED_FILES:
			List<File> selectedFiles = DefaultFileChooser.showOpenMultipleDialog(
					i18n.getString("UIMenuBuilder.filechooser.import.column_based.title", "Import user-defined column-based flat files"),
					null,
					LockFileReader.getExtensionFilters()
					);

			if (selectedFiles == null || selectedFiles.isEmpty())
				return;
			
			Optional<SourceFileReader> optional = ColumnImportDialog.showAndWait(selectedFiles.get(0));
			if (optional.isPresent() && optional.get() != null) {
				SourceFileReader fileReader = optional.get();
				this.importFile(fileReader, selectedFiles);
			}	
			break;

		default:
			System.err.println(this.getClass().getSimpleName() + " Error, unknwon import-type: " + menuItemType);
			break;

		}
	}

	void createReport(File templateFile) {
		try {
			FTLReport ftl = SQLManager.getInstance().getFTLReport();
			File reportFile = DefaultFileChooser.showSaveDialog(
					i18n.getString("UIMenuBuilder.filechooser.report.title", "Save adjustment report"), 
					"report.html",
					new ExtensionFilter(i18n.getString("UIMenuBuilder.extension.html", "Hypertext Markup Language"), "*.html", "*.htm") 
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

	public void checkUpdates() {
		final String address = "https://software.applied-geodesy.org/update.php"; //"https://software.applied-geodesy.org/update.php";
		URLParameter param = new URLParameter("checkupdate", "jag3d");
		try {
			boolean validProperties = false;
			Properties properties = HTTPPropertiesLoader.getProperties(address, param);
			if (properties != null && properties.containsKey("VERSION")) { // VERSION
				int propVersion = Integer.parseInt(properties.getProperty("VERSION", "-1"));

				if (propVersion > 0) {
					validProperties = true; 
					if (propVersion > Version.get()) {
						Optional<ButtonType> result = OptionDialog.showConfirmationDialog(
								i18n.getString("UIMenuBuilder.message.confirmation.outdated_version.title", "New version available"),
								String.format(Locale.ENGLISH, i18n.getString("UIMenuBuilder.message.confirmation.outdated_version.header", "A new version v%d of JAG3D is available.\r\nDo you want to download the latest release?"), propVersion),
								i18n.getString("UIMenuBuilder.message.confirmation.outdated_version.message", "The currently used application is outdated. A new version of JAG3D is available at <software.applied-geodesy.org>.")
								);


						if (result.get() == ButtonType.OK && this.hostServices != null) 
							this.hostServices.showDocument(properties.getProperty("DOWNLOAD", "http://software.applied-geodesy.org"));

					} 
					else {
						OptionDialog.showInformationDialog(
								i18n.getString("UIMenuBuilder.message.information.latest_version.title", "JAG3D is up-to-date"),
								i18n.getString("UIMenuBuilder.message.information.latest_version.header", "You are using the latest version of JAG3D."),
								i18n.getString("UIMenuBuilder.message.information.latest_version.message", "No update found, JAG3D software package is up-to-date.")
								);
					}
				}
			}

			if (!validProperties)
				throw new IOException("Error, could not connect to server to check for updates. Received properties are invalid. " + properties);
		} catch (Exception e) {
			e.printStackTrace();
			OptionDialog.showThrowableDialog (
					i18n.getString("UIMenuBuilder.message.error.check_updates.exception.title", "I/O Error"),
					i18n.getString("UIMenuBuilder.message.error.check_updates.exception.header", "Error, could not connect to server to check for updates."),
					i18n.getString("UIMenuBuilder.message.error.check_updates.exception.message", "An exception has occurred during check for updates."),
					e
					);
		}
	}

	// TODO transfer application to FX
	void showSwingApplication(MenuItemType menuItemType) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				switch (menuItemType) {
				case MODULE_COORDTRANS:
					new CoordTrans(true);
					break;
				case MODULE_FORMFITTINGTOOLBOX:
					new FormFittingToolbox(true);
					break;
				case MODULE_GEOTRA:
					new GeoTra(true);
					break;
				default:
					break;
				}
			}
		});
	}

	private void initHistoryPathFromProperties() {
		BufferedInputStream bis = null;
		final String path = "/properties/paths.default";
		try {
			if (this.getClass().getResource(path) != null) {
				Properties PROPERTIES = new Properties();
				bis = new BufferedInputStream(this.getClass().getResourceAsStream(path));
				PROPERTIES.load(bis);
				String defaultHistoryPath = PROPERTIES.getProperty("HISTORY", System.getProperty("user.home", null));

				if (defaultHistoryPath != null && Files.exists(Paths.get(defaultHistoryPath)) && Files.isDirectory(Paths.get(defaultHistoryPath), LinkOption.NOFOLLOW_LINKS))
					this.historyFile = new File(defaultHistoryPath + File.separator + ".jag3d_history");
				else 
					this.historyFile = new File(System.getProperty("user.home") + File.separator + ".jag3d_history");
			}  
		} catch (Exception e) {
			e.printStackTrace();
			this.historyFile = new File(System.getProperty("user.home") + File.separator + ".jag3d_history");
		}
		finally {
			try {
				if (bis != null)
					bis.close();  
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}