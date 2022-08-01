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
						<figcaption><#outputformat "plainText">${chart_figcaption_map['redundancy']}</#outputformat> r (<#outputformat "plainText">${mean_interval}</#outputformat>: {r | <@printFormattedNumber value=group.left_boundary format=df_percentage/>&nbsp;${unit_abbr_percentage} &le; r &le; <@printFormattedNumber value=group.right_boundary format=df_percentage/>&nbsp;${unit_abbr_percentage}})</figcaption>
						<ol class="chart">
							<li title="${group.absolute_frequency_part_3?string["0"]}"><span style="width: ${group.relative_frequency_part_3?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}%;">${group.absolute_frequency_part_3?string["0"]} (${group.relative_frequency_part_3?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</span></li>
							<li title="${group.absolute_frequency_part_2?string["0"]}"><span style="width: ${group.relative_frequency_part_2?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}%;">${group.absolute_frequency_part_2?string["0"]} (${group.relative_frequency_part_2?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</span></li>
							<li title="${group.absolute_frequency_part_1?string["0"]}"><span style="width: ${group.relative_frequency_part_1?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}%;">${group.absolute_frequency_part_1?string["0"]} (${group.relative_frequency_part_1?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</span></li>
						</ol>
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
						<figcaption><#outputformat "plainText">${chart_figcaption_map['p_prio']}</#outputformat> p-value (<#outputformat "plainText">${mean_interval}</#outputformat>: {p-value | <@printFormattedNumber value=group.left_boundary format=df_percentage/>&nbsp;${unit_abbr_percentage} &le; p-value &le; <@printFormattedNumber value=group.right_boundary format=df_percentage/>&nbsp;${unit_abbr_percentage}})</figcaption>
						<ol class="chart">
							<li title="${group.absolute_frequency_part_3?string["0"]}"><span style="width: ${group.relative_frequency_part_3?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}%;">${group.absolute_frequency_part_3?string["0"]} (${group.relative_frequency_part_3?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</span></li>
							<li title="${group.absolute_frequency_part_2?string["0"]}"><span style="width: ${group.relative_frequency_part_2?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}%;">${group.absolute_frequency_part_2?string["0"]} (${group.relative_frequency_part_2?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</span></li>
							<li title="${group.absolute_frequency_part_1?string["0"]}"><span style="width: ${group.relative_frequency_part_1?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}%;">${group.absolute_frequency_part_1?string["0"]} (${group.relative_frequency_part_1?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</span></li>
						</ol>
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
						<figcaption><#outputformat "plainText">${chart_figcaption_map['influence_on_position']}</#outputformat> EP (<#outputformat "plainText">${mean_interval}</#outputformat>: {EP | <@printFormattedNumber value=group.left_boundary format=df_length_residual/>&nbsp;${unit_abbr_length_residual} &le; EP &le; <@printFormattedNumber value=group.right_boundary format=df_length_residual/>&nbsp;${unit_abbr_length_residual}})</figcaption>
						<ol class="chart">
							<li title="${group.absolute_frequency_part_1?string["0"]}"><span style="width: ${group.relative_frequency_part_1?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}%;">${group.absolute_frequency_part_1?string["0"]} (${group.relative_frequency_part_1?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</span></li>
							<li title="${group.absolute_frequency_part_2?string["0"]}"><span style="width: ${group.relative_frequency_part_2?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}%;">${group.absolute_frequency_part_2?string["0"]} (${group.relative_frequency_part_2?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</span></li>
							<li title="${group.absolute_frequency_part_3?string["0"]}"><span style="width: ${group.relative_frequency_part_3?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}%;">${group.absolute_frequency_part_3?string["0"]} (${group.relative_frequency_part_3?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}&nbsp;%)</span></li>
						</ol>
					</figure>
				</#if>
			</#list>
		</#if>
		
		<#if has_group_params>
			</section>
		</#if>
		
	</#list>
</#if>