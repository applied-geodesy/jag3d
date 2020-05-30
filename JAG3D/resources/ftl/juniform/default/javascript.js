if(typeof document.getElementsByClassName != 'function') {  
	document.getElementsByClassName = function (class_name) {
		var all_obj,ret_obj=new Array(),j=0,teststr;
		if(this.all)
			all_obj=this.all;
		else if(this.getElementsByTagName && !this.all)
			all_obj=this.getElementsByTagName("*");
		var len=all_obj.length;
		for(var i=0;i<len;i++) {
			if(all_obj[i].className.indexOf(class_name)!=-1) {
				teststr=","+all_obj[i].className.split(" ").join(",")+",";
				if(teststr.indexOf(","+class_name+",")!=-1) {
					ret_obj[j]=all_obj[i];
					j++;
				}
			} 
		}
		return ret_obj;
	};
}
if(typeof String.prototype.trim != "function") { 
	String.prototype.trim = function() {
		return this.replace(/^\s+|\s+$/g,"");
	};
};
String.prototype.stripTags = function(){
	return this.replace(/<[^>]*>/g, "");
};
var TableRow = function(elm, value) {
	this.element = elm;
	this.value = value;
};
var TableSorter = function(table) {
	this.table = table;
	this.tHead = this.table.tHead;
	this.tBody = this.table.tBodies[0];
	this.tableRows = [];
	var icons = [String.fromCharCode(9650), String.fromCharCode(9660)];
	this.init = function() {
		if(!document.getElementsByTagName){
			// window.alert("Fehler, Bowser unterstützt DOM-Methoden nicht!");
			return;
		}
		this.addTableHeadEvent( this.tHead );
		this.tableRows = this.getTableRows( this.tBody );
	}
	this.addTableHeadEvent = function(tableHead) {
		var th = tableHead.rows[0].cells;
		var lastClickedRow = null;
		for (var i=0; i<th.length; i++) {
			var self = this;
			var sortIcon = document.createElement("span");
			sortIcon.appendChild(document.createTextNode(icons[0]));
			sortIcon.style.fontSize = "75%";
			sortIcon.style.visibility = "hidden";
			try { th[i].style.cursor = "pointer"; } catch(e){ th[i].style.cursor = "hand"; }
			th[i].appendChild(sortIcon);
			th[i].sortIcon = sortIcon;
			th[i].title = (th[i].ascending < 0)?icons[0]:icons[1];
			th[i].colNumber = i;
			th[i].ascending = 1;
			th[i].onclick = function(e) {
				if (lastClickedRow != null) 
					lastClickedRow.sortIcon.style.visibility = "hidden";
				lastClickedRow = this;
				self.sort(this.colNumber, this.ascending);
				this.sortIcon.firstChild.nodeValue = (this.ascending < 0)?icons[0]:icons[1];
				this.title = (this.ascending > 0)?icons[0]:icons[1];
				this.sortIcon.style.visibility = "";
				this.ascending = -this.ascending;
			};
		}
	}
	this.getTableRows = function(tableBody) {
		var rows = new Array();
		var tr = tableBody.rows;
		for(var i = 0; i < tr.length; i++) {
			rows.push( new TableRow( tr[i], null ) );
		}
		return rows;
	}
	this.sort = function(colNumber, ascending) {
		var sortOrder = function (a, b) {
			return  a.value == b.value ? 0 : a.value > b.value ? -ascending : ascending;
		};
		for (var i=0; i<this.tableRows.length; i++) 
			this.tableRows[i].value = this.getText( this.tBody.rows[i].cells[colNumber] );
		this.tableRows = this.tableRows.sort(sortOrder);
		var clonedTBody = this.tBody.cloneNode(false);
		for (var i=0; i<this.tableRows.length; i++) 
			clonedTBody.appendChild(this.tableRows[i].element.cloneNode(true));
		this.tBody.parentNode.replaceChild(clonedTBody, this.tBody);
		this.tBody = clonedTBody;
	}
	this.getText = function( td ) {
		var input = td.getElementsByTagName("input")[0];
		var value = true;
		if (input && input.type == "text") 
			value = new String(input.value).trim().toLowerCase();
		else if (input && (input.type == "radio" || input.type == "checkbox")) 
			return input.checked;
		else 
			value = new String(td.innerHTML).trim().stripTags().toLowerCase();
		return !isNaN(parseFloat(value))?Math.abs(parseFloat(value)):value;
	}
};
var DOMContentLoaded = false;
function addContentLoadListener (func) {
	if (document.addEventListener) {
		var DOMContentLoadFunction = function () {
			window.DOMContentLoaded = true;
			func();
		};
		document.addEventListener("DOMContentLoaded", DOMContentLoadFunction, false);
	}
	var oldfunc = (window.onload || new Function());
	window.onload = function () {
		if (!window.DOMContentLoaded) {
			oldfunc();
			func();
		}
	};
}
window.addContentLoadListener( function() { 
	var tables = document.getElementsByClassName("j3d_datatable");
	for (var i=0; i<tables.length; i++)
		(new TableSorter(tables[i])).init();
});