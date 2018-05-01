<#ftl output_format="HTML" encoding="utf-8">
<#setting url_escaping_charset="UTF-8">

<#-- Decimal format w.r.t. user preferences -->
<#assign df_angle              = "${digits_angle???then(digits_angle, 0.00000)};;                      roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_angle_residual     = "${digits_angle_residual???then(digits_angle_residual, 0.00)};;       roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_angle_uncertainty  = "${digits_angle_uncertainty???then(digits_angle_uncertainty, 0.00)};; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_length             = "${digits_length???then(digits_length, 0.00000)};;                      roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_length_residual    = "${digits_length_residual???then(digits_length_residual, 0.00)};;       roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_length_uncertainty = "${digits_length_uncertainty???then(digits_length_uncertainty, 0.00)};; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_scale              = "${digits_scale???then(digits_scale, 0.00000)};;                      roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_scale_residual     = "${digits_scale_residual???then(digits_scale_residual, 0.00)};;       roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_scale_uncertainty  = "${digits_scale_uncertainty???then(digits_scale_uncertainty, 0.00)};; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_statistic          = "${digits_statistic???then(digits_statistic, 0.00)};;  roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#-- Unit abbr w.r.t. user preferences -->
<#assign unit_abbr_angle              = "${unit_abbr_angle???then(unit_abbr_angle, '\xfffd')}">
<#assign unit_abbr_angle_residual     = "${unit_abbr_angle_residual???then(unit_abbr_angle_residual, '\xfffd')}">
<#assign unit_abbr_angle_uncertainty  = "${unit_abbr_angle_uncertainty???then(unit_abbr_angle_uncertainty, '\xfffd')}">

<#assign unit_abbr_length             = "${unit_abbr_length???then(unit_abbr_length, '\xfffd')}">
<#assign unit_abbr_length_residual    = "${unit_abbr_length_residual???then(unit_abbr_length_residual, '\xfffd')}">
<#assign unit_abbr_length_uncertainty = "${unit_abbr_length_uncertainty???then(unit_abbr_length_uncertainty, '\xfffd')}">

<#assign unit_abbr_scale              = "${unit_abbr_scale???then(unit_abbr_scale, '\xfffd')}">
<#assign unit_abbr_scale_residual     = "${unit_abbr_scale_residual???then(unit_abbr_scale_residual, '\xfffd')}">
<#assign unit_abbr_scale_uncertainty  = "${unit_abbr_scale_uncertainty???then(unit_abbr_scale_uncertainty, '\xfffd')}">

<#compress>
	<!DOCTYPE html>
	<html lang="<#include "default/i18n/locale.ftlh" parse=false>">
		<head>
			<title>${project_name} - JAG3D</title>
			<meta charset="UTF-8">
			<meta name="generator" content="JAG3D - Java Graticule 3D v${version?string["#"]}">
			<meta name="application-name" content="JAG3D - Java Applied Geodesy 3D - (c) by Michael Loesler - http://applied-geodesy.org">
			<meta name="author" content="${project_operator}">
			<meta name="description" content="${project_description}">
			<meta name="date" content="${project_date?datetime?string("yyyy-MM-dd'T'HH:mm:ss")}">
			<link rel="shortcut icon" href="data:image/x-icon;base64,<#include "default/favicon.ftlh" parse=false>" type="image/x-icon">
			<style><#include "default/style.css" parse=false></style>
			<script><#include "default/javascript.js" parse=false></script>
		</head>
		

		<body>
			<header>
				<#include "default/i18n/title.ftlh" parse=false>
			</header>
		
			<article>
				<#include "default/i18n/general.ftlh" parse=true>
				<#include "default/teststatistic_table.ftlh" parse=true>			
				<#include "default/variance_component_table.ftlh" parse=true>
				<#include "default/principal_component_table.ftlh" parse=true>
			</article>
		
			<article>
				<#include "default/point_table.ftlh" parse=true>
				<#include "default/deflection_table.ftlh" parse=true>
			</article>
			
			<article>
				<#include "default/congruence_analysis_table.ftlh" parse=true>
				<#include "default/strain_parameter_table.ftlh" parse=true>
			</article>
			
			<article>
				<#include "default/observation_table.ftlh" parse=true>
				<#include "default/additional_parameter_table.ftlh" parse=true>
			</article>
			
			<article>
				<#include "default/reliability_table.ftlh" parse=true>
				<#include "default/chart.ftlh" parse=true>
			</article>
			
		</body>
	</html>
</#compress>