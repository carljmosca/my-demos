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
    private final int pauseSeconds;
    private final int maximumJobs;
    private final long maximumJobTimeInSeconds;
    private final int totalJobs;

    public JobManager() {
        String masterUrl = System.getenv("MASTER_URL");
        if (masterUrl == null || masterUrl.isEmpty()) {
            masterUrl = "https://127.0.0.1:8443/";
        }
        Config config = new ConfigBuilder().withMasterUrl(masterUrl).build();
        kubernetesClient = new DefaultKubernetesClient(config);
        namespace = kubernetesClient.getNamespace();
        pauseSeconds = JobManager.getIntValueFromEnv("PAUSE_SECONDS", 20);
        maximumJobs = JobManager.getIntValueFromEnv("MAXIMUM_JOBS", 3);
        maximumJobTimeInSeconds = JobManager.getIntValueFromEnv("MAXIMUM_JOB_TIME_IN_SECONDS", 20);
        totalJobs = JobManager.getIntValueFromEnv("TOTAL_JOBS", 0);
    }

    public void create() {

        int jobNumber = 0;

        while (totalJobs == 0 || jobNumber <= totalJobs) {

            JobList jobList = getJobList();
            if (jobList == null) {
                sleep(pauseSeconds);
                continue;
            }

            removeCompletedJobs(jobList);

            if (getJobCount(jobList, true) < maximumJobs) {

                // create container
                int random = (int) (Math.random() * 500 + 1);
                String jobAndContainerName = "hello-" + ++jobNumber;
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

                LOGGER.debug(String.format("job %s built", jobAndContainerName));

                kubernetesClient.batch().jobs().inNamespace(namespace).withName("test").create(job);

                LOGGER.debug(String.format("job %s created", jobAndContainerName));
            }

        }

    }

    private static int getIntValueFromEnv(String env, int defaultValue) {
        int result;
        try {
            result = Integer.parseInt(env);
        } catch (NumberFormatException e) {
            System.out.println("Did not find valid value for " + env + " variable; using " + defaultValue);
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

    private List<Job> getJobs(JobList jobList, boolean active) {
        List<Job> jobs = new ArrayList<>();
        try {
            if (jobList != null) {
                for (Job job : jobList.getItems()) {
                    if (job.getStatus() != null) {
                        if (active && job.getStatus().getActive() > 0) {
                            jobs.add(job);
                        }
                        if (!active && job.getStatus().getActive() == 0) {
                            jobs.add(job);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Exception getting jobs: %s", e.getMessage()));
        }
        return jobs;
    }

    private void removeCompletedJobs(JobList jobList) {
        if (jobList == null || jobList.getItems() == null) {
            return;
        }
        List<Job> jobs = new ArrayList<>();
        for (Job job : jobList.getItems()) {
            if (job.getStatus() != null) {
                if (job.getStatus().getSucceeded() != null && job.getStatus().getSucceeded() > 0) {
                    jobs.add(job);
                } else if (job.getStatus().getFailed() != null && job.getStatus().getFailed() > 0) {
                    jobs.add(job);
                }
            }
        }
        if (!jobs.isEmpty()) {
            kubernetesClient.batch().jobs().delete(jobs);
        }
    }

    private int getJobCount(JobList jobList, boolean active) {
        int jobCount = 0;
        try {
            if (jobList != null && jobList.getItems() != null) {
                for (Job job : jobList.getItems()) {
                    if (job.getStatus() != null) {
                        JobStatus jobStatus = job.getStatus();
                        int activeJobs = (jobStatus.getActive() != null ? jobStatus.getActive() : 0);
                        int successfulJobs = (jobStatus.getSucceeded() != null ? jobStatus.getSucceeded() : 0);
                        int failedJobs = (jobStatus.getFailed() != null ? jobStatus.getFailed() : 0);
                        if (active && activeJobs > 0) {
                            jobCount++;
                        }
                        if (!active && activeJobs == 0) {
                            jobCount++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Exception getting jobs: %s", e.getMessage()));
            if (active) {
                return maximumJobs + 1;
            }
        }
        return jobCount;
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}
