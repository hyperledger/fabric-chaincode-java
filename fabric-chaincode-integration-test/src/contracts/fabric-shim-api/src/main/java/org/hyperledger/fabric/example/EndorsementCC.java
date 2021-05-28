package org.hyperledger.fabric.example;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.impl.StateBasedEndorsementFactory;

import com.google.gson.JsonArray;

@Contract()
public class EndorsementCC implements ContractInterface {

    private static Log _logger = LogFactory.getLog(EndorsementCC.class);
   
    public void setup(Context ctx) {
            _logger.info("Init java EndorsementCC");
            ctx.getStub().putStringState("pub", "foo");
            _logger.info("Init done");
    }

    @Transaction()
    public void addorgs(Context ctx, String type, String orgs) {

        _logger.info("Invoking addOrgs");

        byte[] epBytes;
        if ("pub".equals(type)) {
            epBytes = ctx.getStub().getStateValidationParameter("pub");
        } else if ("priv".equals(type)) {
            epBytes = ctx.getStub().getPrivateDataValidationParameter("col", "priv");
        } else {
            throw new RuntimeException("Unknown key specified");
        }

        StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(epBytes);
        ep.addOrgs(StateBasedEndorsement.RoleType.RoleTypePeer, orgs);
        epBytes = ep.policy();
        if ("pub".equals(type)) {
            ctx.getStub().setStateValidationParameter("pub", epBytes);
        } else {
            ctx.getStub().setPrivateDataValidationParameter("col", "priv", epBytes);
        }


    }
    
    @Transaction()
    public void delorgs(Context ctx, String type, String orgs) {
    
            _logger.info("Invoking delOrgs");


            byte[] epBytes;
            if ("pub".equals(type)) {
                epBytes = ctx.getStub().getStateValidationParameter("pub");
            } else if ("priv".equals(type)) {
                epBytes = ctx.getStub().getPrivateDataValidationParameter("col", "priv");
            } else {
                throw new RuntimeException("Unknown key specified");
            }

            StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(epBytes);
            // delete organizations from the endorsement policy of that key
            ep.delOrgs(orgs);
            epBytes = ep.policy();
            if ("pub".equals(type)) {
                ctx.getStub().setStateValidationParameter("pub", epBytes);
            } else {
                ctx.getStub().setPrivateDataValidationParameter("col", "priv", epBytes);
            }

            ctx.getStub().setStateValidationParameter("endorsed_state", epBytes);

    }

    @Transaction()
    public String listorgs(Context ctx, String type) {
     
            _logger.info("Invoking listOrgs");

            byte[] epBytes;
            if ("pub".equals(type)) {
                epBytes = ctx.getStub().getStateValidationParameter("pub");
            } else if ("priv".equals(type)) {
                epBytes = ctx.getStub().getPrivateDataValidationParameter("col", "priv");
            } else {
                throw new RuntimeException("Unknown key specified");
            }
            StateBasedEndorsement ep = StateBasedEndorsementFactory.getInstance().newStateBasedEndorsement(epBytes);

            List<String> orgs = ep.listOrgs();
            JsonArray orgsList = new JsonArray();
            orgs.forEach(org -> orgsList.add(org));

            String s = orgsList.toString();
            _logger.info("orgsList "+s);
            
            return orgsList.toString();

    }
    @Transaction()
    public void delEP(Context ctx, String type) {
        
            _logger.info("Invoking delEP");

            if ("pub".equals(type)) {
                ctx.getStub().setStateValidationParameter("pub", null);
            } else if ("priv".equals(type)) {
                ctx.getStub().setPrivateDataValidationParameter("col", "priv", null);
            } else {
                throw new RuntimeException("Unknown key specified");
            }
            
    }
    @Transaction()
    public void setval(Context ctx, String type, String value) {
            _logger.info("Invoking setVal");

            if ("pub".equals(type)) {
                ctx.getStub().putStringState("pub", value);
            } else if ("priv".equals(type)) {
                ctx.getStub().putPrivateData("col", "priv",value);
            } else {
               throw new RuntimeException("Unknown key specified");
            }
    }
    @Transaction()
    public String getval(Context ctx, String type) {
            _logger.info("Invoking getVal");
            byte[]  buffer;
            if ("pub".equals(type)) {
                buffer =  (ctx.getStub().getState("pub"));
            } else if ("priv".equals(type)) {
               buffer = (ctx.getStub().getPrivateData("col", "priv"));
            } else {
                throw new RuntimeException("Unknown key specified");
            }
            
     
            String value = new String(buffer,UTF_8);
            return value;
    }

}
