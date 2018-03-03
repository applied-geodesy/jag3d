<thead>
	<tr>
		<th><#outputformat "plainText">${point_table_header_map['id']}</#outputformat></th>
		
		<#if dimension != 1>
			<th><#outputformat "plainText">${point_table_header_map['x']}0<span class="unit">${unit_abbr_map['metre']}</span></#outputformat></th>
			<th><#outputformat "plainText">${point_table_header_map['y']}0<span class="unit">${unit_abbr_map['metre']}</span></#outputformat></th>
		</#if>
		
		<#if dimension != 2>
			<th><#outputformat "plainText">${point_table_header_map['z']}0<span class="unit">${unit_abbr_map['metre']}</span></#outputformat></th>
		</#if>
		
		<#if dimension != 1>
			<th><#outputformat "plainText">${point_table_header_map['x']}<span class="unit">${unit_abbr_map['metre']}</span></#outputformat></th>
			<th><#outputformat "plainText">${point_table_header_map['y']}<span class="unit">${unit_abbr_map['metre']}</span></#outputformat></th>
		</#if>
			
		<#if dimension != 2>
			<th><#outputformat "plainText">${point_table_header_map['z']}<span class="unit">${unit_abbr_map['metre']}</span></#outputformat></th>
		</#if>			
		
		<#if dimension != 1>
			<th><#outputformat "plainText">&sigma;<sub>X</sub><span class="unit">${unit_abbr_map['millimetre']}</span></#outputformat></th>
			<th><#outputformat "plainText">&sigma;<sub>Y</sub><span class="unit">${unit_abbr_map['millimetre']}</span></#outputformat></th>
		</#if>
			
		<#if dimension != 2>
			<th><#outputformat "plainText">&sigma;<sub>Z</sub><span class="unit">${unit_abbr_map['millimetre']}</span></#outputformat></th>
		</#if>
			
		<#if dimension != 1>
			<th><#outputformat "plainText">r<sub>X</sub></#outputformat></th>
			<th><#outputformat "plainText">r<sub>Y</sub></#outputformat></th>
		</#if>
		
		<#if dimension != 2>
			<th><#outputformat "plainText">r<sub>Z</sub></#outputformat></th>
		</#if>
	
		<#if dimension != 1>
			<th><#outputformat "plainText">&epsilon;<sub>X</sub><span class="unit">${unit_abbr_map['millimetre']}</span></#outputformat></th>
			<th><#outputformat "plainText">&epsilon;<sub>Y</sub><span class="unit">${unit_abbr_map['millimetre']}</span></#outputformat></th>
		</#if>
			
		<#if dimension != 2>
			<th><#outputformat "plainText">&epsilon;<sub>Z</sub><span class="unit">${unit_abbr_map['millimetre']}</span></#outputformat></th>
		</#if>
	
		<#if dimension != 1>
			<th><#outputformat "plainText">&nabla;<sub>X</sub><span class="unit">${unit_abbr_map['millimetre']}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;<sub>Y</sub><span class="unit">${unit_abbr_map['millimetre']}</span></#outputformat></th>
		</#if>
		
		<#if dimension != 2>
			<th><#outputformat "plainText">&nabla;<sub>Z</sub><span class="unit">${unit_abbr_map['millimetre']}</span></#outputformat></th>
		</#if>
	
		<th><#outputformat "plainText">&Omega;</#outputformat></th>
			
		<th><#outputformat "plainText">T<sub>prio</sub></#outputformat></th>
		<th><#outputformat "plainText">T<sub>post</sub></#outputformat></th>
			
		<th>T &le; &Phi;<sup>-1</sup> | H<sub>0</sub></th>
	</tr>
</thead>