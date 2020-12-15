package fr.laposte.disbr.maven.plugins.tests;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import fr.laposte.disbr.maven.plugins.swaggerdiff.DiffMojo;

public class MyMojoTest
{
    @Rule
    public MojoRule rule = new MojoRule()
    {
        @Override
        protected void before() throws Throwable 
        {
        }

        @Override
        protected void after()
        {
        }
    };

    /**
     * @throws Exception if any
     */
    @Test
    public void testSomething()
            throws Exception
    {
        File pom = new File( "target/test-classes/project-to-test/" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        DiffMojo myMojo = ( DiffMojo ) rule.lookupConfiguredMojo( pom, "diff" );
        assertNotNull( myMojo );
        myMojo.execute();

        File outputFile = ( File ) rule.getVariableValueFromObject( myMojo, "outputFile" );
        assertNotNull( outputFile );
        assertTrue( outputFile.exists() );
    }

}

