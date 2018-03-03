<#assign form_parameter_header_map = {
	"type"         : "Parameter",
	"value"        : "Value"
}>

<#assign form_type_map = {
	"circle_2d"           : "Circle 2D",
	"ellipse_2d"          : "Ellipse 2D",
	"line_2d"             : "Line 2D",
	"polynomial"          : "Polynom",
	"quadric_2d"          : "Quadric 2D",
	"circle_3d"           : "Circle 3D",
	"cone"                : "Cone",
	"cylinder"            : "Cylinder",
	"ellipse_3d"          : "Ellipse 3D",
	"ellipsoid"           : "Ellipsoid",
	"line_3d"             : "Line 3D",
	"paraboloid"          : "Paraboloid",
	"plane"               : "Plane",
	"quadric_3d"          : "Quadric 3D",
	"sphere"              : "Sphere",
	"torus"               : "Torus",
	"rotation_paraboloid" : "Rotation Paraboloid",
	"circular_cone"       : "Circular Cone",
	"circular_cylinder"   : "Circular Cylinder"
}>

<#assign form_parameter_caption = "Estimated Form Parameters and Uncertainties">
<#assign form_parameter_headline = "Parameters">
<#if form_type??>
	<#assign form_parameter_headline = "Parameters &ndash; ${form_type_map[form_type]}">
</#if>