/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.SKFSConstants;
import java.util.ArrayList;
import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 *
 * @author dbeach
 */
public class MDSPolicyOptions {
    private final ArrayList<MDSAuthenticatorStatusPolicy> statusReport;
    
    public MDSPolicyOptions(ArrayList<MDSAuthenticatorStatusPolicy> statusReport){
        this.statusReport = statusReport;
    }
    
    public ArrayList<MDSAuthenticatorStatusPolicy> getStatusReports(){
        return statusReport;
    }
   

    public static MDSPolicyOptions parse(JsonObject mdsJson){
        JsonArray ASRJsonArray = mdsJson.getJsonArray(SKFSConstants.POLICY_ATTR_MDS_STATUS_REPORT);
        ArrayList<MDSAuthenticatorStatusPolicy> ASRArrayList = new ArrayList<>();
        ASRJsonArray.forEach((ASRJson) -> {
            ASRArrayList.add(MDSAuthenticatorStatusPolicy.parse((JsonObject)ASRJson));
        });
        return new MDSPolicyOptionsBuilder(ASRArrayList).build();
    }
    
    public static class MDSPolicyOptionsBuilder{
        private final ArrayList<MDSAuthenticatorStatusPolicy> statusReportBuilder;

        public MDSPolicyOptionsBuilder(
            ArrayList<MDSAuthenticatorStatusPolicy> statusReportBuilder){
            this.statusReportBuilder = statusReportBuilder;
        }
        public MDSPolicyOptions build(){
            return new MDSPolicyOptions(statusReportBuilder);
        }
    
        
        
        
    }
}
