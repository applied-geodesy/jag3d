<thead>
	<tr>
		<th><#outputformat "plainText">${position_table_header_map['id']}</#outputformat></th>
		
		<#if view == "SOURCE" || view == "TARGET">
			<#if position_pairs.dimension != 1>
				<th><#outputformat "plainText">${(view == "SOURCE")?then('x', 'X')}<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
				<th><#outputformat "plainText">${(view == "SOURCE")?then('y', 'Y')}<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<th><#outputformat "plainText">${(view == "SOURCE")?then('z', 'Z')}<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
	
			<#if position_pairs.dimension != 1>
				<th><#outputformat "plainText">&sigma;<sub>${(view == "SOURCE")?then('x', 'X')}</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
				<th><#outputformat "plainText">&sigma;<sub>${(view == "SOURCE")?then('y', 'Y')}</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
				
			<#if position_pairs.dimension != 2>
				<th><#outputformat "plainText">&sigma;<sub>${(view == "SOURCE")?then('z', 'Z')}</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
	
			<#if position_pairs.dimension != 1>
				<th><#outputformat "plainText">r<sub>${(view == "SOURCE")?then('x', 'X')}</sub><span class="unit">${unit_abbr_percentage}</span></#outputformat></th>
				<th><#outputformat "plainText">r<sub>${(view == "SOURCE")?then('y', 'Y')}</sub><span class="unit">${unit_abbr_percentage}</span></#outputformat></th>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<th><#outputformat "plainText">r<sub>${(view == "SOURCE")?then('z', 'Z')}</sub><span class="unit">${unit_abbr_percentage}</span></#outputformat></th>
			</#if>
			
			<#if position_pairs.dimension != 1>
				<th><#outputformat "plainText">&epsilon;<sub>${(view == "SOURCE")?then('x', 'X')}</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
				<th><#outputformat "plainText">&epsilon;<sub>${(view == "SOURCE")?then('y', 'Y')}</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
				
			<#if position_pairs.dimension != 2>
				<th><#outputformat "plainText">&epsilon;<sub>${(view == "SOURCE")?then('z', 'Z')}</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
			
		<#elseif view == "OUTLIER">
			<#if position_pairs.dimension != 1>
				<th><#outputformat "plainText">&nabla;<sub>x</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
				<th><#outputformat "plainText">&nabla;<sub>y</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<th><#outputformat "plainText">&nabla;<sub>z</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
			
			<#if position_pairs.dimension != 1>
				<th><#outputformat "plainText">&nabla;<sub>x</sub>(1)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
				<th><#outputformat "plainText">&nabla;<sub>y</sub>(1)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<th><#outputformat "plainText">&nabla;<sub>z</sub>(1)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
			
			<#if position_pairs.dimension != 1>
				<th><#outputformat "plainText">&nabla;<sub>x</sub>(&lambda;)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
				<th><#outputformat "plainText">&nabla;<sub>y</sub>(&lambda;)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<th><#outputformat "plainText">&nabla;<sub>z</sub>(&lambda;)<span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
			</#if>
	
			<th><#outputformat "plainText">log(p<sub>prio</sub>)</#outputformat></th>
			<th><#outputformat "plainText">log(p<sub>post</sub>)</#outputformat></th>
				
			<th><#outputformat "plainText">T<sub>prio</sub></#outputformat></th>
			<th><#outputformat "plainText">T<sub>post</sub></#outputformat></th>
				
			<th>T &le; q | H<sub>0</sub></th>
			
		<#else>
			<#if position_pairs.dimension != 1>
				<th><#outputformat "plainText">x${(view == "TRANSFORMED")?then('', 'x<sub>0</sub>')}<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
				<th><#outputformat "plainText">y${(view == "TRANSFORMED")?then('', 'y<sub>0</sub>')}<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<th><#outputformat "plainText">z${(view == "TRANSFORMED")?then('', 'x<sub>0</sub>')}<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
			
			<#if position_pairs.dimension != 1>
				<th><#outputformat "plainText">X${(view == "TRANSFORMED")?then('', 'x<sub>0</sub>')}<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
				<th><#outputformat "plainText">Y${(view == "TRANSFORMED")?then('', 'x<sub>0</sub>')}<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
			
			<#if position_pairs.dimension != 2>
				<th><#outputformat "plainText">Z${(view == "TRANSFORMED")?then('', 'x<sub>0</sub>')}<span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
			</#if>
			
			<#if view == "TRANSFORMED">	
				<#if position_pairs.dimension != 1>
					<th><#outputformat "plainText">&sigma;<sub>X</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
					<th><#outputformat "plainText">&sigma;<sub>Y</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
				</#if>
						
				<#if position_pairs.dimension != 2>
					<th><#outputformat "plainText">&sigma;<sub>Z</sub><span class="unit">${unit_abbr_length_uncertainty}</span></#outputformat></th>
				</#if>
				
				<#if position_pairs.interpolation??>	
					<#if position_pairs.dimension != 1>
						<th><#outputformat "plainText">&epsilon;<sub>X</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
						<th><#outputformat "plainText">&epsilon;<sub>Y</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
					</#if>
							
					<#if position_pairs.dimension != 2>
						<th><#outputformat "plainText">&epsilon;<sub>Z</sub><span class="unit">${unit_abbr_length_residual}</span></#outputformat></th>
					</#if>
				</#if>
			</#if>
		</#if>

	</tr>
</thead>