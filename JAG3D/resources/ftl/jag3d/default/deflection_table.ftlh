<#if deflection_groups??>
	<#include "i18n/deflection.ftlh" parse=true>
	<#list deflection_groups as group>
		<section>
			<h2>${group.name}</h2>
			<table class="j3d_datatable">
				<caption><#outputformat "plainText">${deflection_table_caption}</#outputformat>: ${group.name}</caption>
				<#include "deflection_thead.ftlh" parse=true>
				<#include "deflection_tbody.ftlh" parse=true>
				<#include "deflection_tfoot.ftlh" parse=true>
			</table>
		</section>
	</#list>
</#if>