	<section>
		<h2>Summary</h2>
		<dl>
			<dt>CoordTrans Version</dt><dd>v${version?string["#"]}</dd>
			<dt>Date of Calculation</dt><dd>${report_creation_date?date?string("dd.MM.yyyy")}</dd>

			<dt>Adjustment Type</dt>
			<dd>
				<#if estimation_type == 'L2NORM'>
					Least squares adjustment (L<sub>2</sub>Norm)
				</#if>
			</dd>
			
			<#if interpolation_type??>
				<dt>Interpolation Type</dt>
				<dd>
					<#if interpolation_type == 'NONE'>
						None
					<#elseif interpolation_type == 'INVERSE_DISTANCE_WEIGHTING'>
						Inverse distance weighting
						<ul>
							<li>Exponent: ${interpolation_idw_exponent?string[df_double]}</li>
							<li>Smooting: ${interpolation_idw_smoothing?string[df_double]}</li>
						</ul>
					<#elseif interpolation_type == 'MULTI_QUADRATIC'>
						Multi-quadratic interpolation
						<ul>
							<li>Exponent: ${interpolation_mq_exponent?string[df_double]}</li>
							<li>Smooting: ${interpolation_mq_smoothing?string[df_double]}</li>
						</ul>
					<#elseif interpolation_type == 'SECTOR'>
						Sector interpolation (angles and distances)
						<ul>
							<li>Numerator exponent: ${interpolation_sect_numerator_exponent?string[df_double]}</li>
							<li>Denominator exponent: ${interpolation_sect_denominator_exponent?string[df_double]}</li>
						</ul>
					</#if>
				</dd>
			</#if>
			
			<dt>Probability Value &alpha;</dt><dd>${test_statistic_probability_value?string[df_percentage]}&nbsp;${unit_abbr_percentage}</dd>
			<dt>Test Power (1-&beta;)</dt><dd>${test_statistic_power_of_test?string[df_percentage]}&nbsp;${unit_abbr_percentage}</dd>

		</dl>
	</section>