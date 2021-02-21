<tbody>
	<#list group.points as point>
		<tr>
			<th>${point.name}</th>
			<th>${point.code}</th>
			
			<#if group.dimension != 1>
				<td><@printFormattedNumber value=point.y0 format=df_length/></td>
				<td><@printFormattedNumber value=point.x0 format=df_length/></td>
			</#if>
			
			<#if group.dimension != 2>
				<td><@printFormattedNumber value=point.z0 format=df_length/></td>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<#if group.dimension != 1>
					<td><@printFormattedNumber value=point.sigma_y0 format=df_length_uncertainty/></td>
					<td><@printFormattedNumber value=point.sigma_x0 format=df_length_uncertainty/></td>
				</#if>
			
				<#if group.dimension != 2>
					<td><@printFormattedNumber value=point.sigma_z0 format=df_length_uncertainty/></td>
				</#if>
			</#if>
			
			<#if group.type != "REFERENCE_POINT">
				<#if group.dimension != 1>
					<td><@printFormattedNumber value=point.y format=df_length/></td>
					<td><@printFormattedNumber value=point.x format=df_length/></td>
				</#if>
				
				<#if group.dimension != 2>
					<td><@printFormattedNumber value=point.z format=df_length/></td>
				</#if>			
				
				<#if group.dimension != 1>
					<td><@printFormattedNumber value=point.sigma_y format=df_length_uncertainty/></td>
					<td><@printFormattedNumber value=point.sigma_x format=df_length_uncertainty/></td>
				</#if>
				
				<#if group.dimension != 2>
					<td><@printFormattedNumber value=point.sigma_z format=df_length_uncertainty/></td>
				</#if>

				<td><@printFormattedNumber value=point.confidence_major_axis format=df_length_uncertainty/></td>
				<#if group.dimension != 1>
					<#if group.dimension == 3>
						<td><@printFormattedNumber value=point.confidence_middle_axis format=df_length_uncertainty/></td>
					</#if>
					<td><@printFormattedNumber value=point.confidence_minor_axis format=df_length_uncertainty/></td>
				</#if>
				
				<#if group.dimension == 3>
					<#if sexagesimal_angle??>
						<#assign signum  = (point.confidence_alpha < 0)?then(-1, 1)>
						<#assign value   = signum * point.confidence_alpha>
						<#assign degree  = value?floor>
						<#assign minutes = ((value - degree) * 60)?floor>
						<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
						<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle]?string[1..])}</td>
						
						<#assign signum  = (point.confidence_beta < 0)?then(-1, 1)>
						<#assign value   = signum * point.confidence_beta>
						<#assign degree  = value?floor>
						<#assign minutes = ((value - degree) * 60)?floor>
						<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
						<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle]?string[1..])}</td>
					<#else>
						<td><@printFormattedNumber value=point.confidence_alpha format=df_angle/></td>
						<td><@printFormattedNumber value=point.confidence_beta format=df_angle/></td>
					</#if>				
				</#if>
				
				<#if group.dimension != 1>
					<#if sexagesimal_angle??>
						<#assign signum  = (point.confidence_gamma < 0)?then(-1, 1)>
						<#assign value   = signum * point.confidence_gamma>
						<#assign degree  = value?floor>
						<#assign minutes = ((value - degree) * 60)?floor>
						<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
						<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle]?string[1..])}</td>
					<#else>
						<td><@printFormattedNumber value=point.confidence_gamma format=df_angle/></td>
					</#if>
				</#if>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<#if group.dimension != 1>
					<td><@printFormattedNumber value=point.redundancy_y format=df_percentage/></td>
					<td><@printFormattedNumber value=point.redundancy_x format=df_percentage/></td>
				</#if>
			
				<#if group.dimension != 2>
					<td><@printFormattedNumber value=point.redundancy_z format=df_percentage/></td>
				</#if>
			</#if>
			
			<#if group.type != "REFERENCE_POINT">
				<#if group.dimension != 1>
					<td><@printFormattedNumber value=point.residual_y format=df_length_residual/></td>
					<td><@printFormattedNumber value=point.residual_x format=df_length_residual/></td>
				</#if>
			
				<#if group.dimension != 2>
					<td><@printFormattedNumber value=point.residual_z format=df_length_residual/></td>
				</#if>
			</#if>
			
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<#if group.dimension != 1>
					<td><@printFormattedNumber value=point.gross_error_y format=df_length_residual/></td>
					<td><@printFormattedNumber value=point.gross_error_x format=df_length_residual/></td>
				</#if>
			
				<#if group.dimension != 2>
					<td><@printFormattedNumber value=point.gross_error_z format=df_length_residual/></td>
				</#if>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<#if group.dimension != 1>
					<td><@printFormattedNumber value=point.influence_on_position_y format=df_length_residual/></td>
					<td><@printFormattedNumber value=point.influence_on_position_x format=df_length_residual/></td>
				</#if>
			
				<#if group.dimension != 2>
					<td><@printFormattedNumber value=point.influence_on_position_z format=df_length_residual/></td>
				</#if>
				
				<td><@printFormattedNumber value=point.influence_on_network_distortion format=df_length_residual/></td>
			</#if>
			
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<#if group.dimension != 1>
					<td><@printFormattedNumber value=point.minimal_detectable_bias_y format=df_length_residual/></td>
					<td><@printFormattedNumber value=point.minimal_detectable_bias_x format=df_length_residual/></td>
				</#if>
			
				<#if group.dimension != 2>
					<td><@printFormattedNumber value=point.minimal_detectable_bias_z format=df_length_residual/></td>
				</#if>
			</#if>
			
			<#if group.type != "REFERENCE_POINT">
				<#if group.dimension != 1>
					<td><@printFormattedNumber value=point.first_principal_component_y format=df_length_residual/></td>
					<td><@printFormattedNumber value=point.first_principal_component_x format=df_length_residual/></td>
				</#if>
			
				<#if group.dimension != 2>
					<td><@printFormattedNumber value=point.first_principal_component_z format=df_length_residual/></td>
				</#if>
			</#if>
			
			<#if group.type == "STOCHASTIC_POINT">
				<td><@printFormattedNumber value=point.omega format=df_statistic/></td>
			</#if>
			
			<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
				<td><@printFormattedNumber value=point.p_prio format=df_statistic/></td>
				<td><@printFormattedNumber value=point.p_post format=df_statistic/></td>
				
				<td><@printFormattedNumber value=point.t_prio format=df_statistic/></td>
				<td><@printFormattedNumber value=point.t_post format=df_statistic/></td>
				
				<td><span class="${point.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${point.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
			</#if>
		</tr>
	</#list>
</tbody>