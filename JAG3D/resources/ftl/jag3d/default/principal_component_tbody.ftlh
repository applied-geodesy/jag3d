<tbody>
	<#list principal_components as component>
		<tr>
			<td>${component.index?string["#"]}</td>
			<td><@printFormattedNumber value=component.value format=df_length_residual/></td>
			<td><@printFormattedNumber value=component.ratio format=df_percentage/></td>
		</tr>
	</#list>
</tbody>