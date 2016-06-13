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

import javax.inject.Inject;
import org.jboss.metrics.automatedmetricsapi.MetricsPropertiesApi;
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
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.eap.additional.testsuite.annotations.EapAdditionalTestsuite;
import org.jboss.metrics.automatedmetricsapi.CodeParamsApi;
import org.jboss.metrics.automatedmetricsapi.MetricsCacheApi;
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
 *
 * The following system property needs to be adding in the standalone.xml for the test to be executed :
 * <system-properties>
 * <property name="jboss.id" value="testing-instance"/>
 * </system-properties>
 */
@RunWith(Arquillian.class)
@EapAdditionalTestsuite({"modules/testcases/jdkAll/Wildfly/automatedmetrics/src/main/java"})
public class OpenAnalyticsTestCase {

    @EJB
    private OpenAnalyticsSessionBean openAnalyticsSessionBean;

    private String groupName = "myTestGroup";

    Statement stmt = null;
    Connection connection = null;

    @Deployment
    public static Archive<?> getDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.addClass(OpenAnalyticsTestCase.class);
        archive.addClass(OpenAnalyticsSessionBean.class);
        archive.addPackage("org.jboss.metrics.jbossautomatedmetricsproperties");
        archive.addPackage("org.jboss.metrics.automatedmetricsapi");
        archive.addPackage("org.jboss.metrics.jbossautomatedmetricslibrary");
        archive.addPackage("org.jboss.metrics.jbossautomatedmetricslibrary2");
        archive.addPackage("org.jboss.metrics.automatedmetrics");
        archive.addAsResource("META-INF/beans.xml");
        return archive;
    }

    @Test
    public void openAnalyticsTest() {
        initializeMetricProperties();

        try {
            openAnalyticsSessionBean.countMethod();
            Thread.sleep(1000);
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) from MyMETRICS.openAnalyticsValues;");
            if (rs.next() && rs.getInt(1) == 0) {
                assertTrue("No location data stored in the database ... ", false);
            }

            rs = stmt.executeQuery("SELECT * from MyMETRICS.openAnalyticsValues;");
            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
            assertFalse("No location data in the database ... ", true);
        } finally {
            try {
                stmt.close();
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(OpenAnalyticsTestCase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void initializeMetricProperties() {
        MetricProperties metricProperties = new MetricProperties();
        metricProperties.setJBossOpenAnalytics("true");
        CodeParamsApi.addUserName("Niki");
        try {
            DataSource ds = null;
            InitialContext ic;
            ic = new InitialContext();
            ds = (DataSource) ic.lookup("java:jboss/datasources/ExampleDS");
            connection = ds.getConnection();
            stmt = connection.createStatement();
            createDbTable(stmt);
            HashMap<String, Statement> dbStmt = new HashMap<String, Statement>();
            dbStmt.put("jboss_analytics_statement", stmt);
            dbStmt.put("jboss_analytics_location_data_statement", stmt);
            metricProperties.setDatabaseStatement(dbStmt);
        } catch (Exception e) {
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

            query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'MyMETRICS' AND table_name = 'openAnalyticsValues'";
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            boolean exists = rs.getInt("COUNT(*)") > 0;

            if (!exists) {
                String sql = "CREATE SCHEMA MyMETRICS";
                try {
                    stmt.executeUpdate(sql);
                    System.out.println("Schema created successfully...");
                } catch (Exception ex) {
                    System.out.println("Schema already exists...");
                }
                sql = "CREATE TABLE MyMETRICS.openAnalyticsValues(ID int NOT NULL AUTO_INCREMENT, IP_RECORD varchar(255),"
                        + "LOCATION_RECORD varchar(255),NUMACCESS_RECORD varchar(255),TIMEACCESS_RECORD varchar(255),METHOD_NAME varchar(255),"
                        + "CLASS_NAME varchar(255),INSTANCE varchar(255),USER_NAME varchar(255),RECORD_TIME DATETIME,PRIMARY KEY(ID));";

                stmt.executeUpdate(sql);
            }

            query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'MyMETRICS' AND table_name = 'openAnalyticsLocationData'";
            rs = stmt.executeQuery(query);
            rs.next();
            exists = rs.getInt("COUNT(*)") > 0;

            if (!exists) {
                String sql = "CREATE TABLE MyMETRICS.openAnalyticsLocationData(SERVER_INSTANCE_NAME varchar(255) NOT NULL,"
                        + "SERVER_INSTANCELOCATION varchar(255),PRIMARY KEY(SERVER_INSTANCE_NAME));";

                stmt.executeUpdate(sql);

                sql = "INSERT INTO MyMETRICS.openAnalyticsLocationData(SERVER_INSTANCE_NAME,SERVER_INSTANCELOCATION) VALUES('testing-instance','CZ, Brno, Red Hat Office, TPB, 2nd Floor, South');";
                stmt.executeUpdate(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
