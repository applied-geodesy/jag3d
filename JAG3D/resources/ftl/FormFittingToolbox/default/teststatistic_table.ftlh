<#include "i18n/teststatistic.ftlh" parse=true>
<section>
	<h2><#outputformat "plainText">${test_statistic_headline}</#outputformat></h2>
	<table>
		<caption><#outputformat "plainText">${test_statistic_table_caption}</#outputformat></caption>
		<#include "teststatistic_thead.ftlh" parse=false>
		<#include "teststatistic_tbody.ftlh" parse=true>
		<#include "teststatistic_tfoot.ftlh" parse=false>
	</table>
</section>