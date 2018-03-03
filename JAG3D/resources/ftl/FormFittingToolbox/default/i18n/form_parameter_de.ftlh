<#assign form_parameter_header_map = {
	"type"         : "Parameter",
	"value"        : "Wert"
}>

<#assign form_type_map = {
	"circle_2d"           : "Kreis 2D",
	"ellipse_2d"          : "Ellipse 2D",
	"line_2d"             : "Gerade 2D",
	"polynomial"          : "Polynom",
	"quadric_2d"          : "Quadrik 2D",
	"circle_3d"           : "Kreis 3D",
	"cone"                : "Kegel",
	"cylinder"            : "Zylinder",
	"ellipse_3d"          : "Ellipse 3D",
	"ellipsoid"           : "Ellipsoid",
	"line_3d"             : "Gerade 3D",
	"paraboloid"          : "Paraboloid",
	"plane"               : "Ebene",
	"quadric_3d"          : "Quadrik 3D",
	"sphere"              : "Kugel",
	"torus"               : "Torus",
	"rotation_paraboloid" : "Rotationsparaboloid",
	"circular_cone"       : "Kreiskegel",
	"circular_cylinder"   : "Kreiszylinder"
}>

<#assign form_parameter_caption = "Gesch&auml;tzte Formparameter und Unsicherheiten">
<#assign form_parameter_headline = "Formparameter">
<#if form_type??>
	<#assign form_parameter_headline = "Formparameter &ndash; ${form_type_map[form_type]}">
</#if>