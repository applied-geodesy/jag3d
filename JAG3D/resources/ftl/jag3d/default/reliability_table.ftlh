<#if reliability_summary??>
	<#include "i18n/reliability.ftlh" parse=true>
	<#include "i18n/observation.ftlh" parse=true>
	<#assign observation_types = ['LEVELING', 'DIRECTION', 'HORIZONTAL_DISTANCE', 'SLOPE_DISTANCE', 'ZENITH_ANGLE']>

	<section>
		<h2>
			<#outputformat "plainText">${reliability_headline}</#outputformat>
		</h2>

		<#if reliability_summary.redundancy?? && reliability_summary.redundancy?size gt 0>
			<table>
				<caption><#outputformat "plainText">${table_caption_map['redundancy']}</#outputformat></caption>
				<#assign group           = reliability_summary.redundancy>
				<#assign df_format       = df_statistic>
				<#assign label_value     = "r<sub>min</sub> [${unit_abbr_percentage}]">
				<#assign label_avg_value = "r<sub>avg</sub> [${unit_abbr_percentage}]">
				<#include "reliability_thead.ftlh" parse=true>
				
				<#list observation_types as observation_type>
					<#include "reliability_tbody.ftlh" parse=true>
				</#list>
				
				<#include "reliability_tfoot.ftlh" parse=false>
			</table>
		</#if>
		
		<#if estimation_type != 'SIMULATION' && reliability_summary.p_prio?? && reliability_summary.p_prio?size gt 0>
			<table>
				<caption><#outputformat "plainText">${table_caption_map['p_prio']}</#outputformat></caption>
				<#assign group           = reliability_summary.p_prio>
				<#assign df_format       = df_statistic>
				<#assign label_value     = "log(p<sub>prio,min</sub>)">
				<#assign label_avg_value = "log(p<sub>prio,avg</sub>)">
				<#include "reliability_thead.ftlh" parse=true>
				
				<#list observation_types as observation_type>
					<#include "reliability_tbody.ftlh" parse=true>
				</#list>
				
				<#include "reliability_tfoot.ftlh" parse=false>
			</table>
		</#if>
		
		<#if reliability_summary.influence_on_position?? && reliability_summary.influence_on_position?size gt 0>
			<table>
				<caption><#outputformat "plainText">${table_caption_map['influence_on_position']}</#outputformat></caption>
				<#assign group           = reliability_summary.influence_on_position>
				<#assign df_format       = df_length_residual>
				<#assign label_value     = "EP<sub>max</sub> [${unit_abbr_length_residual}]">
				<#assign label_avg_value = "EP<sub>avg</sub> [${unit_abbr_length_residual}]">
				<#include "reliability_thead.ftlh" parse=true>
				
				<#list observation_types as observation_type>
					<#include "reliability_tbody.ftlh" parse=true>
				</#list>
				
				<#include "reliability_tfoot.ftlh" parse=false>
			</table>
		</#if>
	</section>
	
</#if>