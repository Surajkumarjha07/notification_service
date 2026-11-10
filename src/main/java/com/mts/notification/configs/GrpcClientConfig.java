package com.mts.notification.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.distributed.jobscheduler.grpc.JobServiceGrpc;import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {
    @Bean
    JobServiceGrpc.JobServiceBlockingStub jobStub(GrpcChannelFactory channelFactory) {
        return JobServiceGrpc.newBlockingStub(channelFactory.createChannel("scheduler"));
    }
}
