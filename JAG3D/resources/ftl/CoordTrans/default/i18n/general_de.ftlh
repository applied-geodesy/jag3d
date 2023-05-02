	<section>
		<h2>Allgemeines</h2>
		<dl>
			<dt>CoordTrans Version</dt><dd>v${version?string["#"]}</dd>
			<dt>Bearbeitungsdatum</dt><dd>${report_creation_date?date?string("dd.MM.yyyy")}</dd>
			
			<dt>Ausgleichungsmodus</dt>
			<dd>
				<#if estimation_type == 'L2NORM'>
					Methode der kleinsten Quadrate (L<sub>2</sub>Norm)
				</#if>
			</dd>
			
			<#if interpolation_type??>
				<dt>Verteilung von Restabweichungen</dt>
				<dd>
					<#if interpolation_type == 'NONE'>
						Keine nachbarschaftstreue Anpassung
					<#elseif interpolation_type == 'INVERSE_DISTANCE_WEIGHTING'>
						Inverse Distanzgewichtung
						<ul>
							<li>Exponent: ${interpolation_idw_exponent?string[df_double]}</li>
							<li>Gl&auml;ttung: ${interpolation_idw_smoothing?string[df_double]}</li>
						</ul>
					<#elseif interpolation_type == 'MULTI_QUADRATIC'>
						Multiquadratische Interpolation
						<ul>
							<li>Exponent: ${interpolation_mq_exponent?string[df_double]}</li>
							<li>Gl&auml;ttung: ${interpolation_mq_smoothing?string[df_double]}</li>
						</ul>
					<#elseif interpolation_type == 'SECTOR'>
						Sektorenmethode (Winkel- und Abstandsgewichtung)
						<ul>
							<li>Exponent im Z&auml;hler: ${interpolation_sect_numerator_exponent?string[df_double]}</li>
							<li>Exponent im Nenner: ${interpolation_sect_denominator_exponent?string[df_double]}</li>
						</ul>
					</#if>
				</dd>
			</#if>
			
			<dt><#outputformat "plainText">Irrtumswahrscheinlichkeit &alpha;</#outputformat></dt><dd>${test_statistic_probability_value?string[df_percentage]}&nbsp;${unit_abbr_percentage}</dd>
			<dt><#outputformat "plainText">Macht des Tests (1-&beta;)</#outputformat></dt><dd>${test_statistic_power_of_test?string[df_percentage]}&nbsp;${unit_abbr_percentage}</dd>
			
		</dl>
	</section>