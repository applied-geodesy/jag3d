<#if source_point_group??>
	<#include "i18n/point.ftlh" parse=true>
	<#assign group = source_point_group>
	<#assign extended = true>
	<section>
		<h2><#outputformat "plainText">${point_type_map['source_point']}</#outputformat></h2>
		<#include "point_table.ftlh" parse=true>
	</section>
</#if>