<#assign observation_table_header_map = {
		"station_id" :          "Standpunkt",
		"target_id" :           "Zielpunkt",
		
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
		"levelling" :            "H&ouml;henunterschiede",							
		"direction" :            "Richtungen",
		"horizontal_distance" :  "Horizontalstrecken",
		"slope_distance" :       "Raumstrecken",
		"zenithangle" :          "Zenitwinkel",
		"gnss1d" :               "GNSS Basislinien 1D",
		"gnss2d" :               "GNSS Basislinien 2D",
		"gnss3d" :               "GNSS Basislinien 3D"
}>

<#assign observation_table_caption = "Beobachtungsgruppe">