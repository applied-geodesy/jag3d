<#ftl output_format="HTML" encoding="utf-8">
<#setting url_escaping_charset="UTF-8">

<#assign df_redundancy         = "0.000;;   roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_omega              = "0.0000;;  roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_teststatistic      = "0.00;;    roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_scale_uncertainty = "0.0;; multipier=1000000 roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_scale             = "0.0;; multipier=1000000 roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_length_gross_error = "0.0;; multipier=1000 roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_length_residual    = "0.0;; multipier=1000 roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_length_uncertainty = "0.0;; multipier=1000 roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_length             = "0.0000;; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_angle_uncertainty  = "0.00;; multipier=1000 roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_angle              = "0.00000;; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_unspecified_parameter  = "0.00000000;; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_matrix_element         = "0.000E00;;   roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#compress>
	<!DOCTYPE html>
	<html>
		<head>
			<title>CoordTrans</title>
			<meta charset="UTF-8">
			<meta name="generator" content="CoordTrans v${ct_version?string["0.0;; roundingMode=halfEven decimalSeparator='.'"]}.${ct_build?string["#"]}">
			<meta name="application-name" content="CoordTrans - The OpenSource Similarity Transformation Program - (c) by Michael Loesler - http://derletztekick.com">
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
				<#if adjustment_type == 'L2NORM'>
					<#include "default/teststatistic_table.ftlh" parse=true>
					<#include "default/variance_component_table.ftlh" parse=true>
				</#if>
			</article>
			
			<article>
				<#include "default/source_point_table.ftlh" parse=true>
				<#include "default/target_point_table.ftlh" parse=true>
			</article>
			
			<article>
				<#include "default/transformation_parameter_table.ftlh" parse=true>
				<#include "default/transformation_matrix_table.ftlh" parse=true>
				<#include "default/rotation_matrix_table.ftlh" parse=true>
				<#include "default/covariance_matrix_table.ftlh" parse=true>
			</article>
			
			<article>
				<#include "default/transformed_point_table.ftlh" parse=true>
			</article>

			<footer><p>CoordTrans &ndash; The <em>OpenSource</em> Similarity Transformation Program &ndash; &copy; Michael L&ouml;sler &ndash; <a href="http://derletztekick.com" title="OpenSource Software Development">derletztekick.com</a></p></footer>
		</body>
	</html>
</#compress>