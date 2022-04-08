package fr.laposte.disbr.maven.plugins.tests;


import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

import fr.laposte.disbr.maven.plugins.swaggerdiff.DiffMojo;

public class MyMojoTest extends AbstractMojoTestCase
{
    protected void setUp() throws Exception {
 
        // required for mojo lookups to work
        super.setUp();
 
    }

    /**
     * @throws Exception if any
     */
    @Test
    public void testSomething()
            throws Exception
    {
        File pom = new File(getBasedir(), "target/test-classes/project-to-test/pom.xml" );
        assertNotNull( pom );
        assertTrue( pom.exists() );
        
        
        DiffMojo myMojo = ( DiffMojo ) lookupMojo ("diff", pom );
        assertNotNull( myMojo );

        myMojo.execute();

        // File outputFile = ( File ) getPluginArtifactFile( myMojo, "outputFile" );
        // assertNotNull( outputFile );
        // assertTrue( outputFile.exists() );
    }

}

