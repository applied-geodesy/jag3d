<#if transformed_point_group??>
	<#include "i18n/point.ftlh" parse=true>
	<#assign group = transformed_point_group>
	<#assign extended = false>
	<section>
		<h2><#outputformat "plainText">${point_type_map['transformed_point']}</#outputformat></h2>
		<#include "point_table.ftlh" parse=true>
	</section>
</#if>