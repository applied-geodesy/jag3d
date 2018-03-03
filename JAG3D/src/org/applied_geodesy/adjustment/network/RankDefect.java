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

package org.applied_geodesy.adjustment.network;

public class RankDefect {
	private boolean userDefinedRankDefect = false;
	
	private DefectType  rx   = DefectType.NOT_SET, 
						ry   = DefectType.NOT_SET,
						rz   = DefectType.NOT_SET,
						tx   = DefectType.NOT_SET,
						ty   = DefectType.NOT_SET,
						tz   = DefectType.NOT_SET,
						sx   = DefectType.NOT_SET,
						sy   = DefectType.NOT_SET,
						sz   = DefectType.NOT_SET,
						mx   = DefectType.NOT_SET,
						my   = DefectType.NOT_SET,
						mz   = DefectType.NOT_SET,
						mxy  = DefectType.NOT_SET,
						mxyz = DefectType.NOT_SET;
	
	public RankDefect() {}
	
	void setScaleXYZ(DefectType defectType) {
		this.mx = this.my = this.mz = this.mxy = DefectType.NOT_SET;
		this.mxyz = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setScaleXY(DefectType defectType) {
		if (this.mxyz != DefectType.NOT_SET)
			this.mx = this.my = this.mz = this.mxy = DefectType.NOT_SET;
		else
			this.mxy = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setScaleX(DefectType defectType) {
		if (this.mxyz != DefectType.NOT_SET)
			this.mx = this.my = this.mz = this.mxy = DefectType.NOT_SET;
		else if (this.mxy != DefectType.NOT_SET)
			this.mx = this.my = DefectType.NOT_SET;
		else
			this.mx = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setScaleY(DefectType defectType) {
		if (this.mxyz != DefectType.NOT_SET)
			this.mx = this.my = this.mz = this.mxy = DefectType.NOT_SET;
		else if (this.mxy != DefectType.NOT_SET)
			this.mx = this.my = DefectType.NOT_SET;
		else
			this.my = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setScaleZ(DefectType defectType) {
		if (this.mxyz != DefectType.NOT_SET)
			this.mx = this.my = this.mz = this.mxy = DefectType.NOT_SET;
		else
			this.mz = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setShearX(DefectType defectType) {
		this.sx = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setShearY(DefectType defectType) {
		this.sy = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setShearZ(DefectType defectType) {
		this.sz = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setRotationX(DefectType defectType) {
		this.rx = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setRotationY(DefectType defectType) {
		this.ry = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setRotationZ(DefectType defectType) {
		this.rz = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setTranslationX(DefectType defectType) {
		this.tx = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setTranslationY(DefectType defectType) {
		this.ty = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	void setTranslationZ(DefectType defectType) {
		this.tz = defectType == DefectType.FIXED?DefectType.FIXED:DefectType.FREE;
	}
	
	public void setScaleXYZDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setScaleXYZ(defectType);
	}
	
	public void setScaleXYDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setScaleXY(defectType);
	}
	
	public void setScaleXDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setScaleX(defectType);
	}
	
	public void setScaleYDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setScaleY(defectType);
	}
	
	public void setScaleZDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setScaleZ(defectType);
	}
	
	public void setShearXDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setShearX(defectType);
	}
	
	public void setShearYDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setShearY(defectType);
	}
	
	public void setShearZDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setShearZ(defectType);
	}
	
	public void setRotationXDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setRotationX(defectType);
	}
	
	public void setRotationYDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setRotationY(defectType);
	}
	
	public void setRotationZDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setRotationZ(defectType);
	}
	
	public void setTranslationXDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setTranslationX(defectType);
	}
	
	public void setTranslationYDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setTranslationY(defectType);
	}
	
	public void setTranslationZDefectType(DefectType defectType) {
		this.userDefinedRankDefect = true;
		this.setTranslationZ(defectType);
	}
	
	public boolean estimateScaleX() {
		return this.mx == DefectType.FREE;
	}
	
	public boolean estimateScaleY() {
		return this.my == DefectType.FREE;
	}
	
	public boolean estimateScaleZ() {
		return this.mz == DefectType.FREE;
	}
	
	public boolean estimateScaleXY() {
		return this.mxy == DefectType.FREE;
	}
	
	public boolean estimateScaleXYZ() {
		return this.mxyz == DefectType.FREE;
	}
	
	public boolean estimateShearX() {
		return this.sx == DefectType.FREE;
	}
	
	public boolean estimateShearY() {
		return this.sy == DefectType.FREE;
	}
	
	public boolean estimateShearZ() {
		return this.sz == DefectType.FREE;
	}
	
	public boolean estimateRotationX() {
		return this.rx == DefectType.FREE;
	}
	
	public boolean estimateRotationY() {
		return this.ry == DefectType.FREE;
	}
	
	public boolean estimateRotationZ() {
		return this.rz == DefectType.FREE;
	}
	
	public boolean estimateTranslationX() {
		return this.tx == DefectType.FREE;
	}
	
	public boolean estimateTranslationY() {
		return this.ty == DefectType.FREE;
	}
	
	public boolean estimateTranslationZ() {
		return this.tz == DefectType.FREE;
	}
	
	public DefectType getScaleXYZ() {
		return this.mxyz;
	}
	
	public DefectType getScaleXY() {
		return this.mxy;
	}
	
	public DefectType getScaleX() {
		return this.mx;
	}
	
	public DefectType getScaleY() {
		return this.my;
	}
	
	public DefectType getScaleZ() {
		return this.mz;
	}
	
	public DefectType getShearX() {
		return this.sx;
	}
	
	public DefectType getShearY() {
		return this.sy;
	}
	
	public DefectType getShearZ() {
		return this.sz;
	}
	
	public DefectType getRotationX() {
		return this.rx;
	}
	
	public DefectType getRotationY() {
		return this.ry;
	}
	
	public DefectType getRotationZ() {
		return this.rz;
	}
	
	public DefectType getTranslationX() {
		return this.tx;
	}
	
	public DefectType getTranslationY() {
		return this.ty;
	}
	
	public DefectType getTranslationZ() {
		return this.tz;
	}
	
	public int getDefect() {
		int d = 0;
		d += this.estimateScaleX()?1:0;
		d += this.estimateScaleY()?1:0;
		d += this.estimateScaleZ()?1:0;
		d += this.estimateScaleXY()?1:0;
		d += this.estimateScaleXYZ()?1:0;
		d += this.estimateRotationX()?1:0;
		d += this.estimateRotationY()?1:0;
		d += this.estimateRotationZ()?1:0;
		d += this.estimateShearX()?1:0;
		d += this.estimateShearY()?1:0;
		d += this.estimateShearZ()?1:0;
		d += this.estimateTranslationX()?1:0;
		d += this.estimateTranslationY()?1:0;
		d += this.estimateTranslationZ()?1:0;
		
		return d;
	}
	
	public boolean isUserDefinedRankDefect() {
		return this.userDefinedRankDefect;
	}
	
	public void reset() {
		this.userDefinedRankDefect = false;
		this.tx = this.ty = this.tz = DefectType.NOT_SET;
		this.rx = this.ry = this.rz = DefectType.NOT_SET;
		this.sx = this.sy = this.sz = DefectType.NOT_SET;
		this.mx = this.my = this.mz = DefectType.NOT_SET;
		this.mxy = this.mxyz = DefectType.NOT_SET;
	}

	@Override
	public String toString() {
		return "RankDefect [rx=" + rx + ", ry=" + ry + ", rz=" + rz + ", tx=" + tx + ", ty=" + ty + ", tz=" + tz
				+ ", sx=" + sx + ", sy=" + sy + ", sz=" + sz + ", mx=" + mx + ", my=" + my + ", mz=" + mz + ", mxy="
				+ mxy + ", mxyz=" + mxyz + ", defect=" + getDefect() + ", userDefinedRankDefect=" + userDefinedRankDefect +"]";
	}
		
	
}
