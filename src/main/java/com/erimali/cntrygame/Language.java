package com.erimali.cntrygame;

import com.erimali.cntrymilitary.Person;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.*;

enum Phrases {
    YES, NO, ALWAYS, USUALLY, SOMETIMES, NEVER, HELLO, HI, WELCOME, THANKS, HELP, CERTAINLY, SIR, MADAM, PROVOCATION, GOOD, BAD;
}

public class Language implements Comparable<Language>, Serializable {
    private static final String DEFLANGPATH = GLogic.RESOURCES_PATH + "countries/languages/";
    // maybe synchronize, ENG to ALB
    private String name;
    private transient List<String> mainPhrases;
    private transient List<List<String>> otherLangPhrases;
    private transient String[] commonMaleNames;
    private transient String[] commonSurnames;
    private int howMany;

    public Language(String name) {
        this.name = upperFirstLowerRestLetters(name);
        //by default howMany = 0, no txt file with data for this one.
    }

    public Language(String name, boolean b) throws Exception {
        this.name = upperFirstLowerRestLetters(name);
        loadLanguage();
    }

    public void loadLanguage() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(DEFLANGPATH + name + ".txt"))) {
            howMany = 1;
            mainPhrases = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    if (commonMaleNames == null) {
                        commonMaleNames = World.getValues(line);
                    } else if (commonSurnames == null) {
                        commonSurnames = World.getValues(line);
                    } else if (mainPhrases.isEmpty()) {
                        mainPhrases.addAll(Arrays.asList(World.getValues(line)));
                    } else {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("FAILED");
        }

    }

    //These are added to the end of the world list or new list for comboLangs.
    // Unify languages
    public Language(Language... langs) {
        if (langs.length < 2) {
            throw new IllegalArgumentException("CANNOT UNIFY " + langs.length + " LANGUAGES");
        }

        commonMaleNames = new String[findCMNlength(langs)];
        commonSurnames = new String[findCSlength(langs)];
        this.otherLangPhrases = new LinkedList<>();
        int i = 0;
        int j = 0;
        StringBuilder sbName = new StringBuilder();
        for (Language t : langs) {
            sbName.append(t.name);
            addCMN(i, t.commonMaleNames);
            i += t.commonMaleNames.length;
            addCS(j, t.commonSurnames);
            j += t.commonSurnames.length;
            howMany += t.howMany;
            if (howMany == 1) {
                this.mainPhrases = t.mainPhrases;
            } else {
                this.otherLangPhrases.add(t.mainPhrases);
            }
        }
        this.name = sbName.toString();
    }

    public static void main(String[] args) {
        try {
            Language l1 = new Language("Albanian", true);
            Language l2 = new Language("Serbian", true);
            System.out.println(translateText(l1, l2, "Po, Ermal. Përshëndetje si je. Unë jam mirë. Pershendetje."));

            Language combined = new Language(l1, l2);
            TESTING.print(combined.mainPhrases, combined.otherLangPhrases.getFirst());
            System.out.println(l1.speak(Phrases.HELLO, "Player"));
            System.out.println(l1.translateToEnglishPhrases("Sigurisht po zotëri"));
            TESTING.print(Language.lauFirstChar("test"));
            TESTING.print(Language.uppercaseFirstChar("t"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addCMN(int start, String[] cmn) {
        int j = 0;
        for (int i = start; i < start + cmn.length; i++) {
            commonMaleNames[i] = cmn[j++];
        }
    }

    private void addCS(int start, String[] cs) {
        int j = 0;
        for (int i = start; i < start + cs.length; i++) {
            commonSurnames[i] = cs[j++];
        }
    }

    public Person generateMale() {
        try {
            Random r = new Random();
            int name = r.nextInt(commonMaleNames.length);
            int surname = r.nextInt(commonSurnames.length);
            return new Person(commonMaleNames[name], commonSurnames[surname], Person.getMale());
        } catch (Exception e) {
            return new Person("Filan", "Fisteku", Person.getMale());
        }
    }

    public String generateMName() {
        int name = (int) (Math.random() * commonMaleNames.length + 1);
        return commonMaleNames[name];
    }

    public String generateSurname() {
        int name = (int) (Math.random() * commonMaleNames.length + 1);
        return commonMaleNames[name];
    }

    public Person generateMaleSonOf(String prefix) {
        Random r = new Random();
        int name = r.nextInt(commonMaleNames.length);
        int surname = r.nextInt(commonMaleNames.length);
        return new Person(commonMaleNames[name], prefix + " " + commonMaleNames[surname], Person.getMale());
    }

    public Person generateMaleSurnameSon(String fix) {
        Random r = new Random();
        int name = r.nextInt(commonMaleNames.length);
        int surname = r.nextInt(commonMaleNames.length);
        return new Person(commonMaleNames[name], commonMaleNames[surname] + fix, Person.getMale());
    }

    public static int findCMNlength(Language... l) {
        int size = 0;
        for (Language t : l) {
            size += t.commonMaleNames.length;
        }
        return size;
    }

    private static int findCSlength(Language... l) {
        int size = 0;
        for (Language t : l) {
            size += t.commonSurnames.length;
        }
        return size;
    }

    // public String translateStep(l,String word)
    public static String translateStep(Language l1, Language l2, String word) {
        int i = l1.mainPhrases.indexOf(word.toLowerCase());
        if (i == -1)
            return word;
        return l2.mainPhrases.get(i);
    }

    public static String translate(Language l1, Language l2, String sentence) {
        StringBuilder sb = new StringBuilder();
        String[] words = sentence.split("\\p{Punct}*\\s+\\p{Punct}*");
        int i = 0;
        boolean uppercased = false;
        while (i < words.length) {
            if (!words[i].isBlank()) {
                if (uppercased)
                    sb.append(translateStep(l1, l2, words[i]));
                else {
                    sb.append(uppercaseFirstChar(translateStep(l1, l2, words[i])));
                    uppercased = true;
                }
                // do this with others? code reduction
                if (i < words.length - 1)
                    sb.append(" ");
            }
            i++;
        }

        return sb.toString();
    }

    public static String translateText(Language l1, Language l2, String text) {
        StringBuilder sb = new StringBuilder();
        String[] sentences = text.split("(?<=[.!?])|(?=[.!?])");
        for (int i = 0; i < sentences.length; i++) {
            sb.append(translate(l1, l2, sentences[i]));
            if (i < sentences.length - 1) {
                sb.append(sentences[++i]);
            }
            sb.append(" ");// \n
        }
        return sb.toString();
    }

    // Hello NAME
    // Pershendetje NAME
    public String speak(Phrases p, String n) {
        return uppercaseFirstChar(mainPhrases.get(p.ordinal()) + " " + n);
    }

    public static String uppercaseFirstChar(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        StringBuilder sb = new StringBuilder(input);
        for (int i = 0; i < sb.length(); i++) {
            char current = sb.charAt(i);
            if (Character.isLetterOrDigit(current))
                if (Character.isLowerCase(current)) {
                    sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
                    break;
                } else {
                    break;
                }
        }
        return sb.toString();
    }

    public static String lauFirstChar(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        StringBuilder sb = new StringBuilder(input);
        int i;
        for (i = 0; i < sb.length(); i++) {
            char current = sb.charAt(i);
            if (Character.isLetterOrDigit(current))
                if (Character.isLowerCase(current)) {
                    sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
                    break;
                } else {
                    break;
                }
        }
        i++;
        while (i < sb.length()) {
            if (Character.isUpperCase(sb.charAt(i))) {
                sb.setCharAt(i, Character.toLowerCase(sb.charAt(i)));
            }
            i++;
        }
        return sb.toString();
    }

    public String translateFromEnglishPhrases(String in) {
        StringBuilder sb = new StringBuilder();
        String[] words = in.split("\\p{Punct}*\\s+\\p{Punct}*");
        int i = 0;
        try {
            int k = Phrases.valueOf(words[i].toUpperCase()).ordinal();
            sb.append(Language.lauFirstChar(mainPhrases.get(k))).append(" ");
        } catch (Exception e) {
            sb.append(words[i]).append(" ");
        }
        for (i = 1; i < words.length; i++) {
            try {
                int k = Phrases.valueOf(words[i].toUpperCase()).ordinal();
                sb.append(mainPhrases.get(k));
                if (i < words.length - 1)
                    sb.append(" ");
            } catch (Exception e) {
                sb.append(words[i]);
                if (i < words.length - 1)
                    sb.append(" ");
            }
        }

        return sb.toString();
    }

    public String translateToEnglishPhrases(String in) {
        StringBuilder sb = new StringBuilder();
        in = in.toLowerCase();
        String[] words = in.split("\\p{Punct}*\\s+\\p{Punct}*");
        int i = 0;
        try {
            int k = mainPhrases.indexOf(words[i]);
            sb.append(Language.lauFirstChar(Phrases.values()[k].toString())).append(" ");
        } catch (Exception e) {
            sb.append(words[i]).append(" ");
        }
        for (i = 1; i < words.length; i++) {
            try {
                int k = mainPhrases.indexOf(words[i]);
                sb.append(Phrases.values()[k].toString().toLowerCase());
                if (i < words.length - 1)
                    sb.append(" ");
            } catch (Exception e) {
                sb.append(words[i]);
                if (i < words.length - 1)
                    sb.append(" ");
            }
        }
        return sb.toString();
    }

    public List<List<String>> getOtherLangPhrases() {
        return otherLangPhrases;
    }

    public void setOtherLangPhrases(List<List<String>> otherLangPhrases) {
        this.otherLangPhrases = otherLangPhrases;
    }

    public static String alphabetStringGen(String[] alphabet, Random rand, int size) {
        StringBuilder sb = new StringBuilder();
        int l = alphabet.length;
        for (int i = 0; i < size; i++) {
            sb.append(alphabet[rand.nextInt(l)]);
        }
        return sb.toString();
    }

    public static String upperFirstLowerRestLetters(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                sb.append(Character.toUpperCase(c));
                i++;
                break;
            }
            i++;
        }
        while (i < s.length()) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                sb.append(Character.toLowerCase(c));
            }
            i++;
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Language o) {
        return this.name.compareTo(o.name);
    }
}
