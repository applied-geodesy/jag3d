<tbody>
	<#list group.deflections as deflection>
		<tr>
			<th>${deflection.name}</th>
			<#if sexagesimal_angle_residual??>
				<#assign signum  = (deflection.dy0 < 0)?then(-1, 1)>
				<#assign value   = signum * deflection.dy0>
				<#assign degree  = value?floor>
				<#assign minutes = ((value - degree) * 60)?floor>
				<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
				<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
				
				<#assign signum  = (deflection.dx0 < 0)?then(-1, 1)>
				<#assign value   = signum * deflection.dx0>
				<#assign degree  = value?floor>
				<#assign minutes = ((value - degree) * 60)?floor>
				<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
				<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
			<#else>
				<td>${deflection.dy0?string[df_angle_residual]}</td>
				<td>${deflection.dx0?string[df_angle_residual]}</td>
			</#if>
			<#if group.type == "STOCHASTIC_POINT">
				<#if sexagesimal_angle_uncertainty??>
					<#assign signum  = (deflection.sigma_dy0 < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.sigma_dy0>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_uncertainty]?string[1..])}</td>
					
					<#assign signum  = (deflection.sigma_dx0 < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.sigma_dx0>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_uncertainty]?string[1..])}</td>
				<#else>
					<td>${deflection.sigma_dy0?string[df_angle_uncertainty]}</td>
					<td>${deflection.sigma_dx0?string[df_angle_uncertainty]}</td>
				</#if>
			</#if>
			
			<#if group.type != "REFERENCE_POINT">
				<#if sexagesimal_angle_residual??>
					<#assign signum  = (deflection.dy < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.dy>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
					
					<#assign signum  = (deflection.dx < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.dx>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
				<#else>
					<td>${deflection.dy?string[df_angle_residual]}</td>
					<td>${deflection.dx?string[df_angle_residual]}</td>
				</#if>
				
				<#if sexagesimal_angle_uncertainty??>
					<#assign signum  = (deflection.sigma_dy < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.sigma_dy>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_uncertainty]?string[1..])}</td>
					
					<#assign signum  = (deflection.sigma_dx < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.sigma_dx>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_uncertainty]?string[1..])}</td>
					
					<#assign signum  = (deflection.confidence_major_axis < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.confidence_major_axis>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_uncertainty]?string[1..])}</td>
					
					<#assign signum  = (deflection.confidence_minor_axis < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.confidence_minor_axis>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_uncertainty]?string[1..])}</td>
				<#else>	
					<td>${deflection.sigma_dy?string[df_angle_uncertainty]}</td>
					<td>${deflection.sigma_dx?string[df_angle_uncertainty]}</td>
					<td>${deflection.confidence_major_axis?string[df_angle_uncertainty]}</td>
					<td>${deflection.confidence_minor_axis?string[df_angle_uncertainty]}</td>
				</#if>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<td>${deflection.redundancy_dy?string[df_statistic]}</td>
				<td>${deflection.redundancy_dx?string[df_statistic]}</td>
				
				<#if sexagesimal_angle_residual??>
					<#assign signum  = (deflection.residual_dy < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.residual_dy>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
					
					<#assign signum  = (deflection.residual_dx < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.residual_dx>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
				<#else>
					<td>${deflection.residual_dy?string[df_angle_residual]}</td>
					<td>${deflection.residual_dx?string[df_angle_residual]}</td>
				</#if>
			</#if>
			
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<#if sexagesimal_angle_residual??>
					<#assign signum  = (deflection.gross_error_dy < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.gross_error_dy>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
					
					<#assign signum  = (deflection.gross_error_dx < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.gross_error_dx>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
				<#else>
					<td>${deflection.gross_error_dy?string[df_angle_residual]}</td>
					<td>${deflection.gross_error_dx?string[df_angle_residual]}</td>
				</#if>
			</#if>
						
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<#if sexagesimal_angle_residual??>
					<#assign signum  = (deflection.minimal_detectable_bias_dy < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.minimal_detectable_bias_dy>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
					
					<#assign signum  = (deflection.minimal_detectable_bias_dx < 0)?then(-1, 1)>
					<#assign value   = signum * deflection.minimal_detectable_bias_dx>
					<#assign degree  = value?floor>
					<#assign minutes = ((value - degree) * 60)?floor>
					<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
					<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
				<#else>
					<td>${deflection.minimal_detectable_bias_dy?string[df_angle_residual]}</td>
					<td>${deflection.minimal_detectable_bias_dx?string[df_angle_residual]}</td>
				</#if>
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