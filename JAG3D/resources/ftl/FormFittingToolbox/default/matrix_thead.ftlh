<thead>
	<tr>
		<#list matrix as row>
			<th>
				<#if row.name??>
					<#if row?is_first>
						#</th><th>
					</#if>
					<#outputformat "plainText">${row.name}</#outputformat>
				</#if>
			</th>
		</#list>
	</tr>
</thead>