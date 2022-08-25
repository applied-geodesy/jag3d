<tfoot>
	<tr>
		<td></td>
		<td></td>

		<#if group.type != "GNSS1D" && group.type != "GNSS2D" && group.type != "GNSS3D">
			<td></td>
			<td></td>
		</#if>

		<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<td></td>
				<td></td>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<td></td>
			</#if>
		<#else>
			<td></td>
		</#if>
		
		<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<td></td>
				<td></td>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<td></td>
			</#if>
		<#else>
			<td></td>
		</#if>
		
		<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<td></td>
				<td></td>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<td></td>
			</#if>
		<#else>
			<td></td>
		</#if>
		
		<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<td></td>
				<td></td>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<td></td>
			</#if>

			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<td><@printFormattedNumber value=group.redundancy_y format=df_statistic/></td>
				<td><@printFormattedNumber value=group.redundancy_x format=df_statistic/></td>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<td><@printFormattedNumber value=group.redundancy_z format=df_statistic/></td>
			</#if>
			
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<td><@printFormattedNumber value=group.max_residual_y format=df_length_residual/></td>
				<td><@printFormattedNumber value=group.max_residual_x format=df_length_residual/></td>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<td><@printFormattedNumber value=group.max_residual_z format=df_length_residual/></td>
			</#if>
			
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<td><@printFormattedNumber value=group.max_gross_error_y format=df_length_residual/></td>
				<td><@printFormattedNumber value=group.max_gross_error_x format=df_length_residual/></td>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<td><@printFormattedNumber value=group.max_gross_error_z format=df_length_residual/></td>
			</#if>
			
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<td></td>
				<td></td>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<td></td>
			</#if>
			
			<td></td>
			
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<td></td>
				<td></td>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<td></td>
			</#if>
			
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<td></td>
				<td></td>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<td></td>
			</#if>
		<#else>
			<td></td>
			<td><@printFormattedNumber value=group.redundancy format=df_statistic/></td>
			<#if apply_angle_unit>
				<td><@printFormattedNumber value=group.max_residual format=df_angle_residual/></td>
			<#else>
				<td><@printFormattedNumber value=group.max_residual format=df_length_residual/></td>
			</#if>
			<#if apply_angle_unit>
				<td><@printFormattedNumber value=group.max_gross_error format=df_angle_residual/></td>
			<#else>
				<td><@printFormattedNumber value=group.max_gross_error format=df_length_residual/></td>
			</#if>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		</#if>
		
		<td><@printFormattedNumber value=group.omega format=df_statistic/></td>
		<td></td>
		<td></td>
		
		<td></td>
		<td></td>
		
		<td><span class="${group.significant?then('rejected', 'accepted')}"><#outputformat "plainText">${group.significant?then('&#x2718;', '&#x2714;')}</#outputformat></span></td>
	</tr>
</tfoot>