package com.erimali.cntrymilitary;

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
        List<MilUnit> tempUnits = defDiv.getUnits();
        boolean running = true;
        Scanner scan = new Scanner(System.in);
        while (running) {
            printOptions();
            System.out.print("Input: ");
            try {
                switch (scan.nextInt()) {
                    case 0:
                        System.out.print(Military.unitDataTypesToString(Military.unitTypes));
                        break;
                    case 1:
                        MilUnit u = makeMilUnit(scan);
                        recruitBuild(scan, u);
                        tempUnits.add(u);
                        break;
                    case 2:
                        MilDiv d = new MilDiv(scan.nextLine());
                        System.out.println("Add Military Leader for bonuses?");
                        if (getYesNo(scan)) {
                            d.setLeader(makeMilLeader(scan));
                        }
                        tempDivs.add(d);
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        genOpponentMil(scan);
                        break;
                    case 6:

                        break;
                    case 7:
                        System.out.println("Player Military");
                        System.out.println(player.toStringLong());
                        break;
                    case 8:
                        System.out.println("Opponent Military");
                        System.out.println(opponent.toStringLong());
                        break;
                    case 9:
                        running = false;
                        break;
                }
            } catch (InputMismatchException inputMismatchException) {
                System.out.println("Wrong input");
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private static void recruitBuild(Scanner scan, MilUnit u) {
        boolean vehicle = u.getData().isVehicle();
        System.out.println("Max Size: " + u.getData().maxSize);
        System.out.print("How much to " + (vehicle ? "build" : "recruit") + ": ");
        int val = Math.max(0, scan.nextInt());
        if (u instanceof MilSoldiers) {
            ((MilSoldiers) u).train(val);
        } else if (u instanceof MilVehicles) {
            ((MilVehicles) u).build(val);
        }
    }

    private static MilLeader makeMilLeader(Scanner scan) {
        System.out.println("Example: General->Name,Surname");
        return new MilLeader(scan.nextLine());
    }

    public static void printOptions() {
        System.out.println("Army simulator options");
        System.out.println("0) Show unit types");
        System.out.println("1) Make MilUnit");
        System.out.println("2) Make MilDiv");
        System.out.println("3) MilUnit vs MilUnit");
        System.out.println("4) MilDiv vs MilDiv");
        System.out.println("5) Generate Opponent Military");
        System.out.println("6) Final war (Player Military vs Opponent Military)");
        System.out.println("7) View Player Military");
        System.out.println("8) View Opponent Military");
        System.out.println("9) Exit");

    }

    public static MilUnit makeMilUnit(Scanner scan) {
        System.out.println("type:");
        int type = scan.nextInt();
        System.out.println("subtype/index:");
        int index = scan.nextInt();
        return Military.makeUnit(0, type, index);
    }

    public static MilUnit randMilUnit() {
        int type = (int) (Math.random() * MilUnitData.getMaxTypes());
        List<MilUnitData> l = Military.getUnitTypesList(type);
        int index = (int) (Math.random() * l.size());
        return Military.makeUnit(1, type, index);
    }

    public static void genOpponentMil(Scanner scan) {
        System.out.println("How many divisions: ");
        int nD = scan.nextInt();
        System.out.println("How many units per division: ");
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
        System.out.print("(y)es/(n)o: ");
        String s = scan.nextLine();
        if (s.isBlank())
            return false;
        char c = s.trim().charAt(0);
        return c == 'y' || c == 'Y';
    }
}
