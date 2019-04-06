<tbody>
	<#list vce as vce_comp>
		<#if vce_type_map[vce_comp.type]?? && vce_comp.number_of_observations?? && vce_comp.number_of_observations gt 0 >
			<tr>
				<th><#outputformat "plainText">${vce_type_map[vce_comp.type]}</#outputformat></th>
				<td>${vce_comp.number_of_observations?string["#"]}</td>
				<td><@printFormattedNumber value=vce_comp.redundancy format=df_statistic/></td>
				<td><@printFormattedNumber value=vce_comp.omega format=df_statistic/></td>
				<td><@printFormattedNumber value=vce_comp.sigma2apost format=df_statistic/></td>
				<td><@printFormattedNumber value=vce_comp.quantile format=df_statistic/></td>
				<td><span class="${vce_comp.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${vce_comp.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
			</tr>
		</#if>
	</#list>
</tbody>