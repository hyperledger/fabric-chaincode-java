package org.hyperledger.fabric.example;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.protobuf.ByteString;
import io.netty.handler.ssl.OpenSsl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.impl.StateBasedEndorsementFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EndorsementCC extends ChaincodeBase {

    private static Log _logger = LogFactory.getLog(EndorsementCC.class);

    private static Map<String, Method> functions;

    static {
        functions = new HashMap<>();
        try {
            functions.put("addorgs", EndorsementCC.class.getMethod("addOrgs", ChaincodeStub.class));
            functions.put("delorgs", EndorsementCC.class.getMethod("delOrgs", ChaincodeStub.class));
            functions.put("listorgs", EndorsementCC.class.getMethod("listOrgs", ChaincodeStub.class));
            functions.put("delep", EndorsementCC.class.getMethod("delEP", ChaincodeStub.class));
            functions.put("setval", EndorsementCC.class.getMethod("setVal", ChaincodeStub.class));
            functions.put("getval", EndorsementCC.class.getMethod("getVal", ChaincodeStub.class));
            functions.put("cc2cc", EndorsementCC.class.getMethod("invokeCC", ChaincodeStub.class));
        } catch (NoSuchMethodException e) {
            _logger.error(e);
        }
    }

    @Override
    public Response init(ChaincodeStub stub) {
        try {
            _logger.info("Init java EndorsementCC");
            stub.putStringState("endorsed_state", "foo");
            return newSuccessResponse();
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            _logger.info("Invoking java EndorsementCC");
            String funcName = stub.getFunction();

            if (functions.containsKey(funcName)) {
                return (Response) functions.get(funcName).invoke(this, stub);
            }
            return newErrorResponse("Unknown function " + funcName);
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    public Response addOrgs(ChaincodeStub stub) {
        try {
            _logger.info("Invoking addOrgs");
            List<String> parameters = stub.getParameters();
            if (parameters.isEmpty()) {
                return newErrorResponse("No orgs to add specified");
            }

            byte[] epBytes = stub.getStateValidationParameter("endorsed_state");
            StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(epBytes);
            ep.addOrgs(StateBasedEndorsement.RoleType.RoleTypePeer, parameters.toArray(new String[]{}));
            epBytes = ep.policy();
            stub.setStateValidationParameter("endorsed_state", epBytes);

            return newSuccessResponse(new byte[]{});

        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    public Response delOrgs(ChaincodeStub stub) {
        try {
            _logger.info("Invoking delOrgs");
            List<String> parameters = stub.getParameters();
            if (parameters.isEmpty()) {
                return newErrorResponse("No orgs to delete specified");
            }

            byte[] epBytes = stub.getStateValidationParameter("endorsed_state");
            StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(epBytes);
            ep.delOrgs(parameters.toArray(new String[]{}));
            epBytes = ep.policy();
            stub.setStateValidationParameter("endorsed_state", epBytes);

            return newSuccessResponse(new byte[]{});
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    public Response listOrgs(ChaincodeStub stub) {
        try {
            _logger.info("Invoking listOrgs");
            byte[] epBytes = stub.getStateValidationParameter("endorsed_state");
            StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(epBytes);

            List<String> orgs = ep.listOrgs();
            JsonArray orgsList = new JsonArray();
            orgs.forEach(org -> orgsList.add(org));
            return newSuccessResponse(orgsList.toString().getBytes());
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    public Response delEP(ChaincodeStub stub) {
        try {
            _logger.info("Invoking delEP");
            stub.setStateValidationParameter("endorsed_state", null);
            return newSuccessResponse(new byte[]{});
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    public Response setVal(ChaincodeStub stub) {
        try {
            _logger.info("Invoking setVal");
            List<String> parameters = stub.getParameters();
            if (parameters.size() != 1) {
                return newErrorResponse("setval expects one argument");
            }

            stub.putStringState("endorsed_state", parameters.get(0));
            return newSuccessResponse(new byte[]{});
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    public Response getVal(ChaincodeStub stub) {
        try {
            _logger.info("Invoking getVal");

            return newSuccessResponse(stub.getState("endorsed_state"));
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    public Response invokeCC(ChaincodeStub stub) {
        try {
            _logger.info("Invoking invokeCC");
            List<byte[]> args = stub.getArgs();
            if (args.size() < 3) {
                return newErrorResponse("cc2cc expects at least two arguments (channel and chaincode)");
            }
            String channel = new String(args.get(0), UTF_8);
            String cc = new String(args.get(1), UTF_8);

            List<byte[]> nargs = args.subList(2, args.size());

            return stub.invokeChaincode(cc, nargs, channel);
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    public static void main(String[] args) {
        System.out.println("OpenSSL avaliable: " + OpenSsl.isAvailable());
        new EndorsementCC().start(args);
    }

}
