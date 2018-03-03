<tbody>
	<#list test_statistic_params as test_statistic_param>
		<tr>
			<td>
				<#if !test_statistic_param.f1?is_infinite && test_statistic_param.f1 lt 0.0075>
					${test_statistic_param.f1?string["0.##E0;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.' exponentSeparator='E'"]}
				<#else>
					${test_statistic_param.f1?string["0.000;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.'"]}
				</#if>
			</td>
			<td>${test_statistic_param.f2?string["#;; infinity='∞' nan='NaN'"]}</td>
			<td>
				<#if !test_statistic_param.alpha?is_infinite && test_statistic_param.alpha lt 0.0075>
					${test_statistic_param.alpha?string["0.##E0;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.' exponentSeparator='E'"]}
				<#else>
					${test_statistic_param.alpha?string["0.00;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.'"]}
				</#if>
			</td>
			<td>
				<#if !test_statistic_param.beta?is_infinite && test_statistic_param.beta lt 0.0075>
					${test_statistic_param.beta?string["0.##E0;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.' exponentSeparator='E'"]}
				<#else>
					${test_statistic_param.beta?string["0.00;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.'"]}
				</#if>	
			</td>
			<td>${test_statistic_param.ncp?string["0.000;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.'"]}</td>
			<td>${test_statistic_param.quantile?string["0.00;; infinity='∞' nan='NaN' roundingMode=halfEven decimalSeparator='.'"]}</td>
		</tr>
	</#list>
</tbody>