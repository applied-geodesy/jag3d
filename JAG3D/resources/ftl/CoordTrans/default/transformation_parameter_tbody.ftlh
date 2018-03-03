<tbody>
	<#list transformation_parameter as param>
		<#assign param_unit_type="UNSPECIFIED">
		<#if param.type == 'Rx' || param.type == 'Ry' || param.type == 'Rz' || param.type == 'Sx' || param.type == 'Sy' || param.type == 'Sz'>
			<#assign param_unit_type="ANGLE">
		<#elseif param.type == 'Mx' || param.type == 'My' || param.type == 'Mz'>
			<#assign param_unit_type="SCALE">
		<#elseif param.type == 'Tx' || param.type == 'Ty' || param.type == 'Tz'>
			<#assign param_unit_type="LENGTH">
		</#if>
		<tr>
			<th><#outputformat "plainText">${param.type}</#outputformat></th>

			<td>
				<#if param_unit_type == "ANGLE">
					${(param.value * 200.0/Math.PI)?string[df_angle]} ${unit_abbr_map['gradian']}
				<#elseif param_unit_type == "LENGTH">
					${param.value?string[df_length]} ${unit_abbr_map['metre']}
				<#elseif param_unit_type == "SCALE">
					${(param.value - 1)?string[df_scale]} ${unit_abbr_map['millimetre_per_kilometre']}
				<#else>
					${param.value?string[df_unspecified_parameter]}
				</#if>
			</td>
			
			<td>
				<#if param_unit_type == "ANGLE">
					${(param.sigma * 200.0/Math.PI)?string[df_angle_uncertainty]} ${unit_abbr_map['milligradian']}
				<#elseif param_unit_type == "LENGTH">
					${param.sigma?string[df_length_uncertainty]} ${unit_abbr_map['millimetre']}
				<#elseif param_unit_type == "SCALE">
					${param.sigma?string[df_scale_uncertainty]} ${unit_abbr_map['millimetre_per_kilometre']}
				<#else>
					${param.sigma?string[df_unspecified_parameter]}
				</#if>
			</td>
			
			<td><span class="${param.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${param.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
		</tr>
	</#list>
</tbody>