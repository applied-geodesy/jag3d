<tfoot>
	<tr>
		<td></td>
		<td></td>
		
		<#if group.dimension != 1>
			<td></td>
			<td></td>
		</#if>
		
		<#if group.dimension != 2>
			<td></td>
		</#if>
		
		<#if group.dimension != 1>
			<td></td>
			<td></td>
		</#if>
			
		<#if group.dimension != 2>
			<td></td>
		</#if>

		<td></td>
		<#if group.dimension != 1>
			<#if group.dimension == 3>
				<td></td>
			</#if>
			<td></td>
		</#if>
			
		<#if group.dimension == 3>
			<td></td>
			<td></td>
		</#if>
			
		<#if group.dimension != 1>
			<td></td>
		</#if>

		<#if group.dimension != 1>
			<td></td>
			<td></td>
		</#if>
		
		<#if group.dimension != 2>
			<td></td>
		</#if>

		<#if group.dimension != 1>
			<td></td>
			<td></td>
		</#if>
		
		<#if group.dimension != 2>
			<td></td>
		</#if>
		
		<td></td>
		<td></td>
			
		<td></td>
		<td></td>
			
		<td><span class="${group.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${group.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
	</tr>
</tfoot>