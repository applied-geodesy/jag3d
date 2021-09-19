<tfoot>
	<tr>
		<td></td>
		
		<td></td>
		<td></td>
		
		<#if group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<td></td>
			<td></td>
		</#if>
		
		<#if group.type != "REFERENCE_VERTICAL_DEFLECTION">
			<td></td>
			<td></td>	
			
			<td></td>
			<td></td>

			<td></td>
			<td></td>
		</#if>
		
		<#if group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<td><@printFormattedNumber value=group.redundancy_y format=df_percentage/></td>
			<td><@printFormattedNumber value=group.redundancy_x format=df_percentage/></td>
			
			<#if sexagesimal_angle_residual??>
				<#assign signum  = (group.max_residual_y < 0)?then(-1, 1)>
				<#assign value   = signum * group.max_residual_y>
				<#assign degree  = value?floor>
				<#assign minutes = ((value - degree) * 60)?floor>
				<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
				<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
				
				<#assign signum  = (group.max_residual_x < 0)?then(-1, 1)>
				<#assign value   = signum * group.max_residual_x>
				<#assign degree  = value?floor>
				<#assign minutes = ((value - degree) * 60)?floor>
				<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
				<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
			<#else>
				<td><@printFormattedNumber value=group.max_residual_y format=df_angle_residual/></td>
				<td><@printFormattedNumber value=group.max_residual_x format=df_angle_residual/></td>
			</#if>
		</#if>
		
		<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<#if sexagesimal_angle_residual??>
				<#assign signum  = (group.max_gross_error_y < 0)?then(-1, 1)>
				<#assign value   = signum * group.max_gross_error_y>
				<#assign degree  = value?floor>
				<#assign minutes = ((value - degree) * 60)?floor>
				<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
				<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
				
				<#assign signum  = (group.max_gross_error_x < 0)?then(-1, 1)>
				<#assign value   = signum * group.max_gross_error_x>
				<#assign degree  = value?floor>
				<#assign minutes = ((value - degree) * 60)?floor>
				<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
				<td>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[df_angle_residual]?string[1..])}</td>
			<#else>
				<td><@printFormattedNumber value=group.max_gross_error_y format=df_angle_residual/></td>
				<td><@printFormattedNumber value=group.max_gross_error_x format=df_angle_residual/></td>
			</#if>
		</#if>
		
		<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<td></td>
			<td></td>
		</#if>
		
		<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<td></td>
			<td></td>
		</#if>
		
		<#if group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<td><@printFormattedNumber value=group.omega format=df_statistic/></td>
		</#if>
		
		<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<td></td>
			<td></td>
			
			<td></td>
			<td></td>
			
			<td></td>
		</#if>
	</tr>
</tfoot>