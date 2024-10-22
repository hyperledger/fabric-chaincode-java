package org.hyperleder.fabric.shim.integration.util;

import java.util.Arrays;
import java.util.Map;
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

    public String invoke(String org, String... args) {
        Map<String, String> orgEnv = FabricState.getState().orgEnv(org);
        PeerBuilder coreBuilder = Peer.newBuilder().ccname(ccname).channel(channel);

        Result r = coreBuilder.argsTx(args).build(orgEnv).run();
        System.out.println(r.stdout);
        String text = r.stdout.stream()
                .filter(line -> line.matches(".*chaincodeInvokeOrQuery.*"))
                .collect(Collectors.joining(System.lineSeparator()))
                .trim();

        if (!text.contains("result: status:200")) {
            Command logsCommand = new Command(Arrays.asList("docker", "logs", "microfab"), orgEnv);
            logsCommand.run();
            throw new RuntimeException(text);
        }

        int payloadIndex = text.indexOf("payload:");
        if (payloadIndex > 1) {
            return text.substring(payloadIndex + 9, text.length() - 1);
        }
        return "success";
    }
}
