<#assign form_parameter_header_map = {
	"type"   : "Parametar",
	"value"  : "Vrijednost"
}>

<#assign form_type_map = {
	"circle_2d"           : "Krug 2D",
	"ellipse_2d"          : "Elipsa 2D",
	"line_2d"             : "Linija 2D",
	"polynomial"          : "Polinom",
	"quadric_2d"          : "Kvadar 2D",
	"circle_3d"           : "Krug 3D",
	"cone"                : "Kupa",
	"cylinder"            : "Cilindar",
	"ellipse_3d"          : "Elipsa 3D",
	"ellipsoid"           : "Elipsoid",
	"line_3d"             : "Linija 3D",
	"paraboloid"          : "Paraboloid",
	"plane"               : "Ravan",
	"quadric_3d"          : "Kvadar 3D",
	"sphere"              : "Sfera",
	"torus"               : "Torus",
	"rotation_paraboloid" : "Rotacioni Paraboloid",
	"circular_cone"       : "Kru&zcaron;na kupa",
	"circular_cylinder"   : "Kru&zcaron;ni cilindar"
}>

<#assign form_parameter_headline = "Parametri">
<#if form_type??>
	<#assign form_parameter_headline = "Parametri forme &ndash; ${form_type_map[form_type]}">
</#if>
<#assign form_parameter_caption = "Parametri">