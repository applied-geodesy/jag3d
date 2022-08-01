	<section>
		<h2>Summary</h2>
		<dl>
			<dt>JUniForm Version</dt><dd>v${version?string["#"]}</dd>
			<dt>Date of Calculation</dt><dd>${report_creation_date?date?string("dd.MM.yyyy")}</dd>

			<dt>Geometric primitives</dt>
			<dd>
				<#if geometric_primitives??>
					<#list geometric_primitives as geometry>
						<ul>
							<li>
								<#if geometry.type == "QUADRATIC_CURVE">
									Quadratic curve
								<#elseif geometry.type == "LINE">
									Line
								<#elseif geometry.type == "CIRCLE">
									Circle
								<#elseif geometry.type == "ELLIPSE">
									Ellipse
								<#elseif geometry.type == "PLANE">
									Plane
								<#elseif geometry.type == "SPHERE">
									Sphere
								<#elseif geometry.type == "ELLIPSOID">
									Ellipsoid
								<#elseif geometry.type == "CYLINDER">
									Cylinder
								<#elseif geometry.type == "CONE">
									Cone
								<#elseif geometry.type == "PARABOLOID">
									Paraboloid
								<#elseif geometry.type == "QUADRATIC_SURFACE">
									Quadratic surface
								<#else>
									<#outputformat "plainText">${geometry.type}</#outputformat>
								</#if>
								<#outputformat "plainText">(${geometry.name})</#outputformat>
							</li>
						</ul>
					</#list>
				</#if>
			</dd>
			
			<dt>Adjustment Type</dt>
			<dd>
				<#if estimation_type == 'L2NORM'>
					Least squares adjustment (L<sub>2</sub>Norm)
				<#elseif estimation_type == 'SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION'>
					Spherical Simplex Unscented Transformation
				</#if>
			</dd>

			<dt><#outputformat "plainText">Probability Value &alpha;</#outputformat></dt><dd>${test_statistic_probability_value?string[df_percentage]}&nbsp;${unit_abbr_percentage}</dd>
			<dt><#outputformat "plainText">Test Power (1-&beta;)</#outputformat></dt><dd>${test_statistic_power_of_test?string[df_percentage]}&nbsp;${unit_abbr_percentage}</dd>

		</dl>
	</section>