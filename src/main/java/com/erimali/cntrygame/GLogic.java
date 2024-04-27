package com.erimali.cntrygame;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.erimali.cntrymilitary.*;
import com.erimali.compute.MathSolver;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Duration;

public class GLogic implements Serializable {
    protected static final String RESOURCESPATH = "src/main/resources/";
    private transient GameStage gs; // keep reference

    private transient Timeline timeline;
    private transient KeyFrame keyframe;
    private transient Duration interval;
    private static final double defaultIntervalInSeconds = 1;
    //keep in double or int (?) Math.pow might be problematic
    private static final double MIN_GAME_SPEED_INTERVAL = 0.25;
    private static final double MAX_GAME_SPEED_INTERVAL = 4;
    private double gameSpeedInterval;
    // Date
    private GDate inGDate;//
    private static final int DEF_START_DAY = 1;
    private static final int DEF_START_MONTH = 1;
    private static final int DEF_START_YEAR = 2024;

    // World with countries
    private World world;
    private World[] moon_planets;//solar system

    private transient List<MilUnitData>[] unitTypes;

    private List<War> wars;
    //private List<WarResult> finishedWars;
    // Wars saved as GNews ... (special type, int)
    private Currencies currencies;
    //TAKE CARE WHEN SAVING WHILE NOTHING IS CHOSEN
    private int playerId;
    private Country player;
    // Game Events
    private PriorityQueue<GEvent> gameEvents;
    public List<CommandLine.PeriodicCommand>[] periodicCommands;
    //after removed put in stack/deque/list?
    private static final String DEF_GAMEEVENTSPATH = RESOURCESPATH + "data/gameEvents.txt";
    // Game news
    private List<GNews> gameNews; // is it even necessary
    // if webview news.html only wanted

    // Improving Relations
    private Map<Short, Set<Short>> improvingRelations;
    private Map<Integer, List<MilUnit>> recruitingBuildUnits;
    //can be decentralized if i have a Set<Short> inside Diplomacy...
    //still would require method traverse...

    // NEW GAME
    public GLogic(GameStage gs) {
        this.inGDate = new GDate(DEF_START_DAY, DEF_START_MONTH, DEF_START_YEAR);
        this.gs = gs;
        this.gameSpeedInterval = defaultIntervalInSeconds;
        startTimer();
        this.world = new World(this);
        CommandLine.initCMD(world.getCountries());
        this.gameEvents = new PriorityQueue<>();
        loadGameEvents(DEF_GAMEEVENTSPATH);
        initPeriodicCommands();
        this.currencies = new Currencies();
        this.improvingRelations = new HashMap<>();
        this.wars = new LinkedList<>();
        loadAllUnitData();
        this.recruitingBuildUnits = new HashMap<>();
        if (GOptions.isAllowMods())
            loadMods();
    }


    public void startTimer() {
        startTimer(gameSpeedInterval);
    }

    public void startTimer(double intervalInSeconds) {

        interval = Duration.seconds(intervalInSeconds);
        keyframe = new KeyFrame(interval, event -> {
            if (!gs.paused) {
                dailyTick();
            }
        });
        timeline = new Timeline(keyframe);

        timeline.setCycleCount(Timeline.INDEFINITE);

        timeline.play();
    }

    public void increaseSpeed() {
        gameSpeedInterval /= 2;
        if (gameSpeedInterval < MIN_GAME_SPEED_INTERVAL) {
            gameSpeedInterval = MIN_GAME_SPEED_INTERVAL;
            return;
        }
        timeline.stop();
        startTimer();
    }

    public void decreaseSpeed() {
        gameSpeedInterval *= 2;
        if (gameSpeedInterval > MAX_GAME_SPEED_INTERVAL) {
            gameSpeedInterval = MAX_GAME_SPEED_INTERVAL;
            return;
        }
        timeline.stop();
        startTimer();
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

    public void setGameStage(GameStage gs) {
        this.gs = gs;
    }

    ///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////

    public void dailyTick() {
        inGDate.nextDay();
        if (inGDate.isFirstDayOfYear()) {
            yearlyTick();
        }
        if (inGDate.isFirstDayOfMonth()) {
            monthlyTick();
        }
        if (inGDate.isFirstDayOfWeek()) {
            weeklyTick();
        }
        gs.changeDate(inGDateInfo());
        if (!gameEvents.isEmpty()) {
            GEvent gEvent = gameEvents.peek();
            if (gEvent.getDate().equals(inGDate)) {
                //isCanHappen PROBLEMATIC
                if (gEvent.isCanHappen()) {
                    gs.popupGEvent(gameEvents.poll());
                } else {
                    //......
                    gameEvents.poll();
                }
            }
        }

        execPeriodicCommands(0);

    }

    public void weeklyTick() {
        world.weeklyMilTick(recruitingBuildUnits.keySet());

        execPeriodicCommands(1);
    }

    public void monthlyTick() {
        if (!improvingRelations.isEmpty()) {
            tickImproveRelations();
        }
        //...
        world.monthlyUpdate();
        gs.changeSelectedProvInfo();
        gs.changeSelectedCountryInfo();

        execPeriodicCommands(2);
    }

    public void yearlyTick() {
        world.yearlyUpdate();

        execPeriodicCommands(3);

    }

    public Country getCountry(String c) {
        return world.getCountry(c);
    }

    public Country getCountry(int id) {
        return world.getCountry(id);
    }

    public String inGDateInfo() {
        return inGDate.toString();
    }

    public String inGDateInfo(char sep) {
        return inGDate.toString(sep);
    }

    public String toStringCountry(int c) {
        Country country = world.getCountry(c);
        if (country != null) {
            return country.toString(0);
        }
        return "";// No such country
    }

    public CountryArray getWorldCountries() {
        return world.getCountries();
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
        gameEvents.add(gEvent);
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
        if (improvingRelations.containsKey(c1)) {
            Set<Short> improving = improvingRelations.get(c1);
            improving.add(c2);
        } else {
            Set<Short> improving = new HashSet<>();
            improving.add(c2);
            improvingRelations.put(c1, improving);
        }
    }

    public void addImprovingRelations(int c2) {
        addImprovingRelations((short) playerId, (short) c2);
    }

    public void removeImprovingRelations(short c1, short c2) {
        if (improvingRelations.containsKey(c1)) {
            Set<Short> improving = improvingRelations.get(c1);
            improving.remove(c2);
            if (improving.isEmpty()) {
                improvingRelations.remove(c1);
            }
        }
    }

    public void removeImprovingRelations(int c2) {
        removeImprovingRelations((short) playerId, (short) c2);
    }

    public boolean isImprovingRelations(int c2) {
        return isImprovingRelations((short) playerId, (short) c2);
    }

    public boolean isImprovingRelations(short c1, short c2) {
        if (this.improvingRelations.containsKey(c1)) {
            return improvingRelations.get(c1).contains(c2);
        } else {
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

    public void improveRelations(int c1, int c2) {
        world.getCountry(c1).improveRelations(c2);
    }

    public void improveRelations(int c1, int c2, short amount) {
        world.getCountry(c1).improveRelations(c2, amount);
    }


    // Game Events
    private void loadGameEvents(String path) {
        loadGameEvents(new File(path));
    }

    private void loadGameEvents(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
                if (inGDate.compareTo(evDate) > 0) {
                    options.clear();
                    commands.clear();
                    continue;
                }
                if (data.length == 3)
                    gameEvents.add(new GEvent(data[1], new GDate(data[2]), description,
                            options.toArray(new String[options.size()]),
                            commands.toArray(new String[commands.size()])));
                else if (data.length == 4)
                    gameEvents.add(new GEvent(data[1], new GDate(data[2]), data[3], description,
                            options.toArray(new String[options.size()]),
                            commands.toArray(new String[commands.size()])));
                options.clear();
                commands.clear();
            }
        } catch (Exception e) {
            TESTING.print("EVENT LOADING CRITICAL ERROR");
        }
    }

    public GDate specialDateFromNowInDays(int days) {
        GDate n = new GDate(inGDate.getDay(), inGDate.getMonth(), inGDate.getYear());
        n.addDays(days);
        return n;
    }

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
        // translate.self.Yes
        String[] parts = string.split("\\.");
        if (parts.length < 2) {
            return string;
        }
        try {
            //or country.self.name
            //country.self.rel.xk
            StringBuilder replace = new StringBuilder();
            switch (parts[0].toUpperCase()) {
                case "COUNTRY":
                    String reference = parts[1].trim().toUpperCase();
                    Country c;
                    if (reference.equals("SELF")) {
                        c = player;
                    } else {
                        c = world.getCountry(reference);
                    }
                    if (c == null)
                        return "NULL";
                    switch (parts[2].toUpperCase()) {
                        case "NAME":
                            replace.append(c.getName());
                            break;
                        case "GOV":
                            Government gov = c.getGovernment();
                            switch (parts[3].toUpperCase()) {
                                case "TYPE":
                                    replace.append(gov.getType());
                                    break;
                                case "HOS":
                                    replace.append(gov.getHeadOfState());
                                case "HOG":
                                    replace.append(gov.getHeadOfGovernment());
                                default:
                                    break;
                            }
                            break;
                        case "REL":
                            int relC = CountryArray.getIndex(parts[3].trim());
                            replace.append(c.getRelations(relC));
                            break;
                        default:
                            break;
                    }
                    break;
                case "TRANSLATE":
                    String ref2 = parts[1].trim().toUpperCase();

                    if (!GOptions.isTranslateGEvent()) {
                        return parts[2];
                    }
                    short lang = -1;
                    try {
                        lang = player.getMainLanguage();
                    } catch (Exception e) {

                    }
                    Language l;
                    if (ref2.equalsIgnoreCase("SELF")) {
                        l = world.getLanguages().get(lang);
                    } else {
                        l = world.searchLanguage(parts[2]);
                    }
                    if (l != null) {
                        replace.append(l.translateFromEnglishPhrases(parts[2]));
                    } else {
                        replace.append(parts[2]);
                    }
                    break;
                case "DATE":
                    if (parts.length == 2) {
                        replace.append(specialDate(parts[1]).toString());
                    } else {
                        switch (parts[1].toUpperCase()) {
                            case "NOW":
                                //date.now.dd/mm/yyyy
                                replace.append(replaceTextWithDate(parts[2], inGDate));
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
        String result = in.replace("dd", String.valueOf(date.getDay())).replace("mm", String.valueOf(date.getMonth())).replace("yyyy",
                String.valueOf(date.getYear())).replace("mlong", date.getMonthName());
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

    public void declareWar(int a, int o, CasusBelli casusBelli) {
        War w = getCountry(a).declareWar(o, world.getCountries(), casusBelli);
        if (w != null)
            wars.add(w);
    }

    public void declareWar(int o, CasusBelli casusBelli) {
        declareWar(playerId, o, casusBelli);
    }
// FIX
    //public void finishWar(int index) {finishedWars.add( wars.remove(index).toString() + "Won/Lost");}

    public boolean isSubjectOfPlayer(int c) {
        return player.hasSubject(c);
    }

    public World getMoon() {
        return moon_planets[0];
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
        return player.sendAllianceRequest(world.getCountries(), c);
    }

    public boolean breakAlliance(int c) {
        return player.breakAlliance(world.getCountries(), c);
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
        return player.getName();
    }

    public String getProvInfo(int selectedProv) {
        return world.getProvInfo(selectedProv);
    }

    public void dailyWarBattles() {
        for (War w : wars) {
            w.dayTick();
        }
    }

    public void giveMoney(int selectedCountry, Double result) {
        giveMoney(playerId, selectedCountry, result);
    }

    public void giveMoney(int i1, int i2, double amount) {
        Country c1 = world.getCountry(i1);
        Country c2 = world.getCountry(i2);
        if (c1 != null && c2 != null) {
            c1.getEconomy().giveMoney(c2, amount);
        }
    }

    public void loadAllUnitData() {
        //noinspection unchecked
        unitTypes = (List<MilUnitData>[]) new ArrayList[MilUnitData.getMaxTypes()];
        Military.loadAllUnitData(unitTypes);
    }

    public void correlateAllUnitData() {
        for (Country c : world.getCountries()) {
            c.getMilitary().correlateUnitData(unitTypes);
        }
    }


    public double getSpeed() {
        return 1 / gameSpeedInterval;
    }

    public TreeView<MilUnitData> makeTreeViewUnitTypes() {
        MilUnitData rootEmpty = new MilUnitData();
        TreeItem<MilUnitData> root = new TreeItem<>(rootEmpty);

        TreeView<MilUnitData> tree = new TreeView<>(root);
        for (List<MilUnitData> l : unitTypes) {
            if (!l.isEmpty()) {
                TreeItem<MilUnitData> rl = new TreeItem<>(l.getFirst());
                for (int i = 1; i < l.size(); i++) {
                    rl.getChildren().add(new TreeItem<>(l.get(i)));
                }
                root.getChildren().add(rl);
            }
        }
        tree.setShowRoot(false);
        return tree;
    }

    //manpower and resources of owner should be used
    public void makeMilUnit(int ownerId, int provId, MilUnitData d) {
        //no units, one unit (only key should be and empty list), more than one -> one recruiting/getting built rest in queue
        //
        MilUnit u = d.isVehicle() ? new MilVehicles(d, ownerId) : new MilSoldiers(d, ownerId);
        if (recruitingBuildUnits.containsKey(provId)) {
            recruitingBuildUnits.get(provId).add(u);
        } else {
            world.recruitBuildMilUnit(u, provId);
            recruitingBuildUnits.put(provId, new LinkedList<>());
        }
    }

    public void contMilUnit(int provId) {
        if (recruitingBuildUnits.containsKey(provId)) {
            List<MilUnit> l = recruitingBuildUnits.get(provId);
            if (l.isEmpty()) {
                recruitingBuildUnits.remove(provId);
            } else {
                MilUnit u = recruitingBuildUnits.get(provId).removeFirst();
                world.recruitBuildMilUnit(u, provId);
            }
        }
    }

    public WorldMap getWorldMap() {
        return gs.getMap();
    }


    public void initPeriodicCommands() {
        //noinspection unchecked
        periodicCommands = (List<CommandLine.PeriodicCommand>[]) new List[4];
        for (int i = 0; i < periodicCommands.length; i++) {
            periodicCommands[i] = new LinkedList<>();
        }
    }

    public void execPeriodicCommands(int i) {
        periodicCommands[i].removeIf(CommandLine.PeriodicCommand::run);
    }

    public void addPeriodicCommand(String string, boolean admin) {
        String[] k = string.split("\\s+", 3);
        if (k.length == 3) {
            TESTING.print(k[0], k[1], k[2]);
            int i = CommandLine.PeriodicCommand.getPeriod(k[0]);
            int times = GUtils.parseI(k[1]);
            if (i >= 0 && i < periodicCommands.length && times > 0)
                periodicCommands[i].add(new CommandLine.PeriodicCommand(k[2], admin, times));
        }
    }

    public void loadMods() {
        String modsPath = GOptions.getModsPath();
        File modsDir = new File(modsPath);
        File[] events = modsDir.listFiles((dir, name) -> dir.isFile() && name.toUpperCase().contains("EVENTS"));
        if (events != null)
            for (File ev : events)
                loadGameEvents(ev);
        CommandLine.loadEriScripts(modsPath + "scripts/");

    }
}
