package com.tamm.identity.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tamm.identity.service.AuditServices;
import com.tamm.identity.utils.AuditListener;

// @Deprecated
@Configuration
public class AuditConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuditServices.class)
    public AuditServices auditService() {
        return new AuditServices();
    }

    @Bean
    @ConditionalOnMissingBean(AuditListener.class)
    public AuditListener auditEntityListener() {
        return new AuditListener();
    }
}
