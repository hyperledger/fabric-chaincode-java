package org.hyperleder.fabric.shim.integration.util;

import java.util.Map;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.hyperleder.fabric.shim.integration.util.Command.Result;
import org.hyperleder.fabric.shim.integration.util.Peer.PeerBuilder;

public class InvokeHelper {
    
    private String ccname;
    private String channel;
        
    public static InvokeHelper newHelper(String ccname, String channel) {
        
        InvokeHelper ih = new InvokeHelper();
        
        ih.ccname = ccname;
        ih.channel = channel;
        
        return ih;
    }
    
    public String invoke(String org, String... args){
        Map<String,String> orgEnv = orgEnv(org);
        PeerBuilder coreBuilder = Peer.newBuilder().ccname(ccname).channel(channel);

        Result r = coreBuilder.argsTx(args).build(orgEnv).run();
        System.out.println(r.stdout);
        String text = r.stdout.stream()
            .filter(line -> line.matches(".*chaincodeInvokeOrQuery.*"))
            .collect(Collectors.joining(System.lineSeparator()))
            .trim();

        if (!text.contains("result: status:200")){
            throw new RuntimeException(text);
        } 

        int payloadIndex = text.indexOf("payload:");
        if (payloadIndex>1){
            return text.substring(payloadIndex+9,text.length()-1);
        }
        return "success";
    }
 
    public static Map<String, String> orgEnv(String org) {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();

        Path mspConfigPath = Paths.get(s, "src/test/resources/_cfg/_msp/" + org, org + "admin/msp");
        if (!Files.exists(mspConfigPath)){
            throw new RuntimeException("MSP Directory can't be found: "+mspConfigPath.toString());
        }

        Map<String, String> env = new HashMap<String, String>();

        env.put("CORE_PEER_MSPCONFIGPATH", mspConfigPath.toString());
        env.put("CORE_PEER_LOCALMSPID", org + "MSP");
        env.put("CORE_PEER_ADDRESS", org + "peer-api.127-0-0-1.nip.io:8080");


        System.out.println(env);
        return env;

    }

}
