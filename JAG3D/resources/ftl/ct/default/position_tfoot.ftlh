<tfoot>
	<tr>
		<td></td>
		<#if view == "SOURCE" || view == "TARGET">
			<#if position_pairs.dimension != 1>
				<td></td>
				<td></td>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<td></td>
			</#if>
			
			<#if position_pairs.dimension != 1>
				<td></td>
				<td></td>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<td></td>
			</#if>
			
			<#if position_pairs.dimension != 1>
				<td><@printFormattedNumber value=(view == "SOURCE")?then(position_pairs.redundancy_x, position_pairs.redundancy_X) format=df_statistic/></td>
				<td><@printFormattedNumber value=(view == "SOURCE")?then(position_pairs.redundancy_y, position_pairs.redundancy_Y) format=df_statistic/></td>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<td><@printFormattedNumber value=(view == "SOURCE")?then(position_pairs.redundancy_z, position_pairs.redundancy_Z) format=df_statistic/></td>
			</#if>
				
			<#if position_pairs.dimension != 1>
				<td><@printFormattedNumber value=(view == "SOURCE")?then(position_pairs.max_residual_x, position_pairs.max_residual_X) format=df_length_residual/></td>
				<td><@printFormattedNumber value=(view == "SOURCE")?then(position_pairs.max_residual_y, position_pairs.max_residual_Y) format=df_length_residual/></td>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<td><@printFormattedNumber value=(view == "SOURCE")?then(position_pairs.max_residual_z, position_pairs.max_residual_Z) format=df_length_residual/></td>
			</#if>
		
		<#elseif view == "OUTLIER">		
			
			<#if position_pairs.dimension != 1>
				<td><@printFormattedNumber value=position_pairs.max_gross_error_x format=df_length_residual/></td>
				<td><@printFormattedNumber value=position_pairs.max_gross_error_y format=df_length_residual/></td>
			</#if>	
		
			<#if position_pairs.dimension != 2>
				<td><@printFormattedNumber value=position_pairs.max_gross_error_z format=df_length_residual/></td>
			</#if>
			
			<#if position_pairs.dimension != 1>
				<td></td>
				<td></td>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<td></td>
			</#if>
			
			<#if position_pairs.dimension != 1>
				<td></td>
				<td></td>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<td></td>
			</#if>
				
			<td></td>
			<td></td>
				
			<td></td>
			<td></td>
				
			<td><span class="${position_pairs.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${position_pairs.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
		
		<#else>
			<#if position_pairs.dimension != 1>
				<td></td>
				<td></td>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<td></td>
			</#if>
			
			<#if position_pairs.dimension != 1>
				<td></td>
				<td></td>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<td></td>
			</#if>
			
			<#if view == "TRANSFORMED">	
				<#if position_pairs.dimension != 1>
					<td></td>
					<td></td>
				</#if>
					
				<#if position_pairs.dimension != 2>
					<td></td>
				</#if>
					
				<#if interpolation_type?? && interpolation_type != 'NONE'>
					<#if position_pairs.dimension != 1>
						<td></td>
						<td></td>
					</#if>
					
					<#if position_pairs.dimension != 2>
						<td></td>
					</#if>
				</#if>
			</#if>
		</#if>

	</tr>
</tfoot>