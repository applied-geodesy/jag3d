<tbody>
	<#list correlation_matrix as row>
		<tr>
			<#list row.data as value>
				<td>${value?string[df_statistic]}</td>
			</#list>
		</tr>
	</#list>
</tbody>