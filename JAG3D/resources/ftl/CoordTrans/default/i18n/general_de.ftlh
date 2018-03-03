	<section>
		<h2>Projekt</h2>
		<dl>
			<dt>CoordTrans</dt><dd>v${ct_version?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}.${ct_build?string["#"]}</dd>
			<dt>Transformationstyp</dt>
			<dd>${dimension?string["#"]}D-Transformation</dd>
			<dt>Bearbeitungsdatum</dt><dd>${date?datetime?string("dd.MM.yyyy' - 'HH:mm:ss")}</dd>
			
			<dt>Ausgleichungsmodus</dt>
			<dd>
				<#if adjustment_type == 'L1NORM'>
					Robuste Parametersch&auml;tzung (D&auml;nische Methode), k = ${threshold?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}
				<#elseif adjustment_type == 'L2NORM'>
					Methode der kleinsten Quadrate (Gau&szlig;-Helmert-Modell)
				</#if>
			</dd>
			
			<#if neighbourhood_adjustment_scheme?? && neighbourhood_adjustment_scheme != "NO_SCHEME">
				<dt>Restklaffenverteilung</dt>
				<dd>
					<#assign hasParams = false>
					<#if neighbourhood_adjustment_scheme == 'DISTANCE_WEIGHT_SCHEME'>
						<#assign hasParams = true>
						Abstandsgewichtete Verteilung
					<#elseif neighbourhood_adjustment_scheme == 'MULTI_QUADRATIC_SCHEME'>
						<#assign hasParams = true>
						Multiquadratische Interpolation
					<#elseif neighbourhood_adjustment_scheme == 'SECTOR_METHODE_SCHEME'>
						<#assign hasParams = true>
						Abstands- und Winkelgewichtete Sektorenmethode
					<#elseif neighbourhood_adjustment_scheme == 'NATURAL_NEIGHBOUR_SCHEME'>	
						Natural Neighbour (nur X,Y-Koordinaten, keine Extrapolation)
					</#if>
					<#if hasParams>
						(a = ${neighbourhood_adjustment_scheme_parameter_a?string["0.00;; roundingMode=halfEven decimalSeparator='.'"]}, 
						b = ${neighbourhood_adjustment_scheme_parameter_b?string["0.00;; roundingMode=halfEven decimalSeparator='.'"]})
					</#if>
				</dd>
			</#if>
			
			<dt>Irrtumswahrscheinlichkeit &alpha; [%]</dt><dd>${alpha?string["0.0#;; roundingMode=halfEven decimalSeparator='.'"]}</dd>
			<dt>Macht des Tests (1-&beta;) [%]</dt><dd>${beta?string["0.0#;; roundingMode=halfEven decimalSeparator='.'"]}</dd>
		</dl>
	</section>