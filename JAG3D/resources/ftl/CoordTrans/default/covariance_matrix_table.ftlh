<#if covariance_matrix??>
	<#assign matrix = covariance_matrix>
	<#include "i18n/matrix.ftlh" parse=true>
	<section>
		<h2><#outputformat "plainText">${matrix_type_map['covariance_matrix']}</#outputformat></h2>
		<#include "matrix_table.ftlh" parse=true>
	</section>
</#if>