<#if target_point_group??>
	<#include "i18n/point.ftlh" parse=true>
	<#assign group = target_point_group>
	<#assign extended = true>
	<section>
		<h2><#outputformat "plainText">${point_type_map['target_point']}</#outputformat></h2>
		<#include "point_table.ftlh" parse=true>
	</section>
</#if>