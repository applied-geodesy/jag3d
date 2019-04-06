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
			<td><@printFormattedNumber value=group.redundancy_dy format=df_statistic/></td>
			<td><@printFormattedNumber value=group.redundancy_dx format=df_statistic/></td>
			
			<td></td>
			<td></td>
		</#if>
		
		<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
			<#if sexagesimal_angle_residual??>
				<#assign signum  = (group.max_gross_error_dy < 0)?then(-1, 1)>
				<#assign value   = signum * group.max_gross_error_dy>
				<#assign degree  = value?floor>
				<#assign minutes = ((value - degree) * 60)?floor>
				<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
				<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
				
				<#assign signum  = (group.max_gross_error_dx < 0)?then(-1, 1)>
				<#assign value   = signum * group.max_gross_error_dx>
				<#assign degree  = value?floor>
				<#assign minutes = ((value - degree) * 60)?floor>
				<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
				<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
			<#else>
				<td><@printFormattedNumber value=group.max_gross_error_dy format=df_angle_residual/></td>
				<td><@printFormattedNumber value=group.max_gross_error_dx format=df_angle_residual/></td>
			</#if>
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