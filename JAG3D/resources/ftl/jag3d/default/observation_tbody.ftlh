<tbody>
	<#list group.observations as observation>
		<tr>
			<th>${observation.start_point_name}</th>
			<th>${observation.end_point_name}</th>
			
			<#if group.type != "GNSS1D" && group.type != "GNSS2D" && group.type != "GNSS3D">
				<td><@printFormattedNumber value=observation.instrument_height format=df_length/></td>
				<td><@printFormattedNumber value=observation.reflector_height format=df_length/></td>
			</#if>
			
			<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.y0 format=df_length/></td>
					<td><@printFormattedNumber value=observation.x0 format=df_length/></td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.z0 format=df_length/></td>
				</#if>
			<#elseif apply_angle_unit>
				<td><@printFormattedNumber value=observation.value_0 format=df_angle/></td>
			<#else>
				<td><@printFormattedNumber value=observation.value_0 format=df_length/></td>
			</#if>
						
			<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.sigma_y0 format=df_length_uncertainty/></td>
					<td><@printFormattedNumber value=observation.sigma_x0 format=df_length_uncertainty/></td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.sigma_z0 format=df_length_uncertainty/></td>
				</#if>
			<#elseif apply_angle_unit>
				<td><@printFormattedNumber value=observation.sigma_0 format=df_angle_uncertainty/></td>
			<#else>
				<td><@printFormattedNumber value=observation.sigma_0 format=df_length_uncertainty/></td>
			</#if>
			
			<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.y format=df_length/></td>
					<td><@printFormattedNumber value=observation.x format=df_length/></td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.z format=df_length/></td>
				</#if>
			<#elseif apply_angle_unit>
				<td><@printFormattedNumber value=observation.value format=df_angle/></td>
			<#else>
				<td><@printFormattedNumber value=observation.value format=df_length/></td>
			</#if>
						
			<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.sigma_y format=df_length_uncertainty/></td>
					<td><@printFormattedNumber value=observation.sigma_x format=df_length_uncertainty/></td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.sigma_z format=df_length_uncertainty/></td>
				</#if>
				
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.redundancy_y format=df_percentage/></td>
					<td><@printFormattedNumber value=observation.redundancy_x format=df_percentage/></td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.redundancy_z format=df_percentage/></td>
				</#if>
				
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.residual_y format=df_length_residual/></td>
					<td><@printFormattedNumber value=observation.residual_x format=df_length_residual/></td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.residual_z format=df_length_residual/></td>
				</#if>
				
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.gross_error_y format=df_length_residual/></td>
					<td><@printFormattedNumber value=observation.gross_error_x format=df_length_residual/></td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.gross_error_z format=df_length_residual/></td>
				</#if>
				
				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.influence_on_position_y format=df_length_residual/></td>
					<td><@printFormattedNumber value=observation.influence_on_position_x format=df_length_residual/></td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.influence_on_position_z format=df_length_residual/></td>
				</#if>
				
				<td><@printFormattedNumber value=observation.influence_on_network_distortion format=df_length_residual/></td>

				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.maximum_tolerable_bias_y format=df_length_residual/></td>
					<td><@printFormattedNumber value=observation.maximum_tolerable_bias_x format=df_length_residual/></td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.maximum_tolerable_bias_z format=df_length_residual/></td>
				</#if>

				<#if group.type == "GNSS2D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.minimal_detectable_bias_y format=df_length_residual/></td>
					<td><@printFormattedNumber value=observation.minimal_detectable_bias_x format=df_length_residual/></td>
				</#if>
				<#if group.type == "GNSS1D" || group.type == "GNSS3D">
					<td><@printFormattedNumber value=observation.minimal_detectable_bias_z format=df_length_residual/></td>
				</#if>
			<#else>
				<#if apply_angle_unit>
					<td><@printFormattedNumber value=observation.sigma format=df_angle_uncertainty/></td>
				<#else>
					<td><@printFormattedNumber value=observation.sigma format=df_length_uncertainty/></td>
				</#if>	
				<td><@printFormattedNumber value=observation.redundancy format=df_percentage/></td>
				
				<#if apply_angle_unit>
					<td><@printFormattedNumber value=observation.residual format=df_angle_residual/></td>
					<td><@printFormattedNumber value=observation.gross_error format=df_angle_residual/></td>
				<#else>
					<td><@printFormattedNumber value=observation.residual format=df_length_residual/></td>
					<td><@printFormattedNumber value=observation.gross_error format=df_length_residual/></td>
				</#if>

				<td><@printFormattedNumber value=observation.influence_on_position format=df_length_residual/></td>
				<td><@printFormattedNumber value=observation.influence_on_network_distortion format=df_length_residual/></td>
				
				<#if apply_angle_unit>
					<td><@printFormattedNumber value=observation.maximum_tolerable_bias format=df_angle_residual/></td>
					<td><@printFormattedNumber value=observation.minimal_detectable_bias format=df_angle_residual/></td>
				<#else>
					<td><@printFormattedNumber value=observation.maximum_tolerable_bias format=df_length_residual/></td>
					<td><@printFormattedNumber value=observation.minimal_detectable_bias format=df_length_residual/></td>
				</#if>
			</#if>
			
			<td><@printFormattedNumber value=observation.omega format=df_statistic/></td>
			
			<td><@printFormattedNumber value=observation.p_prio format=df_statistic/></td>
			<td><@printFormattedNumber value=observation.p_post format=df_statistic/></td>
				
			<td><@printFormattedNumber value=observation.t_prio format=df_statistic/></td>
			<td><@printFormattedNumber value=observation.t_post format=df_statistic/></td>
			
			<td><span class="${observation.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${observation.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
		</tr>
	</#list>
</tbody>			