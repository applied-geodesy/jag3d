<#if congruence_analysis_groups??>
	<#include "i18n/congruence_analysis.ftlh" parse=true>
	<#list congruence_analysis_groups as group>
		<section>
			<h2>${group.name}</h2>
			<table class="j3d_datatable">
				<caption><#outputformat "plainText">${congruence_analysis_table_caption}</#outputformat>: ${group.name}</caption>
				<#include "congruence_analysis_thead.ftlh" parse=true>
				<#include "congruence_analysis_tbody.ftlh" parse=true>
				<#include "congruence_analysis_tfoot.ftlh" parse=true>
			</table>
		</section>
	</#list>
</#if>