<#ftl output_format="plainText" encoding="utf-8">

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

<#assign df_vector              = "${digits_vector???then(digits_vector, 0.00000)};;                      roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_vector_residual     = "${digits_vector_residual???then(digits_vector_residual, 0.00)};;       roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_vector_uncertainty  = "${digits_vector_uncertainty???then(digits_vector_uncertainty, 0.00)};; roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

<#assign df_statistic          = "${digits_statistic???then(digits_statistic, 0.00)};;  roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">
<#assign df_double             = "${digits_double???then(digits_double, 0.00000)};;  roundingMode=halfEven decimalSeparator='.' infinity='∞' nan='NaN'">

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

<#assign unit_abbr_vector              = "${unit_abbr_vector???then(unit_abbr_vector, '\xfffd')}">
<#assign unit_abbr_vector_residual     = "${unit_abbr_vector_residual???then(unit_abbr_vector_residual, '\xfffd')}">
<#assign unit_abbr_vector_uncertainty  = "${unit_abbr_vector_uncertainty???then(unit_abbr_vector_uncertainty, '\xfffd')}">

<#macro printFormattedNumber value format>
	<#if format == df_angle && sexagesimal_angle?? || format == df_angle_residual && sexagesimal_angle_residual?? || format == df_angle_uncertainty && sexagesimal_angle_uncertainty??>
		<#assign signum  = (value < 0)?then(-1, 1)>
		<#assign value   = signum * value>
		<#assign degree  = value?floor>
		<#assign minutes = ((value - degree) * 60)?floor>
		<#assign seconds = ((((value - degree) * 60) - minutes) * 60)>
		<#t>${signum * degree} &#x00B7; ${(100 + minutes)?string[1..]} &#x00B7; ${((100 + seconds)?string[format]?string[1..])}<#t>
	<#else>
		<#t>${value?string[format]?left_pad(15, " ")}<#t>
	</#if>
  <#return>
</#macro>
<#list unknown_feature_parameters as parameter>
	<#if parameter.visible>
		${parameter.parameter_type?right_pad(30, " ")}<#t>
		<#if parameter.unit_type == "ANGLE">
			<@printFormattedNumber value=parameter.value format=df_angle/> 
			<@printFormattedNumber value=parameter.sigma format=df_angle_uncertainty/>
		<#elseif parameter.unit_type == "LENGTH">
			<@printFormattedNumber value=parameter.value format=df_length/> 
			<@printFormattedNumber value=parameter.sigma format=df_length_uncertainty/>
		<#elseif parameter.unit_type == "SCALE">
			<@printFormattedNumber value=parameter.value format=df_scale/> 
			<@printFormattedNumber value=parameter.sigma format=df_scale_uncertainty/>
		<#elseif parameter.unit_type == "VECTOR">
			<@printFormattedNumber value=parameter.value format=df_vector/> 
			<@printFormattedNumber value=parameter.sigma format=df_vector_uncertainty/>
		<#elseif parameter.unit_type == "DOUBLE">
			<@printFormattedNumber value=parameter.value format=df_double/> 
			<@printFormattedNumber value=parameter.sigma format=df_double/>
		<#else>
			<@printFormattedNumber value=parameter.value format=df_statistic/> 
			<@printFormattedNumber value=parameter.sigma format=df_statistic/>
		</#if>
			
	</#if>
</#list>