package data;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_DEFAULT)
public class ConvertToJson {
	private String name;
	private List<ConvertToJson> children;
	private int size;
	
	public ConvertToJson(){
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public List<ConvertToJson> getChildren() {
		return children;
	}

	public void setChildren(List<ConvertToJson> children) {
		this.children = children;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
