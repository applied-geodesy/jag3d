<#if vertical_deflection_groups??>
	<#include "i18n/vertical_deflection.ftlh" parse=true>
	<#list vertical_deflection_groups as group>
		<section>
			<h2>
				<#if group.type == "REFERENCE_VERTICAL_DEFLECTION">
					<#outputformat "plainText">${vertical_deflection_type_map['reference_vertical_deflection']}</#outputformat>:
				<#elseif group.type == "STOCHASTIC_VERTICAL_DEFLECTION">
					<#outputformat "plainText">${vertical_deflection_type_map['stochastic_vertical_deflection']}</#outputformat>:
				<#elseif group.type == "UNKNOWN_VERTICAL_DEFLECTION">
					<#outputformat "plainText">${vertical_deflection_type_map['unknown_vertical_deflection']}</#outputformat>:
				</#if>
				${group.name}
			</h2>
			<table class="j3d_datatable">
				<caption><#outputformat "plainText">${vertical_deflection_table_caption}</#outputformat>: ${group.name}</caption>
				<#include "vertical_deflection_thead.ftlh" parse=true>
				<#include "vertical_deflection_tbody.ftlh" parse=true>
				<#include "vertical_deflection_tfoot.ftlh" parse=true>
			</table>
		</section>
	</#list>
</#if>