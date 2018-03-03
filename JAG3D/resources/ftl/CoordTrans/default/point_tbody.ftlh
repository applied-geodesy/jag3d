<tbody>
	<#list group as point>
		<tr>
			<th>${point.point_id}</th>
			
			<#if dimension != 1>
				<td>${point.x0?string[df_length]}</td>
				<td>${point.y0?string[df_length]}</td>
			</#if>
			
			<#if dimension != 2>
				<td>${point.z0?string[df_length]}</td>
			</#if>
			
			<#if dimension != 1>
				<td>${point.x?string[df_length]}</td>
				<td>${point.y?string[df_length]}</td>
			</#if>
			
			<#if dimension != 2>
				<td>${point.z?string[df_length]}</td>
			</#if>			
			
			<#if dimension != 1>
				<td>${point.sigma_x?string[df_length_uncertainty]}</td>
				<td>${point.sigma_y?string[df_length_uncertainty]}</td>
			</#if>
			
			<#if dimension != 2>
				<td>${point.sigma_z?string[df_length_uncertainty]}</td>
			</#if>
			
			<#if extended>
				<#if dimension != 1>
					<td>${point.redundancy_x?string[df_redundancy]}</td>
					<td>${point.redundancy_y?string[df_redundancy]}</td>
				</#if>
			
				<#if dimension != 2>
					<td>${point.redundancy_z?string[df_redundancy]}</td>
				</#if>

				
				<#if dimension != 1>
					<td>${point.epsilon_x?string[df_length_residual]}</td>
					<td>${point.epsilon_y?string[df_length_residual]}</td>
				</#if>
				
				<#if dimension != 2>
					<td>${point.epsilon_z?string[df_length_residual]}</td>
				</#if>
				
				<#if dimension != 1>
					<td>${point.nabla_x?string[df_length_gross_error]}</td>
					<td>${point.nabla_y?string[df_length_gross_error]}</td>
				</#if>
			
				<#if dimension != 2>
					<td>${point.nabla_z?string[df_length_gross_error]}</td>
				</#if>

				<td>${point.omega?string[df_omega]}</td>
				
				<td>${point.t_prio?string[df_teststatistic]}</td>
				<td>${point.t_post?string[df_teststatistic]}</td>
				
				<td><span class="${point.outlier?then('rejected', 'accepted')}"><#outputformat "plainText">${point.outlier?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
			</#if>
		</tr>
	</#list>
</tbody>