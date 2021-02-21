<tbody>
	<#list group.deflections as deflection>
		<tr>
			<th>${deflection.name}</th>
			<td><@printFormattedNumber value=deflection.y0 format=df_angle_residual/></td>
			<td><@printFormattedNumber value=deflection.x0 format=df_angle_residual/></td>
			<#if group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
				<td><@printFormattedNumber value=deflection.sigma_y0 format=df_angle_uncertainty/></td>
				<td><@printFormattedNumber value=deflection.sigma_x0 format=df_angle_uncertainty/></td>
			</#if>
			
			<#if group.type != "REFERENCE_VERTICAL_DEFLECTION">
				<td><@printFormattedNumber value=deflection.y format=df_angle_residual/></td>
				<td><@printFormattedNumber value=deflection.x format=df_angle_residual/></td>
				<td><@printFormattedNumber value=deflection.sigma_y format=df_angle_uncertainty/></td>
				<td><@printFormattedNumber value=deflection.sigma_x format=df_angle_uncertainty/></td>
				<td><@printFormattedNumber value=deflection.confidence_major_axis format=df_angle_uncertainty/></td>
				<td><@printFormattedNumber value=deflection.confidence_minor_axis format=df_angle_uncertainty/></td>
			</#if>
			
			<#if group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
				<td><@printFormattedNumber value=deflection.redundancy_y format=df_percentage/></td>
				<td><@printFormattedNumber value=deflection.redundancy_x format=df_percentage/></td>
				<td><@printFormattedNumber value=deflection.residual_y format=df_angle_residual/></td>
				<td><@printFormattedNumber value=deflection.residual_x format=df_angle_residual/></td>
			</#if>
			
			<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
				<td><@printFormattedNumber value=deflection.gross_error_y format=df_angle_residual/></td>
				<td><@printFormattedNumber value=deflection.gross_error_x format=df_angle_residual/></td>
			</#if>
						
			<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
				<td><@printFormattedNumber value=deflection.minimal_detectable_bias_y format=df_angle_residual/></td>
				<td><@printFormattedNumber value=deflection.minimal_detectable_bias_x format=df_angle_residual/></td>
			</#if>
			
			<#if group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
				<td><@printFormattedNumber value=deflection.omega format=df_statistic/></td>
			</#if>
			
			<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
				<td><@printFormattedNumber value=deflection.p_prio format=df_statistic/></td>
				<td><@printFormattedNumber value=deflection.p_post format=df_statistic/></td>
				
				<td><@printFormattedNumber value=deflection.t_prio format=df_statistic/></td>
				<td><@printFormattedNumber value=deflection.t_post format=df_statistic/></td>
				
				<td><span class="${deflection.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${deflection.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
			</#if>
		</tr>
	</#list>
</tbody>