<thead>
	<tr>
		<th><#outputformat "plainText">${point_table_header_map['id']}</#outputformat></th>
		
		<#if feature_points.dimension != 1>
			<th><#outputformat "plainText">x<sub>0</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			<th><#outputformat "plainText">y<sub>0</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
		</#if>
		
		<#if feature_points.dimension != 2>
			<th><#outputformat "plainText">z<sub>0</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
		</#if>
		
		<#if feature_points.dimension != 1>
			<th><#outputformat "plainText">x<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			<th><#outputformat "plainText">y<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
		</#if>
		
		<#if feature_points.dimension != 2>
			<th><#outputformat "plainText">z<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
		</#if>

		<#if feature_points.dimension != 1>
			<th><#outputformat "plainText">&sigma;<sub>x</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			<th><#outputformat "plainText">&sigma;<sub>y</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
		</#if>
			
		<#if feature_points.dimension != 2>
			<th><#outputformat "plainText">&sigma;<sub>z</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
		</#if>

		<#if feature_points.dimension != 1>
			<th><#outputformat "plainText">r<sub>y</sub><span class="unit">${unit_abbr_percentage}</span></#outputformat></th>
			<th><#outputformat "plainText">r<sub>x</sub><span class="unit">${unit_abbr_percentage}</span></#outputformat></th>
		</#if>
		
		<#if feature_points.dimension != 2>
			<th><#outputformat "plainText">r<sub>z</sub><span class="unit">${unit_abbr_percentage}</span></#outputformat></th>
		</#if>
		
		<#if feature_points.dimension != 1>
			<th><#outputformat "plainText">&epsilon;<sub>x</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&epsilon;<sub>y</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>
			
		<#if feature_points.dimension != 2>
			<th><#outputformat "plainText">&epsilon;<sub>z</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>

		<#if feature_points.dimension != 1>
			<th><#outputformat "plainText">&nabla;<sub>x</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;<sub>y</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>
		
		<#if feature_points.dimension != 2>
			<th><#outputformat "plainText">&nabla;<sub>z</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>
		
		<#if feature_points.dimension != 1>
			<th><#outputformat "plainText">&nabla;<sub>x</sub>(1)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;<sub>y</sub>(1)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>
		
		<#if feature_points.dimension != 2>
			<th><#outputformat "plainText">&nabla;<sub>z</sub>(1)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>
		
		<#if feature_points.dimension != 1>
			<th><#outputformat "plainText">&nabla;<sub>x</sub>(&lambda;)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			<th><#outputformat "plainText">&nabla;<sub>y</sub>(&lambda;)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>
		
		<#if feature_points.dimension != 2>
			<th><#outputformat "plainText">&nabla;<sub>z</sub>(&lambda;)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
		</#if>

		<th><#outputformat "plainText">log(p<sub>prio</sub>)</#outputformat></th>
		<th><#outputformat "plainText">log(p<sub>post</sub>)</#outputformat></th>
			
		<th><#outputformat "plainText">T<sub>prio</sub></#outputformat></th>
		<th><#outputformat "plainText">T<sub>post</sub></#outputformat></th>
			
		<th>T &le; q | H<sub>0</sub></th>

	</tr>
</thead>