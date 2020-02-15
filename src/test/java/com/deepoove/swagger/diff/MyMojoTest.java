package com.deepoove.swagger.diff;


import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;

import com.deepoove.swagger.diff.DiffMojo;

import java.io.File;

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

