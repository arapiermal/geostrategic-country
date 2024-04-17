package com.erimali.cntrymilitary;

import com.erimali.cntrygame.TESTING;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class MilTest {
    protected static Military player = new Military();
    protected static Military opponent = new Military();

    public static void main(String[] args) {
        Military.loadAllUnitData(null);

        List<MilDiv> tempDivs = player.getDivisions();
        MilDiv defDiv = new MilDiv("Default Division");
        player.addDivision(defDiv);
        List<MilUnit> defUnits = defDiv.getUnits();
        boolean running = true;
        Scanner scan = new Scanner(System.in);
        printOptions();
        while (running) {
            print("Input: ");
            try {
                switch (scan.nextLine().trim()) {
                    case "0":
                        print(player.unitDataTypesToString(Military.unitTypes));
                        break;
                    case "1":
                        MilUnit u = makeMilUnit(scan);
                        if (u == null)
                            throw new IllegalArgumentException("Either you don't have access because of tech or wrong type:index");
                        recruitBuild(scan, u);
                        if (tempDivs.size() > 1) {
                            println("Which division would you like to add this unit in?");
                            int ind = Math.max(0, Math.min(tempDivs.size() - 1, getIndex(scan)));
                            tempDivs.get(ind).addUnit(u);
                        } else {
                            defUnits.add(u);
                        }
                        break;
                    case "2":
                        print("Division name: ");
                        MilDiv d = new MilDiv(scan.nextLine());
                        println("Add Military Leader for bonuses?");
                        if (getYesNo(scan)) {
                            d.setLeader(makeMilLeader(scan));
                        }
                        tempDivs.add(d);
                        break;
                    case "3":
                        trainTroops(scan);
                        break;
                    case "4":
                        improveTech(scan);
                        break;
                    case "5":
                        battleMode(scan);
                        break;
                    case "6":
                        genOpponentMil(scan);
                        break;
                    case "7":
                        println("Player Military");
                        println(player.toStringLong());
                        break;
                    case "8":
                        println("Opponent Military");
                        println(opponent.toStringLong());
                        break;
                    case "9":
                        running = false;
                        break;
                    default:
                        printOptions();
                }
            } catch (InputMismatchException inputMismatchException) {
                println("Wrong input");
            } catch (IllegalArgumentException illegalArgumentException) {
                println(illegalArgumentException);
            } catch (Exception e) {
                println("ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void battleMode(Scanner scan) {
        boolean running = true;
        printBattleOptions();
        while (running) {
            print("Input: ");
            try {
                switch (scan.nextLine()) {
                    case "1":
                        MilUnit u1 = selectMilUnit(scan, player);
                        MilUnit u2 = selectMilUnit(scan, opponent);
                        unitVsUnit(u1, u2);
                        break;
                    case "2":
                        MilDiv d1 = selectMilDiv(scan, player);
                        MilDiv d2 = selectMilDiv(scan, opponent);
                        int res;
                        do {
                            res = divVsDiv(d1, d2);
                        } while (res == 0 && getYesNo(scan, "Continue battle?"));
                        break;
                    case "3":
                        stopAllRetreat();
                        break;
                    case "4":
                        maximizeAllUnits();
                        break;
                    case "5":
                        opponent.getDivisions().clear();
                        break;
                    case "6":
                        MilDiv selDiv = selectMilDiv(scan, player);
                        MilUnit remUnit = selectMilUnit(scan, player,selDiv);
                        selDiv.removeMilUnit(remUnit);
                        break;
                    case "7":
                        MilDiv remDiv = selectMilDiv(scan, player);
                        player.removeMilDiv(remDiv);
                        break;
                    case "9":
                        running = false;
                        break;
                    default:
                        printBattleOptions();
                }
            } catch (InputMismatchException inputMismatchException) {
                println("Wrong input");
            } catch (Exception e) {
                println("ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void unitVsUnit(MilUnit u, MilUnit o) {
        int res;
        while ((res = u.attack(o)) == 0) {
            println("Us: \t" + u.size + " , morale: " + u.morale, "Them:\t" + o.size + " , morale: " + o.morale);
        }
        println("Us: \t" + u.size + " , morale: " + u.morale, "Them:\t" + o.size + " , morale: " + o.morale);
        println(res > 0 ? "WIN" : "LOST");
    }

    private static int divVsDiv(MilDiv d, MilDiv o) {
        int res = d.attack(o);
        println(d.getTotalSize() + " our units left", o.getTotalSize() + " their units left");
        println("Score: " + res);
        println(res == 0 ? "DRAW" : res > 0 ? "WIN" : "LOST");
        return res;
    }

    private static void trainTroops(Scanner scan) {
        println(player.toStringDivs());
        println("Which division to train?");
        int i = getIndex(scan);
        println("How much to train?");
        int amount = getIndex(scan);
        player.getDivisions().get(i).train(amount);
        println("Results");
        println(player.getDivisions().get(i).toStringUnits());
    }

    private static void recruitBuild(Scanner scan, MilUnit u) {
        boolean vehicle = u.getData().isVehicle();
        println("Max Size: " + u.getData().maxSize);
        print("How much to " + (vehicle ? "build" : "recruit") + ": ");
        int val = Math.max(0, getIndex(scan));
        if (u instanceof MilSoldiers) {
            ((MilSoldiers) u).recruit(val);
        } else if (u instanceof MilVehicles) {
            ((MilVehicles) u).build(val);
        }
    }

    private static MilLeader makeMilLeader(Scanner scan) {
        println("Example: General->Name,Surname");
        return new MilLeader(scan.nextLine());
    }

    public static void printOptions() {
        println("Army simulator options");
        println("0) Show (available) unit types");
        println("1) Make MilUnit");
        println("2) Make MilDiv");
        println("3) Train troops");
        println("4) Improve tech");
        println("5) Battle");
        println("6) Generate Opponent Military");
        println("7) View Player Military");
        println("8) View Opponent Military");
        println("9) Exit");
        println("Anything else -> help");
    }

    public static void improveTech(Scanner scan) {
        print("MilTech type: ");
        int type = getIndex(scan);
        print("Progress amount (100 per level up): ");
        short amount = scan.nextShort();
        player.addMilTechProgress(type, amount);
    }

    public static void printBattleOptions() {
        println("1) MilUnit vs MilUnit");
        println("2) MilDiv vs MilDiv");
        println("3) Stop all retreat");
        println("4) Maximize all units (size)");
        println("5) Erase opponent military");
        println("6) Remove player unit");
        println("7) Remove player division");

        println("9) Go back");
    }
    public static void stopAllRetreat(){
        for(MilDiv d : player.getDivisions())
            d.stopAllRetreating();
        for(MilDiv d : opponent.getDivisions())
            d.stopAllRetreating();
    }
    public static void maximizeAllUnits(){
        for(MilDiv d : player.getDivisions())
            d.maximizeSizeAllUnits();
        for(MilDiv d : opponent.getDivisions())
            d.maximizeSizeAllUnits();
    }
    public static MilUnit selectMilUnit(Scanner scan, Military m) {
        MilDiv d = selectMilDiv(scan, m);
        println(d.toStringUnits());
        print("Unit Index: ");
        return d.getUnit(getIndex(scan));
    }

    public static MilUnit selectMilUnit(Scanner scan, Military m, MilDiv d) {
        println(d.toStringUnits());
        print("Unit Index: ");
        return d.getUnit(getIndex(scan));
    }

    public static MilDiv selectMilDiv(Scanner scan, Military m) {
        println(m.toStringDivs());
        print("Div Index: ");
        return m.getDivision(getIndex(scan));
    }
    public static int getIndex(Scanner scan){
        String s = scan.nextLine().trim();
        try{
            int val = Integer.parseInt(s);
            return val;
        } catch(NumberFormatException e){
            return 0;
        }
    }
    public static MilUnit makeMilUnit(Scanner scan) {
        print("type: ");
        int type = getIndex(scan);
        print("subtype/index: ");
        int index = getIndex(scan);
        if (player.getMilTechLevel(type) >= Military.getUnitTypesList(type).get(index).minMilTech)
            return Military.makeUnit(0, type, index);
        else
            return null;
    }

    public static MilUnit randMilUnit() {
        if (Math.random() < 0.5)
            return Military.makeUnit(1, 0, 0);
        MilUnit rand;
        do {
            int type = (int) (Math.random() * MilUnitData.getMaxTypes());
            List<MilUnitData> l = Military.getUnitTypesList(type);
            int index = (int) (Math.random() * l.size());
            rand = Military.makeUnit(1, type, index);
        } while (rand == null);
        return rand;
    }

    public static void genOpponentMil(Scanner scan) {
        print("How many divisions: ");
        int nD = getIndex(scan);
        print("How many units per division: ");
        int nU = getIndex(scan);
        for (int i = 0; i < nD; i++) {
            MilDiv d;
            if (Math.random() < 0.5)
                d = new MilDiv("Division " + (i + 1));
            else
                d = new MilDiv("Division " + (i + 1), new MilLeader());
            for (int j = 0; j < nU; j++) {
                MilUnit u = randMilUnit();
                u.maximizeSize();
                d.addUnit(u);
            }
            opponent.addDivision(d);
        }
    }

    public static boolean getYesNo(Scanner scan, String... arg) {
        for(String s : arg){
            println(s);
        }
        print("(y)es/(n)o: ");
        String s = scan.nextLine();
        if (s.isBlank())
            return false;
        char c = s.trim().charAt(0);
        return c == 'y' || c == 'Y';
    }

    public static void println(Object... arg) {
        if (arg.length == 0) {
            System.out.println();
        }
        for (Object o : arg) {
            System.out.println(o);
        }
    }

    public static void print(Object... arg) {
        for (Object o : arg) {
            System.out.print(o);
        }
    }
}
