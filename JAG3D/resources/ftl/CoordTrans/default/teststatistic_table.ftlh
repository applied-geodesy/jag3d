<#include "i18n/teststatistic.ftlh" parse=true>
<section>
	<h2>
		<#if test_statistic_method?? && test_statistic_method == 'BAARDA_METHOD'>
			<#outputformat "plainText">${test_statistic_map['b_method']}</#outputformat>
		<#elseif test_statistic_method?? && test_statistic_method == 'SIDAK'>
			<#outputformat "plainText">${test_statistic_map['sidak']}</#outputformat>
		<#else>
			<#outputformat "plainText">${test_statistic_map['none']}</#outputformat>
		</#if>
	</h2>
	<#if test_statistic_params??>
		<table>
			<caption><#outputformat "plainText">${test_statistic_table_caption}</#outputformat></caption>
			<#include "teststatistic_thead.ftlh" parse=true>
			<#include "teststatistic_tbody.ftlh" parse=true>
			<#include "teststatistic_tfoot.ftlh" parse=false>
		</table>
	</#if>
</section>