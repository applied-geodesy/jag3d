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

package org.applied_geodesy.ui.tex;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

public class LaTexLabel extends Label {
	private class TexChangeListener implements ChangeListener<String> {
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			process();
		}
	}
	
	private int style = TeXConstants.STYLE_DISPLAY;
	private float size = 16f;
	private Color foregroundColor = Color.BLACK;
	private StringProperty tex = new SimpleStringProperty();
	
	public LaTexLabel() {
		this(null);
	}
	
	public LaTexLabel(String tex) {
		this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		this.tex.addListener(new TexChangeListener());
		this.setTex(tex);
	}
	
	private void process() {
		if (this.tex.get().isBlank())
			this.setGraphic(null);
		else {
			TeXFormula formula = new TeXFormula(this.tex.get());
			Image imageAWT = formula.createBufferedImage(this.style, this.size, this.foregroundColor, null);
			WritableImage writableImage = SwingFXUtils.toFXImage((BufferedImage) imageAWT, null);
			ImageView view = new ImageView(writableImage);
			this.setGraphic(view);
		}
	}
	
	public StringProperty texProperty() {
		return this.tex;
	}
	
	public String getTex() {
		return this.tex.get();
	}
	
	public void setTex(String tex) {
		this.tex.set(tex);
	}
	
	public void setStyle(int style) {
		this.style = style;
		this.process();
	}
	
	public void setSize(float size) {
		this.size = size;
		this.process();
	}
	
	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
		this.process();
	}
}
