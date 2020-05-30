	<section>
		<h2>Allgemeines</h2>
		<dl>
			<dt>JUniForm Version</dt><dd>v${version?string["#"]}</dd>
			<dt>Bearbeitungsdatum</dt><dd>${report_date?date?string("dd.MM.yyyy")}</dd>

			<dt>Geometrische Primitive</dt>
			<dd>
				<#if geometric_primitives??>
					<#list geometric_primitives as geometry>
						<ul>
							<li>
								<#if geometry.type == "QUADRATIC_CURVE">
									Kurve 2. Ordnung
								<#elseif geometry.type == "LINE">
									Line
								<#elseif geometry.type == "CIRCLE">
									Kreis
								<#elseif geometry.type == "ELLIPSE">
									Ellipse
								<#elseif geometry.type == "PLANE">
									Ebene
								<#elseif geometry.type == "SPHERE">
									Kugel
								<#elseif geometry.type == "ELLIPSOID">
									Ellipsoid
								<#elseif geometry.type == "CYLINDER">
									Zylinder
								<#elseif geometry.type == "CONE">
									Kegel
								<#elseif geometry.type == "PARABOLOID">
									Paraboloid
								<#elseif geometry.type == "QUADRATIC_SURFACE">
									Fl&auml;che 2. Ordnung
								<#else>
									<#outputformat "plainText">${geometry.type}</#outputformat>
								</#if>
								<#outputformat "plainText">(${geometry.name})</#outputformat>
							</li>
						</ul>
					</#list>
				</#if>
			</dd>

			<dt>Irrtumswahrscheinlichkeit &alpha; [%]</dt><dd>${test_statistic_probability_value?string[df_statistic]}</dd>
			<dt>Macht des Tests (1-&beta;) [%]</dt><dd>${test_statistic_power_of_test?string[df_statistic]}</dd>

		</dl>
	</section>