<#include "i18n/variance_component.ftlh" parse=true>
<section>
	<h2><#outputformat "plainText">${variance_component_headline}</#outputformat></h2>
	<#if vce??>
		<table>
			<caption><#outputformat "plainText">${variance_component_caption}</#outputformat></caption>
			<#include "variance_component_thead.ftlh" parse=true>
			<#include "variance_component_tbody.ftlh" parse=true>
			<#include "variance_component_tfoot.ftlh" parse=false>
		</table>
	</#if>
</section>