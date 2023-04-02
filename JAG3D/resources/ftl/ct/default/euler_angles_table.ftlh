<#if euler_angles_conventions??>
	<#include "i18n/euler_angles.ftlh" parse=true>
	<section>
		<h2>
			<#outputformat "plainText">${euler_angles_table_headline}</#outputformat>
		</h2>

		<table>
			<caption><#outputformat "plainText">${euler_angles_table_caption}</#outputformat></caption>
			
			<#include "euler_angles_thead.ftlh" parse=true>
			<#include "euler_angles_tbody.ftlh" parse=true>
			<#include "euler_angles_tfoot.ftlh" parse=true>

		</table>
	</section>
</#if>