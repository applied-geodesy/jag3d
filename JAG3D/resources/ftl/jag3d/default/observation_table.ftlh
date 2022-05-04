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
				<caption><#outputformat "plainText">
					${observation_table_caption}</#outputformat>: ${group.name}
					<#if group.uncertainties??>
						<ul class="group_uncertainties">
							<#if apply_angle_unit>
								<#if group.uncertainties.zero_point_offset??><li>&sigma;<sub>a</sub> = <@printFormattedNumber value=group.uncertainties.zero_point_offset format=df_angle_uncertainty/><span class="unit">${unit_abbr_angle_uncertainty}</span></li></#if>
								<#if group.uncertainties.square_root_distance_dependent??><li>&sigma;<sub>b</sub> = <@printFormattedNumber value=group.uncertainties.square_root_distance_dependent format=df_length_uncertainty/><span class="unit">${unit_abbr_length_uncertainty}</span></li></#if>
								<#if group.uncertainties.distance_dependent??><li>&sigma;<sub>c</sub> = <@printFormattedNumber value=group.uncertainties.distance_dependent format=df_length_uncertainty/><span class="unit">${unit_abbr_length_uncertainty}</span></li></#if>
							<#else>
								<#if group.uncertainties.zero_point_offset??><li>&sigma;<sub>a</sub> = <@printFormattedNumber value=group.uncertainties.zero_point_offset format=df_length_uncertainty/><span class="unit">${unit_abbr_length_uncertainty}</span></li></#if>
								<#if group.uncertainties.square_root_distance_dependent??><li>&sigma;<sub>b</sub> = <@printFormattedNumber value=group.uncertainties.square_root_distance_dependent format=df_length_uncertainty/><span class="unit">${unit_abbr_length_uncertainty}</span></li></#if>
								<#if group.uncertainties.distance_dependent??><li>&sigma;<sub>c</sub> = <@printFormattedNumber value=group.uncertainties.distance_dependent format=df_scale_uncertainty/><span class="unit">${unit_abbr_scale_uncertainty}</span></li></#if>
							</#if>
						</ul>
					</#if>
				</caption>
				<#include "observation_thead.ftlh" parse=true>
				<#include "observation_tbody.ftlh" parse=true>
				<#include "observation_tfoot.ftlh" parse=true>
			</table>
		</section>
	</#list>
</#if>