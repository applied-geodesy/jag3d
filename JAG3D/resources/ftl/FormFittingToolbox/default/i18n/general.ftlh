	<section>
		<h2>Project</h2>
		<dl>
			<dt>FormFittingToolbox</dt><dd>v${fft_version?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}.${fft_build?string["#"]}</dd>
			<dt>Name of regression form</dt><dd>
				<#if form_type??>
					<#outputformat "plainText">${form_type_map[form_type]}</#outputformat>
				</#if>
			</dd>

			<dt>Date of calculation</dt><dd>${date?datetime?string("dd.MM.yyyy' - 'HH:mm:ss")}</dd>			
			<dt>Probability value &alpha; [%]</dt><dd>${alpha?string["0.0#;; roundingMode=halfEven decimalSeparator='.'"]}</dd>
			<dt>Test power (1-&beta;) [%]</dt><dd>${beta?string["0.0#;; roundingMode=halfEven decimalSeparator='.'"]}</dd>
		</dl>
	</section>