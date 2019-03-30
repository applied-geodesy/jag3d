<tbody>
	<#list group.deflections as deflection>
		<tr>
			<th>${deflection.name}</th>
			
			<#if sexagesimal_angle??>
				<#assign degree  = deflection.dy0?floor>
				<#assign minutes = ((deflection.dy0 - degree) * 60)?floor>
				<#assign seconds = ((((deflection.dy0 - degree) * 60) - minutes) * 60)>
				<td>${degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle]?string[1..])}</td>

				<#assign degree  = deflection.dx0?floor>
				<#assign minutes = ((deflection.dx0 - degree) * 60)?floor>
				<#assign seconds = ((((deflection.dx0 - degree) * 60) - minutes) * 60)>
				<td>${degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle]?string[1..])}</td>
			<#else>
				<td>${deflection.dy0?string[df_angle]}</td>
				<td>${deflection.dx0?string[df_angle]}</td>
			</#if>

			<#if group.type == "STOCHASTIC_POINT">
				<td>${deflection.sigma_dy0?string[df_angle_uncertainty]}</td>
				<td>${deflection.sigma_dx0?string[df_angle_uncertainty]}</td>
			</#if>
			
			<#if group.type != "REFERENCE_POINT">
				<#if sexagesimal_angle??>
					<#assign degree  = deflection.dy?floor>
					<#assign minutes = ((deflection.dy - degree) * 60)?floor>
					<#assign seconds = ((((deflection.dy - degree) * 60) - minutes) * 60)>
					<td>${degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle]?string[1..])}</td>

					<#assign degree  = deflection.dx?floor>
					<#assign minutes = ((deflection.dx - degree) * 60)?floor>
					<#assign seconds = ((((deflection.dx - degree) * 60) - minutes) * 60)>
					<td>${degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle]?string[1..])}</td>
				<#else>
					<td>${deflection.dy?string[df_angle]}</td>
					<td>${deflection.dx?string[df_angle]}</td>
				</#if>
				
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