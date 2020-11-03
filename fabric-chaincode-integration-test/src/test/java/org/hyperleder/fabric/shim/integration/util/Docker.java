/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.util;
import java.util.ArrayList;
import java.util.List;

/** Represents the 'docker' cli command
 * 
 * 
 *
 */
public class Docker extends Command {

    public static DockerBuilder newBuilder(){
        return new DockerBuilder();
    }

    static public class DockerBuilder implements Cloneable {
        boolean exec;
        String container;
        String script;
        String channel;

        public DockerBuilder duplicate() {
            try {
                return (DockerBuilder) this.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        public DockerBuilder script(String script){
            this.script = script;
            return this;
        }
        
        public DockerBuilder channel(String channel){
            this.channel = channel;
            return this;
        }

        public DockerBuilder container(String container){
            this.container = container;
            return this;
        }

        public DockerBuilder exec(){
            this.exec = true;
            return this;
        }

        public Docker build(){

            ArrayList<String> list = new ArrayList<>();
            list.add("docker");
            if(exec){
                list.add("exec");

            }
            
            if (container == null || container.isEmpty()){
                throw new RuntimeException("container should be set");
            }
            list.add(container);
            
            if (script == null || script.isEmpty()){
                throw new RuntimeException("script should be set");
            }
            list.add(script);
            
            if (channel == null || channel.isEmpty()){
                throw new RuntimeException("channel should be set");
            }
            list.add(channel);

            
            return new Docker(list);
        }
    }
       
    Docker(List<String> cmd) {
       super(cmd);
    }

}