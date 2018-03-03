<#if group??>
	<table class="j3d_datatable">
		<caption><#outputformat "plainText">${point_table_caption}</#outputformat></caption>
		<#include "point_thead.ftlh" parse=true>
		<#include "point_tbody.ftlh" parse=true>
		<#include "point_tfoot.ftlh" parse=true>
	</table>
</#if>