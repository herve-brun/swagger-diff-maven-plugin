package com.deepoove.swagger.diff;

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

import com.deepoove.swagger.diff.out.HtmlRender;
import com.deepoove.swagger.diff.out.OutputStyle;

/**
 * Goal which outputs the differences between two swagger APIs to a file and
 * eventually deploys the produced artifact.
 */
@Mojo(name = "diff", defaultPhase = LifecyclePhase.NONE)
public class DiffMojo extends AbstractMojo {

	/**
	 * Si <code>true</code> alors le fichier {@link #outputFile} ne sera pas déployé
	 * dans le dépôt distant
	 */
	@Parameter(property = "attachAtifacts", defaultValue = "true", required = false)
	private boolean attachAtifacts;

	/** Fichier qui sera produit par ce goal maven */
	@Parameter(defaultValue = "${project.build.directory}/swagger/", property = "outputDirectory", required = false)
	private String outputDirectory;

	@Parameter(defaultValue = "swagger-diff", property = "outputFilePrefix", required = false)
	private String outputFilePrefix;

	/** Localisation de l'ancienne API. Peut être un fichier ou une URL. */
	@Parameter(property = "oldApi", required = true)
	private String oldApi;

	/** Localisation de la nouvelle API. Peut être un fichier ou une URL. */
	@Parameter(property = "newApi", required = true)
	private String newApi;

	/** Version swagger des API comparées. */
	@Parameter(property = "swaggerVersion", required = true)
	private String swaggerVersion;

	/** Helper utilisé pour accéder au projet maven en cours de production */
	@Component
	private MavenProjectHelper projectHelper;

	/** Le projet maven en cours de production */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	/**
	 * Titre écrit dans l'en-tête du rapport généré. N''est pas utilisé si
	 * {@link #outputStyles} est {@link OutputStyle#MARKDOWN}.
	 */
	@Parameter(defaultValue = "Changelog", property = "title", required = false)
	private String title;

	/** Style du rapport. Valeurs possibles : HTML, MARKDOWN. */
	@Parameter(property = "outputStyle")
	private List<OutputStyle> outputStyles;

	/**
	 * Contexte de build utilisé pour dire à l'environnement de développement que le
	 * fichier {@link #outputFile} a été mis à jour.
	 */
	@Component
	private BuildContext buildContext;

	public void execute() throws MojoExecutionException {
		final Log logger = getLog();
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Computing swagger specification differences between API %1$s and %2$s", oldApi,
					newApi));
		}

		// Calcul des différences swagger entre les deux API
		final SwaggerDiff diff = SwaggerDiff.compareV2(oldApi, newApi);

		// Texte qui sera écrit dans outputFile
		String toWrite = null;

		// Dessinateur du fichier en sortie de ce mojo
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

			// On dessine le résultat
			toWrite = render.render(diff);

			// Et on l'écrit dans le fichier de sortie en UTF-8
			Writer fw = null;
			try {
				fw = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);
				fw.write(toWrite);
				fw.close();
				buildContext.refresh(outputFile);

				if (logger.isInfoEnabled()) {
					logger.info(String.format("Wrote output file %1$s", outputFile));
				}

				if (attachAtifacts) {
					projectHelper.attachArtifact(project, outputStyle.getExtension(), "swagger-diff", outputFile);
				}
			} catch (final IOException e) {
				buildContext.addMessage(outputFile, 0, 0, "Could not write an output file", BuildContext.SEVERITY_ERROR,
						e);
				throw new MojoExecutionException("Could not write an output file", e);
			} finally {
				IOUtils.closeQuietly(fw);
			}
		}
	}
}
