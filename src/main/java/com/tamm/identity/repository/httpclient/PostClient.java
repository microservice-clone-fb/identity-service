package com.tamm.identity.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;

import com.tamm.identity.configuration.AuthenticationRequestInterceptor;

@FeignClient(
        name = "post-service",
        url = "${app.services.post}",
        configuration = {AuthenticationRequestInterceptor.class})
public interface PostClient {}
