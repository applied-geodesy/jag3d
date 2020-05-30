<tbody>
	<#list feature_points.points as point>
		<tr>
			<th>${point.name}</th>
			
			<#if feature_points.dimension != 1>
				<td><@printFormattedNumber value=point.x0 format=df_length/></td>
				<td><@printFormattedNumber value=point.y0 format=df_length/></td>
			</#if>
			
			<#if feature_points.dimension != 2>
				<td><@printFormattedNumber value=point.z0 format=df_length/></td>
			</#if>
						
			<#if feature_points.dimension != 1>
				<td><@printFormattedNumber value=point.x format=df_length/></td>
				<td><@printFormattedNumber value=point.y format=df_length/></td>
			</#if>
				
			<#if feature_points.dimension != 2>
				<td><@printFormattedNumber value=point.z format=df_length/></td>
			</#if>			
				
			<#if feature_points.dimension != 1>
				<td><@printFormattedNumber value=point.sigma_x format=df_length_uncertainty/></td>
				<td><@printFormattedNumber value=point.sigma_y format=df_length_uncertainty/></td>
			</#if>
				
			<#if feature_points.dimension != 2>
				<td><@printFormattedNumber value=point.sigma_z format=df_length_uncertainty/></td>
			</#if>

			<#if feature_points.dimension != 1>
				<td><@printFormattedNumber value=point.redundancy_x format=df_statistic/></td>
				<td><@printFormattedNumber value=point.redundancy_y format=df_statistic/></td>
			</#if>
			
			<#if feature_points.dimension != 2>
				<td><@printFormattedNumber value=point.redundancy_z format=df_statistic/></td>
			</#if>
			
			<#if feature_points.dimension != 1>
				<td><@printFormattedNumber value=point.residual_x format=df_length_residual/></td>
				<td><@printFormattedNumber value=point.residual_y format=df_length_residual/></td>
			</#if>
			
			<#if feature_points.dimension != 2>
				<td><@printFormattedNumber value=point.residual_z format=df_length_residual/></td>
			</#if>
			
			<#if feature_points.dimension != 1>
				<td><@printFormattedNumber value=point.gross_error_x format=df_length_residual/></td>
				<td><@printFormattedNumber value=point.gross_error_y format=df_length_residual/></td>
			</#if>
			
			<#if feature_points.dimension != 2>
				<td><@printFormattedNumber value=point.gross_error_z format=df_length_residual/></td>
			</#if>
			
			<td><@printFormattedNumber value=point.p_prio format=df_statistic/></td>
			<td><@printFormattedNumber value=point.p_post format=df_statistic/></td>
				
			<td><@printFormattedNumber value=point.t_prio format=df_statistic/></td>
			<td><@printFormattedNumber value=point.t_post format=df_statistic/></td>
				
			<td><span class="${point.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${point.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>

		</tr>
	</#list>
</tbody>