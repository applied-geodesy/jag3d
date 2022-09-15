<#if point_groups??>
	<#include "i18n/plot.ftlh" parse=true>
	
	<#assign vector_scale = vector_scale???then(vector_scale, 250.0)>
	<#assign svg_width    = 700>
	<#assign svg_height   = 700> 
	<#assign df_svg       = "0.00000;; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

	<#assign points = {}>
	<#assign plot_network = false>

	<#-- Create list of points -->
	<#list point_groups as group>
		<#if group.visible>
			<#assign plot_network = true>
			<#list group.points as point>
				<#if min_x?? && min_y?? && max_x?? && max_y??>
					<#assign min_x = [point.x - point.helmert_major_axis * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length, min_x]?min>
					<#assign min_y = [point.y - point.helmert_major_axis * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length, min_y]?min>
					<#assign max_x = [point.x + point.helmert_major_axis * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length, max_x]?max>
					<#assign max_y = [point.y + point.helmert_major_axis * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length, max_y]?max>
				<#else>
					<#assign min_x = point.x - point.helmert_major_axis * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length>
					<#assign min_y = point.y - point.helmert_major_axis * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length>
					<#assign max_x = point.x + point.helmert_major_axis * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length>
					<#assign max_y = point.y + point.helmert_major_axis * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length>
				</#if>
				<#assign points = {point.name : point} + points>
			</#list>
		</#if>
	</#list>
	
	<#if plot_network?? && plot_network>
		<#if layers["RELATIVE_CONFIDENCE"]?? && layers["RELATIVE_CONFIDENCE"].visible && (layers["POINT_SHIFT_HORIZONTAL"]?? && layers["POINT_SHIFT_HORIZONTAL"].visible || layers["POINT_SHIFT_VERTICAL"]?? && layers["POINT_SHIFT_VERTICAL"].visible)>
			<#list congruence_analysis_groups as group>
				<#list group.point_pairs as point_pair>
					<#if points[point_pair.start_point_name]?? && points[point_pair.end_point_name]??>
						<#assign point1 = points[point_pair.start_point_name]>
						<#assign point2 = points[point_pair.end_point_name]>
							
						<#assign dx = (point2.x - point1.x)>
						<#assign dy = (point2.y - point1.y)>
						
						<#assign x = point1.x + 0.5*dx>
						<#assign y = point1.y + 0.5*dy>
						
						<#assign min_x = [x - point_pair.confidence_major_axis_2d * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length, min_x]?min>
						<#assign min_y = [y - point_pair.confidence_major_axis_2d * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length, min_y]?min>
						<#assign max_x = [x + point_pair.confidence_major_axis_2d * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length, max_x]?max>
						<#assign max_y = [y + point_pair.confidence_major_axis_2d * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length, max_y]?max>
						
						<#if group.dimension != 1 && (layers["POINT_SHIFT_HORIZONTAL"]?? && layers["POINT_SHIFT_HORIZONTAL"].visible)>
							<#assign min_x = [x + dx * vector_scale, min_x]?min>
							<#assign min_y = [y + dy * vector_scale, min_y]?min>
							<#assign max_x = [x + dx * vector_scale, max_x]?max>
							<#assign max_y = [y + dy * vector_scale, max_y]?max>
						<#elseif group.dimension != 2 && (layers["POINT_SHIFT_VERTICAL"]?? && layers["POINT_SHIFT_VERTICAL"].visible)>
							<#assign min_y = [y + point_pair.z * vector_scale, min_y]?min>
							<#assign max_y = [y + point_pair.z * vector_scale, max_y]?max>
						</#if>
					</#if>
				</#list>
			</#list>
		</#if>

		<#assign min_x = min_x-0.1*(max_x - min_x)>
		<#assign min_y = min_y-0.1*(max_y - min_y)>
		<#assign max_x = max_x+0.1*(max_x - min_x)>
		<#assign max_y = max_y+0.1*(max_y - min_y)>
		<#assign scale_to_view = [(max_x - min_x) / svg_height, (max_y - min_y) / svg_width]?max>
		
		<#assign svg_width  = (max_y - min_y)/scale_to_view>
		<#assign svg_height = (max_x - min_x)/scale_to_view> 

		<section>
			<h2><#outputformat "plainText">${plot_headline}</#outputformat></h2>
			<svg
				height="<@printFormattedNumber value=svg_height format=df_svg/>" width="<@printFormattedNumber value=svg_width format=df_svg/>" 
				viewBox="<@printFormattedNumber value=min_y format=df_svg/> <@printFormattedNumber value=min_x format=df_svg/> <@printFormattedNumber value=(max_y - min_y) format=df_svg/> <@printFormattedNumber value=(max_x - min_x) format=df_svg/>" 
				xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">

				<defs>
					<marker 
						id="arrowhead_shiftvector" 
						markerWidth  = "7" 
						markerHeight = "5" 
						refX="7" 
						refY="2.5" 
						orient="auto">
						<polygon 
							points="0 0, 7 2.5, 0 5" 
							fill="#ff8c00" />
					</marker>
				</defs>
				
				<g transform="translate(0,<@printFormattedNumber value=(max_x + min_x) format=df_svg/>) scale(1,-1)">

					<#-- iterate over all layers -->
					<#list layers as layer, layer_properties>
						<#if layer_properties.visible?? && layer_properties.visible>
							
							<#-- Draw absolute ellipses (type helmert) -->
							<#if layer == "ABSOLUTE_CONFIDENCE">
								<#list point_groups as group>
									<#if group.visible && group.type != "REFERENCE_POINT">
										<#list group.points as point>
											<#if group.dimension != 1>
												<ellipse 
													cx="0" 
													cy="0" 
													ry="<@printFormattedNumber value=point.helmert_major_axis * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length format=df_svg/>" 
													rx="<@printFormattedNumber value=point.helmert_minor_axis * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length format=df_svg/>"
													transform="translate(<@printFormattedNumber value=point.y format=df_svg/>, <@printFormattedNumber value=point.x format=df_svg/>) scale(1,-1) rotate(<@printFormattedNumber value=point.helmert_alpha*unit_conversion_angle*180/Math.PI format=df_svg/>, 0, 0) " 
													stroke="#000000" 
													stroke-width="0.25" 
													fill="#cccccc" 
													vector-effect="non-scaling-stroke" 
													stroke-dasharray="[1 0]" />
											<#elseif group.dimension == 1>
												<line 
													x1="<@printFormattedNumber value=point.y format=df_svg/>"
													y1="<@printFormattedNumber value=point.x - 0.5*point.helmert_major_axis * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length format=df_svg/>"
													x2="<@printFormattedNumber value=point.y format=df_svg/>"
													y2="<@printFormattedNumber value=point.x + 0.5*point.helmert_major_axis * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length format=df_svg/>"
													stroke="#000000" 
													stroke-width="2.5" 
													vector-effect="non-scaling-stroke" 
													stroke-dasharray="[1 0]" /> 
											</#if>
										</#list>
									</#if>
								</#list>
							</#if>
							
							<#-- Draw relative ellipses (confidence ellipses) -->
							<#if layer == "RELATIVE_CONFIDENCE" && (layers["POINT_SHIFT_HORIZONTAL"]?? && layers["POINT_SHIFT_HORIZONTAL"].visible || layers["POINT_SHIFT_VERTICAL"]?? && layers["POINT_SHIFT_VERTICAL"].visible)>
								<#list congruence_analysis_groups as group>
										<#list group.point_pairs as point_pair>
											<#if points[point_pair.start_point_name]?? && points[point_pair.end_point_name]??>
												<#assign point1 = points[point_pair.start_point_name]>
												<#assign point2 = points[point_pair.end_point_name]>
													
												<#assign x = point1.x + 0.5*(point2.x - point1.x)>
												<#assign y = point1.y + 0.5*(point2.y - point1.y)>
	
												<#if group.dimension != 1 && layers["POINT_SHIFT_HORIZONTAL"].visible>
													<ellipse 
														cx="0" 
														cy="0" 
														ry="<@printFormattedNumber value=point_pair.confidence_major_axis_2d * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length format=df_svg/>" 
														rx="<@printFormattedNumber value=point_pair.confidence_minor_axis_2d * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length format=df_svg/>"
														transform="translate(<@printFormattedNumber value=y format=df_svg/>, <@printFormattedNumber value=x format=df_svg/>) scale(1,-1) rotate(<@printFormattedNumber value=point_pair.confidence_alpha_2d*unit_conversion_angle*180/Math.PI format=df_svg/>, 0, 0) " 
														stroke="#000000" 
														stroke-width="0.25" 
														fill="#ffffe0" 
														vector-effect="non-scaling-stroke" 
														stroke-dasharray="[1 0]" />
												<#elseif group.dimension == 1 && layers["POINT_SHIFT_VERTICAL"].visible>
													<line 
														x1="<@printFormattedNumber value=y format=df_svg/>"
														y1="<@printFormattedNumber value=x - 0.5*point_pair.confidence_major_axis_2d * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length format=df_svg/>"
														x2="<@printFormattedNumber value=y format=df_svg/>"
														y2="<@printFormattedNumber value=x + 0.5*point_pair.confidence_major_axis_2d * vector_scale * unit_conversion_length_uncertainty / unit_conversion_length format=df_svg/>"
														stroke="#ffffe0" 
														stroke-width="2.5" 
														vector-effect="non-scaling-stroke" 
														stroke-dasharray="[1 0]" /> 
												</#if>
											</#if>
										</#list>
								</#list>
							</#if>
							
							<#if layer == "POINT_SHIFT_HORIZONTAL" || layer == "POINT_SHIFT_VERTICAL">
								<#list point_groups as group>
									<#if group.visible && group.type == "DATUM_POINT">
										<#list group.points as point>
											<#if point.significant>
												<#if group.dimension != 1 && layer == "POINT_SHIFT_HORIZONTAL">
													<line 
														x1="<@printFormattedNumber value=point.y format=df_svg/>"
														y1="<@printFormattedNumber value=point.x format=df_svg/>"
														x2="<@printFormattedNumber value=point.y + point.gross_error_y * vector_scale * unit_conversion_length_residual / unit_conversion_length format=df_svg/>"
														y2="<@printFormattedNumber value=point.x + point.gross_error_x * vector_scale * unit_conversion_length_residual / unit_conversion_length format=df_svg/>"
														stroke="#ff8c00" 
														stroke-width="1.5" 
														vector-effect="non-scaling-stroke" 
														stroke-dasharray="[1 0]" 
														marker-end="url(#arrowhead_shiftvector)" />
												</#if>
												<#if group.dimension != 2 && layer == "POINT_SHIFT_VERTICAL">
													<line 
														x1="<@printFormattedNumber value=point.y format=df_svg/>"
														y1="<@printFormattedNumber value=point.x format=df_svg/>"
														x2="<@printFormattedNumber value=point.y format=df_svg/>"
														y2="<@printFormattedNumber value=point.x + point.gross_error_z * vector_scale * unit_conversion_length_residual / unit_conversion_length format=df_svg/>"
														stroke="#ffaf00" 
														stroke-width="1.5" 
														vector-effect="non-scaling-stroke" 
														stroke-dasharray="[1 0]" 
														marker-end="url(#arrowhead_shiftvector)" />
												</#if>
											</#if>
										</#list>
									</#if>
								</#list>
							
								<#if congruence_analysis_groups??>
									<#list congruence_analysis_groups as group>
										<#list group.point_pairs as point_pair>
											<#if points[point_pair.start_point_name]?? && points[point_pair.end_point_name]??>
												<#assign point1 = points[point_pair.start_point_name]>
												<#assign point2 = points[point_pair.end_point_name]>
														
												<#assign dx = (point2.x - point1.x)>
												<#assign dy = (point2.y - point1.y)>
												
												<#assign x = point1.x + 0.5*dx>
												<#assign y = point1.y + 0.5*dy>
												
												<#if group.dimension != 1 && layer == "POINT_SHIFT_HORIZONTAL">
													<line 
														x1="<@printFormattedNumber value=y format=df_svg/>"
														y1="<@printFormattedNumber value=x format=df_svg/>"
														x2="<@printFormattedNumber value=y + dy * vector_scale format=df_svg/>"
														y2="<@printFormattedNumber value=x + dx * vector_scale format=df_svg/>"
														stroke="#ff8c00" 
														stroke-width="1.5" 
														vector-effect="non-scaling-stroke" 
														stroke-dasharray="[1 0]"
														marker-end="url(#arrowhead_shiftvector)" />
												</#if>
												<#if group.dimension != 2 && layer == "POINT_SHIFT_VERTICAL">
													<line 
														x1="<@printFormattedNumber value=y format=df_svg/>"
														y1="<@printFormattedNumber value=x format=df_svg/>"
														x2="<@printFormattedNumber value=y format=df_svg/>"
														y2="<@printFormattedNumber value=x + point_pair.z * vector_scale format=df_svg/>"
														stroke="#ffaf00" 
														stroke-width="1.5" 
														vector-effect="non-scaling-stroke" 
														stroke-dasharray="[1 0]"
														marker-end="url(#arrowhead_shiftvector)" />
												</#if>
											</#if>
										</#list>
									</#list>
								</#if>
							</#if>
							
							<#-- Draw observation lines -->
							<#if observation_groups?? && layer == "OBSERVATION_APOSTERIORI">
								<#assign observation_lines = {}>
								<#assign backsides = {}>
								<#list observation_groups as group>
									<#list group.observations as observation>
										
										<#if points[observation.start_point_name]?? && points[observation.end_point_name]??>
											<#assign point_pair1 = [observation.start_point_name, observation.end_point_name]?join("-")>
											<#assign point_pair2 = [observation.end_point_name, observation.start_point_name]?join("-")>
											<#if observation_lines[point_pair1]?? || backsides[point_pair2]??>
												<#continue>
											<#elseif observation_lines[point_pair2]?? && !backsides[point_pair2]??>
												<#assign backsides = {point_pair2 : true} + backsides>
											<#else>
												<#assign observation_lines = {point_pair1 : {
														'point1': points[observation.start_point_name], 
														'point2': points[observation.end_point_name]
												}  } + observation_lines>
				
											</#if>
										</#if>
									</#list>
								</#list>
			
								<#if observation_lines??>
									<#list observation_lines as key, line>
										<#assign dx = (line.point2.x - line.point1.x)>
										<#assign dy = (line.point2.y - line.point1.y)>
										<#assign distance = [Math.sqrt(dx*dx + dy*dy), 1]?max>
										<#assign dx = dx / distance>
										<#assign dy = dy / distance>
										<#assign maxLength = 125*scale_to_view>
										<#assign backside  = backsides[key]???then(1,0)>
										<line 
											x1="<@printFormattedNumber value=line.point1.y format=df_svg/>"
											y1="<@printFormattedNumber value=line.point1.x format=df_svg/>"
											x2="<@printFormattedNumber value=line.point1.y + ([0.35*distance, maxLength]?min) * dy format=df_svg/>"
											y2="<@printFormattedNumber value=line.point1.x + ([0.35*distance, maxLength]?min) * dx format=df_svg/>"
											vector-effect="non-scaling-stroke"
											stroke="#778899" 
											stroke-width="0.25" 
											stroke-dasharray="[1 0]" /> 
										<#if backside == 1>
											<line 
												x1="<@printFormattedNumber value=line.point2.y format=df_svg/>"
												y1="<@printFormattedNumber value=line.point2.x format=df_svg/>"
												x2="<@printFormattedNumber value=line.point2.y - ([0.35*distance, maxLength]?min) * dy format=df_svg/>"
												y2="<@printFormattedNumber value=line.point2.x - ([0.35*distance, maxLength]?min) * dx format=df_svg/>"
												vector-effect="non-scaling-stroke"
												stroke="#778899" 
												stroke-width="0.25" 
												stroke-dasharray="[1 0]" /> 
										</#if>
										<line 
											x1="<@printFormattedNumber value=line.point1.y + ([0.35*distance, maxLength]?min) * dy format=df_svg/>"
											y1="<@printFormattedNumber value=line.point1.x + ([0.35*distance, maxLength]?min) * dx format=df_svg/>"
											x2="<@printFormattedNumber value=line.point2.y - backside * ([0.35*distance, maxLength]?min) * dy format=df_svg/>"
											y2="<@printFormattedNumber value=line.point2.x - backside * ([0.35*distance, maxLength]?min) * dx format=df_svg/>"
											vector-effect="non-scaling-stroke" 
											stroke="#778899" 
											stroke-width="0.25" 
											stroke-dasharray="2" /> 
									</#list>
								</#if>	
							</#if>
	
							<#-- Draw points -->
							<#if ["NEW_POINT_APOSTERIORI", "DATUM_POINT_APOSTERIORI", "STOCHASTIC_POINT_APOSTERIORI", "REFERENCE_POINT_APOSTERIORI"]?seq_contains(layer)>
								<#list point_groups as group>
									<#if group.type == "NEW_POINT" && layer == "NEW_POINT_APOSTERIORI">
										<#assign layer_visible = true>
									<#elseif group.type == "DATUM_POINT" && layer == "DATUM_POINT_APOSTERIORI">
										<#assign layer_visible = true>
									<#elseif group.type == "STOCHASTIC_POINT" && layer == "STOCHASTIC_POINT_APOSTERIORI">
										<#assign layer_visible = true>
									<#elseif group.type == "REFERENCE_POINT" && layer == "REFERENCE_POINT_APOSTERIORI">
										<#assign layer_visible = true>
									<#else>
										<#assign layer_visible = false>
									</#if>
								
									<#if layer_visible && group.visible>
										<#list group.points as point>
											<#if group.type == "NEW_POINT">
												<circle 
													cx="0" 
													cy="0" 
													r="3" 
													stroke="#000000" 
													stroke-width="0.2" 
													fill="#dda0dd"
													vector-effect="non-scaling-stroke" 
													transform="translate(<@printFormattedNumber value=point.y format=df_svg/>, <@printFormattedNumber value=point.x format=df_svg/>) scale(<@printFormattedNumber value=scale_to_view format=df_svg/>, <@printFormattedNumber value=scale_to_view format=df_svg/>) " />
											<#elseif group.type == "DATUM_POINT">
												<polygon 
													points="-4.0,-2.3 0,4.7 4.0,-2.3" 
													fill="#1e90ff" 
													stroke="#000000" 
													stroke-width="0.2" 
													vector-effect="non-scaling-stroke" 
													transform="translate(<@printFormattedNumber value=point.y format=df_svg/>, <@printFormattedNumber value=point.x format=df_svg/>) scale(<@printFormattedNumber value=scale_to_view format=df_svg/>, <@printFormattedNumber value=scale_to_view format=df_svg/>) " />
											<#elseif group.type == "STOCHASTIC_POINT">
												<polygon 
													points="-3,-3 -3,3, 3,3 3,-3" 
													fill="#daa520" 
													stroke="#000000" 
													stroke-width="0.2" 
													vector-effect="non-scaling-stroke" 
													transform="translate(<@printFormattedNumber value=point.y format=df_svg/>, <@printFormattedNumber value=point.x format=df_svg/>) scale(<@printFormattedNumber value=scale_to_view format=df_svg/>, <@printFormattedNumber value=scale_to_view format=df_svg/>) " />
											<#elseif group.type == "REFERENCE_POINT">
												<polygon 
													points="-3,-3 -3,3, 3,3 3,-3" 
													fill="#006400" 
													stroke="#000000" 
													stroke-width="0.2" 
													vector-effect="non-scaling-stroke" 
													transform="rotate(45,<@printFormattedNumber value=point.y format=df_svg/>, <@printFormattedNumber value=point.x format=df_svg/>) translate(<@printFormattedNumber value=point.y format=df_svg/>, <@printFormattedNumber value=point.x format=df_svg/>) scale(<@printFormattedNumber value=scale_to_view format=df_svg/>, <@printFormattedNumber value=scale_to_view format=df_svg/>) " />				
											<#else>
												<#continue>
											</#if>
											<text 
												x="0" 
												y="0"
												fill="#696969" 
												font-size="0.7em" 
												transform="translate(<@printFormattedNumber value=point.y+7*scale_to_view format=df_svg/>,<@printFormattedNumber value=point.x-10*scale_to_view format=df_svg/>) scale(<@printFormattedNumber value=scale_to_view format=df_svg/>, <@printFormattedNumber value=-scale_to_view format=df_svg/>) ">${point.name}</text>
												
										</#list>
									</#if>
								</#list>
							</#if>
							
						</#if>
					</#list>
	
					<#-- Draw scalebar -->
					<#assign world_scalebar = 0.1 * (max_y - min_y)>
					<#assign exponent  = Math.log10(world_scalebar)?int>
					<#assign magnitude = Math.pow(10, exponent)>
					<#assign ratio     = Math.ceil(world_scalebar/magnitude)>
					
					<#if (world_scalebar/magnitude) < 0.5>
						<#assign exponent  = exponent - 1> 
						<#assign magnitude = Math.pow(10, exponent)>
						<#assign ratio     = Math.ceil(world_scalebar/magnitude)>
					</#if>
				
					<#assign world_scalebar = ratio*magnitude>
					
					<#list [1,2,3] as i>
						<#assign color_fill = (i%2 == 0)?then("#f6f6f6", "#333333")>
						<rect 
							x="<@printFormattedNumber value=max_y - 10*scale_to_view - i*world_scalebar format=df_svg/>"
							y="<@printFormattedNumber value=min_x +  4*scale_to_view format=df_svg/> " 
							width="<@printFormattedNumber value=world_scalebar format=df_svg/>" 
							height="<@printFormattedNumber value=3*scale_to_view format=df_svg/>"
							stroke="#000" 
							stroke-width="0.5" 
							vector-effect="non-scaling-stroke" 
							fill="${color_fill}" />
					</#list>
					<text 
						text-anchor="middle"
						dominant-baseline="baseline"
						x="0"
						y="0" 
						fill="gray" 
						font-size="0.6em"
						transform="translate(<@printFormattedNumber value=max_y-10*scale_to_view - 1.5*world_scalebar format=df_svg/>, <@printFormattedNumber value=min_x + 10*scale_to_view format=df_svg/>) scale(<@printFormattedNumber value=scale_to_view format=df_svg/>, <@printFormattedNumber value=-scale_to_view format=df_svg/>) ">${(3*world_scalebar)?int} ${unit_abbr_length}</text>
				</g>
				<#outputformat "plainText">${plot_svg_support}</#outputformat>
			</svg>
		</section>
	</#if>
</#if>