<tbody>
	<#list test_statistic_params as test_statistic_param>
		<tr>
			<td>
				<#if !test_statistic_param.d1?is_infinite && test_statistic_param.d1 lt 0.0075>
					${test_statistic_param.d1?string["0.##E0;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.' exponentSeparator='E'"]}
				<#else>
					${test_statistic_param.d1?string[df_statistic]}
				</#if>
			</td>
			<td>${test_statistic_param.d2?string[df_statistic]}</td>
			<td>
				<#if !test_statistic_param.probability_value?is_infinite && test_statistic_param.probability_value lt 0.0075>
					${test_statistic_param.probability_value?string["0.##E0;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.' exponentSeparator='E'"]}
				<#else>
					${test_statistic_param.probability_value?string[df_statistic]}
				</#if>
			</td>
			<td>
				<#if !test_statistic_param.power_of_test?is_infinite && test_statistic_param.power_of_test lt 0.0075>
					${test_statistic_param.power_of_test?string["0.##E0;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.' exponentSeparator='E'"]}
				<#else>
					${test_statistic_param.power_of_test?string[df_statistic]}
				</#if>	
			</td>
			<td>${test_statistic_param.non_centrality_parameter?string[df_statistic]}</td>
			<td>${test_statistic_param.p_value?string[df_statistic]}</td>
			<td>${test_statistic_param.quantile?string[df_statistic]}</td>
		</tr>
	</#list>
</tbody>