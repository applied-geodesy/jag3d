<#if correlation_matrix??>
	<#include "i18n/matrix.ftlh" parse=true>
	<section>
		<h2>
			<#outputformat "plainText">${matrix_table_headline}</#outputformat>
		</h2>

		<table>
			<caption><#outputformat "plainText">${matrix_table_caption}</#outputformat></caption>
			
			<#include "matrix_thead.ftlh" parse=true>
			<#include "matrix_tbody.ftlh" parse=true>
			<#include "matrix_tfoot.ftlh" parse=true>

		</table>
	</section>
</#if>