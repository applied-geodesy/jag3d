<#if form_parameter??>
	<section>
		<h2>
			<#outputformat "plainText">${form_parameter_headline}
				<#if form_type??>
					${form_type_map[form_type]}
				</#if>
			</#outputformat>
		</h2>
		<table>
			<caption><#outputformat "plainText">${form_parameter_caption}</#outputformat></caption>
			<#include "form_parameter_thead.ftlh" parse=true>
			<#include "form_parameter_tbody.ftlh" parse=true>
			<#include "form_parameter_tfoot.ftlh" parse=false>
		</table>
	</section>
</#if>