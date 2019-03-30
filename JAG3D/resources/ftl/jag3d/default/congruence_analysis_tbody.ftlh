<tbody>
	<#list group.point_pairs as point_pair>
		<tr>
			<th>${point_pair.start_point_name}</th>
			<th>${point_pair.end_point_name}</th>
			
			<#if group.dimension != 1>
				<td>${point_pair.y?string[df_length]}</td>
				<td>${point_pair.x?string[df_length]}</td>
			</#if>
			
			<#if group.dimension != 2>
				<td>${point_pair.z?string[df_length]}</td>
			</#if>
			
			<#if group.dimension != 1>
				<td>${point_pair.sigma_y?string[df_length_uncertainty]}</td>
				<td>${point_pair.sigma_x?string[df_length_uncertainty]}</td>
			</#if>
			
			<#if group.dimension != 2>
				<td>${point_pair.sigma_z?string[df_length_uncertainty]}</td>
			</#if>
			
			<td>${point_pair.confidence_major_axis?string[df_length_uncertainty]}</td>
			<#if group.dimension != 1>
				<#if group.dimension == 3>
					<td>${point_pair.confidence_middle_axis?string[df_length_uncertainty]}</td>
				</#if>
				<td>${point_pair.confidence_minor_axis?string[df_length_uncertainty]}</td>
			</#if>
				
			<#if group.dimension == 3>
				<#if sexagesimal_angle??>
					<#assign degree  = point_pair.confidence_alpha?floor>
					<#assign minutes = ((point_pair.confidence_alpha - degree) * 60)?floor>
					<#assign seconds = ((((point_pair.confidence_alpha - degree) * 60) - minutes) * 60)>
					<td>${degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle]?string[1..])}</td>

					<#assign degree  = point_pair.confidence_beta?floor>
					<#assign minutes = ((point_pair.confidence_beta - degree) * 60)?floor>
					<#assign seconds = ((((point_pair.confidence_beta - degree) * 60) - minutes) * 60)>
					<td>${degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle]?string[1..])}</td>
				<#else>
					<td>${point_pair.confidence_alpha?string[df_angle]}</td>
					<td>${point_pair.confidence_beta?string[df_angle]}</td>
				</#if>
			</#if>
				
			<#if group.dimension != 1>
				<#if sexagesimal_angle??>
					<#assign degree  = point_pair.confidence_gamma?floor>
					<#assign minutes = ((point_pair.confidence_gamma - degree) * 60)?floor>
					<#assign seconds = ((((point_pair.confidence_gamma - degree) * 60) - minutes) * 60)>
					<td>${degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle]?string[1..])}</td>
				<#else>
					<td>${point_pair.confidence_gamma?string[df_angle]}</td>
				</#if>
			</#if>
			
			<#if group.dimension != 1>
				<td>${point_pair.gross_error_y?string[df_length_residual]}</td>
				<td>${point_pair.gross_error_x?string[df_length_residual]}</td>
			</#if>
			
			<#if group.dimension != 2>
				<td>${point_pair.gross_error_z?string[df_length_residual]}</td>
			</#if>

			<#if group.dimension != 1>
				<td>${point_pair.minimal_detectable_bias_y?string[df_length_residual]}</td>
				<td>${point_pair.minimal_detectable_bias_x?string[df_length_residual]}</td>
			</#if>
			
			<#if group.dimension != 2>
				<td>${point_pair.minimal_detectable_bias_z?string[df_length_residual]}</td>
			</#if>
			
			<td>${point_pair.p_prio?string[df_statistic]}</td>
			<td>${point_pair.p_post?string[df_statistic]}</td>
				
			<td>${point_pair.t_prio?string[df_statistic]}</td>
			<td>${point_pair.t_post?string[df_statistic]}</td>
				
			<td><span class="${point_pair.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${point_pair.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
		</tr>
	</#list>
</tbody>