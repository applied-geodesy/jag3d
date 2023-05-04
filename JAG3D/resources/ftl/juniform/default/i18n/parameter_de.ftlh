<#assign parameter_type_map = {
	"ORIGIN_COORDINATE_X"          : "Aufpunkt x",
	"ORIGIN_COORDINATE_Y"          : "Aufpunkt y",
	"ORIGIN_COORDINATE_Z"          : "Aufpunkt z",
	"PRIMARY_FOCAL_COORDINATE_X"   : "Prim&auml;rer Brennpunkt x",
	"PRIMARY_FOCAL_COORDINATE_Y"   : "Prim&auml;rer Brennpunkt y",
	"PRIMARY_FOCAL_COORDINATE_Z"   : "Prim&auml;rer Brennpunkt z",
	"SECONDARY_FOCAL_COORDINATE_X" : "Sekund&auml;rer Brennpunkt x",
	"SECONDARY_FOCAL_COORDINATE_Y" : "Sekund&auml;rer Brennpunkt y",
	"SECONDARY_FOCAL_COORDINATE_Z" : "Sekund&auml;rer Brennpunkt z",
	"COORDINATE_X"                 : "Point x",
	"COORDINATE_Y"                 : "Point y",
	"COORDINATE_Z"                 : "Point z",
	"RADIUS"                       : "Radius r",
	"ANGLE"                        : "Winkel &phi;",
	"MAJOR_AXIS_COEFFICIENT"       : "Hauptachsenkoeffizient a",
	"MIDDLE_AXIS_COEFFICIENT"      : "Hauptachsenkoeffizient b",
	"MINOR_AXIS_COEFFICIENT"       : "Hauptachsenkoeffizient c",
	"LENGTH"                       : "Abstand d",
	"VECTOR_X"                     : "Vektor x",
	"VECTOR_Y"                     : "Vektor y",
	"VECTOR_Z"                     : "Vektor z",
	"VECTOR_LENGTH"                : "Vektorl&auml;nge",
	"ROTATION_COMPONENT_R11"       : "Drehmatrixelement r11",
	"ROTATION_COMPONENT_R12"       : "Drehmatrixelement r12",
	"ROTATION_COMPONENT_R13"       : "Drehmatrixelement r13",
	"ROTATION_COMPONENT_R21"       : "Drehmatrixelement r21",
	"ROTATION_COMPONENT_R22"       : "Drehmatrixelement r22",
	"ROTATION_COMPONENT_R23"       : "Drehmatrixelement r23",
	"ROTATION_COMPONENT_R31"       : "Drehmatrixelement r31",
	"ROTATION_COMPONENT_R32"       : "Drehmatrixelement r32",
	"ROTATION_COMPONENT_R33"       : "Drehmatrixelement r33",
	"POLYNOMIAL_COEFFICIENT_A"     : "Polynomkoeffizient A",
	"POLYNOMIAL_COEFFICIENT_B"     : "Polynomkoeffizient B",
	"POLYNOMIAL_COEFFICIENT_C"     : "Polynomkoeffizient C",
	"POLYNOMIAL_COEFFICIENT_D"     : "Polynomkoeffizient D",
	"POLYNOMIAL_COEFFICIENT_E"     : "Polynomkoeffizient E",
	"POLYNOMIAL_COEFFICIENT_F"     : "Polynomkoeffizient F",
	"POLYNOMIAL_COEFFICIENT_G"     : "Polynomkoeffizient G",
	"POLYNOMIAL_COEFFICIENT_H"     : "Polynomkoeffizient H",
	"POLYNOMIAL_COEFFICIENT_I"     : "Polynomkoeffizient I",
	"CONSTANT"                     : "Hilfsgr&ouml;&szlig;e/Konstante"
}>

<#assign parameter_processing_map = {
	"POSTPROCESSING"    : "berechnet",
	"ADJUSTMENT"        : "gesch&auml;tzt",
	"FIXED"             : "fixiert"
}>

<#assign parameter_header_map = {
	"name"             : "Name",
	"parameter_type"   : "Parametertyp",
	"processing_type"  : "Prozessierungsart",
	"value"            : "Wert",
	"uncertainty"      : "Unsicherheit",
	"description"      : "Beschreibung",
	"column"           : "Spalte"
}>

<#assign parameter_table_headline = "Merkmalsparameter">
<#assign parameter_table_caption = "Liste der gesch&auml;tzten Merkmalsparameter">