<thead>
	<tr>
		<th><#outputformat "plainText">${observation_table_header_map['station_id']}</#outputformat></th>
		<th><#outputformat "plainText">${observation_table_header_map['target_id']}</#outputformat></th>

		<#if group.type != "GNSS1D" && group.type != "GNSS2D" && group.type != "GNSS3D">
			<th><#outputformat "plainText">${observation_table_header_map['ih']}<span class="unit">${unit_abbr_length}</span></#outputformat></th>
			<th><#outputformat "plainText">${observation_table_header_map['th']}<span class="unit">${unit_abbr_length}</span></#outputformat></th>
		</#if>
		
		<#if group.type == "LEVELING">
			<th><#outputformat "plainText">${observation_table_header_map['levelling']}0<span class="unit">${unit_abbr_length}</span></#outputformat></th>
		<#elseif group.type == "DIRECTION">
			<th><#outputformat "plainText">${observation_table_header_map['direction']}0<span class="unit">${unit_abbr_angle}</span></#outputformat></th>
		<#elseif group.type == "HORIZONTAL_DISTANCE">
			<th><#outputformat "plainText">${observation_table_header_map['horizontal_distance']}0<span class="unit">${unit_abbr_length}</span></#outputformat></th>
		<#elseif group.type == "SLOPE_DISTANCE">
			<th><#outputformat "plainText">${observation_table_header_map['slope_distance']}0<span class="unit">${unit_abbr_length}</span></#outputformat></th>
		<#elseif group.type == "ZENITH_ANGLE">
			<th><#outputformat "plainText">${observation_table_header_map['zenith_angle']}0<span class="unit">${unit_abbr_angle}</span></#outputformat></th>
		<#elseif group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">${observation_table_header_map['gnss_dy']}0<span class="unit">${unit_abbr_length}</span></#outputformat></th>
				<th><#outputformat "plainText">${observation_table_header_map['gnss_dx']}0<span class="unit">${unit_abbr_length}</span></#outputformat></th>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">${observation_table_header_map['gnss_dz']}0<span class="unit">${unit_abbr_length}</span></#outputformat></th>
			</#if>
		</#if>
		
		<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">&sigma;<sub>Y0</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
				<th><#outputformat "plainText">&sigma;<sub>X0</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">&sigma;<sub>Z0</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
		<#else>
			<th><#outputformat "plainText">&sigma;0<span class="unit">${apply_angle_unit?then(unit_abbr_angle_uncertainty, unit_abbr_length_uncertainty)}</span></#outputformat></th>
		</#if>
		
		<#if group.type == "LEVELING">
			<th><#outputformat "plainText">${observation_table_header_map['levelling']}<span class="unit">${unit_abbr_length}</span></#outputformat></th>
		<#elseif group.type == "DIRECTION">
			<th><#outputformat "plainText">${observation_table_header_map['direction']}<span class="unit">${unit_abbr_angle}</span></#outputformat></th>
		<#elseif group.type == "HORIZONTAL_DISTANCE">
			<th><#outputformat "plainText">${observation_table_header_map['horizontal_distance']}<span class="unit">${unit_abbr_length}</span></#outputformat></th>
		<#elseif group.type == "SLOPE_DISTANCE">
			<th><#outputformat "plainText">${observation_table_header_map['slope_distance']}<span class="unit">${unit_abbr_length}</span></#outputformat></th>
		<#elseif group.type == "ZENITH_ANGLE">
			<th><#outputformat "plainText">${observation_table_header_map['zenith_angle']}<span class="unit">${unit_abbr_angle}</span></#outputformat></th>
		<#elseif group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">${observation_table_header_map['gnss_dy']}<span class="unit">${unit_abbr_length}</span></#outputformat></th>
				<th><#outputformat "plainText">${observation_table_header_map['gnss_dx']}<span class="unit">${unit_abbr_length}</span></#outputformat></th>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">${observation_table_header_map['gnss_dz']}<span class="unit">${unit_abbr_length}</span></#outputformat></th>
			</#if>
		</#if>
		
		<#if group.type == "GNSS1D" || group.type == "GNSS2D" || group.type == "GNSS3D">
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">&sigma;<sub>Y</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
				<th><#outputformat "plainText">&sigma;<sub>X</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">&sigma;<sub>Z</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
			
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">r<sub>Y</sub></#outputformat></th>
				<th><#outputformat "plainText">r<sub>X</sub></#outputformat></th>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">r<sub>Z</sub></#outputformat></th>
			</#if>
			
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">&nabla;<sub>Y</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
				<th><#outputformat "plainText">&nabla;<sub>X</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">&nabla;<sub>Z</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
			
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">EP<sub>Y</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
				<th><#outputformat "plainText">EP<sub>X</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">EP<sub>Z</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
			
			<th><#outputformat "plainText">EP&middot;SP<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			
			<#if group.type == "GNSS2D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">&nabla;(&alpha;,&beta;)<sub>Y</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
				<th><#outputformat "plainText">&nabla;(&alpha;,&beta;)<sub>X</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
			<#if group.type == "GNSS1D" || group.type == "GNSS3D">
				<th><#outputformat "plainText">&nabla;(&alpha;,&beta;)<sub>Z</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
		<#else>
			<th><#outputformat "plainText">&sigma;<span class="unit">${apply_angle_unit?then(unit_abbr_angle_uncertainty, unit_abbr_length_uncertainty)}</span></#outputformat></th>
			<th><#outputformat "plainText">r</#outputformat></th>
			<th><#outputformat "plainText">&nabla;<span class="unit">${apply_angle_unit?then(unit_abbr_angle_residual, unit_abbr_length_residual)}</span></#outputformat></th>
			<th><#outputformat "plainText">EP<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">EF&#183;SP<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;(&alpha;,&beta;)<span class="unit">${apply_angle_unit?then(unit_abbr_angle_residual, unit_abbr_length_residual)}</span></#outputformat></th>
		</#if>
		
		<th><#outputformat "plainText">&Omega;</#outputformat></th>
		<th><#outputformat "plainText">log(p<sub>prio</sub>)</#outputformat></th>
		<th><#outputformat "plainText">log(p<sub>post</sub>)</#outputformat></th>
		
		<th><#outputformat "plainText">T<sub>prio</sub></#outputformat></th>
		<th><#outputformat "plainText">T<sub>post</sub></#outputformat></th>
		
		<th>T &le; &Phi;<sup>-1</sup> | H<sub>0</sub></th>
	</tr>
</thead>