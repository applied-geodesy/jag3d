<tbody>
	<#list form_parameter as param>
		<#assign param_unit_type="UNSPECIFIED">
		<#if param.type == 'Rx' || param.type == 'Ry' || param.type == 'Rz'>
			<#assign param_unit_type="ANGLE">
		<#elseif param.type == 'Q0' || param.type == 'Q1' || param.type == 'Q2' || param.type == 'Q3'>
			<#assign param_unit_type="QUATERNION">
		<#elseif param.type == 'Mx' || param.type == 'My' || param.type == 'Mz' || param.type == 'R' || param.type == 'A' || param.type == 'B' || param.type == 'C' || param.type == 'D' || param.type == 'F1x' || param.type == 'F1y' || param.type == 'F1z' || param.type == 'F2x' || param.type == 'F2y' || param.type == 'F2z'>
			<#assign param_unit_type="LENGTH">
		<#elseif param.type == 'Nx' || param.type == 'Ny' || param.type == 'Nz'>
			<#assign param_unit_type="NORMAL_VECTOR">
		</#if>
		<tr>
			<th><#outputformat "plainText">${param.type}</#outputformat></th>

			<td>
				<#if param_unit_type == "ANGLE">
					${(param.value * 200.0/Math.PI)?string[df_angle]} ${unit_abbr_map['gradian']}
				<#elseif param_unit_type == "LENGTH">
					${param.value?string[df_length]} ${unit_abbr_map['metre']}
				<#elseif param_unit_type == "NORMAL_VECTOR">
					${param.value?string[df_vector]}
				<#elseif param_unit_type == "QUATERNION">
					${param.value?string[df_quaternion]}
				<#else>
					${param.value?string[df_unspecified_parameter]}
				</#if>
			</td>
			
			<td>
				<#if param_unit_type == "ANGLE">
					${(param.sigma * 200.0/Math.PI)?string[df_angle_uncertainty]} ${unit_abbr_map['milligradian']}
				<#elseif param_unit_type == "LENGTH">
					${param.sigma?string[df_length_uncertainty]} ${unit_abbr_map['millimetre']}
				<#elseif param_unit_type == "NORMAL_VECTOR">
					${param.sigma?string[df_vector_uncertainty]}
				<#elseif param_unit_type == "QUATERNION">
					${param.value?string[df_quaternion_uncertainty]}
				<#else>
					${param.sigma?string[df_unspecified_parameter]}
				</#if>
			</td>
		</tr>
		
	</#list>
</tbody>