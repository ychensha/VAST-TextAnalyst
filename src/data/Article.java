package data;

import java.util.ArrayList;

public class Article {
	private String date;
	private ArrayList<String> persons;
	private String event;
	private Boolean isHighLight = false;
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}

	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public Boolean getIsHighLight() {
		return isHighLight;
	}
	public void setIsHighLight(Boolean isHighLight) {
		this.isHighLight = isHighLight;
	}
	public ArrayList<String> getPersons() {
		return persons;
	}
	public void setPersons(ArrayList<String> persons) {
		this.persons = persons;
	}

}
