<#if congruence_analysis_groups??>
	<#include "i18n/strain_parameter.ftlh" parse=true>
	<#assign is_table_added=false>
	<#list congruence_analysis_groups as group>
		<#if group.strain_parameters?? && group.strain_parameters?size gt 0>
			<#if !is_table_added>
				<section>
				<h2><#outputformat "plainText">${strain_parameter_headline}</#outputformat></h2>
				<table>
				<caption><#outputformat "plainText">${strain_parameter_table_caption}</#outputformat></caption>
				<#include "strain_parameter_thead.ftlh" parse=true>
				<#assign is_table_added=true>
				<tbody>
			</#if>
			
			<#include "strain_parameter_tbody.ftlh" parse=true>
			
		</#if>
	</#list>
	<#if is_table_added>
		</tbody>
		<#include "strain_parameter_tfoot.ftlh" parse=false>
		</table></section>
	</#if>
</#if>