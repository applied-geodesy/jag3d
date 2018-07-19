<tbody>
	<#list group.observations as observation>
		<tr>
			<th>${observation.start_point_name}</th>
			<th>${observation.end_point_name}</th>
			
			<#if group.type != "GNSS1D" && group.type != "GNSS2D" && group.type != "GNSS3D">
				<td>${observation.instrument_height?string[df_length]}</td>
				<td>${observation.reflector_height?string[df_length]}</td>
			</#if>
			
			<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td>${observation.y0?string[df_length]}</td>
					<td>${observation.x0?string[df_length]}</td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td>${observation.z0?string[df_length]}</td>
				</#if>
			<#elseif apply_angle_unit>
				<td>${observation.value_0?string[df_angle]}</td>
			<#else>
				<td>${observation.value_0?string[df_length]}</td>
			</#if>
						
			<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td>${observation.sigma_y0?string[df_length_uncertainty]}</td>
					<td>${observation.sigma_x0?string[df_length_uncertainty]}</td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td>${observation.sigma_z0?string[df_length_uncertainty]}</td>
				</#if>
			<#elseif apply_angle_unit>
				<td>${observation.sigma_0?string[df_angle_uncertainty]}</td>
			<#else>
				<td>${observation.sigma_0?string[df_length_uncertainty]}</td>
			</#if>
			
			<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td>${observation.y?string[df_length]}</td>
					<td>${observation.x?string[df_length]}</td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td>${observation.z?string[df_length]}</td>
				</#if>
			<#else>
				<td>${observation.value?string[apply_angle_unit?then(df_angle, df_length)]}</td>
			</#if>
						
			<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td>${observation.sigma_y?string[df_length_uncertainty]}</td>
					<td>${observation.sigma_x?string[df_length_uncertainty]}</td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td>${observation.sigma_z?string[df_length_uncertainty]}</td>
				</#if>
				
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td>${observation.redundancy_y?string[df_statistic]}</td>
					<td>${observation.redundancy_x?string[df_statistic]}</td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td>${observation.redundancy_z?string[df_statistic]}</td>
				</#if>
				
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td>${observation.gross_error_y?string[df_length_residual]}</td>
					<td>${observation.gross_error_x?string[df_length_residual]}</td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td>${observation.gross_error_z?string[df_length_residual]}</td>
				</#if>
				
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td>${observation.influence_on_position_y?string[df_length_residual]}</td>
					<td>${observation.influence_on_position_x?string[df_length_residual]}</td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td>${observation.influence_on_position_z?string[df_length_residual]}</td>
				</#if>
				
				<td>${observation.influence_on_network_distortion?string[df_length_residual]}</td>

				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td>${observation.minimal_detectable_bias_y?string[df_length_residual]}</td>
					<td>${observation.minimal_detectable_bias_x?string[df_length_residual]}</td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td>${observation.minimal_detectable_bias_z?string[df_length_residual]}</td>
				</#if>
			<#else>
				<td>${observation.sigma?string[apply_angle_unit?then(df_angle_uncertainty, df_length_uncertainty)]}</td>
				<td>${observation.redundancy?string[df_statistic]}</td>
				<td>${observation.gross_error?string[apply_angle_unit?then(df_angle_residual, df_length_residual)]}</td>
				<td>${observation.influence_on_position?string[df_length_residual]}</td>
				<td>${observation.influence_on_network_distortion?string[df_length_residual]}</td>
				<td>${observation.minimal_detectable_bias?string[apply_angle_unit?then(df_angle_residual, df_length_residual)]}</td>
			</#if>
			
			<td>${observation.omega?string[df_statistic]}</td>
			
			<td>${observation.p_prio?string[df_statistic]}</td>
			<td>${observation.p_post?string[df_statistic]}</td>
				
			<td>${observation.t_prio?string[df_statistic]}</td>
			<td>${observation.t_post?string[df_statistic]}</td>
			
			<td><span class="${observation.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${observation.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
		</tr>
	</#list>
</tbody>			