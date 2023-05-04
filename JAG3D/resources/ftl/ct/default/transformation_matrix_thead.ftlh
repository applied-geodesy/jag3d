<thead>
	<tr>
		<#list matrix as row>
			<th>
				<#if row.parameter??>
					<#outputformat "plainText">${row.parameter}</#outputformat>
				</#if>
			</th>
		</#list>
	</tr>
</thead>