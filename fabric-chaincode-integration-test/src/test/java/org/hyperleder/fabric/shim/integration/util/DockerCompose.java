/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperleder.fabric.shim.integration.util;
import java.util.ArrayList;
import java.util.List;

/** Represents the 'peer' cli command
 * 
 * 
 *
 */
public class DockerCompose extends Command {

    public static DockerComposeBuilder newBuilder(){
        return new DockerComposeBuilder();
    }
    
    static public class DockerComposeBuilder extends Command.Builder<DockerCompose>{
        String composeFile;

        boolean up = true;
        boolean detach = false;

        public DockerComposeBuilder file(String composeFile){
            this.composeFile = composeFile;
            return this;
        }
        
        public DockerComposeBuilder duplicate() {
            return (DockerComposeBuilder) super.duplicate();
        }

        public DockerComposeBuilder up(){
            this.up = true;
            return this;
        }
        
        public DockerComposeBuilder detach(){
            this.detach = true;
            return this;
        }
        
        public DockerComposeBuilder down(){
            this.up = false;
            return this;
        }
      
        public DockerCompose build(){

            ArrayList<String> list = new ArrayList<>();
            list.add("docker-compose");
            if (composeFile!=null && !composeFile.isEmpty()) {
                list.add("-f");
                list.add(composeFile);
            }
            list.add(up?"up":"down");
            if (detach){
                list.add("-d");
            }
            

            return new DockerCompose(list);
        }
    }
       
    DockerCompose(List<String> cmd) {
       super(cmd);
       super.env.put("COMPOSE_PROJECT_NAME","first-network");
    }
    
}