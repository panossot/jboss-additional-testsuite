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
package org.jboss.test.automatedmetrics.javase.metricClassTests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.metrics.javase.automatedmetricsjavaseapi.MetricsCacheApi;
import org.jboss.metrics.javase.automatedmetricsjavaseapi.MetricsPropertiesApi;
import org.jboss.metrics.jbossautomatedmetricsproperties.MetricProperties;
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
public class CacheStorageMetricClassTestCase {

    @EJB
    private MetricsApiSessionBean metricsApiSessionBean;
    
    private String groupName = "myTestGroup";

    @Deployment
    public static Archive<?> getDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.addClass(CacheStorageMetricClassTestCase.class);
        archive.addClass(MetricsApiSessionBean.class);
        archive.addClass(MetricsClass.class);
        archive.addPackage("org.jboss.metrics.jbossautomatedmetricsproperties");
        archive.addPackage("org.jboss.metrics.javase.automatedmetricsjavaseapi");
        archive.addPackage("org.jboss.metrics.automatedmetricsjavase");
        archive.addPackage("org.jboss.metrics.jbossautomatedmetricslibrary");;
        return archive;
    }
    
    @Test
    public void cacheStorageMetricClassTest() {
        initializeMetricProperties();

        try {
            metricsApiSessionBean.countMethod();
            Set<String> metricNames = MetricsCacheApi.getMetricsCache(groupName).keySet();
            Iterator<String> iob = metricNames.iterator();
            while (iob.hasNext()) {
                String key = iob.next();
                if (key.contains("count2")) {
                    ArrayList<Object> comparableObject = new ArrayList<>();
                    comparableObject.add(2.0);
                    boolean correct = MetricsCacheApi.compareMetricsCacheValuesByKey(groupName, key, comparableObject);
                    assertTrue("Data are not contained in cache ... ", correct);
                }else if (key.contains("count")) {
                    ArrayList<Object> comparableObject = new ArrayList<>();
                    comparableObject.add(1.0);
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
        MetricProperties metricProperties = new MetricProperties();
        metricProperties.setCacheStore("true");
        MetricsPropertiesApi.storeProperties(groupName, metricProperties);
    }
}


