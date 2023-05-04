<tbody>
	<#list unknown_transformation_parameters as parameter>
		<#if parameter.visible && parameter_type_map[parameter.parameter_type]?? && parameter_processing_map[parameter.processing_type]??>
			<tr>
				<td><#outputformat "plainText">${parameter_type_map[parameter.parameter_type]}</#outputformat></th>
				<td><#outputformat "plainText">${parameter_processing_map[parameter.processing_type]}</#outputformat></th>
				
				<td>
					<#if parameter.unit_type == "ANGLE">
						<@printFormattedNumber value=parameter.value format=df_angle/> ${unit_abbr_angle}
					<#elseif parameter.unit_type == "LENGTH">
						<@printFormattedNumber value=parameter.value format=df_length/> ${unit_abbr_length}
					<#elseif parameter.unit_type == "SCALE">
						<@printFormattedNumber value=parameter.value format=df_scale/> ${unit_abbr_scale}
					<#elseif parameter.unit_type == "VECTOR">
						<@printFormattedNumber value=parameter.value format=df_vector/> ${unit_abbr_vector}
					<#elseif parameter.unit_type == "DOUBLE">
						<@printFormattedNumber value=parameter.value format=df_double/>
					<#else>
						<@printFormattedNumber value=parameter.value format=df_statistic/>
					</#if>
				</td>
				
				<td>
					<#if parameter.unit_type == "ANGLE">
						<@printFormattedNumber value=parameter.sigma format=df_angle_uncertainty/> ${unit_abbr_angle_uncertainty}
					<#elseif parameter.unit_type == "LENGTH">
						<@printFormattedNumber value=parameter.sigma format=df_length_uncertainty/> ${unit_abbr_length_uncertainty}
					<#elseif parameter.unit_type == "SCALE">
						<@printFormattedNumber value=parameter.sigma format=df_scale_uncertainty/> ${unit_abbr_scale_uncertainty}
					<#elseif parameter.unit_type == "VECTOR">
						<@printFormattedNumber value=parameter.sigma format=df_vector_uncertainty/> ${unit_abbr_vector_uncertainty}
					<#elseif parameter.unit_type == "DOUBLE">
						<@printFormattedNumber value=parameter.sigma format=df_double/>
					<#else>
						<@printFormattedNumber value=parameter.sigma format=df_statistic/>
					</#if>
				</td>
				
				<td>${(parameter.column &lt; 0)?then('-', parameter.column)}</th>
	
			</tr>
		</#if>
	</#list>
</tbody>