/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.carljmosca.openshift.demo;

import io.fabric8.kubernetes.api.model.batch.Job;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author moscac
 */
public class JobStatusResult implements Serializable {

    private int activeJobsCount;
    private int successfulJobsCount;
    private int failedJobsCount;
    private final List<Job> activeJobs;
    private final List<Job> successfulJobs;
    private final List<Job> failedJobs;

    public JobStatusResult() {
        activeJobs = new ArrayList<>();
        successfulJobs = new ArrayList<>();
        failedJobs = new ArrayList<>();
    }

    public int getActiveJobsCount() {
        return activeJobsCount;
    }

    public void setActiveJobsCount(int activeJobsCount) {
        this.activeJobsCount = activeJobsCount;
    }

    public int getSuccessfulJobsCount() {
        return successfulJobsCount;
    }

    public void setSuccessfulJobsCount(int successfulJobsCount) {
        this.successfulJobsCount = successfulJobsCount;
    }

    public int getFailedJobsCount() {
        return failedJobsCount;
    }

    public void setFailedJobsCount(int failedJobsCount) {
        this.failedJobsCount = failedJobsCount;
    }

    public List<Job> getActiveJobs() {
        return activeJobs;
    }

    public List<Job> getSuccessfulJobs() {
        return successfulJobs;
    }

    public List<Job> getFailedJobs() {
        return failedJobs;
    }

    public void incrementActiveJobs(int activeJobs) {
        this.activeJobsCount += activeJobs;
    }

    public void incrementFailedJobs(int failedJobs) {
        this.failedJobsCount += failedJobs;
    }

    public void incrementSuccessfulJobs(int successfulJobs) {
        this.successfulJobsCount += successfulJobs;
    }
}
