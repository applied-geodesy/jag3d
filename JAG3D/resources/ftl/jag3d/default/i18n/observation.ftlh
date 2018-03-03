<#assign observation_table_header_map = {
		"station_id" :          "Station-Id",
		"target_id" :           "Target-Id",
		
		"ih" :                  "ih",
		"th" :                  "th",
				
		"levelling" :           "&delta;h",
		"horizontal_distance" : "s<sub>2D</sub>",
		"slope_distance" :      "s<sub>3D</sub>",
		"direction" :           "r",
		"zenith_angle" :        "z",
		
		"gnss_dx" :             "&delta;X",
		"gnss_dy" :             "&delta;Y",
		"gnss_dz" :             "&delta;Z"
}>

<#assign observation_type_map = {
		"levelling" :            "Levelling",
		"direction" :            "Directions",
		"horizontal_distance" :  "Horizontal Distances",
		"slope_distance" :       "Slope Distances",
		"zenithangle" :          "Zenith Angles",
		"gnss1d" :               "GNSS Baseline 1D",
		"gnss2d" :               "GNSS Baseline 2D",
		"gnss3d" :               "GNSS Baseline 3D"
}>

<#assign observation_table_caption = "Observation Group">