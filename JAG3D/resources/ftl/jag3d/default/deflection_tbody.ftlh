<tbody>
	<#list group.deflections as deflection>
		<tr>
			<th>${deflection.name}</th>
			<td>${deflection.dy0?string[df_angle_residual]}</td>
			<td>${deflection.dx0?string[df_angle_residual]}</td>

			<#if group.type == "STOCHASTIC_POINT">
				<td>${deflection.sigma_dy0?string[df_angle_uncertainty]}</td>
				<td>${deflection.sigma_dx0?string[df_angle_uncertainty]}</td>
			</#if>
			
			<#if group.type != "REFERENCE_POINT">
				<td>${deflection.dy?string[df_angle_residual]}</td>
				<td>${deflection.dx?string[df_angle_residual]}</td>
				<td>${deflection.sigma_dy?string[df_angle_uncertainty]}</td>
				<td>${deflection.sigma_dx?string[df_angle_uncertainty]}</td>
				<td>${deflection.confidence_major_axis?string[df_angle_uncertainty]}</td>
				<td>${deflection.confidence_minor_axis?string[df_angle_uncertainty]}</td>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<td>${deflection.redundancy_dy?string[df_statistic]}</td>
				<td>${deflection.redundancy_dx?string[df_statistic]}</td>
				
				<td>${deflection.residual_dy?string[df_angle_residual]}</td>
				<td>${deflection.residual_dx?string[df_angle_residual]}</td>
			</#if>
			
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<td>${deflection.gross_error_dy?string[df_angle_residual]}</td>
				<td>${deflection.gross_error_dx?string[df_angle_residual]}</td>
			</#if>
						
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<td>${deflection.minimal_detectable_bias_dy?string[df_angle_residual]}</td>
				<td>${deflection.minimal_detectable_bias_dx?string[df_angle_residual]}</td>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<td>${deflection.omega?string[df_statistic]}</td>
			</#if>
			
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<td>${deflection.p_prio?string[df_statistic]}</td>
				<td>${deflection.p_post?string[df_statistic]}</td>
				
				<td>${deflection.t_prio?string[df_statistic]}</td>
				<td>${deflection.t_post?string[df_statistic]}</td>
				
				<td><span class="${deflection.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${deflection.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
			</#if>
		</tr>
	</#list>
</tbody>