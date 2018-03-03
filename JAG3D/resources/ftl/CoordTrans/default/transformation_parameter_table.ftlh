<#if transformation_parameter??>
	<#include "i18n/transformation_parameter.ftlh" parse=true>
	<section>
		<h2><#outputformat "plainText">${transformation_parameter_headline}</#outputformat></h2>
		<table>
			<caption><#outputformat "plainText">${transformation_parameter_caption}</#outputformat></caption>
			<#include "transformation_parameter_thead.ftlh" parse=true>
			<#include "transformation_parameter_tbody.ftlh" parse=true>
			<#include "transformation_parameter_tfoot.ftlh" parse=false>
		</table>
	</section>
</#if>