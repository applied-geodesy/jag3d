<tfoot>
	<tr>
		<td></td>
		
		<td></td>
		<td></td>
		
		<#if group.type == "STOCHASTIC_POINT">
			<td></td>
			<td></td>
		</#if>
		
		<#if group.type != "REFERENCE_POINT">
			<td></td>
			<td></td>	
			
			<td></td>
			<td></td>

			<td></td>
			<td></td>
		</#if>
		
		<#if group.type == "STOCHASTIC_POINT">
			<td>${group.redundancy_dy?string[df_statistic]}</td>
			<td>${group.redundancy_dx?string[df_statistic]}</td>
			
			<td></td>
			<td></td>
		</#if>
		
		<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
			<td>${group.max_gross_error_dy?string[df_angle_residual]}</td>
			<td>${group.max_gross_error_dx?string[df_angle_residual]}</td>
		</#if>
		
		<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
			<td></td>
			<td></td>
		</#if>
		
		<#if group.type == "STOCHASTIC_POINT">
			<td></td>
		</#if>
		
		<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
			<td></td>
			<td></td>
			
			<td></td>
			<td></td>
			
			<td></td>
		</#if>
	</tr>
</tfoot>