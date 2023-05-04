<tbody>
	<#list position_pairs.positions as position>
		<tr>
			<th>${position.name}</th>
			
			<#if view == "SOURCE" || view == "TARGET">
				<#if position_pairs.dimension != 1>
					<td><@printFormattedNumber value=(view == "SOURCE")?then(position.x, position.X) format=df_length/></td>
					<td><@printFormattedNumber value=(view == "SOURCE")?then(position.y, position.Y) format=df_length/></td>
				</#if>
					
				<#if position_pairs.dimension != 2>
					<td><@printFormattedNumber value=(view == "SOURCE")?then(position.z, position.Z) format=df_length/></td>
				</#if>			
					
				<#if position_pairs.dimension != 1>
					<td><@printFormattedNumber value=(view == "SOURCE")?then(position.sigma_x, position.sigma_X) format=df_length_uncertainty/></td>
					<td><@printFormattedNumber value=(view == "SOURCE")?then(position.sigma_y, position.sigma_Y) format=df_length_uncertainty/></td>
				</#if>
					
				<#if position_pairs.dimension != 2>
					<td><@printFormattedNumber value=(view == "SOURCE")?then(position.sigma_z, position.sigma_Z) format=df_length_uncertainty/></td>
				</#if>
	
				<#if position_pairs.dimension != 1>
					<td><@printFormattedNumber value=(view == "SOURCE")?then(position.redundancy_x, position.redundancy_X) format=df_percentage/></td>
					<td><@printFormattedNumber value=(view == "SOURCE")?then(position.redundancy_y, position.redundancy_Y) format=df_percentage/></td>
				</#if>
				
				<#if position_pairs.dimension != 2>
					<td><@printFormattedNumber value=(view == "SOURCE")?then(position.redundancy_z, position.redundancy_Z) format=df_percentage/></td>
				</#if>
				
				<#if position_pairs.dimension != 1>
					<td><@printFormattedNumber value=(view == "SOURCE")?then(position.residual_x, position.residual_X) format=df_length_residual/></td>
					<td><@printFormattedNumber value=(view == "SOURCE")?then(position.residual_y, position.residual_Y) format=df_length_residual/></td>
				</#if>
				
				<#if position_pairs.dimension != 2>
					<td><@printFormattedNumber value=(view == "SOURCE")?then(position.residual_z, position.residual_Z) format=df_length_residual/></td>
				</#if>
				
			<#elseif view == "OUTLIER">	
				<#if position_pairs.dimension != 1>
					<td><@printFormattedNumber value=position.gross_error_x format=df_length_residual/></td>
					<td><@printFormattedNumber value=position.gross_error_y format=df_length_residual/></td>
				</#if>
				
				<#if position_pairs.dimension != 2>
					<td><@printFormattedNumber value=position.gross_error_z format=df_length_residual/></td>
				</#if>
				
				<#if position_pairs.dimension != 1>
					<td><@printFormattedNumber value=position.maximum_tolerable_bias_x format=df_length_residual/></td>
					<td><@printFormattedNumber value=position.maximum_tolerable_bias_y format=df_length_residual/></td>
				</#if>
				
				<#if position_pairs.dimension != 2>
					<td><@printFormattedNumber value=position.maximum_tolerable_bias_z format=df_length_residual/></td>
				</#if>
				
				<#if position_pairs.dimension != 1>
					<td><@printFormattedNumber value=position.minimal_detectable_bias_x format=df_length_residual/></td>
					<td><@printFormattedNumber value=position.minimal_detectable_bias_y format=df_length_residual/></td>
				</#if>
				
				<#if position_pairs.dimension != 2>
					<td><@printFormattedNumber value=position.minimal_detectable_bias_z format=df_length_residual/></td>
				</#if>
				
				<td><@printFormattedNumber value=position.p_prio format=df_statistic/></td>
				<td><@printFormattedNumber value=position.p_post format=df_statistic/></td>
					
				<td><@printFormattedNumber value=position.t_prio format=df_statistic/></td>
				<td><@printFormattedNumber value=position.t_post format=df_statistic/></td>
					
				<td><span class="${position.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${position.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>

			<#else> 
				<#if position_pairs.dimension != 1>
					<td><@printFormattedNumber value=(view == "TRANSFORMED")?then(position.x, position.x0) format=df_length/></td>
					<td><@printFormattedNumber value=(view == "TRANSFORMED")?then(position.y, position.y0) format=df_length/></td>
				</#if>
				
				<#if position_pairs.dimension != 2>
					<td><@printFormattedNumber value=(view == "TRANSFORMED")?then(position.z, position.z0) format=df_length/></td>
				</#if>
							
				<#if position_pairs.dimension != 1>
					<td><@printFormattedNumber value=(view == "TRANSFORMED")?then(position.X, position.X0) format=df_length/></td>
					<td><@printFormattedNumber value=(view == "TRANSFORMED")?then(position.Y, position.Y0) format=df_length/></td>
				</#if>
					
				<#if position_pairs.dimension != 2>
					<td><@printFormattedNumber value=(view == "TRANSFORMED")?then(position.Z, position.Z0) format=df_length/></td>
				</#if>
				
				<#if view == "TRANSFORMED">	
					<#if position_pairs.dimension != 1>
						<td><@printFormattedNumber value=position.sigma_X format=df_length_uncertainty/></td>
						<td><@printFormattedNumber value=position.sigma_Y format=df_length_uncertainty/></td>
					</#if>
						
					<#if position_pairs.dimension != 2>
						<td><@printFormattedNumber value=position.sigma_Z format=df_length_uncertainty/></td>
					</#if>
					
					<#if interpolation_type?? && interpolation_type != 'NONE'>
						<#if position_pairs.dimension != 1>
							<td><@printFormattedNumber value=position.residual_X format=df_length_residual/></td>
							<td><@printFormattedNumber value=position.residual_Y format=df_length_residual/></td>
						</#if>
						
						<#if position_pairs.dimension != 2>
							<td><@printFormattedNumber value=position.residual_Z format=df_length_residual/></td>
						</#if>
					</#if>
				</#if>
			</#if>

		</tr>
	</#list>
</tbody>