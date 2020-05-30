<tbody>
	<#list test_statistic_params as test_statistic_param>
		<tr>
			<td>
				<#if !test_statistic_param.d1?is_infinite && test_statistic_param.d1 lt 0.0075>
					${test_statistic_param.d1?string["0.##E0;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.' exponentSeparator='E'"]}
				<#else>
					<@printFormattedNumber value=test_statistic_param.d1 format=df_statistic/>
				</#if>
			</td>
			<td><@printFormattedNumber value=test_statistic_param.d2 format=df_statistic/></td>
			<td>
				<#if !test_statistic_param.probability_value?is_infinite && test_statistic_param.probability_value lt 0.0075>
					${test_statistic_param.probability_value?string["0.##E0;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.' exponentSeparator='E'"]}
				<#else>
					<@printFormattedNumber value=test_statistic_param.probability_value format=df_statistic/>
				</#if>
			</td>
			<td>
				<#if !test_statistic_param.power_of_test?is_infinite && test_statistic_param.power_of_test lt 0.0075>
					${test_statistic_param.power_of_test?string["0.##E0;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.' exponentSeparator='E'"]}
				<#else>
					<@printFormattedNumber value=test_statistic_param.power_of_test format=df_statistic/>
				</#if>	
			</td>
			<td><@printFormattedNumber value=test_statistic_param.non_centrality_parameter format=df_statistic/></td>
			<td><@printFormattedNumber value=test_statistic_param.p_value format=df_statistic/></td>
			<td><@printFormattedNumber value=test_statistic_param.quantile format=df_statistic/></td>
		</tr>
	</#list>
</tbody>