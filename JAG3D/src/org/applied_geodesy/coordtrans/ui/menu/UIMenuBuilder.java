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

package org.applied_geodesy.coordtrans.ui.menu;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.applied_geodesy.adjustment.transformation.HeightTransformation;
import org.applied_geodesy.adjustment.transformation.PlanarAffineTransformation;
import org.applied_geodesy.adjustment.transformation.SpatialAffineTransformation;
import org.applied_geodesy.adjustment.transformation.Transformation;
import org.applied_geodesy.adjustment.transformation.TransformationChangeListener;
import org.applied_geodesy.adjustment.transformation.TransformationEvent;
import org.applied_geodesy.adjustment.transformation.TransformationEvent.TransformationEventType;
import org.applied_geodesy.adjustment.transformation.TransformationType;
import org.applied_geodesy.adjustment.transformation.point.FramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.ObservedFramePosition;
import org.applied_geodesy.coordtrans.ui.CoordTrans;
import org.applied_geodesy.coordtrans.ui.dialog.FilePathsSelectionDialog;
import org.applied_geodesy.coordtrans.ui.dialog.FilePathsSelectionDialog.FilePathPair;
import org.applied_geodesy.coordtrans.ui.dialog.ReadFileProgressDialog;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.coordtrans.ui.io.reader.PositionFileReader;
import org.applied_geodesy.coordtrans.ui.io.writer.FTLReport;
import org.applied_geodesy.coordtrans.ui.io.writer.MatlabTransformationAdjustmentResultWriter;
import org.applied_geodesy.coordtrans.ui.table.UIFramePositionPairTableBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIHomologousFramePositionPairTableBuilder;
import org.applied_geodesy.coordtrans.ui.tree.UITreeBuilder;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.ui.io.DefaultFileChooser;
import org.applied_geodesy.util.ObservableUniqueList;

import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser.ExtensionFilter;

public class UIMenuBuilder implements TransformationChangeListener {

	private static UIMenuBuilder menuBuilder = new UIMenuBuilder();
	private I18N i18n = I18N.getInstance();
	private MenuEventHandler menuEventHandler = new MenuEventHandler(this);
	private MenuBar menuBar;
	private UIMenuBuilder() {}
	private Menu reportMenu;

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
		Menu fileMenu       = createMenu(i18n.getString("UIMenuBuilder.menu.file.label", "_File"),  true);
		
		Menu adjustmentMenu = createMenu(i18n.getString("UIMenuBuilder.menu.adjustment.label", "Ad_justment"), true);
		Menu analysisMenu   = createMenu(i18n.getString("UIMenuBuilder.menu.analysis.label", "Anal_ysis"), true);
		this.reportMenu     = createMenu(i18n.getString("UIMenuBuilder.menu.report.label", "Repor_t"), true);
		Menu helpMenu       = createMenu(i18n.getString("UIMenuBuilder.menu.help.label", "_?"), true);
		
		this.createFileMenu(fileMenu);
		this.createAnalysisMenu(analysisMenu);
		this.createAdjustmentMenu(adjustmentMenu);
		this.createReportMenu(this.reportMenu);
		this.createHelpMenu(helpMenu);
		
		this.menuBar.getMenus().addAll(
				fileMenu,
				adjustmentMenu,
				analysisMenu,
				this.reportMenu,
				helpMenu
		);
	}
	
	private void createFileMenu(Menu parentMenu) {
		MenuItem importPositionsItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.file.import.positions.label",  "Imp_ort data"), true, MenuItemType.IMPORT_POSITIONS, new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		MenuItem exitItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.file.exit.label", "_Exit"), true, MenuItemType.EXIT, new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		
		parentMenu.getItems().addAll(
				importPositionsItem,
				new SeparatorMenuItem(),
				exitItem
		);
	}
	
	private void createAdjustmentMenu(Menu parentMenu) {
		MenuItem leastSquaresItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.adjustment.leastsquares.label", "_Least-squares"), true, MenuItemType.LEAST_SQUARES, new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem teststatisticItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.adjustment.teststatistic.label", "Test st_atistic"), true, MenuItemType.TEST_STATISTIC, new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem preferencesItem   = createMenuItem(i18n.getString("UIMenuBuilder.menu.adjustment.preferences.label", "Preferen_ces"), true, MenuItemType.PREFERENCES, new KeyCodeCombination(KeyCode.ENTER, KeyCombination.ALT_DOWN), this.menuEventHandler, false);
		parentMenu.getItems().addAll(
				leastSquaresItem,
				teststatisticItem,
				new SeparatorMenuItem(),
				preferencesItem
		);
	}
	
	private void createAnalysisMenu(Menu parentMenu) {
		MenuItem quantileItem          = createMenuItem(i18n.getString("UIMenuBuilder.menu.analysis.quantile.label", "_Quantiles"), true, MenuItemType.QUANTILES, new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem varianceComponentItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.analysis.variance_component.label", "_Variance components"), true, MenuItemType.VARIANCE_COMPONENT_ESTIMATION, new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		
		
		parentMenu.getItems().addAll(
				varianceComponentItem,
				quantileItem
		);
	}
	
	private void createReportMenu(Menu parentMenu) {
		MenuItem matlabItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.report.matlab.label", "_Matlab file"), true, MenuItemType.EXPORT_MATLAB, new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		parentMenu.getItems().add(matlabItem);
		
		List<File> templateFiles = FTLReport.getTemplates();
		if (templateFiles != null && !templateFiles.isEmpty()) {
			for (File templateFile : templateFiles) {
				MenuItem templateFileItem = createMenuItem(templateFile.getName(), false, MenuItemType.REPORT, templateFile, null, this.menuEventHandler, true);
				parentMenu.getItems().add(templateFileItem);
			}
		}
	}
	
	private void createHelpMenu(Menu parentMenu) {
		MenuItem aboutItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.help.about.label", "A_bout CoordTrans"), true, MenuItemType.ABOUT, new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		parentMenu.getItems().addAll(
				aboutItem
		);
	}
	
	private static Menu createMenu(String label, boolean mnemonicParsing) {
		Menu menu = new Menu(label);
		menu.setMnemonicParsing(mnemonicParsing);		
		return menu;
	}
	
	private static MenuItem createMenuItem(String label, boolean mnemonicParsing, MenuItemType menuItemType, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		MenuItem menuItem = createMenuItem(new MenuItem(label), mnemonicParsing, menuItemType, keyCodeCombination, menuEventHandler, disable);

		return menuItem;
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

	private static MenuItem createMenuItem(String label, boolean mnemonicParsing, MenuItemType menuItemType, File file, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		FileMenuItem menuItem = (FileMenuItem)createMenuItem(new FileMenuItem(label), mnemonicParsing, menuItemType, keyCodeCombination, menuEventHandler, disable);
		menuItem.setFile(file);
		return menuItem;
	}
	
	
	void importPositions() {
		FilePathPair filePathPair = null;
		Optional<FilePathPair> optional = FilePathsSelectionDialog.showAndWait();
		
		if (optional.isPresent())
			filePathPair = optional.get();
		
		if (filePathPair != null) 
			this.importPositions(filePathPair);
	}
	
	private void importPositions(FilePathPair filePathPair) {
		TransformationType transformationType = filePathPair.getTransformationType();
		PositionFileReader fileReader = new PositionFileReader(transformationType);
		ReadFileProgressDialog<Map<String, ObservedFramePosition>> dialog = new ReadFileProgressDialog<Map<String, ObservedFramePosition>>();
		Optional<List<Map<String, ObservedFramePosition>>> optional = dialog.showAndWait(
				fileReader, 
				List.of(filePathPair.getSourceFilePath().toFile(), filePathPair.getTargetFilePath().toFile()),
				Boolean.TRUE
				);
		
		List<Map<String, ObservedFramePosition>> positions = null;
		
		if (optional.isPresent()) {
			positions = optional.get();
		}

		if (positions == null || positions.size() < 2)
			return;
		
		Map<String, ObservedFramePosition> sourceSystemPositions = positions.get(0);
		Map<String, ObservedFramePosition> targetSystemPositions = positions.get(1); 
		
		if (sourceSystemPositions == null || sourceSystemPositions.isEmpty() || targetSystemPositions == null || targetSystemPositions.isEmpty())
			return;
		
		ObservableUniqueList<HomologousFramePositionPair> homologousFramePositionPairs = new ObservableUniqueList<HomologousFramePositionPair>(Math.min(sourceSystemPositions.size(), targetSystemPositions.size()));
		ObservableUniqueList<FramePositionPair> framePositionPairs = new ObservableUniqueList<FramePositionPair>(sourceSystemPositions.size());
		
		for (Map.Entry<String, ObservedFramePosition> point : sourceSystemPositions.entrySet()) {
			String name = point.getKey();
			ObservedFramePosition sourcePosition = point.getValue();
			
			framePositionPairs.add(new FramePositionPair(name, sourcePosition));
			
			if (targetSystemPositions.containsKey(name)) {
				ObservedFramePosition targetPosition = targetSystemPositions.get(name);

				switch (transformationType) {
				case HEIGHT:
					homologousFramePositionPairs.add(
							new HomologousFramePositionPair(
									name, 
									
									sourcePosition.getZ(), 
									sourcePosition.getDispersionApriori(), 
									
									targetPosition.getZ(), 
									targetPosition.getDispersionApriori())
							);
					break;
				case PLANAR:
					homologousFramePositionPairs.add(
							new HomologousFramePositionPair(
									name, 
									
									sourcePosition.getX(), 
									sourcePosition.getY(), 
									sourcePosition.getDispersionApriori(), 
									
									targetPosition.getX(), 
									targetPosition.getY(), 
									targetPosition.getDispersionApriori())
							);
					break;
				case SPATIAL:
					homologousFramePositionPairs.add(
							new HomologousFramePositionPair(
									name, 
									
									sourcePosition.getX(), 
									sourcePosition.getY(), 
									sourcePosition.getZ(), 
									sourcePosition.getDispersionApriori(), 
									
									targetPosition.getX(), 
									targetPosition.getY(), 
									targetPosition.getZ(), 
									targetPosition.getDispersionApriori())
							);
					break;			
				}
			}
		}
		
		UIHomologousFramePositionPairTableBuilder homologousFramePositionPairTableBuilder = UIHomologousFramePositionPairTableBuilder.getInstance();
		homologousFramePositionPairTableBuilder.setTransformationType(transformationType);
		TableView<HomologousFramePositionPair> homologousPositionPairTable = homologousFramePositionPairTableBuilder.getTable();
		ObservableList<HomologousFramePositionPair> homologousPositionPairTableModel = homologousFramePositionPairTableBuilder.getTableModel(homologousPositionPairTable);
		homologousPositionPairTableModel.setAll(homologousFramePositionPairs);

		UIFramePositionPairTableBuilder framePositionPairTableBuilder = UIFramePositionPairTableBuilder.getInstance();
		framePositionPairTableBuilder.setTransformationType(transformationType);
		TableView<FramePositionPair> positionPairTable = framePositionPairTableBuilder.getTable();
		ObservableList<FramePositionPair> positionPairTableModel = framePositionPairTableBuilder.getTableModel(positionPairTable);
		positionPairTableModel.setAll(framePositionPairs);
		
		// Create new transformation	
		Transformation transformation = null;
		switch(transformationType) {
		case HEIGHT:
			transformation = new HeightTransformation();
			break;
		case PLANAR:
			transformation = new PlanarAffineTransformation();
			break;
		case SPATIAL:
			transformation = new SpatialAffineTransformation();
			break;
		default:
			break;
		}

		if (transformation == null)
			CoordTrans.setTitle(null);
		else
			CoordTrans.setTitle(transformationType.getDimension());
		
		this.fireTransformationChanged(transformation);
	}
	
	void createMatlabFile() {
		try {
			if (UITreeBuilder.getInstance().getTransformationAdjustment().getTransformation() == null)
				return;

			ExtensionFilter extensionFilter = new ExtensionFilter(i18n.getString("UIMenuBuilder.report.extension.mat", "Binary Matlab file"), "*.mat", "*.MAT");
			String fileNameSuggestion = "transformation.mat";

			MatlabTransformationAdjustmentResultWriter matWriter = new MatlabTransformationAdjustmentResultWriter();
			File binFile = DefaultFileChooser.showSaveDialog(
					CoordTrans.getStage(),
					i18n.getString("UIMenuBuilder.filechooser.export.matlab.title", "Save Matlab file"), 
					fileNameSuggestion,
					extensionFilter
			);
			if (binFile != null && matWriter != null) {
				matWriter.toFile(binFile, UITreeBuilder.getInstance().getTransformationAdjustment());
			}

		} catch (Exception e) {
			e.printStackTrace();
			OptionDialog.showThrowableDialog (
					i18n.getString("UIMenuBuilder.message.error.export.matlab.exception.title", "I/O Error"),
					i18n.getString("UIMenuBuilder.message.error.export.matlab.exception.header", "Error, could not binary Matlab file."),
					i18n.getString("UIMenuBuilder.message.error.export.matlab.exception.message", "An exception has occurred during file creation."),
					e
					);
		}
	}
	
	void createReport(File templateFile) {
		try {
			if (UITreeBuilder.getInstance().getTransformationAdjustment().getTransformation() == null)
				return;

			Pattern pattern = Pattern.compile(".*?\\.(\\w+)\\.ftlh$", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(templateFile.getName().toLowerCase());
			String extension = "html";
			ExtensionFilter extensionFilter = new ExtensionFilter(i18n.getString("UIMenuBuilder.report.extension.html", "Hypertext Markup Language"), "*.html", "*.htm", "*.HTML", "*.HTM");
			if (matcher.find() && matcher.groupCount() == 1) {
				extension = matcher.group(1);
				extensionFilter = new ExtensionFilter(String.format(Locale.ENGLISH, i18n.getString("UIMenuBuilder.report.extension.template", "%s-File"), extension), "*." + extension); 
			}
			
			String fileNameSuggestion = "report." + extension;

			FTLReport ftl = new FTLReport(UITreeBuilder.getInstance().getTransformationAdjustment());
			File reportFile = DefaultFileChooser.showSaveDialog(
					CoordTrans.getStage(),
					i18n.getString("UIMenuBuilder.filechooser.export.report.title", "Save adjustment report"), 
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
					i18n.getString("UIMenuBuilder.message.error.export.report.exception.title", "I/O Error"),
					i18n.getString("UIMenuBuilder.message.error.export.report.exception.header", "Error, could not create adjustment report."),
					i18n.getString("UIMenuBuilder.message.error.export.report.exception.message", "An exception has occurred during report creation."),
					e
					);
		}
	}
	
	public void setReportMenuDisable(boolean disable) {
		for (MenuItem item : this.reportMenu.getItems())
			item.setDisable(disable);
	}
	
	private void fireTransformationChanged(Transformation transformation) {
//		this.setTransformation(transformation);
		UITreeBuilder treeBuilder = UITreeBuilder.getInstance();
		treeBuilder.getTransformationAdjustment().setTransformation(transformation);
		treeBuilder.handleTreeSelections();
	}
	
	void disableMenu(boolean disable) {
		List<Menu> menus = this.menuBar.getMenus();
		for (Menu menu : menus)
			this.disableMenu(menu, disable);
	}
	
	private void disableMenu(Menu menu, boolean disable) {
		List<MenuItem> items = menu.getItems();
		for (MenuItem item : items) {
			if (item instanceof Menu)
				this.disableMenu((Menu)item, disable);
			else if (item.getUserData() != null && item.getUserData() instanceof MenuItemType) { 
				MenuItemType itemType = (MenuItemType)item.getUserData();
				switch(itemType) {

				case TEST_STATISTIC:
				case LEAST_SQUARES:
				case QUANTILES:
				case VARIANCE_COMPONENT_ESTIMATION:
					item.setDisable(disable);
					break;
					
				case REPORT:
				case EXPORT_MATLAB:
					this.setReportMenuDisable(true);
					break;
					
				case EXIT:
				case ABOUT:
				case PREFERENCES:
				case IMPORT_POSITIONS:
					// do nothing
					break;
				}
			}
		}
	}

	@Override
	public void transformationChanged(TransformationEvent evt) {
		this.disableMenu(evt.getEventType() != TransformationEventType.TRANSFORMATION_MODEL_ADDED);
	}
}
