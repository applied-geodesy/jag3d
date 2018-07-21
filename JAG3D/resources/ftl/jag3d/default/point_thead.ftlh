<thead>
	<tr>
		<th><#outputformat "plainText">${point_table_header_map['id']}</#outputformat></th>
		<th><#outputformat "plainText">${point_table_header_map['code']}</#outputformat></th>
		
		<#if group.dimension != 1>
			<th><#outputformat "plainText">${point_table_header_map['y']}0<span class="unit">${unit_abbr_length}</span></#outputformat></th>
			<th><#outputformat "plainText">${point_table_header_map['x']}0<span class="unit">${unit_abbr_length}</span></#outputformat></th>
		</#if>
		
		<#if group.dimension != 2>
			<th><#outputformat "plainText">${point_table_header_map['z']}0<span class="unit">${unit_abbr_length}</span></#outputformat></th>
		</#if>
		
		<#if group.type == "STOCHASTIC_POINT">
			<#if group.dimension != 1>
				<th><#outputformat "plainText">&sigma;<sub>Y0</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
				<th><#outputformat "plainText">&sigma;<sub>X0</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
		
			<#if group.dimension != 2>
				<th><#outputformat "plainText">&sigma;<sub>Z0</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
		</#if>
		
		<#if group.type != "REFERENCE_POINT">
			<#if group.dimension != 1>
				<th><#outputformat "plainText">${point_table_header_map['y']}<span class="unit">${unit_abbr_length}</span></#outputformat></th>
				<th><#outputformat "plainText">${point_table_header_map['x']}<span class="unit">${unit_abbr_length}</span></#outputformat></th>
			</#if>
			
			<#if group.dimension != 2>
				<th><#outputformat "plainText">${point_table_header_map['z']}<span class="unit">${unit_abbr_length}</span></#outputformat></th>
			</#if>			
			
			<#if group.dimension != 1>
				<th><#outputformat "plainText">&sigma;<sub>Y</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
				<th><#outputformat "plainText">&sigma;<sub>X</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
			
			<#if group.dimension != 2>
				<th><#outputformat "plainText">&sigma;<sub>Z</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>

			<th><#outputformat "plainText">A<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			<#if group.dimension != 1>
				<#if group.dimension == 3>
					<th><#outputformat "plainText">B<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
				</#if>
				<th><#outputformat "plainText">C<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
			
			<#if group.dimension == 3>
				<th><#outputformat "plainText">&alpha;<span class="unit">${unit_abbr_angle}</span></#outputformat></th>
				<th><#outputformat "plainText">&beta;<span class="unit">${unit_abbr_angle}</span></#outputformat></th>
			</#if>
			
			<#if group.dimension != 1>
				<th><#outputformat "plainText">&gamma;<span class="unit">${unit_abbr_angle}</span></#outputformat></th>
			</#if>
		</#if>
		
		<#if group.type == "STOCHASTIC_POINT">
			<#if group.dimension != 1>
				<th><#outputformat "plainText">r<sub>Y</sub></#outputformat></th>
				<th><#outputformat "plainText">r<sub>X</sub></#outputformat></th>
			</#if>
		
			<#if group.dimension != 2>
				<th><#outputformat "plainText">r<sub>Z</sub></#outputformat></th>
			</#if>
		</#if>
		
		<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
			<#if group.dimension != 1>
				<th><#outputformat "plainText">&nabla;<sub>Y</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
				<th><#outputformat "plainText">&nabla;<sub>X</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
		
			<#if group.dimension != 2>
				<th><#outputformat "plainText">&nabla;<sub>Z</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
		</#if>
		
		<#if group.type == "STOCHASTIC_POINT">
			<#if group.dimension != 1>
				<th><#outputformat "plainText">EP<sub>Y</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
				<th><#outputformat "plainText">EP<sub>X</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
		
			<#if group.dimension != 2>
				<th><#outputformat "plainText">EP<sub>Z</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
			
			<th><#outputformat "plainText">EP&middot;SP<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>
		
		<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
			<#if group.dimension != 1>
				<th><#outputformat "plainText">&nabla;<sub>Y</sub>(&alpha;,&beta;)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
				<th><#outputformat "plainText">&nabla;<sub>X</sub>(&alpha;,&beta;)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
		
			<#if group.dimension != 2>
				<th><#outputformat "plainText">&nabla;<sub>Z</sub>(&alpha;,&beta;)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
		</#if>
		
		<#if group.type != "REFERENCE_POINT">
			<#if group.dimension != 1>
				<th><#outputformat "plainText">&varsigma;<sub>Y</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
				<th><#outputformat "plainText">&varsigma;<sub>X</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
		
			<#if group.dimension != 2>
				<th><#outputformat "plainText">&varsigma;<sub>Z</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
		</#if>
		
		<#if group.type == "STOCHASTIC_POINT">
			<th><#outputformat "plainText">&Omega;</#outputformat></th>
		</#if>
		
		<#if group.type == "REFERENCE_POINT" || group.type == "STOCHASTIC_POINT" || (congruence_analysis && group.type == "DATUM_POINT")>
			<th><#outputformat "plainText">log(p<sub>prio</sub>)</#outputformat></th>
			<th><#outputformat "plainText">log(p<sub>post</sub>)</#outputformat></th>
			
			<th><#outputformat "plainText">T<sub>prio</sub></#outputformat></th>
			<th><#outputformat "plainText">T<sub>post</sub></#outputformat></th>
			
			<th>T &le; q | H<sub>0</sub></th>
		</#if>
	</tr>
</thead>