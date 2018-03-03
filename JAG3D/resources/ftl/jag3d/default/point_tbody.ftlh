<tbody>
	<#list group.points as point>
		<tr>
			<th>${point.name}</th>
			<th>${point.code}</th>
			
			<#if group.dimension != 1>
				<td>${point.y0?string[df_length]}</td>
				<td>${point.x0?string[df_length]}</td>
			</#if>
			
			<#if group.dimension != 2>
				<td>${point.z0?string[df_length]}</td>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<#if group.dimension != 1>
					<td>${point.sigma_y0?string[df_length_uncertainty]}</td>
					<td>${point.sigma_x0?string[df_length_uncertainty]}</td>
				</#if>
			
				<#if group.dimension != 2>
					<td>${point.sigma_z0?string[df_length_uncertainty]}</td>
				</#if>
			</#if>
			
			<#if group.type != "REFERENCE_POINT">
				<#if group.dimension != 1>
					<td>${point.y?string[df_length]}</td>
					<td>${point.x?string[df_length]}</td>
				</#if>
				
				<#if group.dimension != 2>
					<td>${point.z?string[df_length]}</td>
				</#if>			
				
				<#if group.dimension != 1>
					<td>${point.sigma_y?string[df_length_uncertainty]}</td>
					<td>${point.sigma_x?string[df_length_uncertainty]}</td>
				</#if>
				
				<#if group.dimension != 2>
					<td>${point.sigma_z?string[df_length_uncertainty]}</td>
				</#if>

				<td>${point.confidence_major_axis?string[df_length_uncertainty]}</td>
				<#if group.dimension != 1>
					<#if group.dimension == 3>
						<td>${point.confidence_middle_axis?string[df_length_uncertainty]}</td>
					</#if>
					<td>${point.confidence_minor_axis?string[df_length_uncertainty]}</td>
				</#if>
				
				<#if group.dimension == 3>
					<td>${point.confidence_alpha?string[df_angle]}</td>
					<td>${point.confidence_beta?string[df_angle]}</td>
				</#if>
				
				<#if group.dimension != 1>
					<td>${point.confidence_gamma?string[df_angle]}</td>
				</#if>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<#if group.dimension != 1>
					<td>${point.redundancy_y?string[df_statistic]}</td>
					<td>${point.redundancy_x?string[df_statistic]}</td>
				</#if>
			
				<#if group.dimension != 2>
					<td>${point.redundancy_z?string[df_statistic]}</td>
				</#if>
			</#if>
			
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<#if group.dimension != 1>
					<td>${point.gross_error_y?string[df_length_residual]}</td>
					<td>${point.gross_error_x?string[df_length_residual]}</td>
				</#if>
			
				<#if group.dimension != 2>
					<td>${point.gross_error_z?string[df_length_residual]}</td>
				</#if>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<#if group.dimension != 1>
					<td>${point.influence_on_position_y?string[df_length_residual]}</td>
					<td>${point.influence_on_position_x?string[df_length_residual]}</td>
				</#if>
			
				<#if group.dimension != 2>
					<td>${point.influence_on_position_z?string[df_length_residual]}</td>
				</#if>
				
				<td>${point.influence_on_network_distortion?string[df_length_residual]}</td>
			</#if>
			
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<#if group.dimension != 1>
					<td>${point.minimal_detectable_bias_y?string[df_length_residual]}</td>
					<td>${point.minimal_detectable_bias_x?string[df_length_residual]}</td>
				</#if>
			
				<#if group.dimension != 2>
					<td>${point.minimal_detectable_bias_z?string[df_length_residual]}</td>
				</#if>
			</#if>
			
			<#if group.type != "REFERENCE_POINT">
				<#if group.dimension != 1>
					<td>${point.first_principal_component_y?string[df_length_residual]}</td>
					<td>${point.first_principal_component_x?string[df_length_residual]}</td>
				</#if>
			
				<#if group.dimension != 2>
					<td>${point.first_principal_component_z?string[df_length_residual]}</td>
				</#if>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<td>${point.omega?string[df_statistic]}</td>
			</#if>
			
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<td>${point.p_prio?string[df_statistic]}</td>
				<td>${point.p_post?string[df_statistic]}</td>
				
				<td>${point.t_prio?string[df_statistic]}</td>
				<td>${point.t_post?string[df_statistic]}</td>
				
				<td><span class="${point.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${point.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
			</#if>
		</tr>
	</#list>
</tbody>