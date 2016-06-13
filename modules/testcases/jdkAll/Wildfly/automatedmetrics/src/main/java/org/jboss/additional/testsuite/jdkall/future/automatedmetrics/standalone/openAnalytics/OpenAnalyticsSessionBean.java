/*
 * Copyleft 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.additional.testsuite.jdkall.future.automatedmetrics.standalone.openAnalytics;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import org.jboss.eap.additional.testsuite.annotations.EapAdditionalTestsuite;
import org.jboss.metrics.automatedmetricsapi.JBossOpenAnalytics;

/**
 *
 * @author Panagiotis Sotiropoulos
 */
@Stateful
@LocalBean
@EapAdditionalTestsuite({"modules/testcases/jdkAll/Wildfly/automatedmetrics/src/main/java"})
public class OpenAnalyticsSessionBean {

    private int count = 0;
    
    private int count2 = 0;
    
    private String username = "Niki";

    public OpenAnalyticsSessionBean() {
    }

    @JBossOpenAnalytics(userRecord=true,className="OpenAnalyticsSessionBean",methodName="countMethod",userName="username",recordDbName="MyMETRICS", 
            recordTableName="openAnalyticsValues",locationDbName="MyMETRICS", locationTableName="openAnalyticsLocationData",
            dbStatement="jboss_analytics_statement", locationDbStatement="jboss_analytics_location_data_statement", groupName="myTestGroup")
    public int countMethod() {
        count++;
        count2 += 2;
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(OpenAnalyticsSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return count;
    }

}
