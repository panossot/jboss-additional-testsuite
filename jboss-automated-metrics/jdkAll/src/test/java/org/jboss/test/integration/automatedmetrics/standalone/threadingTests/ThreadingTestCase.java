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
package org.jboss.test.integration.automatedmetrics.standalone.threadingTests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javax.inject.Inject;
import org.jboss.metrics.automatedmetricsapi.MetricsPropertiesApi;
import org.jboss.metrics.jbossautomatedmetricsproperties.MetricProperties;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.metrics.automatedmetricsapi.MetricsCacheApi;
import org.jboss.metrics.jbossautomatedmetricslibrary.MetricsCacheCollection;
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
public class ThreadingTestCase {

    @Inject 
    MetricsClass metricsClass;
    
    private String groupName = "myTestGroup";
    
    MetricsApiSessionBean metricsBean;
    
    MetricsApiSessionBean metricsBean2;

    @Deployment
    public static Archive<?> getDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.addClass(ThreadingTestCase.class);
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
    public void testServerStart() {
        initializeMetricProperties();

        try {
            MetricsThreads mTreads =  new MetricsThreads(metricsBean, 5, "1");
            mTreads.start();
         
            MetricsThreads mTreads2 =  new MetricsThreads(metricsBean2, 5, "2");
            mTreads2.start();
            
            MetricsThreads mTreads3 =  new MetricsThreads(metricsBean2, 5, "3");
            mTreads3.start();
            
            while (mTreads.getT().isAlive() || mTreads2.getT().isAlive() || mTreads3.getT().isAlive()){};
            
            if (MetricsCacheCollection.getMetricsCacheCollection().getMetricsCacheInstance(groupName)!=null)
                System.out.println(MetricsCacheApi.printMetricsCache(groupName));
            
            Set<String> metricNames = MetricsCacheApi.getMetricsCache(groupName).keySet();
            Iterator<String> iob = metricNames.iterator();
            while (iob.hasNext()) {
                String key = iob.next();
                if (key.contains("count2")) {
                    ArrayList<Object> comparableObject = new ArrayList<>();
                    comparableObject.add(2.0);
                    comparableObject.add(4.0);
                    comparableObject.add(6.0);
                    comparableObject.add(8.0);
                    comparableObject.add(10.0);
                    comparableObject.add(12.0);
                    comparableObject.add(14.0);
                    comparableObject.add(16.0);
                    comparableObject.add(18.0);
                    comparableObject.add(20.0);
                    comparableObject.add(22.0);
                    comparableObject.add(24.0);
                    comparableObject.add(26.0);
                    comparableObject.add(28.0);
                    comparableObject.add(30.0);
                    boolean correct = MetricsCacheApi.compareMetricsCacheValuesByKey(groupName, key, comparableObject);
                    assertTrue("Data are not contained in cache ... ", correct);
                }else if (key.contains("count")) {
                    ArrayList<Object> comparableObject = new ArrayList<>();
                    comparableObject.add(1.0);
                    comparableObject.add(2.0);
                    comparableObject.add(3.0);
                    comparableObject.add(4.0);
                    comparableObject.add(5.0);
                    comparableObject.add(6.0);
                    comparableObject.add(7.0);
                    comparableObject.add(8.0);
                    comparableObject.add(9.0);
                    comparableObject.add(10.0);
                    comparableObject.add(11.0);
                    comparableObject.add(12.0);
                    comparableObject.add(13.0);
                    comparableObject.add(14.0);
                    comparableObject.add(15.0);
                    boolean correct = MetricsCacheApi.compareMetricsCacheValuesByKey(groupName, key, comparableObject);
                    assertTrue("Data are not contained in cache ... ", correct);
                }
            }
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

