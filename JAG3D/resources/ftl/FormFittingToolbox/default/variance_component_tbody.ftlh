<tbody>
	<#list vce as vce_comp>
		<#if vce_type_map[vce_comp.type]?? >
			<tr>
				<th><#outputformat "plainText">${vce_type_map[vce_comp.type]}</#outputformat></th>
				<td>${vce_comp.number_of_points?string["#"]}</td>
				<td>${vce_comp.redundancy?string[df_redundancy]}</td>
				<td>${vce_comp.omega?string[df_omega]}</td>
				<td>${vce_comp.sigma2apost?string[df_teststatistic]}</td>
				<td>${vce_comp.k_prio?string[df_teststatistic]}</td>
				<td><span class="${(!vce_comp.t_prio?is_infinite && !vce_comp.k_prio?is_infinite && vce_comp.t_prio gt vce_comp.k_prio)?then('rejected', 'accepted')}"><#outputformat "plainText">${(!vce_comp.t_prio?is_infinite && !vce_comp.k_prio?is_infinite && vce_comp.t_prio gt vce_comp.k_prio)?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
			</tr>
		</#if>
	</#list>
</tbody>