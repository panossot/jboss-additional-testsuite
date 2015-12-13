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
package org.jboss.test.integration.automatedmetrics.javase.threadingTests;

import org.jboss.metrics.javase.automatedmetricsjavaseapi.JbossAutomatedJavaSeMetrics;


/**
 *
 * @author Panagiotis Sotiropoulos
 */
public class MetricsClass {
    private static volatile int count = 0;
    
    private static volatile int count2 = 0;

    public MetricsClass() {
    }
    
    public synchronized int getCount() {
        return count;
    }

    public synchronized void setCount(int count) throws Exception {
        this.count = count;
        JbossAutomatedJavaSeMetrics.metric(this,count,"count","myTestGroup");
    }

    public synchronized int getCount2() {
        return count2;
    }

    public synchronized void setCount2(int count2) throws Exception {
        this.count2 = count2;
        JbossAutomatedJavaSeMetrics.metric(this,count2,"count2","myTestGroup");
    }
    
}
