package com.mts.notification.services;

import com.distributed.jobscheduler.grpc.JobServiceGrpc;
import com.distributed.jobscheduler.grpc.JobStatusReply;
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

    public String updateJobStatus(String jobId, String status, boolean countAsAttempt) {
        try {
            JobStatusReply reply = stub.withDeadlineAfter(3, TimeUnit.SECONDS).updateJobStatus(
                    UpdateJobStatusRequest.newBuilder()
                            .setJobId(jobId)
                            .setStatus(status)
                            .setCountAsAttempt(countAsAttempt)
                            .build()
            );

            return reply.getStatus();
        } catch (StatusRuntimeException e) {
             switch (e.getStatus().getCode()) {
                 case NOT_FOUND, FAILED_PRECONDITION, INVALID_ARGUMENT:
                     log.warn("scheduler rejected status report for {}: {}", jobId, e.getStatus());
                     break;
                 default:
                     log.error("could not report {} for job {}", status, jobId, e);
             }
             return null;
        }
    }
}
