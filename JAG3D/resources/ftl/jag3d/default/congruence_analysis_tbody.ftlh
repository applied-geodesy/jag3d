<tbody>
	<#list group.point_pairs as point_pair>
		<tr>
			<th>${point_pair.start_point_name}</th>
			<th>${point_pair.end_point_name}</th>
			
			<#if group.dimension != 1>
				<td><@printFormattedNumber value=point_pair.y format=df_length/></td>
				<td><@printFormattedNumber value=point_pair.x format=df_length/></td>
			</#if>
			
			<#if group.dimension != 2>
				<td><@printFormattedNumber value=point_pair.z format=df_length/></td>
			</#if>
			
			<#if group.dimension != 1>
				<td><@printFormattedNumber value=point_pair.sigma_y format=df_length_uncertainty/></td>
				<td><@printFormattedNumber value=point_pair.sigma_x format=df_length_uncertainty/></td>
			</#if>
			
			<#if group.dimension != 2>
				<td><@printFormattedNumber value=point_pair.sigma_z format=df_length_uncertainty/></td>
			</#if>
			
			<td><@printFormattedNumber value=point_pair.confidence_major_axis format=df_length_uncertainty/></td>
			<#if group.dimension != 1>
				<#if group.dimension == 3>
					<td><@printFormattedNumber value=point_pair.confidence_middle_axis format=df_length_uncertainty/></td>
				</#if>
				<td><@printFormattedNumber value=point_pair.confidence_minor_axis format=df_length_uncertainty/></td>
			</#if>
				
			<#if group.dimension == 3>
				<td><@printFormattedNumber value=point_pair.confidence_alpha format=df_angle/></td>
				<td><@printFormattedNumber value=point_pair.confidence_beta format=df_angle/></td>
			</#if>
				
			<#if group.dimension != 1>
				<td><@printFormattedNumber value=point_pair.confidence_gamma format=df_angle/></td>
			</#if>
			
			<#if group.dimension != 1>
				<td><@printFormattedNumber value=point_pair.gross_error_y format=df_length_residual/></td>
				<td><@printFormattedNumber value=point_pair.gross_error_x format=df_length_residual/></td>
			</#if>
			
			<#if group.dimension != 2>
				<td><@printFormattedNumber value=point_pair.gross_error_z format=df_length_residual/></td>
			</#if>

			<#if group.dimension != 1>
				<td><@printFormattedNumber value=point_pair.minimal_detectable_bias_y format=df_length_residual/></td>
				<td><@printFormattedNumber value=point_pair.minimal_detectable_bias_x format=df_length_residual/></td>
			</#if>
			
			<#if group.dimension != 2>
				<td><@printFormattedNumber value=point_pair.minimal_detectable_bias_z format=df_length_residual/></td>
			</#if>
			
			<td><@printFormattedNumber value=point_pair.p_prio format=df_statistic/></td>
			<td><@printFormattedNumber value=point_pair.p_post format=df_statistic/></td>
				
			<td><@printFormattedNumber value=point_pair.t_prio format=df_statistic/></td>
			<td><@printFormattedNumber value=point_pair.t_post format=df_statistic/></td>
				
			<td><span class="${point_pair.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${point_pair.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
		</tr>
	</#list>
</tbody>