<#if homogeneous_coordinate_transformation_matrix??>
	<#assign matrix = homogeneous_coordinate_transformation_matrix>
	<#include "i18n/transformation_matrix.ftlh" parse=true>
	<section>
		<h2>
			<#outputformat "plainText">${transformation_matrix_table_headline}</#outputformat>
		</h2>

		<table>
			<caption><#outputformat "plainText">${transformation_matrix_table_caption}</#outputformat></caption>
			
			<#include "transformation_matrix_thead.ftlh" parse=true>
			<#include "transformation_matrix_tbody.ftlh" parse=true>
			<#include "transformation_matrix_tfoot.ftlh" parse=true>

		</table>
	</section>
</#if>