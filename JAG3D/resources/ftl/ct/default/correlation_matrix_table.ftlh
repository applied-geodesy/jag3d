<#if correlation_matrix??>
	<#include "i18n/correlation_matrix.ftlh" parse=true>
	<section>
		<h2>
			<#outputformat "plainText">${correlation_matrix_table_headline}</#outputformat>
		</h2>

		<table>
			<caption><#outputformat "plainText">${correlation_matrix_table_caption}</#outputformat></caption>
			
			<#include "correlation_matrix_thead.ftlh" parse=true>
			<#include "correlation_matrix_tbody.ftlh" parse=true>
			<#include "correlation_matrix_tfoot.ftlh" parse=true>

		</table>
	</section>
</#if>