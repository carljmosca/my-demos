/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.carljmosca.openshift.demo;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.JobList;
import io.fabric8.kubernetes.api.model.batch.JobStatus;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author moscac
 */
public class JobManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobManager.class);
    private final KubernetesClient kubernetesClient;
    private final String namespace;
    private final int maximumJobs;
    private final long maximumJobTimeInSeconds;
    private static final String JOBKIND_JOB = "Job";

    public JobManager() {
        String masterUrl = System.getenv("MASTER_URL");
        if (masterUrl == null || masterUrl.isEmpty()) {
            masterUrl = "https://127.0.0.1:8443/";
        }
        Config config = new ConfigBuilder().withMasterUrl(masterUrl).build();
        kubernetesClient = new DefaultKubernetesClient(config);
        namespace = kubernetesClient.getNamespace();
        maximumJobs = JobManager.getIntValueFromEnv("MAXIMUM_JOBS", 3);
        maximumJobTimeInSeconds = JobManager.getIntValueFromEnv("MAXIMUM_JOB_TIME_IN_SECONDS", 20);
    }

    public void create() {

        JobList jobList = getJobList();
        if (jobList == null) {
            return;
        }

        removeCompletedJobs(jobList);

        JobStatusResult jobStatusResult = getJobs(jobList);

        LOGGER.info(String.format("Current jobs - active: %d, failed: %d, successful: %d",
                jobStatusResult.getActiveJobsCount(), jobStatusResult.getFailedJobsCount(),
                jobStatusResult.getSuccessfulJobsCount()));

        int jobCount = jobStatusResult.getActiveJobsCount();
        // the cronjob itself apparently counts as an active job
        while (++jobCount <= (maximumJobs + 1)) {

            // create container
            int random = (int) (Math.random() * 500 + 1);
            UUID uuid = UUID.randomUUID();

            String jobAndContainerName = "hello-" + uuid.toString();
            Container container = new Container();
            container.setName(jobAndContainerName);
            container.setImage("carljmosca/java-hello");
            List<EnvVar> env = new ArrayList<>();
            env.add(new EnvVar("PAUSE_SECONDS", Integer.toString(random), null));
            container.setEnv(env);
            List<Container> containers = new ArrayList<>();
            containers.add(container);

            PodSpec podSpec = new PodSpec();
            podSpec.setContainers(containers);
            podSpec.setRestartPolicy("OnFailure");

            PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
            ObjectMeta metadata = new ObjectMeta();
            metadata.setName(jobAndContainerName);
            podTemplateSpec.setMetadata(metadata);
            podTemplateSpec.setSpec(podSpec);

            Job job = new JobBuilder()
                    .withApiVersion("batch/v1")
                    .withNewMetadata()
                    .withName(jobAndContainerName)
                    .endMetadata()
                    .withNewSpec()
                    .withParallelism(1)
                    .withCompletions(1)
                    .withActiveDeadlineSeconds(maximumJobTimeInSeconds)
                    .endSpec()
                    .withNewSpec()
                    .withNewTemplate()
                    .withSpec(podSpec)
                    .endTemplate()
                    .endSpec()
                    .build();

            kubernetesClient.batch().jobs().inNamespace(namespace).withName("test").create(job);

            LOGGER.debug(String.format("Created job %s", jobAndContainerName));
        }

    }

    private static int getIntValueFromEnv(String env, int defaultValue) {
        int result;
        String value = "";
        try {
            value = System.getenv(env);
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.error(String.format("Found invalid value %s for variable %s using ", value, env, defaultValue));
            result = defaultValue;
        }
        return result;
    }

    private JobList getJobList() {
        JobList jobList = null;
        try {
            if (kubernetesClient.batch().jobs().inNamespace(namespace) != null) {
                jobList = kubernetesClient.batch().jobs().inNamespace(namespace).list();
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Exception getting job list: %s", e.getMessage()));
        }
        return jobList;
    }

    private void removeCompletedJobs(JobList jobList) {
        JobStatusResult jobStatusResult = getJobs(jobList);
        if (!jobStatusResult.getFailedJobs().isEmpty()) {
            LOGGER.info(String.format("Removing %d failed jobs", jobStatusResult.getFailedJobsCount()));
            kubernetesClient.batch().jobs().delete(jobStatusResult.getFailedJobs());
        }
        if (!jobStatusResult.getSuccessfulJobs().isEmpty()) {
            LOGGER.info(String.format("Removing %d successful jobs", jobStatusResult.getSuccessfulJobsCount()));
            kubernetesClient.batch().jobs().delete(jobStatusResult.getSuccessfulJobs());
        }
    }

    private JobStatusResult getJobs(JobList jobList) {
        JobStatusResult jobStatusResult = new JobStatusResult();
        try {
            if (jobList != null && jobList.getItems() != null) {
                for (Job job : jobList.getItems()) {
                    if (job.getStatus() != null && JOBKIND_JOB.equals(job.getKind())) {
                        JobStatus jobStatus = job.getStatus();
                        if (jobStatus.getActive() != null) {
                            jobStatusResult.incrementActiveJobs(jobStatus.getActive());
                            jobStatusResult.getActiveJobs().add(job);
                        }
                        if (jobStatus.getFailed() != null) {
                            jobStatusResult.incrementFailedJobs(jobStatus.getFailed());
                            jobStatusResult.getFailedJobs().add(job);
                        }
                        if (jobStatus.getSucceeded() != null) {
                            jobStatusResult.incrementSuccessfulJobs(jobStatus.getSucceeded());
                            jobStatusResult.getSuccessfulJobs().add(job);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Exception getting jobs: %s", e.getMessage()));
        }
        for (Job job : jobStatusResult.getActiveJobs()) {
            LOGGER.debug(String.format("Active job: %s", job.getMetadata().getName()));
        }
        for (Job job : jobStatusResult.getFailedJobs()) {
            LOGGER.debug(String.format("Failed job: %s", job.getMetadata().getName()));
        }
        for (Job job : jobStatusResult.getSuccessfulJobs()) {
            LOGGER.debug(String.format("Successful job: %s", job.getMetadata().getName()));
        }
        return jobStatusResult;
    }

}
