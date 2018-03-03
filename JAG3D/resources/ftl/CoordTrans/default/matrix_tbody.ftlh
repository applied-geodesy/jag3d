<tbody>
	<#list matrix as row>
		<tr>
			<#if row.name??>
				<th><#outputformat "plainText">${row.name}</#outputformat></th>
			</#if>
			<#list row.data as value>
				<td>${value?string[df_matrix_element]}</td>
			</#list>
		</tr>
	</#list>
</tbody>