	<section>
		<h2>Project</h2>
		<dl>
			<dt>JAG3D Version</dt><dd>v${version?string["#"]}</dd>
			<dt>Name of Project</dt><dd>${project_name}</dd>
			<dt>Project Id</dt><dd>${project_project_id}</dd>
			<dt>Customer Id</dt><dd>${project_customer_id}</dd>
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
				<#elseif estimation_type == 'MODIFIED_UNSCENTED_TRANSFORMATION'>
					Modified Unscented Transformation
				<#elseif estimation_type == 'SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION'>
					Spherical Simplex Unscented Transformation
				<#elseif estimation_type == 'SIMULATION'>
					Diagnosis adjustment (Simulation)
				</#if>
			</dd>
			
			<#if projection_type??>
				<dt>Coordinate frame</dt>
				<dd>
					<#if projection_type == 'LOCAL_ELLIPSOIDAL'>
						Local ellipsoidal Earth model using Cartesian coordinates
					<#elseif projection_type == 'GAUSS_KRUEGER'>
						Gau&szlig;-Kr&uuml;ger projection
					<#elseif projection_type == 'UTM'>
						Universal Transverse Mercator (UTM) projection
					<#elseif projection_type == 'LOCAL_CARTESIAN'>
						Local Cartesian model
					</#if>
						
					<ul>
						<#if (projection_type != 'LOCAL_ELLIPSOIDAL' && reduction_earth_curvature?? && reduction_earth_curvature || reduction_height?? && reduction_height || reduction_distance?? && reduction_distance || reduction_direction?? && reduction_direction) && projection_earth_radius??><li>Earth radius: R<sub>0</sub> = ${projection_earth_radius?string[df_length]}&nbsp;${unit_abbr_length}</li></#if>
						<#if (reduction_height?? && reduction_height && projection_type != 'LOCAL_ELLIPSOIDAL') && projection_reference_height??><li>Reference height: h<sub>0</sub> = ${projection_reference_height?string[df_length]}&nbsp;${unit_abbr_length}</li></#if>
						
						<#if projection_type == 'LOCAL_ELLIPSOIDAL'>
							<#if projection_principal_point_x0?? && projection_principal_point_y0?? && projection_principal_point_z0??><li>Local principal point: y<sub>0</sub> = ${projection_principal_point_y0?string[df_length]}&nbsp;${unit_abbr_length}, x<sub>0</sub> = ${projection_principal_point_x0?string[df_length]}&nbsp;${unit_abbr_length}, z<sub>0</sub> = ${projection_principal_point_z0?string[df_length]}&nbsp;${unit_abbr_length}</li></#if>
							<#if projection_reference_latitude?? && projection_reference_longitude?? && projection_reference_height??><li>Global principal point: &phi;<sub>0</sub> = ${projection_reference_latitude?string[df_angle]}&nbsp;${unit_abbr_angle}, &lambda;<sub>0</sub> = ${projection_reference_longitude?string[df_angle]}&nbsp;${unit_abbr_angle}, h<sub>0</sub> = ${projection_reference_height?string[df_length]}&nbsp;${unit_abbr_length}</li></#if>
							<#if projection_major_axis?? && projection_minor_axis??><li>Reference ellipsoid: a = ${projection_major_axis?string[df_length]}&nbsp;${unit_abbr_length}, b = ${projection_minor_axis?string[df_length]}&nbsp;${unit_abbr_length}</li></#if>
						</#if>

						<#if reduction_distance?? && reduction_distance && (projection_type == 'GAUSS_KRUEGER' || projection_type == 'UTM')><li>Distance reduction</li></#if>
						<#if reduction_direction?? && reduction_direction && (projection_type == 'GAUSS_KRUEGER' || projection_type == 'UTM')><li>Direction reduction</li></#if>

						<#if reduction_height?? && reduction_height><li>Height reduction</li></#if>
						<#if reduction_earth_curvature?? && reduction_earth_curvature><li>Earth's curvature reduction</li></#if>
					</ul>
				</dd>
			</#if>

			<dt><#outputformat "plainText">Probability Value &alpha;</#outputformat></dt><dd>${test_statistic_probability_value?string[df_percentage]}&nbsp;${unit_abbr_percentage}</dd>
			<dt><#outputformat "plainText">Test Power (1-&beta;)</#outputformat></dt><dd>${test_statistic_power_of_test?string[df_percentage]}&nbsp;${unit_abbr_percentage}</dd>
		
			<dt>Description</dt>
			<dd><pre>${project_description}</pre></dd>
		</dl>
	</section>