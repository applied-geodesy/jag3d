<thead>
	<tr>
		<th><#outputformat "plainText">${vertical_deflection_table_header_map['id']}</#outputformat></th>
		
		<th><#outputformat "plainText">&zeta;Y0<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
		<th><#outputformat "plainText">&zeta;X0<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
		
		<#if group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">&sigma;<sub>Y0</sub><span class="unit">${unit_abbr_angle_uncertainty}</span></#outputformat></th>
			<th><#outputformat "plainText">&sigma;<sub>X0</sub><span class="unit">${unit_abbr_angle_uncertainty}</span></#outputformat></th>
		</#if>
		
		<#if group.type != "REFERENCE_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">&zeta;Y<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&zeta;X<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>	
			
			<th><#outputformat "plainText">&sigma;<sub>Y</sub><span class="unit">${unit_abbr_angle_uncertainty}</span></#outputformat></th>
			<th><#outputformat "plainText">&sigma;<sub>X</sub><span class="unit">${unit_abbr_angle_uncertainty}</span></#outputformat></th>

			<th><#outputformat "plainText">a<span class="unit">${unit_abbr_angle_uncertainty}</span></#outputformat></th>
			<th><#outputformat "plainText">b<span class="unit">${unit_abbr_angle_uncertainty}</span></#outputformat></th>
		</#if>
		
		<#if group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">r<sub>Y</sub><span class="unit">${unit_abbr_percentage}</span></#outputformat></th>
			<th><#outputformat "plainText">r<sub>X</sub><span class="unit">${unit_abbr_percentage}</span></#outputformat></th>
			
			<th><#outputformat "plainText">&epsilon;<sub>Y</sub></#outputformat></th>
			<th><#outputformat "plainText">&epsilon;<sub>X</sub></#outputformat></th>
		</#if>
		
		<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">&nabla;<sub>Y</sub><span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;<sub>X</sub><span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
		</#if>
		
		<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">&nabla;<sub>Y</sub>(1)<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;<sub>X</sub>(1)<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
		</#if>
		
		<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">&nabla;<sub>Y</sub>(&lambda;)<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;<sub>X</sub>(&lambda;)<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
		</#if>
		
		<#if group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">&Omega;</#outputformat></th>
		</#if>
		
		<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">log(p<sub>prio</sub>)</#outputformat></th>
			<th><#outputformat "plainText">log(p<sub>post</sub>)</#outputformat></th>
			
			<th><#outputformat "plainText">T<sub>prio</sub></#outputformat></th>
			<th><#outputformat "plainText">T<sub>post</sub></#outputformat></th>
			
			<th>T &le; q | H<sub>0</sub></th>
		</#if>
	</tr>
</thead>