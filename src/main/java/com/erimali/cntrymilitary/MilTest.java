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
                        if(tempDivs.size() > 1){
                            println("Which division would you like to add this unit in?");
                            int ind = Math.max(0, Math.min(tempDivs.size() -1, scan.nextInt()));
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
                        genOpponentMil(scan);
                        break;
                    case "6":
                        battleMode(scan);
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
            } catch(IllegalArgumentException illegalArgumentException){
                println(illegalArgumentException);
            } catch (Exception e) {
                println("ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void battleMode(Scanner scan) {
        boolean running = true;
        while (running) {
            printBattleOptions();
            print("Input: ");
            try {
                switch (scan.nextInt()) {
                    case 1:
                        MilUnit u1 = selectMilUnit(scan, player);
                        MilUnit u2 = selectMilUnit(scan, opponent);
                        unitVsUnit(u1,u2);
                        break;
                    case 2:
                        MilDiv d1 = selectMilDiv(scan, player);
                        MilDiv d2 = selectMilDiv(scan, opponent);
                        divVsDiv(d1,d2);
                        break;
                    case 9:
                        running = false;
                        break;
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
            println(u.size + " " + u.morale, o.size + " " + o.morale);
        }
        println(res > 0 ? "WIN" : "LOST");
    }

    private static void divVsDiv(MilDiv d, MilDiv o) {
        int res = d.attack(o);
        println(d.getTotalSize(), o.getTotalSize());
        println(res > 0 ? "WIN" : "LOST");
    }

    private static void trainTroops(Scanner scan) {
        println(player.toStringDivs());
        println("Which division to train?");
        int i = scan.nextInt();
        println("How much to train?");
        int amount = scan.nextInt();
        player.getDivisions().get(i).train(amount);
        println("Results");
        println(player.getDivisions().get(i).toStringUnits());
    }

    private static void recruitBuild(Scanner scan, MilUnit u) {
        boolean vehicle = u.getData().isVehicle();
        println("Max Size: " + u.getData().maxSize);
        print("How much to " + (vehicle ? "build" : "recruit") + ": ");
        int val = Math.max(0, scan.nextInt());
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
        println("5) Generate Opponent Military");
        println("6) Battle");
        println("7) View Player Military");
        println("8) View Opponent Military");
        println("9) Exit");
        println("Anything else -> help");
    }

    public static void improveTech(Scanner scan) {
        print("MilTech type: ");
        int type = scan.nextInt();
        print("Progress amount (100 per level up): ");
        short amount = scan.nextShort();
        player.addMilTechProgress(type, amount);
    }

    public static void printBattleOptions() {
        println("1) MilUnit vs MilUnit");
        println("2) MilDiv vs MilDiv");
        println("9) Go back");
    }

    public static MilUnit selectMilUnit(Scanner scan, Military m) {
        MilDiv d = selectMilDiv(scan, m);
        println(d.toStringUnits(), "Index: ");
        return d.getUnit(scan.nextInt());
    }

    public static MilDiv selectMilDiv(Scanner scan, Military m) {
        println(m.toStringDivs(), "Index: ");
        return m.getDivision(scan.nextInt());
    }

    public static MilUnit makeMilUnit(Scanner scan) {
        println("type:");
        int type = scan.nextInt();
        println("subtype/index:");
        int index = scan.nextInt();
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
        println("How many divisions: ");
        int nD = scan.nextInt();
        println("How many units per division: ");
        int nU = scan.nextInt();
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

    public static boolean getYesNo(Scanner scan) {
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
