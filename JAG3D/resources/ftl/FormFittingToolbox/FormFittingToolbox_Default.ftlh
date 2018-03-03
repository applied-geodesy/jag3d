<#ftl output_format="HTML" encoding="utf-8">
<#setting url_escaping_charset="UTF-8">

<#assign df_redundancy         = "0.000;;   roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_omega              = "0.0000;;  roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_teststatistic      = "0.00;;    roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_vector_uncertainty = "0.00000000;; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_vector             = "0.00000000;; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_quaternion_uncertainty = "0.00000000;; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_quaternion             = "0.00000000;; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_length_gross_error = "0.0;; multipier=1000 roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_length_residual    = "0.0;; multipier=1000 roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_length_uncertainty = "0.0;; multipier=1000 roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_length             = "0.0000;; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_angle_uncertainty  = "0.00;; multipier=1000 roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_angle              = "0.00000;; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_unspecified_parameter  = "0.00000000;; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_matrix_element         = "0.000E00;; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">


<#include "default/i18n/form_parameter.ftlh" parse=true>
<#if type != "UNKNOWN_FORM_TYPE">
	<#assign form_type = type?lower_case>
<#else>
	<#assign form_type = false>
</#if>

<#compress>
	<!DOCTYPE html>
	<html>
		<head>
			<title>
			<#if form_type??>
				<#outputformat "plainText">${form_type_map[form_type]}</#outputformat> &ndash; 
			</#if>
			FormFittingToolbox</title>
			<meta charset="UTF-8">
			<meta name="generator" content="FormFittingToolbox v${fft_version?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}.${fft_build?string["#"]}">
			<meta name="application-name" content="FormFittingToolbox - The OpenSource Shape of Mould Analysis Program - (c) by Michael Loesler - http://derletztekick.com">
			<meta name="date" content="${date?datetime?string("yyyy-MM-dd'T'HH:mm:ss")}">
			<link rel="shortcut icon" href="data:image/x-icon;base64,<#include "default/favicon.ftlh" parse=false>" type="image/x-icon">
			<style><#include "default/style.css" parse=false></style>
			<script><#include "default/javascript.js" parse=false></script>
		</head>
		
		<#include "default/unit.ftlh" parse=true>
		
		<body>
			<header>
				<#include "default/i18n/title.ftlh" parse=false>
			</header>
			
			<article>
				<#include "default/i18n/general.ftlh" parse=true>
				<#include "default/teststatistic_table.ftlh" parse=true>
				<#include "default/variance_component_table.ftlh" parse=true>
			</article>
			
			<article>
				<#include "default/point_table.ftlh" parse=true>
			</article>
			
			<article>
				<#include "default/form_parameter_table.ftlh" parse=true>
				<#include "default/covariance_matrix_table.ftlh" parse=true>
			</article>


			<footer><p>FormFittingToolbox &ndash; The <em>OpenSource</em> Shape of Mould Analysis Program &ndash; &copy; Michael L&ouml;sler &ndash; <a href="http://derletztekick.com" title="OpenSource Software Development">derletztekick.com</a></p></footer>
		</body>
	</html>
</#compress>