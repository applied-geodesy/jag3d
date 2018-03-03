	<section>
		<h2>Project</h2>
		<dl>
			<dt>FormFittingToolbox</dt><dd>v${fft_version?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}.${fft_build?string["#"]}</dd>
			<dt>Naziv oblika regresije</dt><dd>
				<#if form_type??>
					<#outputformat "plainText">${form_type_map[form_type]}</#outputformat>
				</#if>
			</dd>

			<dt>Datum ra&ccaron;unanja</dt><dd>${date?datetime?string("dd.MM.yyyy' - 'HH:mm:ss")}</dd>
			<dt>Vjerovatno&cacute;a &alpha; [%]</dt><dd>${alpha?string["0.0#;; roundingMode=halfEven decimalSeparator='.'"]}</dd>
			<dt>Snaga testa (1-&beta;) [%]</dt><dd>${beta?string["0.0#;; roundingMode=halfEven decimalSeparator='.'"]}</dd>
		</dl>
	</section>