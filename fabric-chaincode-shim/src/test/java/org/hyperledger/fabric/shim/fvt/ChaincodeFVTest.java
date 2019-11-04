/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.fvt;

import static org.hamcrest.Matchers.is;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.COMPLETED;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.ERROR;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.INIT;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.READY;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.REGISTER;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.RESPONSE;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.TRANSACTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;

import org.hyperledger.fabric.protos.peer.Chaincode;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.protos.peer.ProposalResponsePackage;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.shim.chaincode.EmptyChaincode;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.impl.StateBasedEndorsementFactory;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.mock.peer.ChaincodeMockPeer;
import org.hyperledger.fabric.shim.mock.peer.CompleteStep;
import org.hyperledger.fabric.shim.mock.peer.DelValueStep;
import org.hyperledger.fabric.shim.mock.peer.ErrorResponseStep;
import org.hyperledger.fabric.shim.mock.peer.GetHistoryForKeyStep;
import org.hyperledger.fabric.shim.mock.peer.GetQueryResultStep;
import org.hyperledger.fabric.shim.mock.peer.GetStateByRangeStep;
import org.hyperledger.fabric.shim.mock.peer.GetStateMetadata;
import org.hyperledger.fabric.shim.mock.peer.GetValueStep;
import org.hyperledger.fabric.shim.mock.peer.InvokeChaincodeStep;
import org.hyperledger.fabric.shim.mock.peer.PutStateMetadata;
import org.hyperledger.fabric.shim.mock.peer.PutValueStep;
import org.hyperledger.fabric.shim.mock.peer.QueryCloseStep;
import org.hyperledger.fabric.shim.mock.peer.QueryNextStep;
import org.hyperledger.fabric.shim.mock.peer.RegisterStep;
import org.hyperledger.fabric.shim.mock.peer.ScenarioStep;
import org.hyperledger.fabric.shim.utils.MessageUtil;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class ChaincodeFVTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    ChaincodeMockPeer server;

    @After
    public void afterTest() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void testRegister() throws Exception {
        ChaincodeBase cb = new EmptyChaincode();

        List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());

        server = ChaincodeMockPeer.startServer(scenario);

        cb.start(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});

        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 5000, TimeUnit.MILLISECONDS);

        assertThat(server.getLastMessageSend().getType(), is(READY));
        assertThat(server.getLastMessageRcvd().getType(), is(REGISTER));
    }

    @Test
    public void testRegisterAndEmptyInit() throws Exception {
        ChaincodeBase cb = new ChaincodeBase() {
            @Override
            public Response init(ChaincodeStub stub) {
                return ResponseUtils.newSuccessResponse();
            }

            @Override
            public Response invoke(ChaincodeStub stub) {
                return ResponseUtils.newSuccessResponse();
            }
        };

        ByteString payload = org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInput.newBuilder().addArgs(ByteString.copyFromUtf8("")).build().toByteString();
        ChaincodeShim.ChaincodeMessage initMsg = MessageUtil.newEventMessage(INIT, "testChannel", "0", payload, null);

        List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());
        scenario.add(new CompleteStep());

        server = ChaincodeMockPeer.startServer(scenario);

        cb.start(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});
        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 5000, TimeUnit.MILLISECONDS);

        server.send(initMsg);
        ChaincodeMockPeer.checkScenarioStepEnded(server, 2, 5000, TimeUnit.MILLISECONDS);

        assertThat(server.getLastMessageSend().getType(), is(INIT));
        assertThat(server.getLastMessageRcvd().getType(), is(COMPLETED));
    }

    @Test
    public void testInitAndInvoke() throws Exception {
        ChaincodeBase cb = new ChaincodeBase() {
            @Override
            public Response init(ChaincodeStub stub) {
                assertThat(stub.getFunction(), is("init"));
                assertThat(stub.getArgs().size(), is(3));
                stub.putState("a", ByteString.copyFromUtf8("100").toByteArray());
                return ResponseUtils.newSuccessResponse("OK response1");
            }

            @Override
            public Response invoke(ChaincodeStub stub) {
                assertThat(stub.getFunction(), is("invoke"));
                assertThat(stub.getArgs().size(), is(3));
                String aKey = stub.getStringArgs().get(1);
                assertThat(aKey, is("a"));
                stub.getStringState(aKey);
                stub.putState(aKey, ByteString.copyFromUtf8("120").toByteArray());
                stub.delState("delKey");
                return ResponseUtils.newSuccessResponse("OK response2");
            }
        };

        ByteString initPayload = Chaincode.ChaincodeInput.newBuilder()
                .addArgs(ByteString.copyFromUtf8("init"))
                .addArgs(ByteString.copyFromUtf8("a"))
                .addArgs(ByteString.copyFromUtf8("100"))
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage initMsg = MessageUtil.newEventMessage(INIT, "testChannel", "0", initPayload, null);

        List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());
        scenario.add(new PutValueStep("100"));
        scenario.add(new CompleteStep());
        scenario.add(new GetValueStep("100"));
        scenario.add(new PutValueStep("120"));
        scenario.add(new DelValueStep());
        scenario.add(new CompleteStep());

        setLogLevel("DEBUG");
        server = ChaincodeMockPeer.startServer(scenario);

        cb.start(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});
        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 5000, TimeUnit.MILLISECONDS);

        server.send(initMsg);
        ChaincodeMockPeer.checkScenarioStepEnded(server, 3, 5000, TimeUnit.MILLISECONDS);

        assertThat(server.getLastMessageSend().getType(), is(RESPONSE));
        assertThat(server.getLastMessageRcvd().getType(), is(COMPLETED));
        assertThat(ProposalResponsePackage.Response.parseFrom(server.getLastMessageRcvd().getPayload()).getMessage(), is("OK response1"));


        ByteString invokePayload = Chaincode.ChaincodeInput.newBuilder()
                .addArgs(ByteString.copyFromUtf8("invoke"))
                .addArgs(ByteString.copyFromUtf8("a"))
                .addArgs(ByteString.copyFromUtf8("10"))
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage invokeMsg = MessageUtil.newEventMessage(TRANSACTION, "testChannel", "0", invokePayload, null);

        server.send(invokeMsg);

        ChaincodeMockPeer.checkScenarioStepEnded(server, 7, 5000, TimeUnit.MILLISECONDS);
        assertThat(server.getLastMessageSend().getType(), is(RESPONSE));
        assertThat(server.getLastMessageRcvd().getType(), is(COMPLETED));
        assertThat(ProposalResponsePackage.Response.parseFrom(server.getLastMessageRcvd().getPayload()).getMessage(), is("OK response2"));
    }

    @Test
    public void testStateValidationParameter() throws Exception {
        ChaincodeBase cb = new ChaincodeBase() {
            @Override
            public Response init(ChaincodeStub stub) {
                return ResponseUtils.newSuccessResponse("OK response1");
            }

            @Override
            public Response invoke(ChaincodeStub stub) {
                String aKey = stub.getStringArgs().get(1);
                byte[] epBytes = stub.getStateValidationParameter(aKey);
                StateBasedEndorsement stateBasedEndorsement = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(epBytes);
                assertThat(stateBasedEndorsement.listOrgs().size(), is(2));
                stub.setStateValidationParameter(aKey, stateBasedEndorsement.policy());
                return ResponseUtils.newSuccessResponse("OK response2");
            }
        };

        ByteString initPayload = Chaincode.ChaincodeInput.newBuilder()
                .addArgs(ByteString.copyFromUtf8("init"))
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage initMsg = MessageUtil.newEventMessage(INIT, "testChannel", "0", initPayload, null);

        StateBasedEndorsement sbe = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(null);
        sbe.addOrgs(StateBasedEndorsement.RoleType.RoleTypePeer, "Org1");
        sbe.addOrgs(StateBasedEndorsement.RoleType.RoleTypeMember, "Org2");

        List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());
        scenario.add(new CompleteStep());

        scenario.add(new GetStateMetadata(sbe));
        scenario.add(new PutStateMetadata(sbe));
        scenario.add(new CompleteStep());

        setLogLevel("DEBUG");
        server = ChaincodeMockPeer.startServer(scenario);

        cb.start(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});
        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 5000, TimeUnit.MILLISECONDS);

        server.send(initMsg);
        ChaincodeMockPeer.checkScenarioStepEnded(server, 2, 5000, TimeUnit.MILLISECONDS);

        assertThat(server.getLastMessageSend().getType(), is(INIT));
        assertThat(server.getLastMessageRcvd().getType(), is(COMPLETED));
        assertThat(ProposalResponsePackage.Response.parseFrom(server.getLastMessageRcvd().getPayload()).getMessage(), is("OK response1"));


        ByteString invokePayload = Chaincode.ChaincodeInput.newBuilder()
                .addArgs(ByteString.copyFromUtf8("invoke"))
                .addArgs(ByteString.copyFromUtf8("a"))
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage invokeMsg = MessageUtil.newEventMessage(TRANSACTION, "testChannel", "0", invokePayload, null);

        server.send(invokeMsg);

        ChaincodeMockPeer.checkScenarioStepEnded(server, 5, 5000, TimeUnit.MILLISECONDS);
        assertThat(server.getLastMessageSend().getType(), is(RESPONSE));
        assertThat(server.getLastMessageRcvd().getType(), is(COMPLETED));
        assertThat(ProposalResponsePackage.Response.parseFrom(server.getLastMessageRcvd().getPayload()).getMessage(), is("OK response2"));
    }

    @Test
    public void testInvokeRangeQ() throws Exception {
        ChaincodeBase cb = new ChaincodeBase() {
            @Override
            public Response init(ChaincodeStub stub) {
                return ResponseUtils.newSuccessResponse("OK response1");
            }

            @Override
            public Response invoke(ChaincodeStub stub) {
                assertThat(stub.getFunction(), is("invoke"));
                assertThat(stub.getArgs().size(), is(3));
                String aKey = stub.getStringArgs().get(1);
                String bKey = stub.getStringArgs().get(2);

                QueryResultsIterator<KeyValue> stateByRange = stub.getStateByRange(aKey, bKey);
                Iterator<KeyValue> iter = stateByRange.iterator();
                while (iter.hasNext()) {
                    iter.next();
                }
                try {
                    stateByRange.close();
                } catch (Exception e) {
                    fail("No exception expected");
                }
                return ResponseUtils.newSuccessResponse("OK response2");
            }
        };

        ByteString initPayload = Chaincode.ChaincodeInput.newBuilder()
                .addArgs(ByteString.copyFromUtf8(""))
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage initMsg = MessageUtil.newEventMessage(INIT, "testChannel", "0", initPayload, null);

        ByteString invokePayload = Chaincode.ChaincodeInput.newBuilder()
                .addArgs(ByteString.copyFromUtf8("invoke"))
                .addArgs(ByteString.copyFromUtf8("a"))
                .addArgs(ByteString.copyFromUtf8("b"))
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage invokeMsg = MessageUtil.newEventMessage(TRANSACTION, "testChannel", "0", invokePayload, null);

        List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());
        scenario.add(new CompleteStep());
        scenario.add(new GetStateByRangeStep(false, "a", "b"));
        scenario.add(new QueryCloseStep());
        scenario.add(new CompleteStep());
        scenario.add(new GetStateByRangeStep(true, "a", "b"));
        scenario.add(new QueryNextStep(false, "c"));
        scenario.add(new QueryCloseStep());
        scenario.add(new CompleteStep());

        setLogLevel("DEBUG");
        server = ChaincodeMockPeer.startServer(scenario);

        cb.start(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});
        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 5000, TimeUnit.MILLISECONDS);

        server.send(initMsg);
        ChaincodeMockPeer.checkScenarioStepEnded(server, 2, 5000, TimeUnit.MILLISECONDS);


        server.send(invokeMsg);

        ChaincodeMockPeer.checkScenarioStepEnded(server, 5, 5000, TimeUnit.MILLISECONDS);
        assertThat(server.getLastMessageSend().getType(), is(RESPONSE));
        assertThat(server.getLastMessageRcvd().getType(), is(COMPLETED));
        assertThat(ProposalResponsePackage.Response.parseFrom(server.getLastMessageRcvd().getPayload()).getMessage(), is("OK response2"));

        server.send(invokeMsg);

        ChaincodeMockPeer.checkScenarioStepEnded(server, 9, 30000, TimeUnit.MILLISECONDS);
        assertThat(server.getLastMessageSend().getType(), is(RESPONSE));
        assertThat(server.getLastMessageRcvd().getType(), is(COMPLETED));
        assertThat(ProposalResponsePackage.Response.parseFrom(server.getLastMessageRcvd().getPayload()).getMessage(), is("OK response2"));
    }

    @Test
    public void testGetQueryResult() throws Exception {
        ChaincodeBase cb = new ChaincodeBase() {
            @Override
            public Response init(ChaincodeStub stub) {
                return ResponseUtils.newSuccessResponse("OK response1");
            }

            @Override
            public Response invoke(ChaincodeStub stub) {
                String query = stub.getStringArgs().get(1);

                QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);
                Iterator<KeyValue> iter = queryResult.iterator();
                while (iter.hasNext()) {
                    iter.next();
                }
                try {
                    queryResult.close();
                } catch (Exception e) {
                    fail("No exception expected");
                }
                return ResponseUtils.newSuccessResponse("OK response2");
            }
        };

        ByteString initPayload = Chaincode.ChaincodeInput.newBuilder()
                .addArgs(ByteString.copyFromUtf8(""))
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage initMsg = MessageUtil.newEventMessage(INIT, "testChannel", "0", initPayload, null);

        ByteString invokePayload = Chaincode.ChaincodeInput.newBuilder()
                .addArgs(ByteString.copyFromUtf8("invoke"))
                .addArgs(ByteString.copyFromUtf8("query"))
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage invokeMsg = MessageUtil.newEventMessage(TRANSACTION, "testChannel", "0", invokePayload, null);

        List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());
        scenario.add(new CompleteStep());
        scenario.add(new GetQueryResultStep(false, "a", "b"));
        scenario.add(new QueryCloseStep());
        scenario.add(new CompleteStep());
        scenario.add(new GetQueryResultStep(true, "a", "b"));
        scenario.add(new QueryNextStep(false, "c"));
        scenario.add(new QueryCloseStep());
        scenario.add(new CompleteStep());

        setLogLevel("DEBUG");
        server = ChaincodeMockPeer.startServer(scenario);

        cb.start(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});
        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 5000, TimeUnit.MILLISECONDS);

        server.send(initMsg);
        ChaincodeMockPeer.checkScenarioStepEnded(server, 2, 5000, TimeUnit.MILLISECONDS);

        server.send(invokeMsg);

        ChaincodeMockPeer.checkScenarioStepEnded(server, 5, 5000, TimeUnit.MILLISECONDS);
        assertThat(server.getLastMessageSend().getType(), is(RESPONSE));
        assertThat(server.getLastMessageRcvd().getType(), is(COMPLETED));
        assertThat(ProposalResponsePackage.Response.parseFrom(server.getLastMessageRcvd().getPayload()).getMessage(), is("OK response2"));

        server.send(invokeMsg);

        ChaincodeMockPeer.checkScenarioStepEnded(server, 9, 5000, TimeUnit.MILLISECONDS);
        assertThat(server.getLastMessageSend().getType(), is(RESPONSE));
        assertThat(server.getLastMessageRcvd().getType(), is(COMPLETED));
        assertThat(ProposalResponsePackage.Response.parseFrom(server.getLastMessageRcvd().getPayload()).getMessage(), is("OK response2"));
    }

    @Test
    public void testGetHistoryForKey() throws Exception {
        ChaincodeBase cb = new ChaincodeBase() {
            @Override
            public Response init(ChaincodeStub stub) {
                return ResponseUtils.newSuccessResponse("OK response1");
            }

            @Override
            public Response invoke(ChaincodeStub stub) {
                String key = stub.getStringArgs().get(1);

                QueryResultsIterator<KeyModification> queryResult = stub.getHistoryForKey(key);
                Iterator<KeyModification> iter = queryResult.iterator();
                while (iter.hasNext()) {
                    iter.next();
                }
                try {
                    queryResult.close();
                } catch (Exception e) {
                    fail("No exception expected");
                }
                return ResponseUtils.newSuccessResponse("OK response2");
            }
        };

        ByteString initPayload = Chaincode.ChaincodeInput.newBuilder()
                .addArgs(ByteString.copyFromUtf8(""))
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage initMsg = MessageUtil.newEventMessage(INIT, "testChannel", "0", initPayload, null);

        ByteString invokePayload = Chaincode.ChaincodeInput.newBuilder()
                .addArgs(ByteString.copyFromUtf8("invoke"))
                .addArgs(ByteString.copyFromUtf8("key1"))
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage invokeMsg = MessageUtil.newEventMessage(TRANSACTION, "testChannel", "0", invokePayload, null);

        List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());
        scenario.add(new CompleteStep());
        scenario.add(new GetHistoryForKeyStep(false, "1", "2"));
        scenario.add(new QueryCloseStep());
        scenario.add(new CompleteStep());

        setLogLevel("DEBUG");
        server = ChaincodeMockPeer.startServer(scenario);

        cb.start(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});
        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 5000, TimeUnit.MILLISECONDS);

        server.send(initMsg);
        ChaincodeMockPeer.checkScenarioStepEnded(server, 2, 5000, TimeUnit.MILLISECONDS);

        server.send(invokeMsg);

        ChaincodeMockPeer.checkScenarioStepEnded(server, 5, 5000, TimeUnit.MILLISECONDS);
        assertThat(server.getLastMessageSend().getType(), is(RESPONSE));
        assertThat(server.getLastMessageRcvd().getType(), is(COMPLETED));
        assertThat(ProposalResponsePackage.Response.parseFrom(server.getLastMessageRcvd().getPayload()).getMessage(), is("OK response2"));

    }

    @Test
    public void testInvokeChaincode() throws Exception {
        ChaincodeBase cb = new ChaincodeBase() {
            @Override
            public Response init(ChaincodeStub stub) {
                return ResponseUtils.newSuccessResponse("OK response1");
            }

            @Override
            public Response invoke(ChaincodeStub stub) {
                stub.invokeChaincode("anotherChaincode", Collections.emptyList());
                return ResponseUtils.newSuccessResponse("OK response2");
            }
        };

        ByteString initPayload = Chaincode.ChaincodeInput.newBuilder()
                .addArgs(ByteString.copyFromUtf8(""))
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage initMsg = MessageUtil.newEventMessage(INIT, "testChannel", "0", initPayload, null);

        ByteString invokePayload = Chaincode.ChaincodeInput.newBuilder()
                .addArgs(ByteString.copyFromUtf8("invoke"))
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage invokeMsg = MessageUtil.newEventMessage(TRANSACTION, "testChannel", "0", invokePayload, null);

        List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());
        scenario.add(new CompleteStep());
        scenario.add(new InvokeChaincodeStep());
        scenario.add(new CompleteStep());

        setLogLevel("DEBUG");
        server = ChaincodeMockPeer.startServer(scenario);

        cb.start(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});
        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 5000, TimeUnit.MILLISECONDS);

        server.send(initMsg);
        ChaincodeMockPeer.checkScenarioStepEnded(server, 2, 5000, TimeUnit.MILLISECONDS);

        server.send(invokeMsg);

        ChaincodeMockPeer.checkScenarioStepEnded(server, 4, 10000, TimeUnit.MILLISECONDS);
        assertThat(server.getLastMessageSend().getType(), is(RESPONSE));
        assertThat(server.getLastMessageRcvd().getType(), is(COMPLETED));
    }

    @Test
    public void testErrorInitInvoke() throws Exception {
        ChaincodeBase cb = new ChaincodeBase() {
            @Override
            public Response init(ChaincodeStub stub) {
                return ResponseUtils.newErrorResponse("Wrong response1");
            }

            @Override
            public Response invoke(ChaincodeStub stub) {
                return ResponseUtils.newErrorResponse("Wrong response2");
            }
        };

        ByteString payload = org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInput.newBuilder().addArgs(ByteString.copyFromUtf8("")).build().toByteString();
        ChaincodeShim.ChaincodeMessage initMsg = MessageUtil.newEventMessage(INIT, "testChannel", "0", payload, null);

        List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());
        scenario.add(new ErrorResponseStep());
        scenario.add(new ErrorResponseStep());

        setLogLevel("DEBUG");
        server = ChaincodeMockPeer.startServer(scenario);

        cb.start(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});
        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 5000, TimeUnit.MILLISECONDS);

        server.send(initMsg);
        ChaincodeMockPeer.checkScenarioStepEnded(server, 2, 5000, TimeUnit.MILLISECONDS);

        assertThat(server.getLastMessageSend().getType(), is(INIT));
        assertThat(server.getLastMessageRcvd().getType(), is(ERROR));
        assertThat(server.getLastMessageRcvd().getPayload().toStringUtf8(), is("Wrong response1"));

        ByteString invokePayload = Chaincode.ChaincodeInput.newBuilder()
                .build().toByteString();
        ChaincodeShim.ChaincodeMessage invokeMsg = MessageUtil.newEventMessage(TRANSACTION, "testChannel", "0", invokePayload, null);

        server.send(invokeMsg);

        ChaincodeMockPeer.checkScenarioStepEnded(server, 3, 5000, TimeUnit.MILLISECONDS);
        assertThat(server.getLastMessageSend().getType(), is(TRANSACTION));
        assertThat(server.getLastMessageRcvd().getType(), is(ERROR));
        assertThat(server.getLastMessageRcvd().getPayload().toStringUtf8(), is("Wrong response2"));
    }

    @Test
    public void testStreamShutdown() throws Exception {
        ChaincodeBase cb = new ChaincodeBase() {
            @Override
            public Response init(ChaincodeStub stub) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                return ResponseUtils.newSuccessResponse();
            }

            @Override
            public Response invoke(ChaincodeStub stub) {
                return ResponseUtils.newSuccessResponse();
            }
        };

        ByteString payload = org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInput.newBuilder().addArgs(ByteString.copyFromUtf8("")).build().toByteString();
        ChaincodeShim.ChaincodeMessage initMsg = MessageUtil.newEventMessage(INIT, "testChannel", "0", payload, null);

        List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());
        scenario.add(new CompleteStep());

        setLogLevel("DEBUG");
        server = ChaincodeMockPeer.startServer(scenario);

        cb.start(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});
        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 5000, TimeUnit.MILLISECONDS);
        server.send(initMsg);
        server.stop();
        server = null;
    }

    @Test
    public void testChaincodeLogLevel() throws Exception {
        ChaincodeBase cb = new EmptyChaincode();

        List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());
        scenario.add(new CompleteStep());

        setLogLevel("DEBUG");
        server = ChaincodeMockPeer.startServer(scenario);

        cb.start(new String[]{"-a", "127.0.0.1:7052", "-i", "testId"});

        assertEquals("Wrong debug level for " + cb.getClass().getPackage().getName(), Level.FINEST, Logger.getLogger(cb.getClass().getPackage().getName()).getLevel());

    }

    public void setLogLevel(String logLevel) {
        environmentVariables.set("CORE_CHAINCODE_LOGGING_SHIM", logLevel);
        environmentVariables.set("CORE_CHAINCODE_LOGGING_LEVEL", logLevel);
    }
}
