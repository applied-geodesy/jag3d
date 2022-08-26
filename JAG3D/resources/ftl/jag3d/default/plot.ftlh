<#-- Create list of points -->
<#if point_groups??>
	<#include "i18n/plot.ftlh" parse=true>
	<#assign points = {}>
	<#assign plot_network = false>
	<#list point_groups as group>
		<#if group.visible?? && group.visible>
			<#assign plot_network = true>
			<#list group.points as point>
				<#if min_x?? && min_y?? && max_x?? && max_y??>
					<#assign min_x = [point.x - point.helmert_major_axis*scale_confidence_ellipse, min_x]?min>
					<#assign min_y = [point.y - point.helmert_major_axis*scale_confidence_ellipse, min_y]?min>
					<#assign max_x = [point.x + point.helmert_major_axis*scale_confidence_ellipse, max_x]?max>
					<#assign max_y = [point.y + point.helmert_major_axis*scale_confidence_ellipse, max_y]?max>
				<#else>
					<#assign min_x = point.x - point.helmert_major_axis*scale_confidence_ellipse>
					<#assign min_y = point.y - point.helmert_major_axis*scale_confidence_ellipse>
					<#assign max_x = point.x + point.helmert_major_axis*scale_confidence_ellipse>
					<#assign max_y = point.y + point.helmert_major_axis*scale_confidence_ellipse>
				</#if>
				<#assign points = {point.name : point} + points>
			</#list>
		</#if>
	</#list>
	<#if plot_network?? && plot_network>
		<#assign min_x = min_x-20>
		<#assign min_y = min_y-20>
		<#assign max_x = max_x+25>
		<#assign max_y = max_y+25>
		<#assign scale_to_view = [(max_x - min_x) / svg_height, (max_y - min_y) / svg_width]?max>
		<#assign svg_width  = (max_y - min_y)/scale_to_view>
		<#assign svg_height = (max_x - min_x)/scale_to_view> 

		<section>
			<h2><#outputformat "plainText">${plot_headline}</#outputformat></h2>
			<svg 
				height="<@printFormattedNumber value=svg_height format=df_svg/>" width="<@printFormattedNumber value=svg_width format=df_svg/>" 
				viewBox="<@printFormattedNumber value=min_y format=df_svg/> <@printFormattedNumber value=min_x format=df_svg/> <@printFormattedNumber value=(max_y - min_y) format=df_svg/> <@printFormattedNumber value=(max_x - min_x) format=df_svg/>" 
				xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
				<g transform="translate(0,<@printFormattedNumber value=(max_x + min_x) format=df_svg/>) scale(1,-1)">
	
				<#-- Draw ellipses -->
				<#list point_groups as group>
					<#if group.visible?? && group.visible>
						<#list group.points as point>
							<#if group.type != "REFERENCE_POINT">
								<#if group.dimension != 1>
									<ellipse 
										cx="0" 
										cy="0" 
										ry="<@printFormattedNumber value=point.helmert_major_axis*scale_confidence_ellipse format=df_svg/>" 
										rx="<@printFormattedNumber value=point.helmert_minor_axis*scale_confidence_ellipse format=df_svg/>"
										transform="translate(<@printFormattedNumber value=point.y format=df_svg/>, <@printFormattedNumber value=point.x format=df_svg/>) scale(1,-1) rotate(<@printFormattedNumber value=point.helmert_alpha*unit_conversion_angle*180/Math.PI format=df_svg/>, 0, 0) " 
										stroke="darkgray" 
										stroke-width="0.25" 
										fill="lightgray" 
										stroke-dasharray="[1 0]" />
								<#elseif group.dimension == 1>
									<line 
										x1="<@printFormattedNumber value=point.y format=df_svg/>"
										y1="<@printFormattedNumber value=point.x - 0.5*point.helmert_major_axis*scale_confidence_ellipse format=df_svg/>"
										x2="<@printFormattedNumber value=point.y format=df_svg/>"
										y2="<@printFormattedNumber value=point.x + 0.5*point.helmert_major_axis*scale_confidence_ellipse format=df_svg/>"
										stroke="darkgray" 
										stroke-width="1" 
										vector-effect="non-scaling-stroke" 
										stroke-dasharray="[1 0]" /> 
								</#if>
							</#if>
						</#list>
					</#if>
				</#list>
				
				<#-- Draw observation lines -->
				<#if observation_groups??>
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
				</#if>	
	
				<#if observation_lines??>
					<#list observation_lines as key, line>
						<#assign dx = (line.point2.x - line.point1.x)>
						<#assign dy = (line.point2.y - line.point1.y)>
						<#assign distance = [Math.sqrt(dx*dx + dy*dy), 1]?max>
						<#assign dx = dx / distance>
						<#assign dy = dy / distance>
						<#assign maxLength = 125>
						<#assign backside  = backsides[key]???then(1,0)>
						<line 
							x1="<@printFormattedNumber value=line.point1.y format=df_svg/>"
							y1="<@printFormattedNumber value=line.point1.x format=df_svg/>"
							x2="<@printFormattedNumber value=line.point1.y + ([0.35*distance, maxLength]?min) * dy format=df_svg/>"
							y2="<@printFormattedNumber value=line.point1.x + ([0.35*distance, maxLength]?min) * dx format=df_svg/>"
							vector-effect="non-scaling-stroke"
							stroke="grey" 
							stroke-width="0.25" 
							stroke-dasharray="[1 0]" /> 
						<#if backside == 1>
							<line 
								x1="<@printFormattedNumber value=line.point2.y format=df_svg/>"
								y1="<@printFormattedNumber value=line.point2.x format=df_svg/>"
								x2="<@printFormattedNumber value=line.point2.y - ([0.35*distance, maxLength]?min) * dy format=df_svg/>"
								y2="<@printFormattedNumber value=line.point2.x - ([0.35*distance, maxLength]?min) * dx format=df_svg/>"
								vector-effect="non-scaling-stroke"
								stroke="grey" 
								stroke-width="0.25" 
								stroke-dasharray="[1 0]" /> 
						</#if>
						<line 
							x1="<@printFormattedNumber value=line.point1.y + ([0.35*distance, maxLength]?min) * dy format=df_svg/>"
							y1="<@printFormattedNumber value=line.point1.x + ([0.35*distance, maxLength]?min) * dx format=df_svg/>"
							x2="<@printFormattedNumber value=line.point2.y - backside * ([0.35*distance, maxLength]?min) * dy format=df_svg/>"
							y2="<@printFormattedNumber value=line.point2.x - backside * ([0.35*distance, maxLength]?min) * dx format=df_svg/>"
							vector-effect="non-scaling-stroke" 
							stroke="grey" 
							stroke-width="0.25" 
							stroke-dasharray="2" /> 
					</#list>
				</#if>	
	
				<#-- Draw points -->
				<#list point_groups as group>
					<#if group.visible?? && group.visible>
						<#list group.points as point>
							<#if group.type == "NEW_POINT">
								<circle 
									cx="0" 
									cy="0" 
									r="3" 
									stroke="#000" 
									stroke-width="0.2" 
									fill="red"
									vector-effect="non-scaling-stroke" 
									transform="translate(<@printFormattedNumber value=point.y format=df_svg/>, <@printFormattedNumber value=point.x format=df_svg/>) scale(<@printFormattedNumber value=scale_to_view format=df_svg/>, <@printFormattedNumber value=scale_to_view format=df_svg/>) " />
							<#elseif group.type == "DATUM_POINT">
								<polygon 
									points="-4.0,-2.3 0,4.7 4.0,-2.3" 
									fill="blue" 
									stroke="#000" 
									stroke-width="0.2" 
									vector-effect="non-scaling-stroke" 
									transform="translate(<@printFormattedNumber value=point.y format=df_svg/>, <@printFormattedNumber value=point.x format=df_svg/>) scale(<@printFormattedNumber value=scale_to_view format=df_svg/>, <@printFormattedNumber value=scale_to_view format=df_svg/>) " />
							<#elseif group.type == "STOCHASTIC_POINT">
								<polygon 
									points="-3,-3 -3,3, 3,3 3,-3" 
									fill="darkgreen" 
									stroke="#000" 
									stroke-width="0.2" 
									vector-effect="non-scaling-stroke" 
									transform="translate(<@printFormattedNumber value=point.y format=df_svg/>, <@printFormattedNumber value=point.x format=df_svg/>) scale(<@printFormattedNumber value=scale_to_view format=df_svg/>, <@printFormattedNumber value=scale_to_view format=df_svg/>) " />
							<#else>
								<polygon 
									points="-3,-3 -3,3, 3,3 3,-3" 
									fill="blue" 
									stroke="#000" 
									stroke-width="0.2" 
									vector-effect="non-scaling-stroke" 
									transform="rotate(45,<@printFormattedNumber value=point.y format=df_svg/>, <@printFormattedNumber value=point.x format=df_svg/>) translate(<@printFormattedNumber value=point.y format=df_svg/>, <@printFormattedNumber value=point.x format=df_svg/>) scale(<@printFormattedNumber value=scale_to_view format=df_svg/>, <@printFormattedNumber value=scale_to_view format=df_svg/>) " />				
							</#if>
							<text 
								x="0" 
								y="0"
								fill="gray" 
								font-size="0.7em" 
								transform="translate(<@printFormattedNumber value=point.y+7*scale_to_view format=df_svg/>,<@printFormattedNumber value=point.x-10*scale_to_view format=df_svg/>) scale(<@printFormattedNumber value=scale_to_view format=df_svg/>, <@printFormattedNumber value=-scale_to_view format=df_svg/>) ">${point.name}</text>
								
						</#list>
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