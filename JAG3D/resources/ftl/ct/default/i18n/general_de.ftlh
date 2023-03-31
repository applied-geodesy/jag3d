	<section>
		<h2>Allgemeines</h2>
		<dl>
			<dt>CoordTrans Version</dt><dd>v${version?string["#"]}</dd>
			<dt>Bearbeitungsdatum</dt><dd>${report_creation_date?date?string("dd.MM.yyyy")}</dd>
			
			<dt><#outputformat "plainText">Irrtumswahrscheinlichkeit &alpha;</#outputformat></dt><dd>${test_statistic_probability_value?string[df_percentage]}&nbsp;${unit_abbr_percentage}</dd>
			<dt><#outputformat "plainText">Macht des Tests (1-&beta;)</#outputformat></dt><dd>${test_statistic_power_of_test?string[df_percentage]}&nbsp;${unit_abbr_percentage}</dd>
			
		</dl>
	</section>