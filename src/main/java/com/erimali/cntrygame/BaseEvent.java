package com.erimali.cntrygame;

import java.io.Serializable;
//tech related can be BaseEvent
public class BaseEvent implements Serializable {
	private String title;
	private String description;
	private String[] options;
	private String[] commands;

	public BaseEvent(String title,String description, String[] options, String[] commands) {
		this.title = title;
		this.description = description;
		this.options = options;
		this.commands = commands;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

	public void run(int i) {
		CommandLine.execute(commands[i], true);
	}

	public String[] getCommands() {
		return commands;
	}

	public void setCommands(String[] commands) {
		this.commands = commands;
	}
}
