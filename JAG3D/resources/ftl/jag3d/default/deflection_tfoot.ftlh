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
			<td>${group.redundancy_dy?string[df_redundancy]}</td>
			<td>${group.redundancy_dx?string[df_redundancy]}</td>
		</#if>
		
		<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
			<td></td>
			<td></td>
		</#if>
		
		<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
			<td>${group.max_nabla_dy?string[df_angle_gross_error]}</td>
			<td>${group.max_nabla_dx?string[df_angle_gross_error]}</td>
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