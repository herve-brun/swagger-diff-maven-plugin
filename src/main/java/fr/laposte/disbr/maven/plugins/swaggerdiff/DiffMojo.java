package fr.laposte.disbr.maven.plugins.swaggerdiff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.output.MarkdownRender;
import com.deepoove.swagger.diff.output.Render;

import fr.laposte.disbr.maven.plugins.swaggerdiff.out.HtmlRender;
import fr.laposte.disbr.maven.plugins.swaggerdiff.out.OutputStyle;

/**
 * Writes the differences between two OpenAPIv2 APIs to a file, and
 * eventually deploys the produced artifact.
 */
@Mojo(name = "diff", defaultPhase = LifecyclePhase.NONE)
public class DiffMojo extends AbstractMojo {

	/**
	 * If <code>false</code> then the file {@link #outputFile} will not be deployed 
	 * in the remote repository.
	 */
	@Parameter(property = "attachArtifacts", defaultValue = "true", required = false)
	private boolean attachArtifacts;

	/** File to be produced by this maven goal */
	@Parameter(defaultValue = "${project.build.directory}/swagger/", property = "outputDirectory", required = false)
	private String outputDirectory;

	@Parameter(defaultValue = "swagger-diff", property = "outputFilePrefix", required = false)
	private String outputFilePrefix;

	/** Location of the first API. Can be a file or a URL. */
	@Parameter(property = "oldApi", required = true)
	private String oldApi;

	/** Localization of the second API. Can be a file or a URL. */
	@Parameter(property = "newApi", required = true)
	private String newApi;

	/** Swagger version of compared APIs. */
	@Parameter(property = "swaggerVersion", required = true)
	private String swaggerVersion;

	/** Helper used to access maven project in production */
	@Component
	private MavenProjectHelper projectHelper;

	/** The maven project in production */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	/**
	 * Title written in the header of the generated report.
	 * Not used if {@link #outputStyles} is {@link OutputStyle#MARKDOWN}.
	 */
	@Parameter(defaultValue = "Changelog", property = "title", required = false)
	private String title;

	/** Report style. Possible values: HTML, MARKDOWN. */
	@Parameter(property = "outputStyle")
	private List<OutputStyle> outputStyles;

	/**
	 * Build context used to tell the development environment that the
	 * file {@link #outputFile} has been updated.
	 */
	@Component
	private BuildContext buildContext;

	public void execute() throws MojoExecutionException {
		final Log logger = getLog();
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Computing swagger specification differences between API %1$s and %2$s", oldApi,
					newApi));
		}

		// Calculating swagger differences between the two APIs
		final SwaggerDiff diff = SwaggerDiff.compareV2(oldApi, newApi);

		// Text that will be written to outputFile
		String toWrite = null;

		// Drawer of the output file of this mojo
		Render render;

		if (outputStyles == null || outputStyles.size() == 0) {
			if (outputStyles == null) {
				outputStyles = new ArrayList<>();
			}
			outputStyles = Arrays.asList(OutputStyle.values());
		}

		File outputFile;
		for (final OutputStyle outputStyle : outputStyles) {
			final String outputFilename = String.format("%1$s.%2$s", outputFilePrefix, outputStyle.getExtension());
			outputFile = new File(outputDirectory, outputFilename);
			switch (outputStyle) {
			case HTML:
				render = new HtmlRender(title);
				break;

			case MARKDOWN:
				render = new MarkdownRender();
				break;

			default:
				buildContext.addMessage(outputFile, 0, 0, "Unhandled output style", BuildContext.SEVERITY_ERROR, null);
				throw new MojoExecutionException("Unhandled output style");
			}

			// We draw the result
			toWrite = render.render(diff);

			// And we write it in the output file in UTF-8
			Writer fw = null;
			try {
				File directory = new File(String.valueOf(outputDirectory));
				if (!directory.exists()) {
					logger.info(String.format("Directory %1$s does not exist, creating it", outputDirectory));
					if (!directory.mkdir()) {
						String errMsg = String.format("Could not create the output directory %1$s", outputDirectory);
						buildContext.addMessage(outputFile, 0, 0, errMsg, BuildContext.SEVERITY_ERROR, null);
						throw new MojoExecutionException(errMsg);
					}
				} else {
					logger.debug(String.format("Directory %1$s exists, NOT creating it", outputDirectory));
				}
				fw = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
				fw.write(toWrite);
				buildContext.refresh(outputFile);

				if (logger.isInfoEnabled()) {
					logger.info(String.format("Wrote output file %1$s", outputFile));
				}

				if (attachArtifacts) {
					projectHelper.attachArtifact(project, outputStyle.getExtension(), "swagger-diff", outputFile);
				}
			} catch (final IOException e) {
				buildContext.addMessage(outputFile, 0, 0, "Unable to write to output file", BuildContext.SEVERITY_ERROR,
						e);
				throw new MojoExecutionException("Unable to write to output file", e);
			} finally {
				IOUtils.closeQuietly(fw);
			}
		}
	}
}
