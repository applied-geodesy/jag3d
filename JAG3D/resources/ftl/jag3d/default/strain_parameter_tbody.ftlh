	<#list group.strain_parameters as param>
		<#assign param_unit_type="LENGTH">
		<#if param.type == 'STRAIN_SHEAR_X' || param.type == 'STRAIN_SHEAR_Y' || param.type == 'STRAIN_SHEAR_Z' || param.type == 'STRAIN_ROTATION_X' || param.type == 'STRAIN_ROTATION_Y' || param.type == 'STRAIN_ROTATION_Z'>
			<#assign param_unit_type="ANGLE">
		<#elseif param.type == 'STRAIN_SCALE_X' || param.type == 'STRAIN_SCALE_Y' || param.type == 'STRAIN_SCALE_Z'>
			<#assign param_unit_type="SCALE">
		</#if>
		<tr>
			<th>${param?is_first?then(group.name,'')}</th>
			
			<th>
				<#if param.type == 'STRAIN_TRANSLATION_X'>
					<#outputformat "plainText">${strain_parameter_map['translation_x']}</#outputformat>
				<#elseif param.type == 'STRAIN_TRANSLATION_Y'>
					<#outputformat "plainText">${strain_parameter_map['translation_y']}</#outputformat>
				<#elseif param.type == 'STRAIN_TRANSLATION_Z'>
					<#outputformat "plainText">${strain_parameter_map['translation_z']}</#outputformat>
				<#elseif param.type == 'STRAIN_ROTATION_X'>
					<#outputformat "plainText">${strain_parameter_map['rotation_x']}</#outputformat>
				<#elseif param.type == 'STRAIN_ROTATION_Y'>
					<#outputformat "plainText">${strain_parameter_map['rotation_y']}</#outputformat>
				<#elseif param.type == 'STRAIN_ROTATION_Z'>
					<#outputformat "plainText">${strain_parameter_map['rotation_z']}</#outputformat>
				<#elseif param.type == 'STRAIN_SCALE_X'>
					<#outputformat "plainText">${strain_parameter_map['scale_x']}</#outputformat>
				<#elseif param.type == 'STRAIN_SCALE_Y'>
					<#outputformat "plainText">${strain_parameter_map['scale_y']}</#outputformat>
				<#elseif param.type == 'STRAIN_SCALE_Z'>
					<#outputformat "plainText">${strain_parameter_map['scale_z']}</#outputformat>
				<#elseif param.type == 'STRAIN_SHEAR_X'>
					<#outputformat "plainText">${strain_parameter_map['shear_x']}</#outputformat>
				<#elseif param.type == 'STRAIN_SHEAR_Y'>
					<#outputformat "plainText">${strain_parameter_map['shear_y']}</#outputformat>
				<#elseif param.type == 'STRAIN_SHEAR_Z'>
					<#outputformat "plainText">${strain_parameter_map['shear_z']}</#outputformat>
				</#if>
			</th>

			<td>
				<#if param_unit_type == "ANGLE">
					<@printFormattedNumber value=param.value format=df_angle/> ${unit_abbr_angle}
				<#elseif param_unit_type == "LENGTH">
					<@printFormattedNumber value=param.value format=df_length/> ${unit_abbr_length}
				<#else>
					<@printFormattedNumber value=param.value format=df_scale/> ${unit_abbr_scale}
				</#if>
			</td>
			
			<td>
				<#if param_unit_type == "ANGLE">
					<@printFormattedNumber value=param.sigma format=df_angle_uncertainty/> ${unit_abbr_angle_uncertainty}
				<#elseif param_unit_type == "LENGTH">
					<@printFormattedNumber value=param.sigma format=df_length_uncertainty/> ${unit_abbr_length_uncertainty}
				<#else>
					<@printFormattedNumber value=param.sigma format=df_scale_uncertainty/> ${unit_abbr_scale_uncertainty}
				</#if>
			</td>
			
			<td>
				<#if param_unit_type == "ANGLE">
					<@printFormattedNumber value=param.confidence format=df_angle_uncertainty/> ${unit_abbr_angle_uncertainty}
				<#elseif param_unit_type == "LENGTH">
					<@printFormattedNumber value=param.confidence format=df_length_uncertainty/> ${unit_abbr_length_uncertainty}
				<#else>
					<@printFormattedNumber value=param.confidence format=df_scale_uncertainty/> ${unit_abbr_scale_uncertainty}
				</#if>
			</td>
			
			<td>
				<#if param_unit_type == "ANGLE">
					<@printFormattedNumber value=param.gross_error format=df_angle_residual/> ${unit_abbr_angle_residual}
				<#elseif param_unit_type == "LENGTH">
					<@printFormattedNumber value=param.gross_error format=df_length_residual/> ${unit_abbr_length_residual}
				<#else>
					<@printFormattedNumber value=param.gross_error format=df_scale_residual/> ${unit_abbr_scale_residual}
				</#if>
			</td>
			
			<td>
				<#if param_unit_type == "ANGLE">
					<@printFormattedNumber value=param.minimal_detectable_bias format=df_angle_residual/> ${unit_abbr_angle_residual}
				<#elseif param_unit_type == "LENGTH">
					<@printFormattedNumber value=param.minimal_detectable_bias format=df_length_residual/> ${unit_abbr_length_residual}
				<#else>
					<@printFormattedNumber value=param.minimal_detectable_bias format=df_scale_residual/> ${unit_abbr_scale_residual}
				</#if>
			</td>

			<td><@printFormattedNumber value=param.p_prio format=df_statistic/></td>
			<td><@printFormattedNumber value=param.p_post format=df_statistic/></td>
			
			<td><@printFormattedNumber value=param.t_prio format=df_statistic/></td>
			<td><@printFormattedNumber value=param.t_post format=df_statistic/></td>
			
			<td><span class="${param.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${param.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
		</tr>
	</#list>