<#assign variance_component_headline = "Variance Component Estimation">
<#assign variance_component_caption  = "Variance Component Estimation">

<#assign variance_component_header_map = {
		"group"   : "Group name"
}>

<#assign vce_type_map = {
		"GLOBAL" :                                                       "Total Adjustment",
		
		"LEVELING_COMPONENT" :                                           "Levelling",
		"LEVELING_ZERO_POINT_OFFSET_COMPONENT" :                         "Levelling &sigma;<sub>a</sub>",
		"LEVELING_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :            "Levelling &sigma;<sub>b</sub>",
		"LEVELING_DISTANCE_DEPENDENT_COMPONENT" :                        "Levelling &sigma;<sub>c</sub>",
        
		"DIRECTION_COMPONENT" :                                          "Directions",
		"DIRECTION_ZERO_POINT_OFFSET_COMPONENT" :                        "Directions &sigma;<sub>a</sub>",
		"DIRECTION_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :           "Directions &sigma;<sub>b</sub>",
		"DIRECTION_DISTANCE_DEPENDENT_COMPONENT" :                       "Directions &sigma;<sub>c</sub>",
        
		"HORIZONTAL_DISTANCE_COMPONENT" :                                "Horizontal Distances",
		"HORIZONTAL_DISTANCE_ZERO_POINT_OFFSET_COMPONENT" :              "Horizontal Distances &sigma;<sub>a</sub>",
		"HORIZONTAL_DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" : "Horizontal Distances &sigma;<sub>b</sub>",
		"HORIZONTAL_DISTANCE_DISTANCE_DEPENDENT_COMPONENT" :             "Horizontal Distances &sigma;<sub>c</sub>",
        
		"SLOPE_DISTANCE_COMPONENT" :                                     "Slope Distances",
		"SLOPE_DISTANCE_ZERO_POINT_OFFSET_COMPONENT" :                   "Slope Distances &sigma;<sub>a</sub>",
		"SLOPE_DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :      "Slope Distances &sigma;<sub>b</sub>",
		"SLOPE_DISTANCE_DISTANCE_DEPENDENT_COMPONENT" :                  "Slope Distances &sigma;<sub>c</sub>",
        
		"ZENITH_ANGLE_COMPONENT" :                                       "Zenith Angles",
		"ZENITH_ANGLE_ZERO_POINT_OFFSET_COMPONENT" :                     "Zenith Angles &sigma;<sub>a</sub>",
		"ZENITH_ANGLE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :        "Zenith Angles &sigma;<sub>b</sub>",
		"ZENITH_ANGLE_DISTANCE_DEPENDENT_COMPONENT" :                    "Zenith Angles &sigma;<sub>c</sub>",
		
		"GNSS1D_COMPONENT" :                                             "GNSS Baseline 1D",
		"GNSS1D_ZERO_POINT_OFFSET_COMPONENT" :                           "GNSS Baseline 1D &sigma;<sub>a</sub>",
		"GNSS1D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :              "GNSS Baseline 1D &sigma;<sub>b</sub>",
		"GNSS1D_DISTANCE_DEPENDENT_COMPONENT" :                          "GNSS Baseline 1D &sigma;<sub>c</sub>",
		
		"GNSS2D_COMPONENT" :                                             "GNSS Baseline 2D",
		"GNSS2D_ZERO_POINT_OFFSET_COMPONENT" :                           "GNSS Baseline 2D &sigma;<sub>a</sub>",
		"GNSS2D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :              "GNSS Baseline 2D &sigma;<sub>b</sub>",
		"GNSS2D_DISTANCE_DEPENDENT_COMPONENT" :                          "GNSS Baseline 2D &sigma;<sub>c</sub>",
		
		"GNSS3D_COMPONENT" :                                             "GNSS Baseline 3D",
		"GNSS3D_ZERO_POINT_OFFSET_COMPONENT" :                           "GNSS Baseline 3D &sigma;<sub>a</sub>",
		"GNSS3D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :              "GNSS Baseline 3D &sigma;<sub>b</sub>",
		"GNSS3D_DISTANCE_DEPENDENT_COMPONENT" :                          "GNSS Baseline 3D &sigma;<sub>c</sub>",
		
		"STOCHASTIC_POINT_1D_COMPONENT" :                                "Stochastic Points 1D",
		"STOCHASTIC_POINT_2D_COMPONENT" :                                "Stochastic Points 2D",
		"STOCHASTIC_POINT_3D_COMPONENT" :                                "Stochastic Points 3D",
		
		"STOCHASTIC_DEFLECTION_COMPONENT" :                              "Deflection of the Vertical"
}>