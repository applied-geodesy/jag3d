<#assign parameter_type_map = {
	"ORIGIN_COORDINATE_X"          : "Origin x",
	"ORIGIN_COORDINATE_Y"          : "Origin y",
	"ORIGIN_COORDINATE_Z"          : "Origin z",
	"PRIMARY_FOCAL_COORDINATE_X"   : "Primary focal x",
	"PRIMARY_FOCAL_COORDINATE_Y"   : "Primary focal y",
	"PRIMARY_FOCAL_COORDINATE_Z"   : "Primary focal z",
	"SECONDARY_FOCAL_COORDINATE_X" : "Secondary focal x",
	"SECONDARY_FOCAL_COORDINATE_Y" : "Secondary focal y",
	"SECONDARY_FOCAL_COORDINATE_Z" : "Secondary focal z",
	"COORDINATE_X"                 : "Point x",
	"COORDINATE_Y"                 : "Point y",
	"COORDINATE_Z"                 : "Point z",
	"RADIUS"                       : "Radius r",
	"ANGLE"                        : "Angle &phi;",
	"MAJOR_AXIS_COEFFICIENT"       : "Semi-axis coefficient a",
	"MIDDLE_AXIS_COEFFICIENT"      : "Semi-axis coefficient b",
	"MINOR_AXIS_COEFFICIENT"       : "Semi-axis coefficient c",
	"LENGTH"                       : "Length d",
	"VECTOR_X"                     : "Vector x",
	"VECTOR_Y"                     : "Vector y",
	"VECTOR_Z"                     : "Vector z",
	"VECTOR_LENGTH"                : "Vector length",
	"ROTATION_COMPONENT_R11"       : "Rotation-matrix component r11",
	"ROTATION_COMPONENT_R12"       : "Rotation-matrix component r12",
	"ROTATION_COMPONENT_R13"       : "Rotation-matrix component r13",
	"ROTATION_COMPONENT_R21"       : "Rotation-matrix component r21",
	"ROTATION_COMPONENT_R22"       : "Rotation-matrix component r22",
	"ROTATION_COMPONENT_R23"       : "Rotation-matrix component r23",
	"ROTATION_COMPONENT_R31"       : "Rotation-matrix component r31",
	"ROTATION_COMPONENT_R32"       : "Rotation-matrix component r32",
	"ROTATION_COMPONENT_R33"       : "Rotation-matrix component r33",
	"POLYNOMIAL_COEFFICIENT_A"     : "Polynomial coeffizient A",
	"POLYNOMIAL_COEFFICIENT_B"     : "Polynomial coeffizient B",
	"POLYNOMIAL_COEFFICIENT_C"     : "Polynomial coeffizient C",
	"POLYNOMIAL_COEFFICIENT_D"     : "Polynomial coeffizient D",
	"POLYNOMIAL_COEFFICIENT_E"     : "Polynomial coeffizient E",
	"POLYNOMIAL_COEFFICIENT_F"     : "Polynomial coeffizient F",
	"POLYNOMIAL_COEFFICIENT_G"     : "Polynomial coeffizient G",
	"POLYNOMIAL_COEFFICIENT_H"     : "Polynomial coeffizient H",
	"POLYNOMIAL_COEFFICIENT_I"     : "Polynomial coeffizient I",
	"CONSTANT"                     : "Constant"
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
	"uncertainty"      : "Uncertainty",
	"description"      : "Description",
	"column"           : "Column"
}>

<#assign parameter_table_headline = "Feature parameter">
<#assign parameter_table_caption = "List of estimated feature parameter">
