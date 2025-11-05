package com.tamm.identity.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
// import tam.utils.configuration.AuditConfiguration;

@Configuration
@Import(AuditConfiguration.class)
public class IdentityAuditConfig {
    // Empty - chỉ để import configuration
}
