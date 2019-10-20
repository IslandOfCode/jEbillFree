package it.islandofcode.jebill.vtl;

public class Script {
	private String author;
	private String version;
	private String description;
	private String script;
	
	
	public Script(String author, String version, String description, String script) {
		super();
		this.author = author;
		this.version = version;
		this.description = description;
		this.script = cleanScript(script);
	}


	public String getAuthor() {
		return author;
	}


	public String getVersion() {
		return version;
	}


	public String getDescription() {
		return description;
	}


	public String getScript() {
		return script;
	}
	
	//XXX inutile perchè a questo punto ho già letto l'xml
	private String cleanScript(String dirty) {
		return dirty.replaceAll("\\<\\w*\\>", "");//rimuove tutti gli eventuali tag aggiuntivi
	}
	
}
