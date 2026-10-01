package pe.edu.upeu.citas.config;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.upeu.citas.filter.CorrelationIdFilter;

@Configuration
public class FeignTraceConfig {

    @Bean
    public RequestInterceptor traceIdForwardingInterceptor() {
        return template -> {
            String traceId = MDC.get(CorrelationIdFilter.MDC_KEY);
            if (traceId != null) {
                template.header(CorrelationIdFilter.TRACE_ID_HEADER, traceId);
            }
        };
    }
}
