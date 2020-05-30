<#if feature_points??>
	<#include "i18n/point.ftlh" parse=true>
	<section>
		<h2>
			<#outputformat "plainText">${point_table_headline}</#outputformat>
		</h2>
		<table class="j3d_datatable">
			<caption><#outputformat "plainText">${point_table_caption}</#outputformat></caption>
			<#include "point_thead.ftlh" parse=true>
			<#include "point_tbody.ftlh" parse=true>
			<#include "point_tfoot.ftlh" parse=true>
		</table>
	</section>
</#if>