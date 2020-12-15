package fr.laposte.disbr.maven.plugins.swaggerdiff.out;

/**
 * OutputStyle.java
 */
public enum OutputStyle {
	HTML("html"), MARKDOWN("md");

	/** Extension de fichier */
	private String extension;

	private OutputStyle(String extension) {
		this.extension = extension;
	}

	/**
	 * Getter de {@link #extension}
	 * 
	 * @return la valeur de {@link #extension}.
	 */
	public String getExtension() {
		return extension;
	}
}
