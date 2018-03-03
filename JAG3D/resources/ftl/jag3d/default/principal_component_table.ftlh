<#if principal_components??>
	<#include "i18n/principal_component.ftlh" parse=true>
	<section>
		<h2><#outputformat "plainText">${principal_component_headline}</#outputformat></h2>
		<table>
			<caption><#outputformat "plainText">${principal_component_caption}</#outputformat></caption>
			<#include "principal_component_thead.ftlh" parse=true>
			<#include "principal_component_tbody.ftlh" parse=true>
			<#include "principal_component_tfoot.ftlh" parse=false>
		</table>
	</section>
</#if>