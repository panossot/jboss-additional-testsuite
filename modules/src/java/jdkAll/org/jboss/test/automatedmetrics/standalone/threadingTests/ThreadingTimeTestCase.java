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
package org.jboss.test.automatedmetrics.standalone.threadingTests;

import javax.inject.Inject;
import org.jboss.metrics.automatedmetricsapi.MetricsPropertiesApi;
import org.jboss.metrics.jbossautomatedmetricsproperties.MetricProperties;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Panagiotis Sotiropoulos
 */
@RunWith(Arquillian.class)
public class ThreadingTimeTestCase {

    @Inject 
    MetricsClass metricsClass;
    
    private String groupName = "myTestGroup";
    
    MetricsApiSessionBean metricsBean;
    
    MetricsApiSessionBean metricsBean2;

    @Deployment
    public static Archive<?> getDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.addClass(ThreadingTimeTestCase.class);
        archive.addClass(MetricsApiSessionBean.class);
        archive.addClass(MetricsThreads.class);
        archive.addClass(MetricsClass.class);
        archive.addPackage("org.jboss.metrics.jbossautomatedmetricsproperties");
        archive.addPackage("org.jboss.metrics.automatedmetricsapi");
        archive.addPackage("org.jboss.metrics.jbossautomatedmetricslibrary");
        archive.addPackage("org.jboss.metrics.automatedmetrics");
        archive.addAsResource("META-INF/beans.xml");
        return archive;
    }
    
    @Test
    public void threadingTimeTest() {
        initializeMetricProperties();

        try {
            MetricsThreads mTreads =  new MetricsThreads(metricsBean, 33333, "1");
            MetricsThreads mTreads2 =  new MetricsThreads(metricsBean2, 333333, "2");
            MetricsThreads mTreads3 =  new MetricsThreads(metricsBean2, 333333, "3");

            
            System.out.println("Starting ... ");
            long timeInit, timeNow, averageTimeNeeded;
            
            timeInit = System.nanoTime();
            mTreads.start();
            mTreads2.start();
            mTreads3.start();
            
            while (mTreads.getT().isAlive() || mTreads2.getT().isAlive() || mTreads3.getT().isAlive()){};
            
            timeNow = System.nanoTime();
            
            averageTimeNeeded = (timeNow - timeInit) / (333333*3*2);
            System.out.println("averageTimeNeeded : " + averageTimeNeeded);
            System.out.println("Done ...");
            
            assertTrue("No data in the cache store ... ", averageTimeNeeded < 6000);
            
        } catch(Exception e) {
            e.printStackTrace();
            assertFalse("No data in the cache store ... ", true);
        }
    }

    private void initializeMetricProperties() {
        metricsBean = new MetricsApiSessionBean(metricsClass);
        metricsBean2 = new MetricsApiSessionBean(metricsClass);
        
        MetricProperties metricProperties = new MetricProperties();
        metricProperties.setCacheStore("true");
        MetricsPropertiesApi.storeProperties(groupName, metricProperties);
    }
}


