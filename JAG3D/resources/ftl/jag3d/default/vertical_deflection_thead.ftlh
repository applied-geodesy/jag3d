<thead>
	<tr>
		<th><#outputformat "plainText">${vertical_deflection_table_header_map['id']}</#outputformat></th>
		
		<th><#outputformat "plainText">&zeta;y0<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
		<th><#outputformat "plainText">&zeta;x0<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
		
		<#if group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">&sigma;<sub>y0</sub><span class="unit">${unit_abbr_angle_uncertainty}</span></#outputformat></th>
			<th><#outputformat "plainText">&sigma;<sub>x0</sub><span class="unit">${unit_abbr_angle_uncertainty}</span></#outputformat></th>
		</#if>
		
		<#if group.type != "REFERENCE_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">&zeta;y<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&zeta;x<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>	
			
			<th><#outputformat "plainText">&sigma;<sub>y</sub><span class="unit">${unit_abbr_angle_uncertainty}</span></#outputformat></th>
			<th><#outputformat "plainText">&sigma;<sub>x</sub><span class="unit">${unit_abbr_angle_uncertainty}</span></#outputformat></th>

			<th><#outputformat "plainText">a<span class="unit">${unit_abbr_angle_uncertainty}</span></#outputformat></th>
			<th><#outputformat "plainText">b<span class="unit">${unit_abbr_angle_uncertainty}</span></#outputformat></th>
		</#if>
		
		<#if group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">r<sub>y</sub><span class="unit">${unit_abbr_percentage}</span></#outputformat></th>
			<th><#outputformat "plainText">r<sub>x</sub><span class="unit">${unit_abbr_percentage}</span></#outputformat></th>
		</#if>
		
		<#if group.type != "REFERENCE_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">${(group.type == "STOCHASTIC_VERTICAL_DEFLECTION")?string("&epsilon;", "&Delta;")}<sub>y</sub></#outputformat></th>
			<th><#outputformat "plainText">${(group.type == "STOCHASTIC_VERTICAL_DEFLECTION")?string("&epsilon;", "&Delta;")}<sub>x</sub></#outputformat></th>
		</#if>
		
		<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">&nabla;<sub>y</sub><span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;<sub>x</sub><span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
		</#if>
		
		<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">&nabla;<sub>y</sub>(1)<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;<sub>x</sub>(1)<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
		</#if>
		
		<#if group.type == "REFERENCE_VERTICAL_DEFLECTION" || group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
			<th><#outputformat "plainText">&nabla;<sub>y</sub>(&lambda;)<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;<sub>x</sub>(&lambda;)<span class="unit">${unit_abbr_angle_residual}</span></#outputformat></th>
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