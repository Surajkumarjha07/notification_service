package com.mts.notification.services;

import com.distributed.jobscheduler.grpc.JobServiceGrpc;
import com.distributed.jobscheduler.grpc.UpdateJobStatusRequest;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerGrpcReporter {
    private final JobServiceGrpc.JobServiceBlockingStub stub;

    public void updateJobStatus(String jobId, String status) {
        try {
            stub.withDeadlineAfter(3, TimeUnit.SECONDS).updateJobStatus(
                    UpdateJobStatusRequest.newBuilder()
                            .setJobId(jobId)
                            .setStatus(status)
                            .build()
            );
        } catch (StatusRuntimeException e) {
             switch (e.getStatus().getCode()) {
                 case NOT_FOUND, FAILED_PRECONDITION, INVALID_ARGUMENT:
                     log.warn("scheduler rejected status report for {}: {}", jobId, e.getStatus());
                 default:
                     log.error("could not report {} for job {}", status, jobId, e);
             }
        }
    }
}
