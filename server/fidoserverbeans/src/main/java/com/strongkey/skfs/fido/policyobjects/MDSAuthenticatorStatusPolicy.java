/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.SKFSConstants;
import javax.json.JsonObject;

/**
 *
 * @author dbeach
 */
public class MDSAuthenticatorStatusPolicy {
    private final String status;
    private final String priority;
    private final String decision;
   

    private MDSAuthenticatorStatusPolicy( String status,
    String effectiveDate,
    String decision){
        this.status = status;
        this.priority = effectiveDate;
        this.decision = decision;
        
        
    }

    public String getStatus() {
        return status;
    }
    public String getPriority() {
        return priority;
    }
    public String getDecision() {
        return decision;
    }
  
    
          
    

    public static MDSAuthenticatorStatusPolicy parse(JsonObject statusReportJson) {


        return new MDSAuthenticatorStatusPolicyBuilder(
                statusReportJson.getString(SKFSConstants.POLICY_ATTR_MDS_STATUS_REPORT_STATUS),
                statusReportJson.getString(SKFSConstants.POLICY_ATTR_MDS_STATUS_REPORT_PRIORITY),
                statusReportJson.getString(SKFSConstants.POLICY_ATTR_MDS_STATUS_REPORT_DECISION)
        ).build();
    }

    public static class MDSAuthenticatorStatusPolicyBuilder{
         private final String statusBuilder;
        private final String priorityBuilder;
        private final String decisionBuilder;
        

        public MDSAuthenticatorStatusPolicyBuilder( String statusBuilder,
            String priorityBuilder,
            String decisionBuilder){
            this.statusBuilder = statusBuilder;
            this.priorityBuilder = priorityBuilder;
            this.decisionBuilder = decisionBuilder;
        }

        public MDSAuthenticatorStatusPolicy build(){
            return new MDSAuthenticatorStatusPolicy(statusBuilder,
            priorityBuilder,
            decisionBuilder );
        }
    }
}
