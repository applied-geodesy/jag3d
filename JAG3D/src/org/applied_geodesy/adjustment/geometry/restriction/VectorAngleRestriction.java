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

package org.applied_geodesy.adjustment.geometry.restriction;

import java.util.ArrayList;
import java.util.List;

import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import no.uib.cipr.matrix.Matrix;

public class VectorAngleRestriction extends Restriction {
	private ObservableList<UnknownParameter> regressorsA = FXCollections.<UnknownParameter>observableArrayList();
	private ObservableList<UnknownParameter> regressorsB = FXCollections.<UnknownParameter>observableArrayList();
	
	public VectorAngleRestriction() {
		this(false, new ArrayList<UnknownParameter>(0), new ArrayList<UnknownParameter>(0), null);
	}
	
	public VectorAngleRestriction(boolean indispensable, List<UnknownParameter> regressorsA, List<UnknownParameter> regressorsB, UnknownParameter regressand) {
		super(RestrictionType.VECTOR_ANGLE, indispensable);
		this.setRegressand(regressand);

		this.regressorsA.setAll(regressorsA);
		this.regressorsB.setAll(regressorsB);
		
		this.check();
	}
	
	public ObservableList<UnknownParameter> getRegressorsA() {
		return this.regressorsA;
	}
	
	public ObservableList<UnknownParameter> getRegressorsB() {
		return this.regressorsB;
	}
	
	@Override
	public double getMisclosure() {
		this.check();
		
		double dotAB = 0;
		double dotAA = 0;
		double dotBB = 0;
		
		int length = this.regressorsA.size();
		
		for (int i = 0; i < length; i++) {
			double ai = this.regressorsA.get(i).getValue();
			double bi = this.regressorsB.get(i).getValue();
			
			dotAB += ai * bi;
			dotAA += ai * ai;
			dotBB += bi * bi;
		}
		
		return Math.acos(dotAB / Math.sqrt(dotAA) / Math.sqrt(dotBB)) - this.regressand.get().getValue();
	}
	
	@Override
	public void transposedJacobianElements(Matrix JrT) {
		this.check();
		
		int rowIndex = this.getRow();
		
		double dotAB = 0;
		double dotAA = 0;
		double dotBB = 0;
		
		int length = this.regressorsA.size();
		
		for (int i = 0; i < length; i++) {
			double ai = this.regressorsA.get(i).getValue();
			double bi = this.regressorsB.get(i).getValue();
			
			dotAB += ai * bi;
			dotAA += ai * ai;
			dotBB += bi * bi;
		}
		
		for (int i = 0; i < length; i++) {
			UnknownParameter ai = this.regressorsA.get(i);
			UnknownParameter bi = this.regressorsB.get(i);

			if (ai.getColumn() >= 0) 
				JrT.add(ai.getColumn(), rowIndex, -(bi.getValue() / Math.sqrt(dotAA) / Math.sqrt(dotBB) - ai.getValue() * dotAB / Math.pow(dotAA, 1.5) / Math.sqrt(dotBB)) / Math.sqrt(1.0 - dotAB * dotAB / dotAA / dotBB ));
			
			if (bi.getColumn() >= 0) 
				JrT.add(bi.getColumn(), rowIndex, -(ai.getValue() / Math.sqrt(dotAA) / Math.sqrt(dotBB) - bi.getValue() * dotAB / Math.sqrt(dotAA) / Math.pow(dotBB, 1.5)) / Math.sqrt(1.0 - dotAB * dotAB / dotAA / dotBB));			
		}
		
		if (this.regressand.get().getColumn() >= 0)
			JrT.add(this.regressand.get().getColumn(), rowIndex, -1.0);
	}
	
	@Override
	public boolean contains(Object object) {
		if (object == null || !(object instanceof UnknownParameter))
			return false;
		return this.regressand.get() == object || this.regressorsA.contains(object) || this.regressorsB.contains(object);
	}
	
	@Override
	public String toLaTex() {
		return "$\\arccos{\\left(\\frac{\\mathbf a^{\\mathrm T} \\mathbf b} {\\vert \\mathbf a \\vert \\vert \\mathbf b \\vert}\\right)} = c$";
	}
	
	private void check() {
		if (this.regressorsA.size() != this.regressorsB.size())
			throw new IllegalArgumentException("Error, unequal size of factorsA and factorsB " + this.regressorsA.size() + " != " + this.regressorsB.size());
	}
}
