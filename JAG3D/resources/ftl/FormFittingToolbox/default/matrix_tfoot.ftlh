<tfoot>
	<tr>
		<#list matrix as row>
			<td>
				<#if row?is_first && row.name??>
					</td><td>
				</#if>
			</td>
		</#list>
	</tr>
</tfoot>