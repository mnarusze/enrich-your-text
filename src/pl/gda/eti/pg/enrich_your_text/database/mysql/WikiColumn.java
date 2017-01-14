package pl.gda.eti.pg.enrich_your_text.database.mysql;

public class WikiColumn {
	public String name;
	public String type;

	public WikiColumn(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
}
