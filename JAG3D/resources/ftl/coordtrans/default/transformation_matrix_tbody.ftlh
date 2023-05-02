<tbody>
	<#list matrix as row>
		<tr>
			<#list row.data as value>
				<td>${value?string[df_double]}</td>
			</#list>
		</tr>
	</#list>
</tbody>