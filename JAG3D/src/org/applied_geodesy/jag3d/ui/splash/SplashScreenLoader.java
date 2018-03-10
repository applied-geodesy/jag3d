package org.applied_geodesy.jag3d.ui.splash;

import org.applied_geodesy.util.ImageUtils;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreenLoader extends Preloader {

	private Stage splashScreen;

	@Override
	public void start(Stage stage) throws Exception {
		this.splashScreen = stage;

		Image image = null;
		try {
			image = ImageUtils.getImage("JAG3D_255x255_splash.gif");
		} catch (Exception e) {
			e.printStackTrace();
			image = null;
		}

		if (image == null) {
			this.splashScreen.hide();
			return;
		}

		StackPane root = new StackPane();
		root.getChildren().add(new ImageView(image));
		Scene scene = new Scene(root, 256, 256);

		this.splashScreen.initStyle(StageStyle.UNDECORATED);
		this.splashScreen.setResizable(false);
		this.splashScreen.setScene(scene);
		this.splashScreen.show();
	}

	@Override
	public void handleProgressNotification(ProgressNotification pn) { }

	@Override
	public void handleStateChangeNotification(StateChangeNotification evt) {
		if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
			this.splashScreen.hide();
		}
	}    
}