<thead>
	<tr>
		<th><#outputformat "plainText">${congruence_analysis_table_header_map['point_name_reference']}</#outputformat></th>
		<th><#outputformat "plainText">${congruence_analysis_table_header_map['point_name_control']}</#outputformat></th>
		
		<#if group.dimension != 1>
			<th><#outputformat "plainText">&delta;Y<span class="unit">${unit_abbr_length}</span></#outputformat></th>
			<th><#outputformat "plainText">&delta;X<span class="unit">${unit_abbr_length}</span></#outputformat></th>
		</#if>
		
		<#if group.dimension != 2>
			<th><#outputformat "plainText">&delta;Z<span class="unit">${unit_abbr_length}</span></#outputformat></th>
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

		<#if group.dimension != 1>
			<th><#outputformat "plainText">&nabla;<sub>Y</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;<sub>X</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>
		
		<#if group.dimension != 2>
			<th><#outputformat "plainText">&nabla;<sub>Z</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>

		<#if group.dimension != 1>
			<th><#outputformat "plainText">&nabla;<sub>Y</sub>(&alpha;,&beta;)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;<sub>X</sub>(&alpha;,&beta;)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>
		
		<#if group.dimension != 2>
			<th><#outputformat "plainText">&nabla;<sub>Z</sub>(&alpha;,&beta;)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>
		
		<th><#outputformat "plainText">log(p<sub>prio</sub>)</#outputformat></th>
		<th><#outputformat "plainText">log(p<sub>post</sub>)</#outputformat></th>
			
		<th><#outputformat "plainText">T<sub>prio</sub></#outputformat></th>
		<th><#outputformat "plainText">T<sub>post</sub></#outputformat></th>
			
		<th>T &le; &Phi;<sup>-1</sup> | H<sub>0</sub></th>
	</tr>
</thead>