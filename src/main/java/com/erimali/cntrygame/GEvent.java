package com.erimali.cntrygame;

enum Events {
	WORLD_CUP {
		@Override
		public void performEvent() {
		}
	},
	ADV_SCIENTIFIC {
		@Override
		public void performEvent() {
		}
	},
	ADV_TECHNOLOGICAL {
		@Override
		public void performEvent() {
		}
	},
	ADV_AI {
		@Override
		public void performEvent() {
		}
	};

	public abstract void performEvent();
}

public class GEvent implements Comparable<GEvent> {
	private String title;
	private GDate date;
	private String requirements; // not necessary for all, fix;
	private String description;
	private String[] options;
	private String[] commands;
	private boolean canHappen;
	// maybe use commandLine

	public GEvent(String title, GDate date, String description, String[] options, String[] commands) {
		super();
		this.title = title;
		this.date = date;
		this.description = description;
		this.options = options;
		this.commands = commands;
	}
	public GEvent(String title, GDate date, String requirements, String description, String[] options, String[] commands) {
		super();
		this.title = title;
		this.date = date;
		this.requirements = requirements;
		this.description = description;
		this.options = options;
		this.commands = commands;
	}
	public GDate getDate() {
		return date;
	}

	public void setDate(GDate date) {
		this.date = date;
	}

	@Override
	public int compareTo(GEvent o) {
		return this.date.compareTo(o.getDate());
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
		CommandLine.execute(commands[i]);
	}

	public String[] getCommands() {
		return commands;
	}

	public void setCommands(String[] commands) {
		this.commands = commands;
	}
	public String getRequirements() {
		return requirements;
	}
	public void setRequirements(String requirements) {
		this.requirements = requirements;
	}
	public boolean isCanHappen() {
		return canHappen;
	}
	public void setCanHappen() {
		if(requirements != null)
			this.canHappen = CommandLine.checkStatement(requirements);
	}

}
