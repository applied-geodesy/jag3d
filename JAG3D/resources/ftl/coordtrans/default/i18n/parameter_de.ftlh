<#assign parameter_type_map = {
	"SHIFT_X"               : "Verschiebung tx",
	"SHIFT_Y"               : "Verschiebung ty",
	"SHIFT_Z"               : "Verschiebung tz",
	"SHEAR_X"               : "Scherung sx",
	"SHEAR_Y"               : "Scherung sy",
	"SHEAR_Z"               : "Scherung sz",
	"SCALE_X"               : "Skalierung mx",
	"SCALE_Y"               : "Skalierung my",
	"SCALE_Z"               : "Skalierung mz",
	"QUATERNION_Q0"         : "Quaternion q0",
	"QUATERNION_Q1"         : "Quaternion q1",
	"QUATERNION_Q2"         : "Quaternion q2",
	"QUATERNION_Q3"         : "Quaternion q3",
	"EULER_ANGLE_X"         : "Eulerwinkel rx",
	"EULER_ANGLE_Y"         : "Eulerwinkel ry",
	"EULER_ANGLE_Z"         : "Eulerwinkel rz",
	"VECTOR_LENGTH"         : "Vektorl&auml;nge",
	"AUXILIARY_ELEMENT_11"  : "Hilfselement e11",
	"AUXILIARY_ELEMENT_12"  : "Hilfselement e12",
	"AUXILIARY_ELEMENT_13"  : "Hilfselement e13",
	"AUXILIARY_ELEMENT_21"  : "Hilfselement e21",
	"AUXILIARY_ELEMENT_22"  : "Hilfselement e22",
	"AUXILIARY_ELEMENT_23"  : "Hilfselement e23",
	"AUXILIARY_ELEMENT_33"  : "Hilfselement e33",
	"CONSTANT"              : "Konstante"
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

<#assign parameter_table_headline = "Transformationsparameter">
<#assign parameter_table_caption = "Liste der gesch&auml;tzten Transformationsparameter">