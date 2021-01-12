<#assign variance_component_headline = "Varianzkomponentensch&auml;tzung">
<#assign variance_component_caption  = "Varianzkomponentensch&auml;tzung">

<#assign variance_component_header_map = {
		"group"   : "Gruppenname"
}>

<#assign vce_type_map = {
		"GLOBAL" :                                                       "Gesamtausgleichung",
		
		"LEVELING_COMPONENT" :                                           "H&ouml;henunterschiede",
		"LEVELING_ZERO_POINT_OFFSET_COMPONENT" :                         "H&ouml;henunterschiede &sigma;<sub>a</sub>",
		"LEVELING_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :            "H&ouml;henunterschiede &sigma;<sub>b</sub>",
		"LEVELING_DISTANCE_DEPENDENT_COMPONENT" :                        "H&ouml;henunterschiede &sigma;<sub>c</sub>",
        
		"DIRECTION_COMPONENT" :                                          "Richtungen",
		"DIRECTION_ZERO_POINT_OFFSET_COMPONENT" :                        "Richtungen &sigma;<sub>a</sub>",
		"DIRECTION_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :           "Richtungen &sigma;<sub>b</sub>",
		"DIRECTION_DISTANCE_DEPENDENT_COMPONENT" :                       "Richtungen &sigma;<sub>c</sub>",
        
		"HORIZONTAL_DISTANCE_COMPONENT" :                                "Horizontalstrecken",
		"HORIZONTAL_DISTANCE_ZERO_POINT_OFFSET_COMPONENT" :              "Horizontalstrecken &sigma;<sub>a</sub>",
		"HORIZONTAL_DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" : "Horizontalstrecken &sigma;<sub>b</sub>",
		"HORIZONTAL_DISTANCE_DISTANCE_DEPENDENT_COMPONENT" :             "Horizontalstrecken &sigma;<sub>c</sub>",
        
		"SLOPE_DISTANCE_COMPONENT" :                                     "Raumstrecken",
		"SLOPE_DISTANCE_ZERO_POINT_OFFSET_COMPONENT" :                   "Raumstrecken &sigma;<sub>a</sub>",
		"SLOPE_DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :      "Raumstrecken &sigma;<sub>b</sub>",
		"SLOPE_DISTANCE_DISTANCE_DEPENDENT_COMPONENT" :                  "Raumstrecken &sigma;<sub>c</sub>",
        
		"ZENITH_ANGLE_COMPONENT" :                                       "Zenitwinkel",
		"ZENITH_ANGLE_ZERO_POINT_OFFSET_COMPONENT" :                     "Zenitwinkel &sigma;<sub>a</sub>",
		"ZENITH_ANGLE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :        "Zenitwinkel &sigma;<sub>b</sub>",
		"ZENITH_ANGLE_DISTANCE_DEPENDENT_COMPONENT" :                    "Zenitwinkel &sigma;<sub>c</sub>",
		
		"GNSS1D_COMPONENT" :                                             "GNSS Basislinien 1D",
		"GNSS1D_ZERO_POINT_OFFSET_COMPONENT" :                           "GNSS Basislinien 1D &sigma;<sub>a</sub>",
		"GNSS1D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :              "GNSS Basislinien 1D &sigma;<sub>b</sub>",
		"GNSS1D_DISTANCE_DEPENDENT_COMPONENT" :                          "GNSS Basislinien 1D &sigma;<sub>c</sub>",
		
		"GNSS2D_COMPONENT" :                                             "GNSS Basislinien 2D",
		"GNSS2D_ZERO_POINT_OFFSET_COMPONENT" :                           "GNSS Basislinien 2D &sigma;<sub>a</sub>",
		"GNSS2D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :              "GNSS Basislinien 2D &sigma;<sub>b</sub>",
		"GNSS2D_DISTANCE_DEPENDENT_COMPONENT" :                          "GNSS Basislinien 2D &sigma;<sub>c</sub>",
		
		"GNSS3D_COMPONENT" :                                             "GNSS Basislinien 3D",
		"GNSS3D_ZERO_POINT_OFFSET_COMPONENT" :                           "GNSS Basislinien 3D &sigma;<sub>a</sub>",
		"GNSS3D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT" :              "GNSS Basislinien 3D &sigma;<sub>b</sub>",
		"GNSS3D_DISTANCE_DEPENDENT_COMPONENT" :                          "GNSS Basislinien 3D &sigma;<sub>c</sub>",
        
		"STOCHASTIC_POINT_1D_COMPONENT" :                                "H&ouml;henanschlu&szlig;punkte",
		"STOCHASTIC_POINT_2D_COMPONENT" :                                "Lageanschlu&szlig;punkte",
		"STOCHASTIC_POINT_3D_COMPONENT" :                                "Raumanschlu&szlig;punkte",
		
		"STOCHASTIC_DEFLECTION_COMPONENT" :                              "Lotabweichungen"
}>