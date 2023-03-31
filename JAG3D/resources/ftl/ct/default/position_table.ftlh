<#if homologous_position_pairs??>
	<#include "i18n/position.ftlh" parse=true>
	<section>
		<h2>
			<#outputformat "plainText">${position_table_headline_map[view]}</#outputformat> 
		</h2>
		<table class="j3d_datatable">
			<caption><#outputformat "plainText">${position_table_caption_map[view]}</#outputformat></caption> 
			<#include "position_thead.ftlh" parse=true>
			<#include "position_tbody.ftlh" parse=true>
			<#include "position_tfoot.ftlh" parse=true>
		</table>
	</section>
</#if>