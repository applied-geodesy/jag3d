<tbody>
	<#list group as observation>
		<#if observation.type?? && observation.type == observation_type>
			<tr>
				<th>${observation.group_name}</th>

				<th>
					<#if observation.type == 'LEVELING'>
						<#outputformat "plainText">${observation_type_map['levelling']}</#outputformat>
					<#elseif observation.type == 'DIRECTION'>
						<#outputformat "plainText">${observation_type_map['direction']}</#outputformat>
					<#elseif observation.type == 'HORIZONTAL_DISTANCE'>
						<#outputformat "plainText">${observation_type_map['horizontal_distance']}</#outputformat>
					<#elseif observation.type == 'SLOPE_DISTANCE'>
						<#outputformat "plainText">${observation_type_map['slope_distance']}</#outputformat>
					<#elseif observation.type == 'ZENITH_ANGLE'>
						<#outputformat "plainText">${observation_type_map['zenith_angle']}</#outputformat>
					</#if>
				</th>
				
				<td>${observation.start_point_name}</td>
				<td>${observation.end_point_name}</td>

				<td><@printFormattedNumber value=observation.value format=df_format/></td>
				<td><@printFormattedNumber value=observation.average format=df_format/></td>
			</tr>
		</#if>
	</#list>
</tbody>


