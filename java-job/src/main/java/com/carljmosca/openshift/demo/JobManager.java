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
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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

    public JobManager() {
        String masterUrl = System.getenv("MASTER_URL");
        if (masterUrl == null || masterUrl.isEmpty()) {
            masterUrl = "https://127.0.0.1:8443/";
        }
        Config config = new ConfigBuilder().withMasterUrl(masterUrl).build();
        kubernetesClient = new DefaultKubernetesClient(config);
        namespace = kubernetesClient.getNamespace();
        pauseSeconds = JobManager.getIntValueFromEnv("PAUSE_SECONDS", 10);
        maximumJobs = JobManager.getIntValueFromEnv("MAXIMUM_JOBS", 10);
    }

    public void create() {

        while (true) {

            int jobs = kubernetesClient.batch().jobs().list().getItems().size();
            if (jobs >= maximumJobs) {
                System.out.println("Maximum jobs are active; pausing " + pauseSeconds + " seconds.");
                try {
                    Thread.sleep(pauseSeconds * 1000);
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(JobManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                continue;
            }
            // create container
            int random = (int) (Math.random() * 50 + 1);
            Container container = new Container();
            container.setName("hello");
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
            metadata.setName("hello");
            podTemplateSpec.setMetadata(metadata);
            podTemplateSpec.setSpec(podSpec);

            Job job = new JobBuilder()
                    .withApiVersion("batch/v1")
                    .withNewMetadata()
                    .withName("hello")
                    .endMetadata()
                    .withNewSpec()
                    .withParallelism(1)
                    .withCompletions(1)
                    .endSpec()
                    .withNewSpec()
                    .withNewTemplate()
                    .withSpec(podSpec)
                    .endTemplate()
                    .endSpec()
                    .build();

            LOGGER.debug("job built");

            kubernetesClient.batch().jobs().inNamespace(namespace).withName("test").create(job);

            LOGGER.debug("job created");
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

}
