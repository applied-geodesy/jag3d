<tbody>
	<#list euler_angles_conventions as value>
		<tr>
			<th><#outputformat "plainText">${value.convention}</#outputformat></th>
			<td>${value.phi?string[df_angle]}</td>
			<td>${value.theta?string[df_angle]}</td>
			<td>${value.psi?string[df_angle]}</td>
		</tr>
	</#list>
</tbody>