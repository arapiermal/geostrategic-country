package com.erimali.cntrygame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.*;

import com.erimali.compute.MathSolver;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public class GLogic implements Serializable {
	protected static final String RESOURCESPATH = "src/main/resources/";
	private GameStage gs; // take reference
	// get selected country from gs?

	private Timeline timeline;
	private Duration interval;
	private static final double minIntervalInSeconds = 0.25;
	private static final double defaultIntervalInSeconds = 1;
	private static final double maxIntervalInSeconds = 4;
	// Date
	private GDate inGDate;// HMMMMMM
	private static final int DEF_STARTDAY = 1;
	private static final int DEF_STARTMONTH = 1;
	private static final int DEF_STARTYEAR = 2024;

	// Countries
	private World world;
	private World moon;
	// private World planets[]; //solar system
	private List<War> wars;
	private List<String> finishedWars;
	private Currencies currencies;

	private int playerId;
	private Country player;
	// Game Events
	private PriorityQueue<GEvent> gameEvents;
	//after removed put in stack/deque/list?
	private static final String DEF_GAMEEVENTSPATH = RESOURCESPATH+"data/gameEvents.txt";
	// Game news
	private List<GNews> gameNews; // is it even necessary
	// if webview news.html only wanted

	// Improving Relations
	private Map<Short, Set<Short>> improvingRelations;

	// NEW GAME
	public GLogic(GameStage gs) {
		this.inGDate = new GDate(DEF_STARTDAY, DEF_STARTMONTH, DEF_STARTYEAR);
		this.gs = gs;
		gs.changeDate(inGDateInfo());
		startTimer(defaultIntervalInSeconds);
		this.world = new World();
		CommandLine.setCountries(world.getCountries());

		this.gameEvents = loadGameEvents(DEF_GAMEEVENTSPATH);
		this.setCurrencies(new Currencies());

		this.improvingRelations = new HashMap<>();

		this.wars = new LinkedList<>();
	}

//TESTING
	public GLogic() {
		this.inGDate = new GDate(DEF_STARTDAY, DEF_STARTMONTH, DEF_STARTYEAR);
		this.world = new World();
		this.gameEvents = loadGameEvents(DEF_GAMEEVENTSPATH);

	}

	public void startTimer(double intervalInSeconds) {

		interval = Duration.seconds(intervalInSeconds);

		timeline = new Timeline(new KeyFrame(interval, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (gs.isPaused) {

				} else {
					gameTick();
				}
			}
		}));

		timeline.setCycleCount(Timeline.INDEFINITE);

		timeline.play();
	}

	public void pauseTimer() {
		timeline.pause();
	}

	public void playTimer() {
		timeline.play();
	}

	public void stopTimer() {
		if (timeline != null) {
			timeline.stop();
		}
	}

	public void changeTimer() {

	}

	///////////////////////////////////////////////////////
	///////////////////////////////////////////////////////

	public void gameTick() {
		if (passDayNewMonth()) {
			// hmmmmm
			// be aware of cheating
			// on improve (Player) -> wait 30 days until can stop improving
			if (!improvingRelations.isEmpty()) {
				tickImproveRelations();
			}
		}
		gs.changeDate(inGDateInfo());
		if (!gameEvents.isEmpty()) {
			if (gameEvents.peek().getDate().equals(inGDate)) {
				//isCanHappen PROBLEMATIC
				if (gameEvents.peek().isCanHappen()) {
					gs.popupGEvent(gameEvents.poll());
				}
			}
		}
	}

	public boolean passDayNewMonth() {
		inGDate.nextDay();
		if (inGDate.getDay() == 1)
			return true;
		return false;
	}

	public String inGDateInfo() {
		return this.inGDate.toString();
	}

	public String toStringCountry(int c) {
		Country country = world.getCountry(c);
		if (country != null) {
			return country.toString(0);
		}
		return "";// No such country
	}

	public CountryArray getWorldCountries() {
		return this.world.getCountries();
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerName(String playerName) {
		this.playerId = CountryArray.getIndex(playerName);
	}

	public Country getPlayer() {
		return player;
	}

	public void setPlayer(Country player) {
		this.player = player;
	}

	public void addGEvent(GEvent gEvent) {
		this.gameEvents.add(gEvent);
	}

	public void selectPlayer(int cPlayer) {
		Country c = world.getCountry(cPlayer);
		if (c != null) {
			playerId = cPlayer;
			player = c;
			CommandLine.setPlayerCountry(cPlayer);

			updateCannotHappenGameEvents();
		}
	}

	public void updateCannotHappenGameEvents() {
		for (GEvent ge : gameEvents) {
			ge.setCanHappen();
		}
	}

	public void addImprovingRelations(short c1, short c2) {
		if (this.improvingRelations.containsKey(c1)) {
			Set<Short> improving = this.improvingRelations.get(c1);
			improving.add(c2);
		} else {
			Set<Short> improving = new HashSet<>();
			improving.add(c2);
			this.improvingRelations.put(c1, improving);
		}
	}

	public void addImprovingRelations(int c2) {
		addImprovingRelations((short) playerId, (short) c2);
	}

	public void removeImprovingRelations(short c1, short c2) {
		if (this.improvingRelations.containsKey(c1)) {
			Set<Short> improving = this.improvingRelations.get(c1);
			improving.remove(c2);
			if (improving.isEmpty()) {
				this.improvingRelations.remove(c1);
			}
		}
	}

	public void removeImprovingRelations(int c2) {
		removeImprovingRelations((short) playerId, (short) c2);
	}
	public boolean isImprovingRelations(int c2){
		return isImprovingRelations((short) playerId, (short) c2);
	}
	public boolean isImprovingRelations(short c1, short c2) {
		if (this.improvingRelations.containsKey(c1)) {
			return improvingRelations.get(c1).contains(c2);
		} else{
			return false;
		}
	}
	public void tickImproveRelations() {
		for (Short c1 : improvingRelations.keySet()) {
			for (Short c2 : improvingRelations.get(c1)) {
				improveRelations(c1, c2);
			}
		}
	}

	public void improveRelations(String c) {
		player.improveRelations(c);
	}

	public void improveRelations(String c, short amount) {
		player.improveRelations(c, amount);
	}

	public void improveRelations(String c1, String c2) {
		world.getCountry(c1).improveRelations(c2);
	}

	public void improveRelations(int c1, int c2) {
		world.getCountry(c1).improveRelations(c2);
	}

	public void improveRelations(String c1, String c2, short amount) {
		world.getCountry(c1).improveRelations(c2, amount);
	}
	// Game Events

	private PriorityQueue<GEvent> loadGameEvents(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			PriorityQueue<GEvent> gEvents = new PriorityQueue<>();
			String line;
			ArrayList<String> options = new ArrayList<>();
			ArrayList<String> commands = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");
				String description = "";
				if ((line = br.readLine()) != null) {
					description = line;

				}
				while (((line = br.readLine()) != null)) {
					if (line.startsWith("~~~")) {
						break;
					}
					options.add(line);
					if ((line = br.readLine()) != null) {
						commands.add(line);
					}
				}
				GDate evDate = new GDate(data[2]);
				//Can be done sooner to skip
				if(inGDate.compareTo(evDate) > 0){
					options.clear();
					commands.clear();
					continue;
				}
				if (data.length == 3)
					gEvents.add(new GEvent(data[1], new GDate(data[2]), description,
							options.toArray(new String[options.size()]),
							commands.toArray(new String[commands.size()])));
				else if (data.length == 4)
					gEvents.add(new GEvent(data[1], new GDate(data[2]), data[3], description,
							options.toArray(new String[options.size()]),
							commands.toArray(new String[commands.size()])));
				options.clear();
				commands.clear();
			}
			return gEvents;
		} catch (Exception e) {
			TESTING.print("EVENT LOADING ERROR");
			return new PriorityQueue<GEvent>();
		}
	}

	public GDate specialDateFromNowInDays(int days) {
		GDate n = new GDate(inGDate.getDay(), inGDate.getMonth(), inGDate.getYear());
		n.addDays(days);
		return n;
	}
//WITH MORE STUFF IN MathSolver, now and rand became more useless
	public GDate specialDate(String input) {
		String[] s = input.toLowerCase().split("/");
		for (int i = 0; i < s.length; i++) {
			s[i] = s[i].trim();
		}
		if (s.length == 2) {
			if (s[0].startsWith("after")) {
				return specialDateFromNowInDays(GUtils.parseI(s[1]));
			}
		} else if (s.length == 3) {
			if (s[0].contains("now"))
				s[0] = s[0].replace("now", "" + inGDate.getDay());
			if (s[1].contains("now"))
				s[1] = s[1].replace("now", "" + inGDate.getMonth());
			if (s[2].contains("now"))
				s[2] = s[2].replace("now", "" + inGDate.getYear());
			int day = (int) MathSolver.solve(s[0]);
			int month = (int) MathSolver.solve(s[1]);
			int year = (int) MathSolver.solve(s[2]);
			return new GDate(day, month, year, true);
		}
		return null; // handle
	}

	public String parseGEventText(String in) {
		StringBuilder sb = new StringBuilder();
		String[] rows = in.split("~");
		for (int i = 0; i < rows.length; i++) {
			rows[i] = rows[i].trim();

			if (rows[i].startsWith("!@")) {
				continue;
			} else {
                //NOT EFFICIENT
				rows[i] = rows[i].replaceAll("!t", "~");
			}
			for (int j = 0; j < rows[i].length() - 3; j++) {
				char c = rows[i].charAt(j);
				if (c == '!') {
					char c2 = rows[i].charAt(j + 1);
					if (c2 == '{') {
						StringBuilder w = new StringBuilder();
						for (int k = j + 2; k < rows[i].length(); k++) {
							char c3 = rows[i].charAt(k);

							if (c3 == '}') {

								break;
							}

							w.append(c3);
						}
						String ww = w.toString();

						String result = parseTextCommand(ww.trim());
						String remove = "\\Q!{" + ww + "}\\E";
						rows[i] = rows[i].replaceAll(remove, result);
					}
				}
			}
			sb.append(Language.uppercaseFirstChar(rows[i]));
			if (i < rows.length - 1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	public String parseTextCommand(String string) {
		// translate to [Italian]:
		String[] parts = string.split(":");
		if (parts.length < 2) {
			return string;
		}
		try {
			String[] commands = parts[0].split("\\s+");
			StringBuilder replace = new StringBuilder();
			switch (commands[0].toUpperCase()) {
			case "COUNTRY":
				String reference = parts[1].trim().toUpperCase();
				Country c = null;
				if (reference.equals("SELF")) {
					c = player;
				} else {
					c = world.getCountry(reference);
				}
				switch (commands[1].toUpperCase()) {
				case "NAME":
					replace.append(c.getName());
					break;
				case "GOV":
					switch (commands[2].toUpperCase()) {
					case "TYPE":
						replace.append(c.getGovernment().getType());
						break;
					default:
						break;
					}
				default:
					break;
				}
				break;
			case "TRANSLATE":
				if (!GOptions.isTranslateGEvent()) {
					return parts[1];
				}
				short lang = -1;
				try {
					lang = player.getMainLanguage();
				} catch (Exception e) {

				}
				switch (commands[1].toUpperCase()) {
				// FROM => ENGLISH
				case "TO":
					Language l = null;
					if (commands[2].equalsIgnoreCase("SELF")) {
						l = world.getLanguages().get(lang);
					} else {
						l = world.getLanguages().get(world.binarySearchLanguage(commands[2]));/////
					}
					if (l != null) {
						replace.append(l.translateFromEnglishPhrases(parts[1]));
					} else {
						replace.append(parts[1]);
					}
					break;
				// OTHER FROM
				case "FROM":
					break;

				default:
					break;
				}
				break;
			case "DATE":
				if (commands.length == 1) {
					replace.append(specialDate(parts[1]).toString());
				} else {
					switch (commands[1].toUpperCase()) {
					case "NOW":
						replace.append(replaceTextWithDate(parts[1], inGDate));
						break;
					default:
						break;
					}
				}
				break;
			default:
				break;
			}
			return replace.toString();
		} catch (Exception e) {
			ErrorLog.logError(e);
			return "ERROR";
		}
	}

	public static String replaceTextWithDate(String in, GDate date) {
		String result = in.replace("dd", "" + date.getDay()).replace("mm", "" + date.getMonth()).replace("yyyy",
				"" + date.getYear()).replace("mlong", date.getMonthName());
		return result;
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public PriorityQueue<GEvent> getGameEvents() {
		return gameEvents;
	}

	public void setGameEvents(PriorityQueue<GEvent> gameEvents) {
		this.gameEvents = gameEvents;
	}

	public List<GNews> getGameNews() {
		return gameNews;
	}

	public void setGameNews(List<GNews> gameNews) {
		this.gameNews = gameNews;
	}

	// SUBJECT LOGIC

	// WAR
//////////////////////////////////////////////////
//
	public void declareWar(String a, String b, CasusBelli casusBelli) {
		String warName = "";// Albania vs OpponentName - casusBelli
		War w = world.getCountry(a).declareWar(world.getCountry(b), casusBelli);
		wars.add(w);
	}

	public void declareWar(int a, int o, CasusBelli casusBelli) {
		String warName = "";// Albania vs OpponentName - casusBelli
		War w = world.getCountry(a).declareWar(world.getCountry(o), casusBelli);
		wars.add(w);
	}
	public void declareWar(int o, CasusBelli casusBelli) {
		String warName = "";// Albania vs OpponentName - casusBelli
		War w = player.declareWar(world.getCountry(o), casusBelli);
		wars.add(w);
		System.out.println("Player declared war - "+ casusBelli);
	}
// FIX
	public void finishWar(int index) {
		finishedWars.add( wars.remove(index).toString() + "Won/Lost");
	}

	public boolean isSubjectOfPlayer(String c) {
		return player.hasSubject(c);
	}
	public boolean isSubjectOfPlayer(int c) {
		return player.hasSubject(c);
	}

	public World getMoon() {
		return moon;
	}

	public void setMoon(World moon) {
		this.moon = moon;
	}

	public String getRelationsWith(String c) {
		return String.format("%d", player.getRelations(c));
	}

	public String getRelationsWith(int c) {
		return String.format("%d", player.getRelations(c));
	}

	public boolean isAllyWith(int c) {
		return player.isAllyWith(c);
	}
	public boolean sendAllianceRequest(int c) {
		return player.sendAllianceRequest(c);
	}

	public Currencies getCurrencies() {
		return currencies;
	}

	public void setCurrencies(Currencies currencies) {
		this.currencies = currencies;
	}

	public void setCurrencySignPreFix() {
		currencies.setPlayerCurrency(Currencies.getCurrencySign(player.getCurrency()));
	}

	public String getPlayerName() {
		return this.player.getName();
	}

	public String getProvInfo(int selectedProv) {
		return world.getProvInfo(selectedProv);
	}

}
