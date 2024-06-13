package com.erimali.cntrygame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Period;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.erimali.cntrymilitary.MilUnitData;
import com.erimali.compute.EriScript;
import com.erimali.minigames.MGSnakeStage;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


class Command {
    Map<String, Command> subCommands;
    Runnable action;

    public Command(Runnable action) {
        this.action = action;
    }

    public Command(Runnable action, Map<String, Command> subCommands) {
        this.action = action;
        this.subCommands = subCommands;
    }

    public void run(String... args) {
        run(0, args);
    }

    public void run(int i, String... args) {
        if (i >= args.length - 1) {
            this.action.run();
            //return;
        } else {
            this.subCommands.get(args[++i]).run(i, args);
        }
    }
}

public class CommandLine {


    public static class PeriodicCommand {
        String command;
        boolean admin;
        int times;

        PeriodicCommand(String command, boolean admin, int times) {
            this.command = command;
            this.admin = admin;
            this.times = times;
        }

        public boolean run() {
            execute(command, admin);
            return (--times) <= 0;
        }


        public static int getPeriod(String in) {
            return switch (in.trim().toUpperCase()) {
                case "YEARLY", "YEAR" -> 3;
                case "MONTHLY", "MONTH" -> 2;
                case "WEEKLY", "WEEK" -> 1;
                case "DAILY", "DAY" -> 0;
                default -> Integer.MIN_VALUE;
            };
        }
    }

    private static final String COMMAND_SEPARATOR = ";";

    private static GameStage gs;
    //extract below from previous (?)
    private static CountryArray countries;
    private static int playerId = -1;
    private static String playerISO2;

    //
    private static Map<String, EriScript> eriScripts = new HashMap<>();
    //Make changeable in GOptions (?)
    private static final String scriptsPath = GLogic.RESOURCESPATH + "scripts/";

    public static void loadEriScripts(String scriptsPath) {
        File filesPath = new File(scriptsPath);
        loadEriScripts(eriScripts, filesPath);
    }

    public static void loadEriScripts(Map<String, EriScript> eriScripts, File filesPath) {
        if (!filesPath.exists() || !filesPath.isDirectory()) {
            ErrorLog.logError("Error in file path.");
            return;
        }
        File[] arrFiles = filesPath.listFiles();

        if (arrFiles != null) {
            for (File file : arrFiles) {
                if (file.isFile() && (file.getName().endsWith(".erisc") || file.getName().endsWith(".eriscript"))) {
                    try {
                        String name = file.getName().substring(0, file.getName().lastIndexOf('.')).replaceAll("\\s+", "").toUpperCase();
                        EriScript script = new EriScript(file.toPath());
                        eriScripts.put(name, script);
                    } catch (IOException e) {
                        ErrorLog.logError(e);
                    }
                }
            }
        } else {
            ErrorLog.logError("No script files.");
        }
    }

    //throws exception ?
    public static int beginsWithISO2(String in) {
        int i = 0;
        while (i < in.length() - 3) {
            if (Character.isWhitespace(in.charAt(i)))
                i++;
            else if (Character.isLetter(in.charAt(i)) && Character.isLetter(in.charAt(i + 1)) && Character.isWhitespace(in.charAt(i + 2))) {
                i += 3;
                while (i < in.length()) {
                    if (Character.isWhitespace(in.charAt(i)))
                        i++;
                    else
                        return i;
                }
                return i;
            } else
                return -1;
        }
        return -1;
    }

    public static String execute(String in) {
        return execute(in, false);
    }

    public static void executeAll(List<String> in) {
        for (String s : in)
            execute(s, true);
    }

    public static String execute(String in, boolean admin) {
        in = in.trim();
        if (in.length() < 2) {
            return "";
        }
        String temp;
        if (in.length() > 6) {
            temp = in.substring(0, 5);
            if (temp.equalsIgnoreCase("PARSE"))
                return gs.getGame().parseTextCommand(in.substring(6));
            else if (temp.equalsIgnoreCase("CHECK")) {
                return checkStatementsAND(in.substring(6)) ? "TRUE" : "FALSE";
            }
        }

        if (!GOptions.isAllowCLI() && !admin) {
            return "NOT ALLOWED (TURNED OFF IN OPTIONS)";
        }
        if (in.length() > 10 && in.substring(0, 8).equalsIgnoreCase("PERIODIC")) {
            gs.getGame().addPeriodicCommand(in.substring(9), admin);
            return "PERIODIC COMMAND ADDED";
        }
        boolean isPlayer;
        int endISO2 = beginsWithISO2(in);
        Country mainCountry;
        if (endISO2 > 0) {
            String shortName = in.substring(0, 2).toUpperCase();
            in = in.substring(endISO2);
            mainCountry = countries.get(shortName);
            isPlayer = shortName.equals(playerISO2);
        } else if (playerId != -1) {
            mainCountry = countries.get(playerId);
            isPlayer = true;
        } else {
            return "";
        }

        return execute(mainCountry, in, isPlayer);
    }

    public static String execute(Country mainCountry, String in, boolean isPlayer) {
        String shortName = mainCountry.getIso2();

        //UNION EU "European Union"... case preserved inside quotes ""
        String[] k = tokenize(in);
        int cIndex = mainCountry.getCountryId();

        if (k.length < 2)
            return "";
        switch (k[0]) {
            case "ADD":
                switch (k[1]) {
                    case "GDP":
                        if (k.length == 3) {
                            mainCountry.addGDP(k[2]);
                            return "Successfully added " + k[2] + " amount of GDP to " + shortName;
                        }
                        return "";
                    case "REL":
                        if (k.length == 3) {
                            mainCountry.improveRelations(CountryArray.getIndexShort(k[2]));
                            return "Improved relations with " + k[2];
                        }
                        return "";
                    case "TREASURY":
                        if (k.length == 3) {
                            double treasuryAmount = GUtils.parseDoubleAndPercent(k[2]);
                            mainCountry.getEconomy().addTreasuryOrPercent(treasuryAmount);
                            TESTING.print(mainCountry.getTreasury());
                            return "Successfully added " + k[2] + " amount of treasury to " + shortName;
                        }
                        return "";
                    case "POP":
                        if (k.length == 2) {
                            mainCountry.incPopulation();
                        } else if (k.length == 3) {
                            double popInc = GUtils.parseDoubleAndPercent(k[2]);
                            long popAmount;
                            if (popInc >= -1 && popInc <= 1) {
                                popAmount = mainCountry.incPopulation(popInc);

                            } else {
                                popAmount = (long) popInc;
                                mainCountry.addPopulation(popAmount);
                            }
                            return "ADDED A TOTAL OF " + popAmount + " POPULATION";
                        } else if (k.length == 4) {
                            double popInc = GUtils.parseDoubleAndPercent(k[2]);
                            int provId = GUtils.parseI(k[3]);
                            AdmDiv admDiv = gs.getGame().getWorld().getAdmDiv(provId);
                            if (admDiv != null) {
                                if (admDiv.getOwnerId() == mainCountry.getCountryId()) {
                                    int popAmount = mainCountry.addIncPopulation(popInc, admDiv);
                                    return "Added " + popAmount + " population to " + admDiv.getName();
                                } else {
                                    return "ADM DIV " + provId + " DOESN'T BELONG TO " + shortName;
                                }
                            } else {
                                return "ERROR: ADM DIV DOESN'T EXIST";
                            }
                        }

                        return "";
                    case "EVENT":
                        GDate date;
                        if (k.length == 4)
                            date = new GDate(k[3]);
                        else
                            date = gs.getGame().specialDateFromNowInDays(30);

                        //add base event as game event
                        //2/4/2024 (if bigger add)
                        if (gs.getGame().addBaseEventToGameEvents(k[2], date))
                            return "EVENT ADDED";
                        else
                            return "ERROR: FAILED TO ADD EVENT";
                    case "WARSCORE":
                        double amount = GUtils.parseD(k[2]);
                        if (k.length == 3) {
                            for (int i : mainCountry.getMilitary().getAtWarWith()) {

                            }
                        }
                        return "";
                    case "GOV":
                        if (k.length >= 4) {
                            if (k[2].substring(0, 3).equalsIgnoreCase("POL")) {
                                try {
                                    GovPolicy govPolicy = GovPolicy.valueOf(k[3]);
                                    int years = GUtils.parseIntDef(k, 4, 5);
                                    mainCountry.getGovernment().addPolicy(govPolicy, years);
                                } catch (EnumConstantNotPresentException e) {
                                    return "ERROR: Invalid government policy";
                                }
                            }
                        }
                        return "";
                    case "RESPECT":
                        int respect = GUtils.parseIntDef(k, 2, 0);
                        if (respect != 0) {
                            mainCountry.getDiplomacy().addGlobalRespect(respect);
                            return respect + " added to " + mainCountry.getIso2();
                        } else
                            return "NO RESPECT ADDED";
                    default:
                        return "ERROR: Invalid command";
                }
            case "REMOVE":
                switch (k[1]) {
                    case "GOV":
                        if (k.length == 4) {
                            String subK = k[2].substring(0, 3);
                            if (subK.equalsIgnoreCase("POL")) {
                                try {
                                    GovPolicy govPolicy = GovPolicy.valueOf(k[3]);
                                    mainCountry.getGovernment().removePolicy(govPolicy, true);
                                    return "REMOVED POLICY " + govPolicy;
                                } catch (EnumConstantNotPresentException e) {
                                    return "ERROR: Invalid government policy to remove";
                                }
                            }
                        }
                        return "";

                    default:
                        return "";
                }
            case "CHANGE":
                switch (k[1]) {
                    case "COLOR":
                        if (k.length == 3) {
                            if (gs.getMap().containsColor(shortName)) {
                                gs.getMap().changeColor(shortName, k[2]);
                                return "Successfully changed color of " + shortName + " to " + k[2];
                            }
                        }
                        return "";
                    case "COUNTRY":
                        // TO AL
                        return "";
                    case "GOV":
                        switch (k[2]) {
                            case "TYPE":
                                if (k.length == 4) {
                                    mainCountry.changeGovType(k[3]);
                                }
                                return "";
                            case "RULER":
                                if (k.length >= 5) {
                                    mainCountry.changeGovRuler(GUtils.parseI(k[3]), k[4]);
                                }
                                return "";
                            case "HOS":
                                if (k.length >= 4) {
                                    mainCountry.changeGovRuler(0, k[3]);
                                }
                                return "";
                            case "HOG":
                                if (k.length >= 4) {
                                    mainCountry.changeGovRuler(1, k[3]);
                                }
                                return "";
                            default:
                                return "ERROR: Invalid government argument";
                        }
                    default:
                        return "";
                }
                //target country
            case "REBELS":
                RebelType rebelType;
                try {
                    rebelType = RebelType.valueOf(k[1]);
                } catch (EnumConstantNotPresentException e) {
                    return "ERROR: INVALID REBEL TYPE";
                }
                Country opponent = countries.get(k[k.length - 1]);
                if (opponent == null)
                    return "ERROR: INVALID TARGET COUNTRY";
                if (k.length == 3) {


                } else if (k.length == 4) {

                }
                return "";
            //target country
            case "ANNEX":
                if (k.length == 2) {
                    try {
                        int provId = Integer.parseInt(k[1]);
                        gs.getGame().getWorld().annexAdmDiv(cIndex, provId);
                    } catch (NumberFormatException nfe) {
                        mainCountry.annexCountry(countries, CountryArray.getIndex(k[1]), true);
                        gs.getMap().refreshMap();
                    }
                    return shortName + " annexed " + k[1];
                }
                //CHANGED
                //US ANNEX DE
                // ANNEX DE

                return "";
            //target country

            case "OCCUPY":
                if (k.length == 2) {
                    try {
                        int provId = Integer.parseInt(k[1]);
                        gs.getGame().getWorld().occupyAdmDiv(cIndex, provId);
                    } catch (NumberFormatException nfe) {
                        gs.getGame().getWorld().occupyAllAdmDiv(cIndex, k[1]);
                    }
                    gs.getMap().refreshMapIf(0);
                    return shortName + " occupied " + k[1];

                }
                return "";
            //target country

            case "ALLY":
                if (countries.containsKey(k[1])) {
                    if (k.length == 2) {
                        // MAKE MORE EFFICIENT
                        if (countries.containsKey(k[1])) {
                            mainCountry.addAlly(k[1]);
                            countries.get(k[1]).addAlly(shortName);
                            return shortName + " is now ally with " + k[1];
                        }
                    }
                }
                return "";
            //target country

            case "SUBJECT":
                SubjectType type = null;
                try {
                    type = SubjectType.valueOf(k[1]);
                } catch (Exception e) {
                    return "ERROR: INVALID SUBJECT TYPE";
                }

                gs.getGame().getWorld().subjugateCountry(cIndex, CountryArray.getIndex(k[2]), type);
                return "";
            case "UNION":
                if (k.length > 2) {
                    switch (k[1]) {
                        case "ADD":
                            if (k.length > 5) {
                                gs.getGame().getWorld().addUnion(k[2], k[3], k[4], k[5]);
                            }
                            return "";
                        case "REMOVE":
                            if (gs.getGame().getWorld().removeUnion(k[2]))
                                return "Removed union " + k[2];
                            else
                                return "ERROR: Union doesn't exist";
                    }
                }

                return "";
            //target country
            case "WAR":
                CasusBelli casusBelli;
                try {
                    casusBelli = CasusBelli.valueOf(k[2]);
                } catch (EnumConstantNotPresentException e) {
                    casusBelli = CasusBelli.IMPERIALISM;
                }
                if (gs.getGame().declareWar(cIndex, CountryArray.getIndex(k[k.length - 1]), casusBelli)) {
                    if (casusBelli == CasusBelli.INDEPENDENCE) {
                        mainCountry.releaseSubject(k[2]);
                    }
                    return "WAR STARTED";

                } else {
                    gs.showAlert(Alert.AlertType.WARNING, "War declaration failed", "There is already a main war with them");
                    return "ERROR: WAR DECLARATION FAILED";
                }
            case "PLAY":
                switch (k[1]) {
                    case "CHESS":
                        //!Stops execution till game finishes!
                        int res;
                        if (isPlayer) {
                            res = gs.popupChess(k[2]);
                        } else {
                            res = (int) ((Math.random() * 3) - 1);
                        }
                        return res == 0 ? "DRAW" : res > 0 ? "WON" : "LOST";
                    case "TICTACTOE":
                        // DEFAULT
                        if (isPlayer) {
                            if (k.length == 2) {
                                gs.showPopupMGTicTacToe(true, 2);
                                return "";
                            } else if (k.length == 3) {
                                gs.showPopupMGTicTacToe(true, GUtils.parseI(k[2]));
                                return "";
                            } else {
                                if (k[3].equals("X")) {
                                    gs.showPopupMGTicTacToe(true, GUtils.parseI(k[2]));
                                    return "";
                                } else if (k[3].equals("O")) {
                                    gs.showPopupMGTicTacToe(false, GUtils.parseI(k[2]));
                                    return "";
                                } else {
                                    return "";
                                }
                            }
                        } else {
                            return "";
                        }
                    case "2048":
                        if (isPlayer)
                            return String.valueOf(gs.popupMG2048());
                        else
                            return String.valueOf((int) (Math.random() * 2048));
                    case "SNAKE":
                        if (isPlayer)
                            return String.valueOf(gs.popupMGSnake());
                        else
                            return String.valueOf((int) (Math.random() / 1.5 * (Math.pow(MGSnakeStage.getDefSize(), 2))));
                    default:
                        return "ERROR: No such game available";
                }
            case "ALERT":
                if (isPlayer) {
                    Alert.AlertType a;
                    String title;
                    String description;
                    try {
                        a = Alert.AlertType.valueOf(k[1]);
                        title = k[2];
                        description = k[3];
                    } catch (Exception e) {
                        a = Alert.AlertType.NONE;
                        title = k[1];
                        description = k[2];
                    }
                    gs.showAlert(a, title, description);
                    return "";

                } else {
                    return "";
                }
            case "EVENT":
                BaseEvent baseEvent = gs.getGame().getBaseEvent(k[1]);
                if (baseEvent != null) {
                    gs.popupGEvent(baseEvent);
                }
                return "";
            case "GLOBE":
                if (isPlayer) {
                    if (k.length == 2)
                        gs.popupGlobeViewer(GUtils.parseI(k[1]));
                }
                return "";
            case "SCRIPT":
                EriScript script = eriScripts.get(k[1]);
                if (script == null)
                    return "ERROR: NO SUCH SCRIPT LOADED";
                script.execute(2, k);
                return script.toPrintClear();
            case "SKIP":
                int daysToSkip = GUtils.parseIntOrMinMaxDef(k[1], 1, 31, 1);
                for (int i = 0; i < daysToSkip; i++) {
                    gs.getGame().dailyTick();
                }
                return daysToSkip + " days skipped";
            default:
                return "ERROR: Invalid command";
        }
    }

    public static String executeAllLines(String in, boolean admin) {
        StringBuilder result = new StringBuilder();
        String[] commands = in.split(COMMAND_SEPARATOR);//
        result.append(execute(commands[0], admin));
        for (int i = 1; i < commands.length; i++) {
            result.append('\n').append(execute(commands[i], admin));
        }
        return result.toString();
    }

    public static String[] executeAllLinesArr(String in, boolean admin) {
        String[] commands = in.split(COMMAND_SEPARATOR);
        String[] result = new String[commands.length];
        for (int i = 1; i < commands.length; i++) {
            result[i] = execute(commands[i], admin);
        }
        return result;
    }

    public static void executeAllNoResult(String in) {
        if (in.contains(COMMAND_SEPARATOR)) {
            String[] commands = in.split(COMMAND_SEPARATOR);
            for (String command : commands) {
                execute(command);
            }
        } else
            execute(in);
    }

    public static CountryArray getCountries() {
        return countries;
    }

    public static void setCountries(CountryArray countries) {
        CommandLine.countries = countries;
    }

    public static void setPlayerCountry(String playerISO2) {
        CommandLine.playerISO2 = playerISO2;
        CommandLine.playerId = CountryArray.getIndex(playerISO2);
    }

    public static void setPlayerCountry(int playerId) {
        CommandLine.playerISO2 = CountryArray.getIndexISO2(playerId);
        CommandLine.playerId = playerId;
    }

    public static GameStage getGameStage() {
        return gs;
    }

    public static void setGameStage(GameStage gs) {
        CommandLine.gs = gs;
    }

    // if 'IS', rest input
    public static boolean checkStatement(String in) {
        return checkStatement(in, "\\s+");
    }


    //updating HAS requires updating
    public static boolean checkStatement(String in, String separator) {
        boolean isPlaying = playerId > 0;
        String[] k = in.toUpperCase().split(separator);
        if (k[0].equals("NONE")) {
            return true;
        }
        boolean result = false;
        boolean isNot = false;
        // or implement logic elsewhere?
        int i = 0;
        try {
            // IS PLAYER -> true
            if (k[i].equals("IS")) {
                i++;

                if (k[i].equals("NOT")) {
                    isNot = true;
                    i++;
                }
                if (k.length == i + 1 && isPlaying) {
                    if (k[i].contains("|")) {
                        result = checkIsOR(playerISO2, k[i].split("\\|"));
                    } else if (k[i].equals(playerISO2)) {
                        result = true;
                    }
                }
            }
            // if String -> owns all of certain country
            // if Integer -> AdmDiv id
            else if (k[i].equals("HAS")) {
                i++;

                if (k[i].equals("NOT")) {
                    isNot = true;
                    i++;
                }
                if (k.length == i + 1 && isPlaying) {
                    if (k[i].contains("|")) {
                        result = checkHasOR(playerISO2, k[i].split("\\|"));
                    } else if (k[i].contains("&")) {
                        result = checkHasAND(playerISO2, k[i].split("&"));
                    } else if (k[i].equals(playerISO2)) {
                        result = true;
                    }
                }
            }

            if (isNot)
                return !result;
            else
                return result;
        } catch (Exception e) {
            return false;
        }
    }


    //CFormable/Releasable
    private static boolean checkHasAND(String playerISO2, String[] split) {
        World world = gs.getGame().getWorld();
        return world.getInitialProvinces().ownsAllISO2(world.getCountry(playerISO2), split);
    }

    private static boolean checkHasOR(String playerISO2, String[] split) {
        World world = gs.getGame().getWorld();
        return world.getInitialProvinces().ownsAtLeastOneISO2(world.getCountry(playerISO2), split);
    }

    public static boolean checkStatementsAND(String in) {
        String[] s = in.split("\\s*&&\\s*");
        for (int i = 0; i < s.length; i++) {
            //call checkstatements or here with ||
            if (!checkStatement(s[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkIsOR(String val, String... s) {
        for (String c : s) {
            if (val.equals(c)) {
                return true;
            }
        }
        return false;
    }

    //Connect with EriScript like: (?)
    //game:get:alGDP:AL.eco.gdp
    //game.AL.eco.gdp+=alGDP*0.1
    //instanceof there
    public static Object getCountryValue(String in) {

        return null;
    }

    public static void setCountryValue(String in) {
        //Some of above commands become redundant
    }

    public static void setCountryValue(String in, Object o) {
        //Some of above commands become redundant
    }


    public static String[] tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("[^\\s\"]+|\"([^\"]*)\"").matcher(input);

        while (m.find()) {
            //"Filan Fisteku"
            if (m.group(1) != null) {
                tokens.add(m.group(1));
            }
            //SINGLEWORD
            else {
                tokens.add(m.group().toUpperCase());
            }
        }

        return tokens.toArray(new String[0]);
    }

    public static void initCMD(CountryArray countries) {
        setCountries(countries);
        CommandLine.loadEriScripts(scriptsPath);
    }

    public static EriScript getEriScript(String impName, Path p) {
        if (eriScripts == null)
            return null;
        String name = impName.toUpperCase();
        if (eriScripts.containsKey(name)) {
            if (eriScripts.get(name).isInCurrPath(p))
                return eriScripts.get(name);
            else
                return null;
        } else {
            return null;
        }
    }

}
