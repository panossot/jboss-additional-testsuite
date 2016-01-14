/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.logging;

import java.io.File;
import java.io.FilenameFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.logging.Logger;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import org.junit.Before;


@RunWith(Arquillian.class)
public class LoggingTest {

    private static final Logger LOGGER = Logger.getLogger(LoggingTest.class);

    @Deployment
    public static Archive<?> getDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        archive.addClass(LoggingTest.class);
        return archive;
    }

    @Before
    public void setup() {
        ClassLoader classLoader = LoggingTest.class.getClassLoader();
        File classpathRoot = new File(classLoader.getResource("").getPath());
        final File folder = new File(classpathRoot.getPath() + "/../../../../../../../../../../standalone/log");
	final File[] files = folder.listFiles( new FilenameFilter() {
	    @Override
	    public boolean accept( final File dir,
		                   final String name ) {
		return name.matches( "PSRFILE\\.log.*" );
	    }
	} );
	for ( final File file : files ) {
	    if ( !file.delete() ) {
		System.err.println( "Can't remove " + file.getAbsolutePath() );
	    }
	}
    }
    
    @Test
    public void testRotationLogging() {

        for (int i=0; i<100; i++)
            LOGGER.info("Testing the rotation logging of the files ...");

        ClassLoader classLoader = LoggingTest.class.getClassLoader();
        File classpathRoot = new File(classLoader.getResource("").getPath());
        final File folder = new File(classpathRoot.getPath() + "/../../../../../../../../../../standalone/log");
	final File[] files = folder.listFiles( new FilenameFilter() {
	    @Override
	    public boolean accept( final File dir,
		                   final String name ) {
		return name.matches( "PSRFILE\\.log.*" );
	    }
	} );
	
        int filesNum = files.length;
        if (filesNum < 2 || filesNum > 6)
           fail();
            

        assertFalse("Running a logging arquillian test : testRotationLogging() failed... ", false);
    }
}
