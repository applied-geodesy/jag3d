	<section>
		<h2>Project</h2>
		<dl>
			<dt>JAG3D Version</dt><dd>v${version?string["#"]}</dd>
			<dt>Name of Project</dt><dd>${project_name}</dd>
			<dt>Project Id</dt><dd>${project_project_id}</dd>
			<dt>customer Id</dt><dd>${project_customer_id}</dd>
			<dt>Person in Charge</dt><dd>${project_operator}</dd>
			<dt>Date of Calculation</dt><dd>${project_date?date?string("dd.MM.yyyy")}</dd>
			
			
			<dt>Kind of network</dt>
			<dd>
				<#if rank_defect_count?? && rank_defect_count gt 0>
					free Network<#if congruence_analysis> &amp; Deformation Analysis</#if>
					<ul class="rank_defect">
						<#if rank_defect.ty?? && rank_defect.ty><li>t<sub>Y</sub></li></#if>
						<#if rank_defect.tx?? && rank_defect.tx><li>t<sub>X</sub></li></#if>
						<#if rank_defect.tz?? && rank_defect.tz><li>t<sub>Z</sub></li></#if>
						
						<#if rank_defect.ry?? && rank_defect.ry><li>r<sub>Y</sub></li></#if>
						<#if rank_defect.rx?? && rank_defect.rx><li>r<sub>X</sub></li></#if>
						<#if rank_defect.rz?? && rank_defect.rz><li>r<sub>Z</sub></li></#if>
						
						<#if rank_defect.sy?? && rank_defect.sy><li>s<sub>Y</sub></li></#if>
						<#if rank_defect.sx?? && rank_defect.sx><li>s<sub>X</sub></li></#if>
						<#if rank_defect.sz?? && rank_defect.sz><li>s<sub>Z</sub></li></#if>
						
						<#if rank_defect.my?? && rank_defect.my><li>m<sub>Y</sub></li></#if>
						<#if rank_defect.mx?? && rank_defect.mx><li>m<sub>X</sub></li></#if>
						<#if rank_defect.mz?? && rank_defect.mz><li>m<sub>Z</sub></li></#if>
						
						<#if rank_defect.mxy??  && rank_defect.mxy><li>m<sub>YX</sub></li></#if>
						<#if rank_defect.mxyz?? && rank_defect.mxyz><li>m<sub>YXZ</sub></li></#if>
					</ul>
				<#else>
					linked network
				</#if>
			</dd>
			
			<dt>Adjustment Type</dt>
			<dd>
				<#if estimation_type == 'L1NORM'>
					Robust estimator (L<sub>1</sub>Norm), c = ${bounded_influence?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}
				<#elseif estimation_type == 'L2NORM'>
					Least squares adjustment (L<sub>2</sub>Norm)
				<#elseif estimation_type == 'SIMULATION'>
					Pre-analyses (Network Simulation)
				</#if>
			</dd>
			
			<#if projection_type?? && projection_type != 'NONE'>
				<dt>Projection Type</dt>
				<dd>
					2D Projection
					<ul>
						<#if projection_direction_reduction?? && projection_direction_reduction><li>Direction Reduction</li></#if>
						<#if projection_height_reduction?? && projection_height_reduction><li>Height Reduction (H<sub>ref</sub> = ${projection_reference_height?string[df_length]} ${unit_abbr_length})</li></#if>
						<#if projection_gauss_krueger_reduction?? && projection_gauss_krueger_reduction><li>Gau&szlig;-Kr&uuml;ger Reduction</li></#if>
						<#if projection_utm_reduction?? && projection_utm_reduction><li>UTM Reduction</li></#if>
					</ul>
				</dd>
			</#if>

			<dt>Probability Value &alpha; [%]</dt><dd>${test_statistic_probability_value?string[df_statistic]}</dd>
			<dt>Test Power (1-&beta;) [%]</dt><dd>${test_statistic_power_of_test?string[df_statistic]}</dd>
		
			<dt>Description</dt>
			<dd><pre>${project_description}</pre></dd>
		</dl>
	</section>