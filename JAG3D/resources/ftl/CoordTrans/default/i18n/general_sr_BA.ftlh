	<section>
		<h2>Project</h2>
		<dl>
			<dt>CoordTrans</dt><dd>v${ct_version?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}.${ct_build?string["#"]}</dd>
			<dt>Tip transformacije</dt>
			<dd>${dimension?string["#"]}D transformacija</dd>
			<dt>Datum ra&ccaron;unanja</dt><dd>${date?datetime?string("dd.MM.yyyy' - 'HH:mm:ss")}</dd>
			
			<dt>Tip izravnanja</dt>
			<dd>
				<#if adjustment_type == 'L1NORM'>
					Robusna procjena (Danska metoda), k = ${threshold?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}
				<#elseif adjustment_type == 'L2NORM'>
					MNK izravnanje (Gau&szlig;-Helmert model)
				</#if>
			</dd>
			
			<#if neighbourhood_adjustment_scheme?? && neighbourhood_adjustment_scheme != "NO_SCHEME">
				<dt>Flaw dislocation</dt>
				<dd>
					<#assign hasParams = false>
					<#if neighbourhood_adjustment_scheme == 'DISTANCE_WEIGHT_SCHEME'>
						<#assign hasParams = true>
						Distance weighted scheme
					<#elseif neighbourhood_adjustment_scheme == 'MULTI_QUADRATIC_SCHEME'>
						<#assign hasParams = true>
						Multiquadratic interpolation
					<#elseif neighbourhood_adjustment_scheme == 'SECTOR_METHODE_SCHEME'>
						<#assign hasParams = true>
						Distance and angle weighted sector method
					<#elseif neighbourhood_adjustment_scheme == 'NATURAL_NEIGHBOUR_SCHEME'>	
						Natural Neighbour (only X,Y-coordinates, no extrapolation)
					</#if>
					<#if hasParams>
						(a = ${neighbourhood_adjustment_scheme_parameter_a?string["0.00;; roundingMode=halfEven decimalSeparator='.'"]}, 
						b = ${neighbourhood_adjustment_scheme_parameter_b?string["0.00;; roundingMode=halfEven decimalSeparator='.'"]})
					</#if>
				</dd>
			</#if>
			
			<dt>Vjerovatno&cacute;a &alpha; [%]</dt><dd>${alpha?string["0.0#;; roundingMode=halfEven decimalSeparator='.'"]}</dd>
			<dt>Snaga testa (1-&beta;) [%]</dt><dd>${beta?string["0.0#;; roundingMode=halfEven decimalSeparator='.'"]}</dd>
		</dl>
	</section>