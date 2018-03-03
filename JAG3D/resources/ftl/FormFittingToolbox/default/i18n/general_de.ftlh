	<section>
		<h2>Projekt</h2>
		<dl>
			<dt>FormFittingToolbox</dt><dd>v${fft_version?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}.${fft_build?string["#"]}</dd>
			<dt>Name der Regressionsform</dt><dd>
				<#if form_type??>
					<#outputformat "plainText">${form_type_map[form_type]}</#outputformat>
				</#if>
			</dd>

			<dt>Bearbeitungsdatum</dt><dd>${date?datetime?string("dd.MM.yyyy' - 'HH:mm:ss")}</dd>
			<dt>Irrtumswahrscheinlichkeit &alpha; [%]</dt><dd>${alpha?string["0.0#;; roundingMode=halfEven decimalSeparator='.'"]}</dd>
			<dt>Macht des Tests (1-&beta;) [%]</dt><dd>${beta?string["0.0#;; roundingMode=halfEven decimalSeparator='.'"]}</dd>
		</dl>
	</section>