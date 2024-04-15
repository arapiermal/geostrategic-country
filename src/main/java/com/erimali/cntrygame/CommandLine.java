package com.erimali.cntrygame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.erimali.compute.EriScript;
import javafx.scene.control.Alert;


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
    private static final String COMMAND_SEPARATOR = ";";

    private static GameStage gs;
    //extract below from previous (?)
    private static CountryArray countries;
    private static int playerId = -1;
    private static String playerISO2;

    //
    private static Map<String, EriScript> eriScripts;
    //Make changeable in GOptions (?)
    private static String scriptsPath = GLogic.RESOURCESPATH + "scripts/";

    public static void loadEriScripts() {
        eriScripts = new HashMap<>();
        File filesPath = new File(scriptsPath);
        if (!filesPath.exists() || !filesPath.isDirectory()) {
            ErrorLog.logError("Error in file path.");
            return;
        }
        File[] arrFiles = filesPath.listFiles();
        if (arrFiles != null) {
            for (File file : arrFiles) {
                if (file.isFile() && (file.getName().endsWith(".erisc") || file.getName().endsWith(".eriscript"))) {
                    try {
                        String name = file.getName().substring(0, file.getName().lastIndexOf('.')).toUpperCase();
                        String string = new String(Files.readAllBytes(file.toPath()));
                        EriScript script = new EriScript(string);
                        eriScripts.put(name, script);
                    } catch (IOException e) {
                        e.printStackTrace();
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

    public static String execute(String in, boolean admin) {
        String result = "";
        in = in.trim();
        if (in.length() < 2) {
            return result;
        }
        if (in.length() > 6 && in.substring(0, 5).equalsIgnoreCase("PARSE")) {
            return gs.getGame().parseTextCommand(in.substring(6));
        }
        if (!GOptions.isAllowCLI() && !admin) {
            return "NOT ALLOWED (TURNED OFF IN OPTIONS)";
        }

        String shortName;

        int endISO2 = beginsWithISO2(in);
//shortName is inside country...
        Country mainCountry;
        if (endISO2 > 0) {
            shortName = in.substring(0, 2).toUpperCase();
            in = in.substring(endISO2);
            mainCountry = countries.get(shortName);
        } else if (playerId != -1) {
            mainCountry = countries.get(playerId);
            shortName = playerISO2;
        } else {
            return result;// or other types of commands (separated)
        }
        //union EU "European Union"... preserves case inside ""
        String[] k = tokenize(in);       //in.split("\\s+");
        int cIndex = CountryArray.getIndex(shortName);

        if (k.length < 2)
            return result;
        switch (k[0]) {
            case "ADD":
                switch (k[1]) {
                    case "GDP":
                        if (k.length == 3) {
                            mainCountry.addGDP(k[2]);
                            result = "Successfully added " + k[2] + " amount of GDP to " + shortName;
                        }
                        break;
                    case "REL":
                        if (k.length == 3) {
                            mainCountry.improveRelations(CountryArray.getIndexShort(k[2]));
                            result = "Improved relations with " + k[2];
                        }
                        break;
                    default:
                        return "Invalid command";// secondary command
                }
                break;
            case "CHANGE":
                switch (k[1]) {
                    case "COLOR":
                        if (k.length == 3) {
                            if (gs.getMap().containsColor(shortName)) {
                                gs.getMap().changeColor(shortName, k[2]);
                                result = "Successfully changed color of " + shortName + " to " + k[2];
                            }
                        }
                        break;
                    case "COUNTRY":
                        // TO AL
                        break;
                    case "GOV":
                        switch (k[2]) {
                            case "TYPE":
                                if (k.length == 4) {
                                    mainCountry.changeGovType(k[3]);
                                }
                                break;
                            case "RULER":
                                if (k.length == 5) {
                                    mainCountry.changeGovRuler(GUtils.parseI(k[3]), k[4]);
                                }
                                break;
                            default:
                                return "Invalid government argument";
                        }
                    default:
                        break;
                }
                break;
            case "REBELS":
                break;
            case "ANNEX":
                if (k.length == 2) {
                    mainCountry.annexCountry(countries, CountryArray.getIndex(k[1]));
                    result = shortName + " annexed " + k[1];
                    gs.getMap().refreshMap();
                }
                //CHANGED
                //US ANNEX DE
                // ANNEX DE

                break;
            case "ALLY":
                if (countries.containsKey(k[1])) {
                    if (k.length == 2) {
                        // MAKE MORE EFFICIENT
                        if (countries.containsKey(k[1])) {
                            mainCountry.addAlly(k[1]);
                            countries.get(k[1]).addAlly(shortName);
                            result = shortName + " is now ally with " + k[1];
                        }
                    }
                }
                break;
            case "SUBJECT":
                SubjectType type = null;
                try {
                    type = SubjectType.valueOf(k[1]);
                } catch (Exception e) {
                }

                if (type != null) {
                    gs.getGame().getWorld().subjugateCountry(cIndex, CountryArray.getIndex(k[2]), type);
                }
                break;
            case "UNION":
                if (k.length > 4)
                    gs.getGame().getWorld().addUnion(k[1], k[2], k[3], k[4]);
                break;
            case "WAR":
                if (k.length == 2) {
                    gs.getGame().declareWar(cIndex, CountryArray.getIndex(k[1]), CasusBelli.IMPERIALISM);
                } else {
                    gs.getGame().declareWar(cIndex, CountryArray.getIndex(k[1]), CasusBelli.valueOf(k[3]));
                }
            case "PLAY":
                switch (k[1]) {
                    case "CHESS":
                        //!Stops execution till game finishes!
                        int res = gs.popupChess(k[2]);
                        return res == 0 ? "DRAW" : res > 0 ? "WON" : "LOST";
                    case "TICTACTOE":
                        // DEFAULT
                        if (k.length == 2) {
                            gs.showPopupMGTicTacToe(true, 2);
                        } else if (k.length == 3) {
                            gs.showPopupMGTicTacToe(true, GUtils.parseI(k[2]));
                        } else {
                            if (k[3].equals("X")) {
                                gs.showPopupMGTicTacToe(true, GUtils.parseI(k[2]));
                            } else if (k[3].equals("O")) {
                                gs.showPopupMGTicTacToe(false, GUtils.parseI(k[2]));
                            }
                        }
                        break;
                    default:
                        return "No such game available";
                }
                break;
            case "ALERT":
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
                break;
            case "EVENT":
                // implement logic
                // when you want to cause event
                break;
            case "GLOBE":
                if (k.length == 2)
                    gs.popupGlobeViewer(GUtils.parseI(k[1]));
                break;
            case "SCRIPT":
                EriScript script = eriScripts.get(k[1]);
                script.execute(2, k);
                return script.toPrintClear();
            default:
                return "Invalid command";
        }
        return result;
    }

    public static String executeAllLines(String in, boolean admin) {
        StringBuilder result = new StringBuilder();
        String[] commands = in.split(COMMAND_SEPARATOR);//
        result.append(execute(commands[0], admin));
        for(int i = 1; i < commands.length; i++){
            result.append('\n').append(execute(commands[i],admin));
        }
        return result.toString();
    }
    public static String[] executeAllLinesArr(String in, boolean admin) {
        String[] commands = in.split(COMMAND_SEPARATOR);
        String[] result = new String[commands.length];
        for(int i = 1; i < commands.length; i++){
            result[i] = execute(commands[i],admin);
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
        boolean isPlaying = playerId > 0;
        String[] k = in.toUpperCase().split("\\s+");
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
                        result = checkHasAND(playerISO2, k[i].split("\\&"));
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
    private static boolean checkHasAND(String playerISO22, String[] split) {
        // TODO Auto-generated method stub
        return false;
    }

    private static boolean checkHasOR(String playerISO22, String[] split) {
        // TODO Auto-generated method stub
        return false;
    }

    public static boolean checkStatementsAND(String in) {
        String[] s = in.split(";");
        for (int i = 0; i < s.length; i++) {
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

}
