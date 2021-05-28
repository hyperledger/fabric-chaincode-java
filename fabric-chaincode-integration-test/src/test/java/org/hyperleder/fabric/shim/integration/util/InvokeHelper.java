package org.hyperleder.fabric.shim.integration.util;

import java.util.stream.Collectors;

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
    
    public String invoke(String... args){
        PeerBuilder coreBuilder = Peer.newBuilder().ccname(ccname).channel(channel);
        Result r = coreBuilder.argsTx(args).build().run();
        System.out.println(r.stderr);
        String text = r.stderr.stream()
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
    
}
