<#if observation_groups??>
	<#include "i18n/observation.ftlh" parse=true>
	<#list observation_groups as group>
		<#assign apply_angle_unit=false>
		<#if group.type == "DIRECTION" || group.type == "ZENITH_ANGLE">
			<#assign apply_angle_unit=true>
		</#if>
		
		<section>
			<h2>
				<#if group.type == "LEVELING">
					<#outputformat "plainText">${observation_type_map['levelling']}</#outputformat>:
				<#elseif group.type == "DIRECTION">
					<#outputformat "plainText">${observation_type_map['direction']}</#outputformat>:
				<#elseif group.type == "HORIZONTAL_DISTANCE">
					<#outputformat "plainText">${observation_type_map['horizontal_distance']}</#outputformat>:
				<#elseif group.type == "SLOPE_DISTANCE">
					<#outputformat "plainText">${observation_type_map['slope_distance']}</#outputformat>:
				<#elseif group.type == "ZENITH_ANGLE">
					<#outputformat "plainText">${observation_type_map['zenith_angle']}</#outputformat>:
				<#elseif group.type == "GNSS1D">
					<#outputformat "plainText">${observation_type_map['gnss1d']}</#outputformat>:
				<#elseif group.type == "GNSS2D">
					<#outputformat "plainText">${observation_type_map['gnss2d']}</#outputformat>:
				<#elseif group.type == "GNSS3D">
					<#outputformat "plainText">${observation_type_map['gnss3d']}</#outputformat>:
				</#if>
				${group.name}
			</h2>
			<table class="j3d_datatable">
				<caption><#outputformat "plainText">${observation_table_caption}</#outputformat>: ${group.name}</caption>
				<#include "observation_thead.ftlh" parse=true>
				<#include "observation_tbody.ftlh" parse=true>
				<#include "observation_tfoot.ftlh" parse=true>
			</table>
		</section>
	</#list>
</#if>