	<#list group.unknown_parameters as param>
		<#assign param_unit_type="LENGTH">
		<#if param.type == 'ORIENTATION' || param.type == 'ROTATION_X' || param.type == 'ROTATION_Y' || param.type == 'ROTATION_Z'>
			<#assign param_unit_type="ANGLE">
		<#elseif param.type == 'SCALE'>
			<#assign param_unit_type="SCALE">
		</#if>
		<tr>
			<th>${param?is_first?then(group.name,'')}</th>
			
			<th>
				<#if param.type == 'ORIENTATION'>
					<#outputformat "plainText">${additional_parameter_map['orientation']}</#outputformat>
				<#elseif param.type == 'ROTATION_X'>
					<#outputformat "plainText">${additional_parameter_map['rotation_x']}</#outputformat>
				<#elseif param.type == 'ROTATION_Y'>
					<#outputformat "plainText">${additional_parameter_map['rotation_y']}</#outputformat>
				<#elseif param.type == 'ROTATION_Z'>
					<#outputformat "plainText">${additional_parameter_map['rotation_z']}</#outputformat>
				<#elseif param.type == 'SCALE'>
					<#outputformat "plainText">${additional_parameter_map['scale']}</#outputformat>
				<#elseif param.type == 'REFRACTION_INDEX'>
					<#outputformat "plainText">${additional_parameter_map['refraction']}</#outputformat>
				<#elseif param.type == 'ZERO_POINT_OFFSET'>
					<#outputformat "plainText">${additional_parameter_map['zero_point_error']}</#outputformat>
				</#if>
			</th>

			<td>
				<#if param_unit_type == "ANGLE">
					<#if sexagesimal_angle??>
						<#assign degree  = param.value?floor>
						<#assign minutes = ((param.value - degree) * 60)?floor>
						<#assign seconds = ((((param.value - degree) * 60) - minutes) * 60)>
						<td>${degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle]?string[1..])}</td>
					<#else>
						${param.value?string[df_angle]}
					</#if>
					${unit_abbr_angle}
				<#elseif param_unit_type == "LENGTH">
					${param.value?string[df_length_residual]} ${unit_abbr_length_residual}
				<#else>
					${param.value?string[df_scale]} ${unit_abbr_scale}
				</#if>
			</td>
			
			<td>
				<#if param_unit_type == "ANGLE">
					${param.sigma?string[df_angle_uncertainty]} ${unit_abbr_angle_uncertainty}
				<#elseif param_unit_type == "LENGTH">
					${param.sigma?string[df_length_uncertainty]} ${unit_abbr_length_uncertainty}
				<#else>
					${param.sigma?string[df_scale_uncertainty]} ${unit_abbr_scale_uncertainty}
				</#if>
			</td>
			
			<td>
				<#if param_unit_type == "ANGLE">
					${param.confidence?string[df_angle_uncertainty]} ${unit_abbr_angle_uncertainty}
				<#elseif param_unit_type == "LENGTH">
					${param.confidence?string[df_length_uncertainty]} ${unit_abbr_length_uncertainty}
				<#else>
					${param.confidence?string[df_scale_uncertainty]} ${unit_abbr_scale_uncertainty}
				</#if>
			</td>
			
			<#comment>
			<td>
				<#if param_unit_type == "ANGLE">
					${param.gross_error?string[df_angle_residual]} ${unit_abbr_angle_residual}
				<#elseif param_unit_type == "LENGTH">
					${param.gross_error?string[df_length_residual]} ${unit_abbr_length_residual}
				<#else>
					${param.gross_error?string[df_scale_residual]} ${unit_abbr_scale_residual}
				</#if>
			</td>
			
			<td>
				<#if param_unit_type == "ANGLE">
					${param.minimal_detectable_bias?string[df_angle_residual]} ${unit_abbr_angle_residual}
				<#elseif param_unit_type == "LENGTH">
					${param.minimal_detectable_bias?string[df_length_residual]} ${unit_abbr_length_residual}
				<#else>
					${param.minimal_detectable_bias?string[df_scale_residual]} ${unit_abbr_scale_residual}
				</#if>
			</td>

			<td>${param.p_prio?string[df_statistic]}</td>
			<td>${param.p_post?string[df_statistic]}</td>
			
			<td>${param.t_prio?string[df_statistic]}</td>
			<td>${param.t_post?string[df_statistic]}</td>
			</#comment>
			
			<td><span class="${param.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${param.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
		</tr>
	</#list>