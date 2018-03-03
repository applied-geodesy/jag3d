<#if matrix??>
	<table>
		<caption><#outputformat "plainText">${matrix_table_caption}</#outputformat></caption>
			
		<#include "matrix_thead.ftlh" parse=true>
		<#include "matrix_tbody.ftlh" parse=true>
		<#include "matrix_tfoot.ftlh" parse=true>

	</table>
</#if>