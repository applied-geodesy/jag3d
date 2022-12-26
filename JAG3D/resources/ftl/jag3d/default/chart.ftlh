<#if chart_values??>
	<#include "i18n/observation.ftlh" parse=true>
	<#include "i18n/chart.ftlh" parse=true>
	<#assign observation_types = ['LEVELING', 'DIRECTION', 'HORIZONTAL_DISTANCE', 'SLOPE_DISTANCE', 'ZENITH_ANGLE']>
	<#list observation_types as observation_type>
		<#assign has_group_params = false>
		<#if chart_values.redundancy??>
			<#list chart_values.redundancy as group>
				<#if group.type?? && group.type == observation_type && group.absolute_frequency_part_1?? && group.absolute_frequency_part_2?? && group.absolute_frequency_part_3?? && group.relative_frequency_part_1?? && group.relative_frequency_part_2?? && group.relative_frequency_part_3??>
					<#if !has_group_params>
						<section>
						<h2>
							<#if group.type == 'LEVELING'>
								<#outputformat "plainText">${observation_type_map['levelling']}</#outputformat>
							<#elseif group.type == 'DIRECTION'>
								<#outputformat "plainText">${observation_type_map['direction']}</#outputformat>
							<#elseif group.type == 'HORIZONTAL_DISTANCE'>
								<#outputformat "plainText">${observation_type_map['horizontal_distance']}</#outputformat>
							<#elseif group.type == 'SLOPE_DISTANCE'>
								<#outputformat "plainText">${observation_type_map['slope_distance']}</#outputformat>
							<#elseif group.type == 'ZENITH_ANGLE'>
								<#outputformat "plainText">${observation_type_map['zenith_angle']}</#outputformat>
							</#if>
						</h2>
						<#assign has_group_params = true>
					</#if>
					<figure>
						<svg class="piechart" viewBox="0 0 200 100" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
							<#assign total        = (Math.PI * 50.0)>
							<#assign excellent    = (group.relative_frequency_part_3 * total / 100.0)>
							<#assign satisfactory = (group.relative_frequency_part_2 * total / 100.0)>
							<#assign inadequate   = (group.relative_frequency_part_1 * total / 100.0)>
							
							<circle r="25" cx="50" cy="50"
								style="stroke-dasharray: ${excellent?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]} ${total?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; 
								stroke-dashoffset: 0; ">
							</circle>
							
							<circle r="25" cx="50" cy="50"
								style="stroke-dasharray: ${satisfactory?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]} ${total?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; 
								stroke-dashoffset: ${(-excellent)?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; ">
							</circle>
							
							<circle r="25" cx="50" cy="50"
								style="stroke-dasharray: ${inadequate?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]} ${total?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; 
								stroke-dashoffset: ${(-excellent - satisfactory)?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; ">
							</circle>
	
							<rect x="105" y="25"></rect>
							<rect x="105" y="45"></rect>
							<rect x="105" y="65"></rect>
							
							<text x="125" y="34" text-anchor="start">${group.absolute_frequency_part_3?string["0"]} (${group.relative_frequency_part_3?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</text>
							<text x="125" y="54" text-anchor="start">${group.absolute_frequency_part_2?string["0"]} (${group.relative_frequency_part_2?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</text>
							<text x="125" y="74" text-anchor="start">${group.absolute_frequency_part_1?string["0"]} (${group.relative_frequency_part_1?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</text>
						
							<#outputformat "plainText">${plot_svg_support}</#outputformat>
						</svg>
						<figcaption><#outputformat "plainText">${chart_figcaption_map['redundancy']}</#outputformat> r (<#outputformat "plainText">${mean_interval}</#outputformat>: {r | <@printFormattedNumber value=group.left_boundary format=df_percentage/>&nbsp;${unit_abbr_percentage} &le; r &le; <@printFormattedNumber value=group.right_boundary format=df_percentage/>&nbsp;${unit_abbr_percentage}})</figcaption>
					</figure>
				</#if>
			</#list>
		</#if>
		
		<#if chart_values.p_prio??>
			<#list chart_values.p_prio as group>
				<#if estimation_type != 'SIMULATION' && group.type?? && group.type == observation_type && group.absolute_frequency_part_1?? && group.absolute_frequency_part_2?? && group.absolute_frequency_part_3?? && group.relative_frequency_part_1?? && group.relative_frequency_part_2?? && group.relative_frequency_part_3??>
					<#if !has_group_params>
						<section>
						<h2>
							<#if group.type == 'LEVELING'>
								<#outputformat "plainText">${observation_type_map['levelling']}</#outputformat>
							<#elseif group.type == 'DIRECTION'>
								<#outputformat "plainText">${observation_type_map['direction']}</#outputformat>
							<#elseif group.type == 'HORIZONTAL_DISTANCE'>
								<#outputformat "plainText">${observation_type_map['horizontal_distance']}</#outputformat>
							<#elseif group.type == 'SLOPE_DISTANCE'>
								<#outputformat "plainText">${observation_type_map['slope_distance']}</#outputformat>
							<#elseif group.type == 'ZENITH_ANGLE'>
								<#outputformat "plainText">${observation_type_map['zenith_angle']}</#outputformat>
							</#if>
						</h2>
						<#assign has_group_params = true>
					</#if>
					<figure>
						<svg class="piechart" viewBox="0 0 200 100" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
							<#assign total        = (Math.PI * 50.0)>
							<#assign excellent    = (group.relative_frequency_part_3 * total / 100.0)>
							<#assign satisfactory = (group.relative_frequency_part_2 * total / 100.0)>
							<#assign inadequate   = (group.relative_frequency_part_1 * total / 100.0)>
							
							<circle r="25" cx="50" cy="50"
								style="stroke-dasharray: ${excellent?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]} ${total?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; 
								stroke-dashoffset: 0; ">
							</circle>
							
							<circle r="25" cx="50" cy="50"
								style="stroke-dasharray: ${satisfactory?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]} ${total?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; 
								stroke-dashoffset: ${(-excellent)?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; ">
							</circle>
							
							<circle r="25" cx="50" cy="50"
								style="stroke-dasharray: ${inadequate?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]} ${total?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; 
								stroke-dashoffset: ${(-excellent - satisfactory)?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; ">
							</circle>

							<rect x="105" y="25"></rect>
							<rect x="105" y="45"></rect>
							<rect x="105" y="65"></rect>
							
							<text x="125" y="34" text-anchor="start">${group.absolute_frequency_part_3?string["0"]} (${group.relative_frequency_part_3?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</text>
							<text x="125" y="54" text-anchor="start">${group.absolute_frequency_part_2?string["0"]} (${group.relative_frequency_part_2?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</text>
							<text x="125" y="74" text-anchor="start">${group.absolute_frequency_part_1?string["0"]} (${group.relative_frequency_part_1?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</text>
						
							<#outputformat "plainText">${plot_svg_support}</#outputformat>
						</svg>
						<figcaption><#outputformat "plainText">${chart_figcaption_map['p_prio']}</#outputformat> p-value (<#outputformat "plainText">${mean_interval}</#outputformat>: {p-value | <@printFormattedNumber value=group.left_boundary format=df_percentage/>&nbsp;${unit_abbr_percentage} &le; p-value &le; <@printFormattedNumber value=group.right_boundary format=df_percentage/>&nbsp;${unit_abbr_percentage}})</figcaption>
					</figure>		
				</#if>
			</#list>
		</#if>
		
		<#if chart_values.influence_on_position??>
			<#list chart_values.influence_on_position as group>
				<#if group.type?? && group.type == observation_type && group.absolute_frequency_part_1?? && group.absolute_frequency_part_2?? && group.absolute_frequency_part_3?? && group.relative_frequency_part_1?? && group.relative_frequency_part_2?? && group.relative_frequency_part_3??>
					<#if !has_group_params>
						<section>
						<h2>
							<#if group.type == 'LEVELING'>
								<#outputformat "plainText">${observation_type_map['levelling']}</#outputformat>
							<#elseif group.type == 'DIRECTION'>
								<#outputformat "plainText">${observation_type_map['direction']}</#outputformat>
							<#elseif group.type == 'HORIZONTAL_DISTANCE'>
								<#outputformat "plainText">${observation_type_map['horizontal_distance']}</#outputformat>
							<#elseif group.type == 'SLOPE_DISTANCE'>
								<#outputformat "plainText">${observation_type_map['slope_distance']}</#outputformat>
							<#elseif group.type == 'ZENITH_ANGLE'>
								<#outputformat "plainText">${observation_type_map['zenith_angle']}</#outputformat>
							</#if>
						</h2>
						<#assign has_group_params = true>
					</#if>
					<figure>
						<svg class="piechart" viewBox="0 0 200 100" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
							<#assign total        = (Math.PI * 50.0)>
							<#assign excellent    = (group.relative_frequency_part_1 * total / 100.0)>
							<#assign satisfactory = (group.relative_frequency_part_2 * total / 100.0)>
							<#assign inadequate   = (group.relative_frequency_part_3 * total / 100.0)>
							
							<circle r="25" cx="50" cy="50"
								style="stroke-dasharray: ${excellent?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]} ${total?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; 
								stroke-dashoffset: 0; ">
							</circle>
							
							<circle r="25" cx="50" cy="50"
								style="stroke-dasharray: ${satisfactory?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]} ${total?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; 
								stroke-dashoffset: ${(-excellent)?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; ">
							</circle>
							
							<circle r="25" cx="50" cy="50"
								style="stroke-dasharray: ${inadequate?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]} ${total?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; 
								stroke-dashoffset: ${(-excellent - satisfactory)?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}; ">
							</circle>
							
							<rect x="105" y="25"></rect>
							<rect x="105" y="45"></rect>
							<rect x="105" y="65"></rect>
							
							<text x="125" y="34" text-anchor="start">${group.absolute_frequency_part_1?string["0"]} (${group.relative_frequency_part_1?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</text>
							<text x="125" y="54" text-anchor="start">${group.absolute_frequency_part_2?string["0"]} (${group.relative_frequency_part_2?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</text>
							<text x="125" y="74" text-anchor="start">${group.absolute_frequency_part_3?string["0"]} (${group.relative_frequency_part_3?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</text>
							
							<#outputformat "plainText">${plot_svg_support}</#outputformat>
						</svg>
						<figcaption><#outputformat "plainText">${chart_figcaption_map['influence_on_position']}</#outputformat> EP (<#outputformat "plainText">${mean_interval}</#outputformat>: {EP | <@printFormattedNumber value=group.left_boundary format=df_length_residual/>&nbsp;${unit_abbr_length_residual} &le; EP &le; <@printFormattedNumber value=group.right_boundary format=df_length_residual/>&nbsp;${unit_abbr_length_residual}})</figcaption>
					</figure>
					
				</#if>
			</#list>
		</#if>
		
		<#if has_group_params>
			</section>
		</#if>
		
	</#list>
</#if>