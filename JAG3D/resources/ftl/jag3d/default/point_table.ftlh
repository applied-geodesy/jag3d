<#if point_groups??>
	<#include "i18n/point.ftlh" parse=true>
	<#list point_groups as group>
		<section>
			<h2>
				<#if group.type == "REFERENCE_POINT">
					<#outputformat "plainText">${point_type_map['reference_point']}</#outputformat>:
				<#elseif group.type == "STOCHASTIC_POINT">
					<#outputformat "plainText">${point_type_map['stochastic_point']}</#outputformat>:
				<#elseif group.type == "DATUM_POINT">
					<#outputformat "plainText">${point_type_map['datum_point']}</#outputformat>:
				<#elseif group.type == "NEW_POINT">
					<#outputformat "plainText">${point_type_map['new_point']}</#outputformat>:
				</#if>
				${group.name}
			</h2>
			<table class="j3d_datatable">
				<caption><#outputformat "plainText">
					${point_table_caption}</#outputformat>: ${group.name}
					<#if group.uncertainties?? && group.type == "STOCHASTIC_POINT">
						<ul class="group_uncertainties">
							<#if group.dimension != 1 && group.uncertainties.component_y??><li>&sigma;<sub>y</sub> = <@printFormattedNumber value=group.uncertainties.component_y format=df_length_uncertainty/>&nbsp;${unit_abbr_length_uncertainty}</li></#if>
							<#if group.dimension != 1 && group.uncertainties.component_x??><li>&sigma;<sub>x</sub> = <@printFormattedNumber value=group.uncertainties.component_x format=df_length_uncertainty/>&nbsp;${unit_abbr_length_uncertainty}</li></#if>
							<#if group.dimension != 2 && group.uncertainties.component_z??><li>&sigma;<sub>z</sub> = <@printFormattedNumber value=group.uncertainties.component_z format=df_length_uncertainty/>&nbsp;${unit_abbr_length_uncertainty}</li></#if>
						</ul>
					</#if>					
					</caption>
				<#include "point_thead.ftlh" parse=true>
				<#include "point_tbody.ftlh" parse=true>
				<#include "point_tfoot.ftlh" parse=true>
			</table>
		</section>
	</#list>
</#if>