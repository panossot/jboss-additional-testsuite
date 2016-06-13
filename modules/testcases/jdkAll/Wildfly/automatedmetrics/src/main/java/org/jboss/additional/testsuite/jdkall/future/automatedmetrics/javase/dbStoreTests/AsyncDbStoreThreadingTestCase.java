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

import org.jboss.metrics.jbossautomatedmetricsproperties.MetricProperties;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.eap.additional.testsuite.annotations.EapAdditionalTestsuite;
import org.jboss.metrics.javase.automatedmetricsjavaseapi.CodeParamsApi;
import org.jboss.metrics.javase.automatedmetricsjavaseapi.MetricsPropertiesApi;
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
@EapAdditionalTestsuite({"modules/testcases/jdkAll/Wildfly/automatedmetrics/src/main/java"})
public class AsyncDbStoreThreadingTestCase {

    MetricsClass metricsClass;
    
    private String groupName = "myTestGroup";
    
    MetricsApiSessionBean metricsBean;
    
    MetricsApiSessionBean metricsBean2;
    
    Statement stmt = null;
    Connection  connection = null;

    @Deployment
    public static Archive<?> getDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.addClass(AsyncDbStoreThreadingTestCase.class);
        archive.addClass(MetricsApiSessionBean.class);
        archive.addClass(MetricsThreads.class);
        archive.addClass(MetricsClass.class);
        archive.addPackage("org.jboss.metrics.jbossautomatedmetricsproperties");
        archive.addPackage("org.jboss.metrics.javase.automatedmetricsjavaseapi");
        archive.addPackage("org.jboss.metrics.automatedmetricsjavase");
        archive.addPackage("org.jboss.metrics.jbossautomatedmetricslibrary");
        archive.addPackage("org.jboss.metrics.jbossautomatedmetricslibrary2");
        return archive;
    }
    
    @Test
    public void asyncDbStoreThreadingTest() {
        initializeMetricProperties();
        metricsClass.MetricsClass();

        try {
            MetricsThreads mTreads =  new MetricsThreads(metricsBean, "1");
            mTreads.start();
         
            MetricsThreads mTreads2 =  new MetricsThreads(metricsBean2, "2");
            mTreads2.start();
            
            MetricsThreads mTreads3 =  new MetricsThreads(metricsBean2, "3");
            mTreads3.start();
            
            Thread.sleep(1000);
            
            for (int i=1; i <=15; i++) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) from MyMETRICS.metricValues where METRIC_NAME='count' and METRIC_VALUE='" + i + "';");
                if (rs.next() && rs.getInt(1)!=1)
                    assertTrue("Not all data stored in the database ... ", false);
            }
            
            for (int i=1; i <=15; i++) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) from MyMETRICS.metricValues where METRIC_NAME='count2' and METRIC_VALUE='" + 2*i + "';");
                if (rs.next() && rs.getInt(1)!=1)
                    assertTrue("Not all data stored in the database ... ", false);
            }
      
        } catch(Exception e) {
            e.printStackTrace();
            assertFalse("No data in the database store ... ", true);
        } finally {
            try {
                stmt.close();
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(AsyncDbStoreThreadingTestCase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void initializeMetricProperties() {
        metricsClass = new MetricsClass();
        metricsBean = new MetricsApiSessionBean(metricsClass);
        metricsBean2 = new MetricsApiSessionBean(metricsClass);
        
        MetricProperties metricProperties = new MetricProperties();
        metricProperties.setCacheStore("false");
        metricProperties.setCacheMaxSize(10000);
        metricProperties.setDatabaseStore("true");
        HashMap<String,Integer> dbUpdateRates = new HashMap<>();
        dbUpdateRates.put("StoreDBMetric", 5);
        metricProperties.setUpdateRateOfDbQueries(dbUpdateRates);
        CodeParamsApi.addUserName("Niki");
        try {
            DataSource ds = null;
            InitialContext ic; 
            ic = new InitialContext();
            ds = (DataSource)ic.lookup("java:jboss/datasources/ExampleDS");
            connection = ds.getConnection(); 
            stmt = connection.createStatement();
            createDbTable(stmt);
            HashMap<String,Statement> dbStmt = new HashMap<String,Statement>();
            dbStmt.put("statement_1", stmt);
            metricProperties.setDatabaseStatement(dbStmt);
            HashMap<String,String> query1 = new HashMap<String,String>();
            query1.put("StoreDBMetric", "INSERT INTO MyMETRICS.metricValues(SEQUENCE_NUM,METRIC_NAME,METRIC_VALUE,METRIC_INSTANCE,RECORD_TIME) VALUES(#sequenceNum#, '{1}', [1], '{instance}', '{time}');");
            metricProperties.setUpdateDbQueries(query1);
        } catch(Exception e) {
            e.printStackTrace();
        }
        MetricsPropertiesApi.storeProperties(groupName, metricProperties);
    }

    private void createDbTable(Statement stmt) {
        try {
            String query = "DROP SCHEMA MyMETRICS;";
            
            try {
                stmt.executeUpdate(query);      
            } catch (Exception e) {
            }
            
            query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'MyMETRICS' AND table_name = 'metricValues';";
            ResultSet rs = stmt.executeQuery(query);                  
            rs.next();
            boolean exists = rs.getInt("COUNT(*)") > 0;
            
            if (!exists) {
                String sql = "CREATE SCHEMA MyMETRICS;";
                stmt.executeUpdate(sql);
                System.out.println("Schema created successfully...");

                sql = "CREATE TABLE MyMETRICS.metricValues(ID int NOT NULL AUTO_INCREMENT, SEQUENCE_NUM int, METRIC_NAME varchar(255) NOT NULL," +
                      " METRIC_VALUE varchar(255) NOT NULL, METRIC_INSTANCE varchar(255), RECORD_TIME DATETIME, PRIMARY KEY(ID));"; 
                
                stmt.executeUpdate(sql);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

}


