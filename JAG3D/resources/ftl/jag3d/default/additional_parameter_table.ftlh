<#if observation_groups??>
	<#include "i18n/additional_parameter.ftlh" parse=true>
	<#assign is_table_added=false>
	<#list observation_groups as group>
		<#if group.unknown_parameters?? && group.unknown_parameters?size gt 0>
			<#if !is_table_added>
				<section>
				<h2><#outputformat "plainText">${additional_parameter_headline}</#outputformat></h2>
				<table>
				<caption><#outputformat "plainText">${additional_parameter_caption}</#outputformat></caption>
				<#include "additional_parameter_thead.ftlh" parse=true>
				<#assign is_table_added=true>
				<tbody>
			</#if>
			
			<#include "additional_parameter_tbody.ftlh" parse=true>
			
		</#if>
	</#list>
	<#if is_table_added>
		</tbody>
		<#include "additional_parameter_tfoot.ftlh" parse=false>
		</table></section>
	</#if>
</#if>