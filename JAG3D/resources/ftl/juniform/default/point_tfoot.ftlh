<tfoot>
	<tr>
		<td></td>
		
		<#if feature_points.dimension != 1>
			<td></td>
			<td></td>
		</#if>
		
		<#if feature_points.dimension != 2>
			<td></td>
		</#if>
		
		<#if feature_points.dimension != 1>
			<td></td>
			<td></td>
		</#if>
		
		<#if feature_points.dimension != 2>
			<td></td>
		</#if>
		
		<#if feature_points.dimension != 1>
			<td></td>
			<td></td>
		</#if>
		
		<#if feature_points.dimension != 2>
			<td></td>
		</#if>
		
		<#if feature_points.dimension != 1>
			<td><@printFormattedNumber value=feature_points.redundancy_x format=df_statistic/></td>
			<td><@printFormattedNumber value=feature_points.redundancy_y format=df_statistic/></td>
		</#if>
		
		<#if feature_points.dimension != 2>
			<td><@printFormattedNumber value=feature_points.redundancy_z format=df_statistic/></td>
		</#if>
			
		<#if feature_points.dimension != 1>
			<td></td>
			<td></td>
		</#if>
		
		<#if feature_points.dimension != 2>
			<td></td>
		</#if>

		<#if feature_points.dimension != 1>
			<td><@printFormattedNumber value=feature_points.max_gross_error_x format=df_length_residual/></td>
			<td><@printFormattedNumber value=feature_points.max_gross_error_y format=df_length_residual/></td>
		</#if>
		
		<#if feature_points.dimension != 2>
			<td><@printFormattedNumber value=feature_points.max_gross_error_z format=df_length_residual/></td>
		</#if>
			
		<td></td>
		<td></td>
			
		<td></td>
		<td></td>
			
		<td><span class="${feature_points.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${feature_points.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>

	</tr>
</tfoot>