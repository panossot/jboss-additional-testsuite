/*
 * Copyleft 2015 Red Hat, Inc. and/or its affiliates
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
package org.jboss.additional.testsuite.jdkall.future.automatedmetrics.javase.dbStoreTests;

import java.util.concurrent.atomic.AtomicInteger;
import org.jboss.eap.additional.testsuite.annotations.EapAdditionalTestsuite;
import org.jboss.metrics.javase.automatedmetricsjavaseapi.JbossAutomatedJavaSeMetrics;
import org.jboss.metrics.javase.automatedmetricsjavaseapi.JbossAutomatedJavaSeMetricsSyncDbStore;
import org.jboss.metrics.jbossautomatedmetricslibrary2.CodeParamsCollection;

/**
 *
 * @author Panagiotis Sotiropoulos
 */
@EapAdditionalTestsuite({"modules/testcases/jdkAll/Wildfly/automatedmetrics/src/main/java"})
public class SyncDbStoreMetricsClass {
    private static AtomicInteger countAtomic;
    private static int count = 0;
    
    private static int count2 = 0;
    private static AtomicInteger i;
    private String metricUser="Niki";

    public SyncDbStoreMetricsClass() {
    }

    public void SyncDbStoreMetricsClass(){
        countAtomic = new AtomicInteger(1);
        i = new AtomicInteger(1);
    }
    
    public synchronized void getAndSetCountIncreased() throws Exception {
        count = this.countAtomic.getAndIncrement();
        CodeParamsCollection.getCodeParamsCollection().getCodeParamsInstance(metricUser).putIntegerCodeParam("sequenceNum", i.getAndIncrement());
        JbossAutomatedJavaSeMetrics.metric(this,count,"count","myTestGroup");
        JbossAutomatedJavaSeMetricsSyncDbStore.metricsDbStore(this, new Object[]{count}, "myTestGroup", "statement_1", new String[]{"StoreDBMetric","count"}, metricUser);
    }

    public synchronized void getAndSetCount2Increased() throws Exception {
        count2 = this.count2+2;
        CodeParamsCollection.getCodeParamsCollection().getCodeParamsInstance(metricUser).putIntegerCodeParam("sequenceNum", i.getAndIncrement());
        JbossAutomatedJavaSeMetrics.metric(this,count2,"count2","myTestGroup");
        JbossAutomatedJavaSeMetricsSyncDbStore.metricsDbStore(this, new Object[]{count2}, "myTestGroup", "statement_1", new String[]{"StoreDBMetric","count2"}, metricUser);
    }
    
}