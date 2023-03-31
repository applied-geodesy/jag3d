<#assign parameter_type_map = {
	"SHIFT_X"               : "Shift tx",
	"SHIFT_Y"               : "Shift ty",
	"SHIFT_Z"               : "Shift tz",
	"SHEAR_X"               : "Shear sx",
	"SHEAR_Y"               : "Shear sy",
	"SHEAR_Z"               : "Shear sz",
	"SCALE_X"               : "Scale mx",
	"SCALE_Y"               : "Scale my",
	"SCALE_Z"               : "Scale mz",
	"QUATERNION_Q0"         : "Quaternion q0",
	"QUATERNION_Q1"         : "Quaternion q1",
	"QUATERNION_Q2"         : "Quaternion q2",
	"QUATERNION_Q3"         : "Quaternion q3",
	"EULER_ANGLE_X"         : "Euler angle rx",
	"EULER_ANGLE_Y"         : "Euler angle ry",
	"EULER_ANGLE_Z"         : "Euler angle rz",
	"VECTOR_LENGTH"         : "Vector length",
	"AUXILIARY_ELEMENT_11"  : "Auxiliary element e11",
	"AUXILIARY_ELEMENT_12"  : "Auxiliary element e12",
	"AUXILIARY_ELEMENT_13"  : "Auxiliary element e13",
	"AUXILIARY_ELEMENT_21"  : "Auxiliary element e21",
	"AUXILIARY_ELEMENT_22"  : "Auxiliary element e22",
	"AUXILIARY_ELEMENT_23"  : "Auxiliary element e23",
	"AUXILIARY_ELEMENT_33"  : "Auxiliary element e33",
	"CONSTANT"              : "Constant"
}>

<#assign parameter_processing_map = {
	"POSTPROCESSING"    : "estimated",
	"ADJUSTMENT"        : "adjusted",
	"FIXED"             : "fixed"
}>

<#assign parameter_header_map = {
	"name"             : "Name",
	"parameter_type"   : "Parameter type",
	"processing_type"  : "Processing type",
	"value"            : "Value",
	"description"      : "Description",
	"column"           : "Column"
}>

<#assign parameter_table_headline = "Transformation parameter">
<#assign parameter_table_caption = "List of estimated transformation parameter">
