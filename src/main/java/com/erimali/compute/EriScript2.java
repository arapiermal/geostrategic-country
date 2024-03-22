package com.erimali.compute;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.IntStream;

import com.erimali.cntrygame.CommandLine;
import com.erimali.cntrygame.ErrorLog;
import com.erimali.cntrygame.GameAudio;

//PSEUDOCOMPILATION
//SWITCH CASE OF STRINGS COULD BECOME NUMBERS FOR TYPE OF FUNC?
// 0 => EXECUTE ALL ROW
// 1=> FOR
// 2=> IF ... etc
// List<Integer> rowType;
// List<String[]> rows; // params
public class EriScript2 {
    // class which is just a function that doesn't forget previous state?
    private static final String RETURN = "RETURN";
    private static final String EXTRAARG = "EXTRA";
    private String separator = "\n";// System.lineSeparator();
    private List<String[]> rows;
    private String[] params;
    private double[] regDouble;
    // Registry which stores objects? private Object[] registry
    // double registry and int registry?
    // RD0
    // RI0
    private Map<String, Double> variables;
    private Map<String, List<Double>> varArr;

    private Map<String, EriString> varString;
    private Map<String, EriScript2> functions;
    private int i;
    private boolean hasReturned;
    private Stack<Integer> forLoops;
    private Deque<String> errors;
    // private boolean allowHelperVars;
    // like THIS for current running row index

    // Deque<...Stacktrace??> errors?
    private List<String> printed;

    private static final String tempInd = "IND";

    public void addPrintedLines(String... toPrint) {
        for (String line : toPrint) {
            printed.add(line);
        }
    }

    public void printPrinted() {
        for (String s : printed) {
            System.out.println(s);
        }
    }

    public void printVariables() {
        for (Map.Entry<String, Double> entry : variables.entrySet()) {
            System.out.println(entry);
        }
    }

    public void initThings() {
        this.regDouble = new double[64];
        this.varArr = new HashMap<>();
        this.forLoops = new Stack<>();
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        this.rows = new ArrayList<>();
        this.printed = new LinkedList<>();
        this.i = 0;
        this.varString = new HashMap<>();
    }

    public void extractParamNames(String in) {
        this.params = in.trim().split("\\s*,\\s*");
    }

    public EriScript2(String string) {
        this.forLoops = new Stack<>();
        this.variables = new HashMap<>();
        this.varArr = new HashMap<>();
        this.varString = new HashMap<>();
        this.functions = new HashMap<>();
        this.rows = new ArrayList<>();
        this.printed = new LinkedList<>();
        this.regDouble = new double[64];
        String[] splitted = string.split(separator);
        int beginFrom = 0;
        String temp;
        if ((temp = trimNoComments(splitted[0]).toLowerCase()).startsWith("params")) {
            int index = temp.indexOf(':');
            if (index != -1) {
                extractParamNames(temp.substring(index + 1));
            } else {
                extractParamNames(temp.substring(7));
            }
            beginFrom = 1;
        }
        for (i = beginFrom; i < splitted.length; i++) {
            splitted[i] = trimNoComments(splitted[i]);
            if (splitted[i].isBlank())
                continue;
            if (splitted[i].startsWith("@")) {
                //Add import like @import@hello.txt,mali.erisc
                
                loadFunction(splitted);
            } else if (splitted[i].length() > 2 && splitted[i].charAt(splitted[i].length() - 2) == '~') {
                StringBuilder sb = new StringBuilder(splitted[i].substring(0, splitted[i].length() - 2));
                char separateBy = splitted[i].charAt(splitted[i].length() - 1);
                i++;
                uniteRows(sb, splitted, separateBy);
                rows.add(splitRow(sb.toString()));
            } else {
                rows.add(splitRow(splitted[i]));
            }
        }

        i = 0;
    }

    public String[] splitRow(String s) {
        String[] res = s.split("(?<!\\\\):");
        if(res.length==1){
            //No need for trimming for "a = 10"
            if(!hasEriString(2,res[0]))
                res[0] = res[0].replaceAll("\\s+","");
        }
        //Otherwise it is a command
        else if(res.length > 1){
            res[0] = res[0].trim().toUpperCase();
        }
        return res;
    }

    public void uniteRows(StringBuilder sb, String[] splitted, char separator) {
        char sep;
        if (separator != '~') {
            sep = switch (separator) {
                case 's' -> ' ';
                case 't' -> '\t';
                case 'n' -> '\n';
                case ',' -> ',';
                case ';' -> ';';
                default -> '~';
            };
            sb.append(sep); // can be better
            int endSign = -1;
            while (i < splitted.length && (endSign = splitted[i].indexOf('~')) == -1) {
                sb.append(splitted[i]).append(sep);
                i++;
            }
            if (endSign != -1)
                sb.append(splitted[i], 0, endSign);
        } else {
            int endSign = -1;
            while (i < splitted.length && (endSign = splitted[i].indexOf('~')) == -1) {
                sb.append(splitted[i]);
                i++;
            }
            if (endSign != -1)
                sb.append(splitted[i], 0, endSign);
        }
    }

    public void loadFunction(String[] splitted) {
        int hasInputs = splitted[i].indexOf(':');
        if (hasInputs == -1) {
            String fName = splitted[i].substring(1);
            List<String[]> fRows = new ArrayList<>();
            while (++i < splitted.length && !(splitted[i] = splitted[i].trim()).startsWith("#@")) {
                fRows.add(splitRow(splitted[i]));
            }
            functions.put(fName, new EriScript2(fRows, null));
        } else {
            String fName = splitted[i].substring(1, hasInputs);
            String[] params = splitted[i].substring(hasInputs + 1).trim().split("\\s*,\\s*");

            List<String[]> fRows = new ArrayList<>();
            while (++i < splitted.length && !(splitted[i] = splitted[i].trim()).startsWith("#@")) {
                if (splitted[i].isBlank())
                    continue;
                fRows.add(splitRow(splitted[i]));
            }
            functions.put(fName, new EriScript2(fRows, params));
        }
    }

    //PARAM FROM LAST TIME REMEMBERED !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // a = obj.method{0} //function inside function
    public void loadFunction(List<String> splitted) {
        int hasInputs = splitted.get(i).indexOf(':');
        if (hasInputs == -1) {
            String fName = splitted.get(i).substring(1);
            List<String[]> fRows = new ArrayList<>();
            // start from where?!?
            for (String row : splitted) {
                row = row.trim();
                if (row.startsWith("##@")) {
                    i++;
                    break;
                } else {
                    fRows.add(splitRow(row));
                    i++;
                }
            }
            functions.put(fName, new EriScript2(fRows, null));
        } else {
            String fName = splitted.get(i).substring(1, hasInputs);
            String[] params = splitted.get(i).substring(hasInputs + 1).trim().split("\\s*,\\s*");

            List<String[]> fRows = new ArrayList<String[]>();
            for (String row : splitted) {
                row = row.trim();
                if (row.startsWith("##@")) {
                    i++;
                    break;
                } else {
                    fRows.add(splitRow(row));
                    i++;
                }
            }
            functions.put(fName, new EriScript2(fRows, params));
        }
    }

    public EriScript2(List<String[]> fRows, String[] params) {
        initThings();
        this.rows = fRows;
        this.params = params;
    }

    public void execute(double... dParams) {
        if (dParams != null && params != null) {
            int n = Math.min(dParams.length, params.length);
            int j;
            for (j = 0; j < n; j++) {
                variables.put(params[j], dParams[j]);
            }
            int m = Math.max(dParams.length, params.length);
            if (m > n) {
                int k = 0;
                for (; j < m; j++) {
                    variables.put(EXTRAARG + k, dParams[j]);
                    k++;
                }
            }
        }
        execute(0);
        if (hasReturned)
            hasReturned = false;
    }

    public double getOneReturn() {
        return variables.getOrDefault(RETURN, 0.0);
    }

    public double[] getArrReturn() {
        int n = 0;
        while (variables.containsKey(RETURN + n)) {
            n++;
        }
        double out[] = new double[n];
        for (int j = 0; j < n; j++) {
            out[j] = variables.get(RETURN + j);
        }
        return out;
    }

    public double executeOneOutput() {
        execute(0);
        return getOneReturn();
    }

    public double[] executeManyOutputs() {
        execute(0);

        return getArrReturn();
    }

    public List<Double> executeManyOutputsList() {
        execute(0);
        int i = 0;
        List<Double> list = new ArrayList<Double>();
        while (variables.containsKey(RETURN + i)) {
            list.add(variables.get(RETURN + i++));
        }
        return list;
    }

    //double vs int can become problematic
    // execute(double... args)
    public void execute(int start) {
        for (i = start; i < rows.size(); i++) {
            /////////////////////////////////////////
            // variables.put("THIS", Double.valueOf(i));// efficiency --
            /////////////////////////////////////////

            if (hasReturned) {
                return;
            }
            String row[] = rows.get(i);
            // presuming TRIM()
            if (row[0].charAt(0) == '.') {
                return;
            }
            // if (index == 0) {return;}else
            if (row.length > 1) {
                execFuncRow();
            } else {
                execVarRow(row[0]);
            }
            // try {} catch (Exception e) {errors.add(e.toString());}
        }
    }

    public void execFuncRow(int[] args) {
        int tempI = i;
        if (args.length == 1) {
            if (i != args[0]) {
                i = args[0];
                execFuncRow();
                i = tempI;
            }
        }
    }

    public void initRegDouble(double val) {
        for (int i = 0; i < regDouble.length; i++) {
            regDouble[i] = val;
        }
    }

    public void execFuncRow() {
        // CACHED
        String[] parts = rows.get(i);
        String command = parts[0].trim();
        if (command.charAt(0) == '$') {
            if (command.charAt(1) == 'R') {
                if (command.length() == 2) {
                    if (parts[1].contains(",")) {
                        initRegArr(parts[1]);
                    } else {
                        initRegDouble(solveMath(parts[1]));
                    }
                } else if (command.charAt(2) == '[') {
                    int index = solveMathInt(parts[0].substring(3, command.length() - 1));
                    regDouble[index] = solveMath(parts[1]);

                } else {
                    try {
                        int index = Integer.parseInt(parts[0].substring(2));
                        regDouble[index] = solveMath(parts[1]);
                    } catch (NumberFormatException e) {

                    }
                }
            } else if (command.charAt(1) == 'r') {
                // for integers?
            }
            return;
        }
        switch (command.toUpperCase()) {
            case "FOR":
                if (parts.length == 2) {
                    String[] in = parts[1].split("\\s*,\\s*");
                    execVarRow(in[0]);
                    forLoops.push(i + 1);
                    // for efficiency
                    in[1] = in[1].replace("\\s+", "");
                    // make more efficient
                    boolean state = parseBool(in[1]);
                    if (state)
                        while (parseBool(in[1])) {
                            // execute code below it
                            execute(forLoops.peek());
                            // increment/decrement
                            execVarRow(in[2]);
                        }
                    else {
                        skip();
                    }

                    forLoops.pop();
                } else if (parts.length == 3) {
                    parts[1] = parts[1].trim();
                    parts[2] = parts[2].trim();
                    forLoops.push(i + 1);
                    double[] arr = getArr(parts[2]);
                    if (arr != null)
                        for (double a : arr) {
                            variables.put(parts[1], a);
                            execute(forLoops.peek());
                        }
                    else {
                        skip();
                    }
                    forLoops.pop();
                } else if (parts.length == 4) {
                    String varName = parts[1].trim();
                    int a = solveMathInt(parts[2]);
                    int b = solveMathInt(parts[3]);
                    forLoops.push(i + 1);
                    if (a < b)
                        for (int i = a; i < b; i++) {
                            variables.put(varName, Double.valueOf(i));
                            execute(forLoops.peek());
                        }
                    else if (a > b)
                        for (int i = b; i > a; i--) {
                            variables.put(varName, Double.valueOf(i));
                            execute(forLoops.peek());
                        }
                    else {
                        skip();
                    }
                    forLoops.pop();// PUT SOMEWHERE ELSE
                } else if (parts.length == 5) {
                    String varName = parts[1].trim();
                    double a = solveMath(parts[2]);
                    double b = solveMath(parts[3]);
                    double val = Math.abs(solveMath(parts[4]));
                    forLoops.push(i + 1);
                    if (a < b)
                        for (double i = a; i < b; i += val) {
                            variables.put(varName, i);
                            execute(forLoops.peek());
                        }
                    else
                        for (double i = b; i > a; i -= val) {
                            variables.put(varName, i);
                            execute(forLoops.peek());
                        }
                    forLoops.pop();
                } else {
                    throw new IllegalArgumentException(": OVERLOAD :::::::::::::");
                }
                break;
            case "IF":
                execIf();
                break;
            case "PRINT":
                if (parts.length == 2)
                    addPrintedLines(parsePrint(parts[1]));
                else if (parts.length == 1)
                    addPrintedLines("");
                else if (parts.length == 3) {
                    int ind;
                    if (parts[1].isBlank())
                        ind = printed.size() - 1;
                    else
                        ind = (int) solveMath(parts[1]);
                    if (ind < 0) {
                        ind = 0;
                    }
                    if (printed.isEmpty() || ind >= printed.size()) {
                        addPrintedLines(parsePrint(parts[2]));
                    } else {
                        setPrintedLine(ind, printed.get(ind) + parsePrint(parts[2]));
                    }
                }
                break;
            case RETURN:
                if (parts[1].contains(",")) {
                    String[] out = parts[1].trim().split("\\s*,\\s*");// no need?
                    for (int j = 0; j < out.length; j++) {
                        variables.put(RETURN + j, solveMath(out[j]));
                    }
                    // not necessary?
                    if (out.length > 0) {
                        hasReturned = true;
                    }
                    // cause to stop
                } else {
                    variables.put(RETURN, solveMath(parts[1]));
                    hasReturned = true;
                }
                break;
            // improve
            case "EXEC":
                printed.add(CommandLine.executeAllLines(parts[1]));
                break;
            case "CLR":
                clearSpecific(parts[1]);
                break;
            case "GRAPH":
                if (parts.length == 2) {
                    // x => arr?
                    double x[] = getArr(parts[1].trim());
                    EriScriptGUI.showPopupStage(GraphApp.generateGraph(x));
                } else if (parts.length == 3) {
                    // x:y
                    double x[] = getArr(parts[1].trim());
                    double y[] = getArr(parts[2].trim());
                    EriScriptGUI.showPopupStage(GraphApp.generateGraph(x, y));
                } else if (parts.length == 3) {
                    // x:y:z
                }
                break;
            case "INPUT":
                // input:$x:Description,maybe title ???
                // input:#x
                // input:%x
                // EriScriptGUI.showPopupStageInput(InputApp.getDoubleInput());
                String varName = parts[1].trim();
                if (varName.charAt(0) == '$') {
                    double input = EriScriptGUI.showDoubleInputDialog(EriScriptGUI.mainEriGUI);
                    variables.put(varName.substring(1), input);

                } else if (varName.charAt(0) == '#') {
                    double[] values = EriScriptGUI.showDoubleArrInputDialog(EriScriptGUI.mainEriGUI);
                    if (values != null) {
                        setArr(varName.substring(1), values);
                    } else {
                        printed.add("ERROR PARSING ARRAY");
                    }
                } else if (varName.charAt(0) == '%') {

                } else {
                    double input = EriScriptGUI.showDoubleInputDialog(EriScriptGUI.mainEriGUI);

                    variables.put(varName, input);
                }

                break;
            case "DEV":
                if (parts.length == 2) {
                    parseDevCommand(parts[1].trim(), "");
                } else if (parts.length == 3) {
                    parseDevCommand(parts[1].trim(), parts[2]);
                }
                break;
            case "DEF":
                if (parts.length == 3) {
                    boolean isArr = parts[2].contains(",");
                    String name = parts[1].trim();
                    if (isArr) {
                        if (!variables.containsKey(name + "0")) {
                            setArr(name, parts[2]);
                        }
                    } else {
                        if (!variables.containsKey(name)) {
                            variables.put(name, solveMath(parts[2]));
                        }
                    }

                }
                break;
            case "SWITCH":
                // make with $#%
                break;
            case "BREAK":
                break;// implement getting out of FOR LOOP
            case "WAIT":
                try {
                    Thread.sleep((long) solveMath(parts[1]));
                } catch (InterruptedException e) {
                    ErrorLog.logError(e);
                }
                break;
            case "IMG":
                EriIMG e = new EriIMG(Paths.get(parts[1]));
                EriScriptGUI.showPopupStage(e.generateStage());
                break;
            case "SOUND":
                if (parts.length == 2)
                    GameAudio.playShortSound(parts[1]);
				break;
            default:
                if (parts.length < 2)
                    executeFunction(parts[0]);
                else {
                    executeFunction(parts[0], parseDoubleArr(parts[1]));
                }
        }
    }

    private void setPrintedLine(int ind, String string) {
        this.printed.set(ind, string);
    }

    public String removeCharFromString(String s, char c) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != c) {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }

    // can be used when run on top of already run?
    public void parseDevCommand(String leftSide, String rightSide) {
        rightSide = rightSide.replace("\\:", ":");
        String[] cmds = leftSide.split("\\s*\\.\\s*");
        int ind = 0;
        if (cmds.length == 2) {
            ind = solveMathInt(cmds[1]);
            if (ind < 0)
                ind = 0;
            else if (ind >= rows.size())
                ind = rows.size() - 1;

        }
        switch (cmds[0].toUpperCase()) {
            case "ROW":
                rows.set(ind, splitRow(rightSide));
                break;
            case "FUNC":
                // logically cannot be empty (if calling from dev:)
                if (!rows.isEmpty()) {
                    execFuncRow(new int[]{ind});
                }
                break;
        }
    }

    // if left side contains [
    public String parseVarRequest(String in) {
        in = in.replaceAll("\\s+", "");
        int start = -1, end = -1;
        for (int i = 1; i < in.length(); i++) {
            if (in.charAt(i) == '[') {
                start = i;
                break;
            }
        }
        if (start != -1) {
            for (int i = in.length() - 1; i > start; i--) {
                if (in.charAt(i) == ']') {
                    end = i;
                    break;
                }
            }
            if (end != -1)
                return in.substring(0, start) + (int) solveMath(in.substring(start + 1, end));
            else
                return in;
        } else {
            return in;

        }

    }

    public void removeArr(String name) {
        int i = 0;
        String varName;
        while (variables.containsKey((varName = name + i))) {
            variables.remove(varName);
            i++;
        }
    }

    public void clearSpecific(String in) {
        String[] toClear = in.trim().split("\\s*,\\s*");
        for (String v : toClear) {
            if (v.charAt(0) == '$') {
                variables.remove(v.substring(1));
            } else if (v.charAt(0) == '#') {
                removeArr(v.substring(1));
            } else if (v.charAt(0) == '@') {
                //functions.remove(v.substring(1));
                //functions.get(v.substring(1)).clear();
            }
        }
    }

    public String parsePrint2(String s) {
        int t;
        for (t = 0; t < s.length(); t++) {
            if (s.charAt(t) == '$') {
                if (t > 0 && s.charAt(t - 1) == '\\') {
                    // how to remove
                    continue;
                }
                int toInt = 0;
                if (s.charAt(t + 1) == '$') {
                    toInt = 1;
                }
                if (s.charAt(toInt + t + 1) == '{') {
                    int j = toInt + t + 2;
                    while (j < s.length() && s.charAt(j) != '}') {
                        j++;
                    }
                    if (s.charAt(j - 1) == '{') {
                        return "0";
                    }
                    double val = solveMath(s.substring(toInt + t + 2, j));
                    if (toInt == 1)
                        s = s.replace(s.substring(t, j + 1), "" + (int) val);
                    else
                        s = s.replace(s.substring(t, j + 1), "" + val);
                    continue;
                }

                int a = t + 1;
                while (a < s.length() && !(Character.isWhitespace(s.charAt(a)))) {
                    a++;
                }
                if (toInt == 1) {
                    String vName = s.substring(t + 2, a);
                    int val = variables.getOrDefault(vName, 0.0).intValue();
                    s = s.replace(s.substring(t, a), "" + val);
                } else {
                    String vName = s.substring(t + 1, a);
                    double val = variables.getOrDefault(vName, 0.0);
                    s = s.replace(s.substring(t, a), "" + val);
                }
            } else if (s.charAt(t) == '#') {
                if (t > 0 && s.charAt(t - 1) == '\\') {
                    continue;
                }
                int a = t + 1;
                while (a < s.length() && !Character.isWhitespace(s.charAt(a))) {
                    a++;
                }
                String arrName = s.substring(t + 1, a);
                s = s.replace(s.substring(t, a), Arrays.toString(getArr(arrName)));
            } else if (s.charAt(t) == '%') {
                if (t > 0 && s.charAt(t - 1) == '\\') {
                    continue;
                }
                int a = t + 1;
                while (a < s.length() && !Character.isWhitespace(s.charAt(a))) {
                    a++;
                }
                String stringName = s.substring(t + 1, a);
                s = s.replace(s.substring(t, a), getEriString(stringName));
            }
        }

        return s;
    }

    public String parsePrint(String in) {
        StringBuilder sb = new StringBuilder();
        int t;
        for (t = 0; t < in.length(); t++) {
            char ch = in.charAt(t);
            if (ch == '\\') {
                if (t < in.length() - 1) {
                    char nc = in.charAt(t + 1);
                    if (nc == '\\') {
                        sb.append('\\');
                    } else if (nc == 'n') {
                        sb.append('\n');
                    } else if (nc == 't') {
                        sb.append('\t');
                    } else {
                        sb.append(in.charAt(t + 1));
                    }
                    t++;
                }
            } else if (ch == '$') {
                double val = 0;
                int toInt = 0;
                // try catch (INVALID VAR)
                if (t < in.length() - 1 && in.charAt(t + 1) == '$') {
                    toInt = 1;
                }
                if (in.charAt(toInt + t + 1) == '{') {
                    int j = toInt + t + 2;
                    while (j < in.length() && in.charAt(j) != '}') {
                        j++;
                    }
                    if (in.charAt(j - 1) == '{') {
                        sb.append('0');
                    }
                    val = solveMath(in.substring(toInt + t + 2, j));
                    t = j;

                } else {
                    int a = t + 1;
                    while (a < in.length() && !(Character.isWhitespace(in.charAt(a)))) {
                        a++;
                    }
                    if (toInt == 1) {
                        String vName = in.substring(t + 2, a);
                        val = variables.getOrDefault(vName, 0.0).intValue();
                    } else {
                        String vName = in.substring(t + 1, a);
                        val = variables.getOrDefault(vName, 0.0);
                    }
                    t = a - 1;

                }
                if (toInt == 1)
                    sb.append(String.valueOf((int) val));
                else
                    sb.append(String.valueOf(val));
            } else if (ch == '#') {
                int a = t + 1;
                while (a < in.length() && !Character.isWhitespace(in.charAt(a))) {
                    a++;
                }
                String arrName = in.substring(t + 1, a);
                sb.append(Arrays.toString(getArr(arrName)));
                t = a - 1;
            } else if (ch == '%') {
                int a = t + 1;
                while (a < in.length() && !Character.isWhitespace(in.charAt(a))) {
                    a++;
                }
                String stringName = in.substring(t + 1, a);
                sb.append(varString.get(stringName).getChars());
                t = a - 1;

            } else {
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    private void skip() {
        while (++i < rows.size() && !rows.get(i)[0].startsWith(".")) {
        }
    }

    // skip all else method, for
    // if true -> to end; if false, else if true -> to end
    private void execIf() {
        boolean isTrue = false;
        if (parseBool(rows.get(i)[1])) {
            isTrue = true;
            execute(++i);
        } else {
            skip();
        }

        int b;
        while (i < rows.size() && rows.get(i).length > 1) {
            if (isTrue) {
                skip();
            } else {
                if (parseBool(rows.get(i)[1])) {
                    isTrue = true;
                    execute(++i);
                } else {
                    skip();
                }
            }

        }
        if (rows.get(i)[0].toUpperCase().contains("ELSE")) {
            if (isTrue) {
                skip();
            } else {
                execute(++i);
            }
        }
    }

    public double[] doubleListToArr(List<Double> dl) {
        double[] arr = new double[dl.size()];
        int i = 0;
        for (double d : dl) {
            arr[i++] = d;
        }
        return arr;
    }

    public EriScript2 executeFunction(String name, double... values) {
        if (functions.containsKey(name)) {
            EriScript2 f = functions.get(name);
            f.execute(values);
            printed.addAll(f.printed);
            f.printed.clear();

            return f;
        } else {
            return null;
        }
    }

    public void executeFunction(String name, List<Double> values) {
        if (functions.containsKey(name)) {
            EriScript2 f = functions.get(name);
            f.execute(doubleListToArr(values));
            printed.addAll(f.printed);
            f.printed.clear();
        }
    }

    public void clear() {
        this.i = 0;
        this.variables.clear();
        this.forLoops.clear();
        this.printed.clear();
        this.hasReturned = false;
    }

    public boolean parseBool(String input) {
        return BooleanSolver.solve(input, variables);
    }

    // c = @func
    // c = @func:a,b
    public double getReturn() {
        return variables.get(RETURN);
    }

    public double[] getReturnArr() {
        return getArr(RETURN);
    }

    public char[] getReturnCharArr() {
        return varString.get(RETURN).getChars();
    }

    public static int[] getAllIndexes(String inputString, char targetChar) {
        return IntStream.range(0, inputString.length()).filter(i -> inputString.charAt(i) == targetChar).toArray();
    }

    //put List<> outside and clear inside?
    public void execVarRow2(String in) {
        StringBuilder sbVarName = new StringBuilder();
        char firstThing = 0; // if is +-*/^ different approach, if =,different
        char secondThing = 0;// if isOperator and firstThing == secondThing -> ++ -- **
        int j;
        List<String> varNames = null;
        List<Double> values = null;

        for (j = 0; j < in.length(); j++) {
            char c = in.charAt(j);
            if (MathSolver.isOperator(c)) {
                if (firstThing == 0) {
                    firstThing = c;
                } else if (secondThing == 0) {
                    secondThing = c;
                    break;
                }
            } else if (c == '=') {
                if (firstThing == 0) {
                    firstThing = c;
                } else if (secondThing == 0) {
                    secondThing = c;
                }
                break;
            } else if (c == ',') {
                if (varNames == null) {
                    varNames = new LinkedList<String>();
                    varNames.add(sbVarName.toString());
                    sbVarName.setLength(0);
                } else {
                    varNames.add(sbVarName.toString());
                    sbVarName.setLength(0);
                }
                // !!!!!!!!!!!!!!!!!!!
                // a,b,c=1,2,3,4,5
            } else if (c == '[') {
                int k = j + 1;// can be before ','!!!!
                int last = k;
                while (k < in.length()) {
                    if (in.charAt(k) == ']') {
                        last = k;
                    } else if (in.charAt(k) == ',') {
                        break;
                    }
                    k++;
                }
                int arrInd = solveMathInt(in.substring(j + 1, last));
                sbVarName.append(String.valueOf(arrInd));

                j = k;
            }
            // is digit or letter AND _
            else if (!Character.isWhitespace(c)) {
                sbVarName.append(c);
            }
        }
        int ind = j; // index of first '='
        String varName = sbVarName.length() != 0 ? sbVarName.toString() : null;
        // if no ','
        if (firstThing == '=') {
            StringBuilder valString = new StringBuilder();
            for (int k = ind + 1; k < in.length(); k++) {
                char c = in.charAt(k);
                if (c == ',') {
                    if (varNames == null) {
                        values = new LinkedList<Double>();
                        values.add(solveMath(valString.toString()));
                        valString.setLength(0);
                    } else {
                        values.add(solveMath(valString.toString()));
                        valString.setLength(0);
                    }
                } else if (!Character.isWhitespace(c)) {
                    valString.append(c);
                }
            }
        } else if (MathSolver.isOperator(firstThing)) {
            if (firstThing == secondThing) {
                // deal with ++ -- ** maybe in MathSolver itself???
            } else {
                // deal with += -= *= /= ^=
            }
        }

    }

    public void execVarRow(String input) throws NullPointerException {
        // input = input.replaceAll("\\s+", ""); //problems later
        int special = input.indexOf("<=>");// efficiency --
        if (special != -1) {
            String r = input.substring(special + 3).trim();
            String l = input.substring(0, special).trim();
            double tempR = variables.getOrDefault(r, 0.0);
            variables.put(r, variables.getOrDefault(l, 0.0));
            variables.put(l, tempR);
            return;
        }
        String varName = "";

        boolean dealingWithArray = false;
        double var = 0d;
        int index = input.indexOf('=');
        input = replaceFuncsWithVals(input); // make more efficient
        if (index != -1) {
            if (hasEriString(index, input)) {
                varName = input.substring(0, index).trim();
                String rightSide = input.substring(index + 1);
                parseEriString2(varName, rightSide);
                return;
            }
            // LESS TRIM????
            input = removeCharFromString(input, '\s');
            // index changes
            index = input.indexOf('=');
            if (input.length() > 2 && MathSolver.isOperator(input.charAt(index - 1))) {
                // varName = input.substring(0, index - 1);
                varName = subTrim(input, 0, index - 1);
                String rightSide = subTrim(input, index + 1);
                if (rightSide.contains(",")) {
                    if (input.charAt(index - 1) == '+')
                        appendArr(varName, rightSide);
                    return;
                }
                varName = parseVarRequest(varName);
                double leftOperand = variables.get(varName);
                char operator = input.charAt(index - 1);
                // double rightOperand = solveMath(input.substring(index + 1));
                double rightOperand = solveMath(rightSide);
                var = MathSolver.performOperation(operator, rightOperand, leftOperand);
            } else if (input.length() > 1) {
                // varName = input.substring(0, index).trim();
                varName = subTrim(input, 0, index);
                varName = parseVarRequest(varName);
                // String rightSide = input.substring(index + 1);
                String rightSide = subTrim(input, index + 1);
                // simulating array
                if (rightSide.contains(",")) {
                    dealingWithArray = true;
                    double[] vals = parseDoubleArr(rightSide);// .trim()
                    int k = 0;
                    if (varName.contains(",")) {
                        // if rightSide overload => last element of leftSide array
                        String[] varNames = varName.split("\\s*,\\s*");
                        if (varNames.length >= vals.length)
                            for (k = 0; k < vals.length; k++) {
                                variables.put(varNames[k], vals[k]);
                            }
                        else {
                            for (k = 0; k < varNames.length - 1; k++) {
                                variables.put(varNames[k], vals[k]);
                            }
                            for (int j = k; j < vals.length; j++) {
                                variables.put(varNames[k] + (j - k), vals[j]);
                            }
                        }
                    } else {
                        for (int j = 0; j < vals.length; j++) {
                            variables.put(varName + j, vals[j]);
                        }
                    }
                }
                //
                else if (rightSide.contains("...")) {
                    String[] temp = rightSide.split("\\s*\\.\\.\\.\\s*");
                    // from int to double
                    double n = solveMath(temp[1]);
                    // n = Math.abs(n);
                    for (double i = 0; i < n; i++) {
                        // String toMath = temp[0].replace("IND", String.valueOf(i));
                        variables.put(tempInd, i);
                        variables.put(varName + i, solveMath(temp[0]));
                    }
                    variables.remove(tempInd);
                } else {
                    var = solveMath(rightSide);
                }

            }
        } else if (input.endsWith("++")) {
            varName = input.substring(0, input.length() - 2);
            var = variables.get(varName) + 1;
        } else if (input.endsWith("--")) {
            varName = input.substring(0, input.length() - 2);
            var = variables.get(varName) - 1;
        } else if (input.endsWith("**")) {
            varName = input.substring(0, input.length() - 2);
            var = variables.get(varName);
            var *= var;
        }
        if (!dealingWithArray)
            variables.put(varName, var);
    }

    public void appendArr(String arrName, String rightSide) {
        double[] vals = parseDoubleArr(rightSide);
        int len = MathSolver.fakeArrLen(arrName, variables);
        int n = vals.length + len;
        int j = 0;
        for (int k = len; k < n; k++) {
            variables.put(arrName + k, vals[j++]);
        }
    }

    public String replaceFuncsWithVals(String input) {
        int t;
        while ((t = input.indexOf('@')) != -1) {
            int remStart = t;
            int e = t + 1;
            boolean hasInputs = false;
            while (e < input.length()) {
                // inneficient
                if (input.charAt(e) == '@') {
                    hasInputs = false;
                    break;
                } else if (input.charAt(e) == '{') {

                    // make it different
                    if (input.charAt(e + 1) == '}')
                        hasInputs = false;
                    else
                        hasInputs = true;
                    break;
                }
                e++;
            }
            int remEnd = e;
            String fName = input.substring(t + 1, e).trim();
            double[] inputsVal = null;
            if (hasInputs) {
                int start = e;
                while (e < input.length() && input.charAt(e) != '}') {
                    e++;
                }
                remEnd = e;
                inputsVal = parseDoubleArr(input.substring(start, e));
            }
            if (functions.containsKey(fName)) {
                EriScript2 f = executeFunction(fName, inputsVal);
                double val = f.getReturn();
                // stringbuilder all the way?
                // including other things???
                input = input.substring(0, remStart) + val + input.substring(remEnd + 1);
                // input = input.replaceFirst(input.substring(t, e), String.valueOf(val));
            } else {
                input = input.substring(0, remStart) + 0 + input.substring(remEnd + 1);
                // input = input.replaceFirst(input.substring(t, e), "0");
            }

        }
        return input;
    }

    public boolean hasEriString(int index, String in) {
        for (int i = index; i < in.length(); i++) {
            if (isEriStringSign(in.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean isEriStringSign(char c) {
        return c == '\'' || c == '\"' || c == '`';
    }

    public void parseEriString(String varName, String in) {
        // '' for Word "" for Sentence `` for Text
        // use parsePrint?
        // 'hi'+10+'hi'
        char type = (char) 0;
        int start = 0;
        int end = in.length() - 1;
        int highest = 0;
        List<EriString> toJoin = new LinkedList<>();
        // join based on highest?
        for (int i = 0; i < in.length(); i++) {

            if (type == 0) {
                if (in.charAt(i) == '\'') {
                    start = i + 1;
                    type = '\'';
                    highest = Math.max(highest, 1);
                } else if (in.charAt(i) == '\"') {
                    start = i + 1;
                    type = '\"';
                    highest = Math.max(highest, 2);
                } else if (in.charAt(i) == '`') {
                    start = i + 1;
                    type = '`';
                    highest = Math.max(highest, 3);
                } else if (in.charAt(i) == '+') {
                    // ' ' + i + ' '
                    // in case + => prepare for else

                }
            } else {
                if (in.charAt(i) == type) {
                    end = i;
                    EriString es = genEriString(type, in.substring(start, end));
                    type = (char) 0;
                    toJoin.add(es);
                }
            }
        }
        // ignoring highest
        for (int i = 1; i < toJoin.size(); i++) {
            toJoin.get(0).joinWith(toJoin.get(i));
        }
        // FIX
        if (toJoin.size() == 0)
            putEriString(varName, new EriWord(""));
        else
            putEriString(varName, toJoin.get(0));
    }

    public void parseEriString2(String varName, String in) {
        char type = (char) 0;
        int start = 0;
        int end = in.length() - 1;
        int highest = 0;
        StringBuilder sb = new StringBuilder();
        StringBuilder sbVar = new StringBuilder();
        StringBuilder sbFunc = new StringBuilder();
        // join based on highest?
        for (int i = 0; i < in.length(); i++) {
            if (type == 0) {

                if (in.charAt(i) == '\'') {
                    type = '\'';
                    highest = Math.max(highest, 1);
                } else if (in.charAt(i) == '\"') {
                    type = '\"';
                    highest = Math.max(highest, 2);
                } else if (in.charAt(i) == '`') {
                    type = '`';
                    highest = Math.max(highest, 3);
                } else if (in.charAt(i) == '+') {
                    type = '+';

                } else {
                    if (!Character.isWhitespace(in.charAt(i)))
                        sbFunc.append(in.charAt(i));
                }
                if (sbFunc.length() > 0 && isStringSymbol(type)) {
                    String strFunc = sbFunc.toString();
                    sbFunc.setLength(0);
                    i = parseStringFunc(strFunc, in, sb, i);
                }
            } else {
                if (in.charAt(i) == type) {
                    if (type == '+') {
                        sb.append(parseTypedVar(sbVar));
                        sbVar.setLength(0);
                    }
                    type = (char) 0;
                } else {
                    // can be made faster with while()
                    if (type == '+') {
                        if (!Character.isWhitespace(in.charAt(i))) {
                            sbVar.append(in.charAt(i));
                        }
                    } else {
                        sb.append(in.charAt(i));
                    }
                }
            }
        }

        // putEriString(varName, genEriString(STRTYPES[highest], sb.toString()));
        putEriString(varName, new EriWord(sb.toString()));
    }

    public static int parseStringFunc(String func, String in, StringBuilder sb, int start) {
        int i;
        switch (func.trim().toLowerCase()) {
            case "upper":
                for (i = start + 1; i < in.length(); i++) {
                    char ch = in.charAt(i);
                    if (isStringSymbol(ch)) {
                        break;
                    } else {
                        sb.append(Character.toUpperCase(ch));
                    }
                }
                break;
            case "lower":
                for (i = start + 1; i < in.length(); i++) {
                    char ch = in.charAt(i);
                    if (isStringSymbol(ch)) {
                        break;
                    } else {
                        sb.append(Character.toLowerCase(ch));
                    }
                }
                break;
            case "numberstring":
                for (i = start + 1; i < in.length(); i++) {
                    char ch = in.charAt(i);
                    if (isStringSymbol(ch)) {
                        break;
                    } else if (Character.isDigit(ch)) {
                        sb.append(EriString.numberToLetters(ch));
                    } else {
                        sb.append(ch);
                    }
                }
                break;
            default:
                i = start + 1;
        }
        return i;

    }

    public static boolean isStringSymbol(char c) {
        return c == '\'' || c == '\"' || c == '`';
    }

    private String parseTypedVar(StringBuilder sb) {
        String varName = sb.substring(1, sb.length());
        switch (sb.charAt(0)) {
            case '$':
                if (sb.charAt(1) == '$') {
                    return String.valueOf(variables.get(varName.substring(1)).intValue());// INNEFFICIENT
                }
                return variables.get(varName).toString();
            case '#':
                return Arrays.toString(getArr(varName));
            case '%':
                return varString.get(varName).toString();
            default:
                return "";
        }
    }

    private EriString genEriString(char type, String string) {
        switch (type) {
            case '\'':
                return new EriWord(string);
            case '\"':
                return new EriWord(string);
            case '`':
                return new EriWord(string);
            default:
                return null;
        }
    }

    public void putEriString(String var, EriString e) {
        varString.put(var, e);
    }

    public String getEriString(String var) {
        if (varString.containsKey(var))
            return varString.get(var).toString();
        else
            return "INVALID ERISTRING VARIABLE";
    }

    public void setArr(String name, String in) {
        String[] arr = in.split("\\s*,\\s*");
        for (int j = 0; j < arr.length; j++) {
            variables.put(name + j, solveMath(arr[j]));
        }
    }

    public void setArr(String name, double... arr) {
        for (int j = 0; j < arr.length; j++) {
            variables.put(name + j, arr[j]);
        }
    }

    public double[] getArr(String name) {
        if (varArr.containsKey(name)) {
            return varArr.get(name).stream().mapToDouble(Double::doubleValue).toArray();
        }
        int j = 0;
        while (variables.containsKey(name + j)) {
            j++;
        }
        double[] arr = new double[j];
        for (int k = 0; k < j; k++) {
            arr[k] = variables.get(name + k);
        }
        return arr;
    }

    public double[] parseDoubleArr(String in) {
        String[] s = in.split("\\s*,\\s*");
        double[] arr = new double[s.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = solveMath(s[i]);
        }
        return arr;
    }

    public void initRegArr(String in) {
        String[] arr = in.split("\\s*,\\s*");
        int n = Math.min(arr.length, regDouble.length);
        for (int i = 0; i < n; i++) {
            regDouble[i] = solveMath(arr[i]);
        }
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public List<String> getPrinted() {
        return printed;
    }

    public void setPrinted(List<String> printed) {
        this.printed = printed;
    }

    public static String trimNoComments(String s) {
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        int j = s.indexOf("//") - 1;
        if (j == -2)
            j = s.length() - 1;
        while ((j > 0 && Character.isWhitespace(s.charAt(j)))) {
            j--;
        }

        return s.substring(i, j + 1);
    }

    public double solveMath(String in) {
        return MathSolver.solve(in, variables, regDouble);
    }

    public int solveMathInt(String in) {
        return (int) MathSolver.solve(in, variables, regDouble);
    }

    public String toPrint() {
        StringBuilder sb = new StringBuilder();
        for (String s : printed) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    public boolean hasParams() {
        return params != null;
    }

    public String subTrim(String in, int start) {
        return subTrim(in, start, in.length());
    }

    public String subTrim(String in, int start, int end) {
        if (start < 0) {
            start = 0;
        }
        if (end > in.length()) {
            end = in.length();
        }
        if (start > end)
            return in;
        while (start < end && Character.isWhitespace(in.charAt(start))) {
            start++;// error here???
        }

        while (end > start && Character.isWhitespace(in.charAt(end - 1))) {
            end--;
        }
        return in.substring(start, end);
    }

}
