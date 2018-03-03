<tbody>
	<#list vce as vce_comp>
		<#if vce_type_map[vce_comp.type]?? && vce_comp.number_of_observations?? && vce_comp.number_of_observations gt 0 >
			<tr>
				<th><#outputformat "plainText">${vce_type_map[vce_comp.type]}</#outputformat></th>
				<td>${vce_comp.number_of_observations?string["#"]}</td>
				<td>${vce_comp.redundancy?string[df_statistic]}</td>
				<td>${vce_comp.omega?string[df_statistic]}</td>
				<td>${vce_comp.sigma2apost?string[df_statistic]}</td>
				<td>${vce_comp.quantile?string[df_statistic]}</td>
				<td><span class="${vce_comp.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${vce_comp.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
			</tr>
		</#if>
	</#list>
</tbody>