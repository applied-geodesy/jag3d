	<section>
		<h2>Allgemeines</h2>
		<dl>
			<dt>JAG3D Version</dt><dd>v${version?string["#"]}</dd>
			<dt>Name des Projektes</dt><dd>${project_name}</dd>
			<dt>Projektnummer</dt><dd>${project_project_id}</dd>
			<dt>Auftraggeber</dt><dd>${project_customer_id}</dd>
			<dt>Projektbearbeiter</dt><dd>${project_operator}</dd>
			<dt>Bearbeitungsdatum</dt><dd>${project_date?date?string("dd.MM.yyyy")}</dd>
			
			<dt>Art des Netzanschlu&szlig;es</dt>
			<dd>
				<#if rank_defect_count?? && rank_defect_count gt 0>
					freies Netz<#if congruence_analysis> &amp; Deformationsanalyse</#if>
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
					angeschlo&szlig;enes Netz
				</#if>
			</dd>
			
			<dt>Ausgleichungsmodus</dt>
			<dd>
				<#if estimation_type == 'L1NORM'>
					Robuste Parametersch&auml;tzung (L<sub>1</sub>Norm), c = ${robust_estimation_limit?string[df_statistic]}
				<#elseif estimation_type == 'L2NORM'>
					Methode der kleinsten Quadrate (L<sub>2</sub>Norm)
				<#elseif estimation_type == 'UNSCENTED_TRANSFORMATION'>
					Unscented Transformation
				<#elseif estimation_type == 'SIMULATION'>
					Netzplanung (Pr&auml;analyse)
				</#if>
			</dd>
			
			<#if projection_type??>
				<dt>Beobachtungsreduktionen</dt>
				<dd>
					<!--
					<ul>
						<#if projection_earth_radius??><li>Erdradius ${projection_earth_radius?string[df_length]} ${unit_abbr_length}</li></#if>
						<#if projection_reference_height??><li>Referenzh&ouml;he ${projection_reference_height?string[df_length]} ${unit_abbr_length}</li></#if>
					</ul>
					-->
					<ul>
						<#if reduction_distance?? && reduction_distance && projection_type == 'GAUSS_KRUEGER'><li>Gau&szlig;-Kr&uuml;ger Streckenreduktion</li></#if>
						<#if reduction_direction?? && reduction_direction && projection_type == 'GAUSS_KRUEGER'><li>Gau&szlig;-Kr&uuml;ger Richtungsreduktion</li></#if>
						
						<#if reduction_distance?? && reduction_distance && projection_type == 'UTM'><li>UTM Streckenreduktion</li></#if>
						<#if reduction_direction?? && reduction_direction && projection_type == 'UTM'><li>UTM Richtungsreduktion</li></#if>
						
						<#if reduction_height?? && reduction_height><li>H&ouml;henreduktion</li></#if>
						<#if reduction_earth_curvature?? && reduction_earth_curvature><li>Erdkr&uuml;mmungsreduktion</li></#if>
					</ul>
				</dd>
			</#if>

			<dt>Irrtumswahrscheinlichkeit &alpha; [%]</dt><dd>${test_statistic_probability_value?string[df_statistic]}</dd>
			<dt>Macht des Tests (1-&beta;) [%]</dt><dd>${test_statistic_power_of_test?string[df_statistic]}</dd>

			<dt>Kurzbeschreibung</dt>
			<dd><pre>${project_description}</pre></dd>
		</dl>
		
		
		
	</section>