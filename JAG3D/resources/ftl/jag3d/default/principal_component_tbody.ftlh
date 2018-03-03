<tbody>
	<#list principal_components as component>
		<tr>
			<td>${component.index?string["#"]}</td>
			<td>${component.value?string[df_length_residual]}</td>
			<td>${component.ratio?string[df_statistic]}</td>
		</tr>
	</#list>
</tbody>