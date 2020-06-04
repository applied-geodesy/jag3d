<#if unknown_feature_parameters??>
	<#include "i18n/parameter.ftlh" parse=true>
	<section>
		<h2>
			<#outputformat "plainText">${parameter_table_headline}</#outputformat>
		</h2>
		<table>
			<caption><#outputformat "plainText">${parameter_table_caption}</#outputformat></caption>
			<#include "parameter_thead.ftlh" parse=true>
			<#include "parameter_tbody.ftlh" parse=true>
			<#include "parameter_tfoot.ftlh" parse=true>
		</table>
	</section>
</#if>