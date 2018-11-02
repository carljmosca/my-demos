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
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author moscac
 */
public class JobManager {
 
    public void create() {
        
        OpenShiftClient osClient = new DefaultOpenShiftClient();
        
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
        
        osClient.batch().jobs().createNew().withNewMetadata()
                .withName("hello")
                .endMetadata()
                .withNewSpec()
                .withParallelism(1)
                .withCompletions(1)
                .withTemplate(podTemplateSpec)
                .endSpec()
                .done();
             
    }
    
}
