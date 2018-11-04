/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.carljmosca.openshift.demo;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;

import io.fabric8.kubernetes.client.BatchAPIGroupClient;

import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author moscac
 */
public class JobManager {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(JobManager.class);
    
    public void create() {
        String master = "https://192.168.2.2:8443/";
        Config config = new ConfigBuilder().withMasterUrl(master).build();
        KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
        BatchAPIGroupClient osClient = kubernetesClient.adapt(BatchAPIGroupClient.class);
        
        // create container
        Container container = new Container();
        container.setName("hello");
        container.setImage("perl");
        String[] commands = new String[] { "perl", "-e 'print \"Hello World\n\"'" };
        container.setCommand(Arrays.asList(commands));
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
                .withApiVersion("batch/v1beta1")
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
        
        osClient.jobs().create(job);
        
        LOGGER.debug("job created");
             
    }
    
}
