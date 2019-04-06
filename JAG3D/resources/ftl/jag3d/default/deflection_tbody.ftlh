<tbody>
	<#list group.deflections as deflection>
		<tr>
			<th>${deflection.name}</th>
			<td><@printFormattedNumber value=deflection.dy0 format=df_angle_residual/></td>
			<td><@printFormattedNumber value=deflection.dx0 format=df_angle_residual/></td>
			<#if group.type == "STOCHASTIC_POINT">
				<td><@printFormattedNumber value=deflection.sigma_dy0 format=df_angle_uncertainty/></td>
				<td><@printFormattedNumber value=deflection.sigma_dx0 format=df_angle_uncertainty/></td>
			</#if>
			
			<#if group.type != "REFERENCE_POINT">
				<td><@printFormattedNumber value=deflection.dy format=df_angle_residual/></td>
				<td><@printFormattedNumber value=deflection.dx format=df_angle_residual/></td>
				<td><@printFormattedNumber value=deflection.sigma_dy format=df_angle_uncertainty/></td>
				<td><@printFormattedNumber value=deflection.sigma_dx format=df_angle_uncertainty/></td>
				<td><@printFormattedNumber value=deflection.confidence_major_axis format=df_angle_uncertainty/></td>
				<td><@printFormattedNumber value=deflection.confidence_minor_axis format=df_angle_uncertainty/></td>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<td><@printFormattedNumber value=deflection.redundancy_dy format=df_statistic/></td>
				<td><@printFormattedNumber value=deflection.redundancy_dx format=df_statistic/></td>
				<td><@printFormattedNumber value=deflection.residual_dy format=df_angle_residual/></td>
				<td><@printFormattedNumber value=deflection.residual_dx format=df_angle_residual/></td>
			</#if>
			
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<td><@printFormattedNumber value=deflection.gross_error_dy format=df_angle_residual/></td>
				<td><@printFormattedNumber value=deflection.gross_error_dx format=df_angle_residual/></td>
			</#if>
						
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<td><@printFormattedNumber value=deflection.minimal_detectable_bias_dy format=df_angle_residual/></td>
				<td><@printFormattedNumber value=deflection.minimal_detectable_bias_dx format=df_angle_residual/></td>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<td><@printFormattedNumber value=deflection.omega format=df_statistic/></td>
			</#if>
			
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<td><@printFormattedNumber value=deflection.p_prio format=df_statistic/></td>
				<td><@printFormattedNumber value=deflection.p_post format=df_statistic/></td>
				
				<td><@printFormattedNumber value=deflection.t_prio format=df_statistic/></td>
				<td><@printFormattedNumber value=deflection.t_post format=df_statistic/></td>
				
				<td><span class="${deflection.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${deflection.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
			</#if>
		</tr>
	</#list>
</tbody>