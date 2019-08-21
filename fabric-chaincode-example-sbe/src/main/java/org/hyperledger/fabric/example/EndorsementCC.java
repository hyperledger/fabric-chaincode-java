package org.hyperledger.fabric.example;

import com.google.gson.JsonArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.impl.StateBasedEndorsementFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            e.printStackTrace();
            _logger.error(e);
        }
    }

    @Override
    public Response init(ChaincodeStub stub) {
        try {
            _logger.info("Init java EndorsementCC");
            stub.putStringState("pub", "foo");
            _logger.info("Init done");
            return newSuccessResponse();
        } catch (Throwable e) {
            e.printStackTrace();
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
            e.printStackTrace();
            return newErrorResponse(e);
        }
    }

    public Response addOrgs(ChaincodeStub stub) {
        try {
            _logger.info("Invoking addOrgs");
            List<String> parameters = stub.getParameters();
            if (parameters.size() < 2) {
                return newErrorResponse("No orgs to add specified");
            }

            byte[] epBytes;
            if ("pub".equals(parameters.get(0))) {
                epBytes = stub.getStateValidationParameter("pub");
            } else if ("priv".equals(parameters.get(0))) {
                epBytes = stub.getPrivateDataValidationParameter("col", "priv");
            } else {
                return newErrorResponse("Unknown key specified");
            }

            StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(epBytes);
            ep.addOrgs(StateBasedEndorsement.RoleType.RoleTypePeer, parameters.subList(1, parameters.size()).toArray(new String[]{}));
            epBytes = ep.policy();
            if ("pub".equals(parameters.get(0))) {
                stub.setStateValidationParameter("pub", epBytes);
            } else {
                stub.setPrivateDataValidationParameter("col", "priv", epBytes);
            }

            return newSuccessResponse(new byte[]{});

        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    public Response delOrgs(ChaincodeStub stub) {
        try {
            _logger.info("Invoking delOrgs");
            List<String> parameters = stub.getParameters();
            if (parameters.size() < 2) {
                return newErrorResponse("No orgs to delete specified");
            }

            byte[] epBytes;
            if ("pub".equals(parameters.get(0))) {
                epBytes = stub.getStateValidationParameter("pub");
            } else if ("priv".equals(parameters.get(0))) {
                epBytes = stub.getPrivateDataValidationParameter("col", "priv");
            } else {
                return newErrorResponse("Unknown key specified");
            }

            StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(epBytes);
            // delete organizations from the endorsement policy of that key
            ep.delOrgs(parameters.subList(1, parameters.size()).toArray(new String[]{}));
            epBytes = ep.policy();
            if ("pub".equals(parameters.get(0))) {
                stub.setStateValidationParameter("pub", epBytes);
            } else {
                stub.setPrivateDataValidationParameter("col", "priv", epBytes);
            }

            stub.setStateValidationParameter("endorsed_state", epBytes);

            return newSuccessResponse(new byte[]{});
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    public Response listOrgs(ChaincodeStub stub) {
        try {
            _logger.info("Invoking listOrgs");
            List<String> parameters = stub.getParameters();
            if (parameters.size() < 1) {
                return newErrorResponse("No key specified");
            }

            byte[] epBytes;
            if ("pub".equals(parameters.get(0))) {
                epBytes = stub.getStateValidationParameter("pub");
            } else if ("priv".equals(parameters.get(0))) {
                epBytes = stub.getPrivateDataValidationParameter("col", "priv");
            } else {
                return newErrorResponse("Unknown key specified");
            }
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
            List<String> parameters = stub.getParameters();
            if (parameters.size() < 1) {
                return newErrorResponse("No key specified");
            }

            if ("pub".equals(parameters.get(0))) {
                stub.setStateValidationParameter("pub", null);
            } else if ("priv".equals(parameters.get(0))) {
                stub.setPrivateDataValidationParameter("col", "priv", null);
            } else {
                return newErrorResponse("Unknown key specified");
            }
            return newSuccessResponse(new byte[]{});
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    public Response setVal(ChaincodeStub stub) {
        try {
            _logger.info("Invoking setVal");
            List<String> parameters = stub.getParameters();
            if (parameters.size() != 2) {
                return newErrorResponse("setval expects two arguments");
            }

            if ("pub".equals(parameters.get(0))) {
                stub.putStringState("pub", parameters.get(1));
                _logger.info("Put state "+parameters.get(1));
            } else if ("priv".equals(parameters.get(0))) {
                stub.putPrivateData("col", "priv", parameters.get(1));
                _logger.info("Put Private  "+parameters.get(1));
            } else {
                return newErrorResponse("Unknown key specified");
            }
            return newSuccessResponse(new byte[]{});
        } catch (Throwable e) {
            e.printStackTrace();
            return newErrorResponse(e);
        }
    }

    public Response getVal(ChaincodeStub stub) {
        try {
            _logger.info("Invoking getVal");
            List<String> parameters = stub.getParameters();
            if (parameters.size() != 1) {
                return newErrorResponse("setval expects one argument");
            }

            if ("pub".equals(parameters.get(0))) {
                _logger.info(stub.getState("pub"));
                return newSuccessResponse((byte[])stub.getState("pub"));
            } else if ("priv".equals(parameters.get(0))) {
                byte[] d = stub.getPrivateData("col", "priv");
                _logger.info("get privateData" + new String(d,UTF_8));

                return newSuccessResponse((byte[])d);
            } else {
                return newErrorResponse("Unknown key specified");
            }
        } catch (Throwable e) {
            e.printStackTrace();
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
            String channel = new String(args.get(1), UTF_8);
            String cc = new String(args.get(2), UTF_8);

            List<byte[]> nargs = args.subList(3, args.size());

            return stub.invokeChaincode(cc, nargs, channel);
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    public static void main(String[] args) {
        new EndorsementCC().start(args);
    }

}
