package com.erimali.cntrygame;

import java.io.Serializable;

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

public class GEvent extends BaseEvent implements Comparable<GEvent> {
    private GDate date;
    private String requirements;
    private boolean canHappen;

    public GEvent(String title, GDate date, String description, String[] options, String[] commands) {
        super(title, description, options, commands);
        this.date = date;

    }

    public GEvent(String title, GDate date, String requirements, String description, String[] options, String[] commands) {
        super(title, description, options, commands);
        this.date = date;
        this.requirements = requirements;
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

    public void run(int i) {
        CommandLine.execute(getCommand(i), true);
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
        if (requirements != null)
            this.canHappen = CommandLine.checkStatement(requirements);
    }

}
