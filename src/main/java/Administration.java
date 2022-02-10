import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Administration {
    HashMap<String, Dictionary> dictionaryMap;

    public Administration(HashMap<String, Dictionary> dictionaryMap) {
        this.dictionaryMap = dictionaryMap;
    }

    /* return the word map if the dictionary exists */
    HashMap<Integer, Word> getWordMap(String language) {
        Dictionary dictionary;
        if (!dictionaryMap.containsKey(language)) {
            System.out.println("There is no dictionary for the language "
                    + language + ".");
            return null;
        }

        dictionary = dictionaryMap.get(language);

        return dictionary.getWordMap();
    }

    /* checks if a word (or a singular/plural form of it) is in the dictionary */
    Word searchWord(HashMap<Integer, Word> wordMap, String word) {
        /* search the dictionary for the given form */
        int hashCode = word.hashCode();
        if (wordMap.containsKey(hashCode))
            return wordMap.get(hashCode);

        /* search singular and plural list for all words, if found return */
        for (Map.Entry<Integer, Word> entry : wordMap.entrySet()) {
            Word currentWord = entry.getValue();
            ArrayList<String> singularList = currentWord.getSingular();
            ArrayList<String> pluralList = currentWord.getPlural();

            if (singularList.contains(word) || pluralList.contains(word))
                return currentWord;
        }

        return null;
    }

    /* adds a word if it doesn't already exist */
    boolean addWord(Word word, String language) {
        HashMap<Integer, Word> wordMap = getWordMap(language);
        if (wordMap == null)
            return false;

        Word searchedWord = searchWord(wordMap, word.getWord());
        /* if the word / a form of it is in the dictionary, don't add */
        if (searchedWord != null) {
            System.out.println("The word " + word.getWord() +
                    " can't be added in the dictionary " + language +
                    "(already exists). ");
            return false;
        }

        int hashCode = word.hashCode();
        wordMap.put(hashCode, word);
        System.out.println("The word " + word.getWord() + " has been added.");
        return true;
    }

    /* removes a word if it exists in the dictionary */
    boolean removeWord(String word, String language) {
        HashMap<Integer, Word> wordMap = getWordMap(language);
        if (wordMap == null)
            return false;

        /* search the word in the dictionary */
        Word foundWord = searchWord(wordMap, word);
        if (foundWord == null) {
            System.out.println("The word " + word +
                    " can't be removed. (it was not found)");

            return false;
        }

        /* remove the word */
        int hashCode = foundWord.hashCode();
        wordMap.remove(hashCode);
        System.out.println("The word " + foundWord.getWord() +
                " has been removed.");

        return true;
    }

    /* adds a definition from a certain dictionary, if there isn't already
       one from there, then add it */

    boolean addDefinitionForWord(String word, String language,
                                 Definition definition) {
        HashMap<Integer, Word> wordMap = getWordMap(language);
        if (wordMap == null)
            return false;

        Word foundWord = searchWord(wordMap, word);
        if (foundWord == null) {
            System.out.println("Can't add definition for the word " + word +
                    " (doesn't exist).");
            return false;
        }

        List<Definition> defList = foundWord.getDefinitions();

        /* check if a definition from the same dictionary already exists */
        for (Definition currDef : defList) {

            /* if the exact definition already exists, then don't add/update */
            if (currDef.equals(definition)) {
                System.out.println("The same definition from the dictionary " +
                        currDef.getDict() + " already exists.");

                return false;
            }

            /* if there is a definition from that dictionary, but newer, update it */
            if (currDef.getDict().equals(definition.getDict())) {
                if (currDef.getYear() >= definition.getYear()) { // only add newer definitions
                    System.out.println("Can't add definition for the word " +
                            word + " (a newer one from the dictionary " +
                            currDef.getDict() + " exists)");
                    return false;
                }

                int index = defList.indexOf(currDef);
                defList.set(index, definition);
                System.out.println("The definition from the dictionary "
                        + definition.getDict() + " has been updated.");

                return true;
            }
        }

        /* else if there is no definition from that dictionary already, add it */
        defList.add(definition);
        System.out.println("The definition from the dictionary " +
                definition.getDict() + " has been added.");
        return true;
    }

    /* searches for a definition from a certain dictionary, if found remove it */
    boolean removeDefinition(String word, String language,
                             String dictionary) {
        /* the string dictionary format should be: NAME, YEAR*/
        String[] tokens = dictionary.split(", ");
        String dictionaryName = tokens[0];
        int dictionaryYear = Integer.parseInt(tokens[1]);

        HashMap<Integer, Word> wordMap = getWordMap(language);
        if (wordMap == null)
            return false;

        Word foundWord = searchWord(wordMap, word);
        if (foundWord == null) {
            System.out.println("Can't remove definition for the word " + word +
                    " (the word doesn't exists).");
            return false;
        }

        List<Definition> defList = foundWord.getDefinitions();

        for (Definition def : defList)
            if ((def.getDict().equals(dictionaryName)) &&
                    def.getYear() == dictionaryYear) {
                defList.remove(def);
                System.out.println("The definition from the dictionary "
                        + dictionary + " has been removed.");
                return true;
            }

        System.out.println("There is no definition from the dictionary "
                + dictionary + ".");
        return false;
    }

    ArrayList<Definition> getDefinitionsForWord(String word, String language) {
        HashMap<Integer, Word> wordMap = getWordMap(language);
        if (wordMap == null)
            return null;

        Word foundWord = searchWord(wordMap, word);
        if (foundWord == null) {
            System.out.println("The word " + word + " is not in the "
                    + language + " dictionary.");
            return null;
        }

        ArrayList<Definition> definitions =
                new ArrayList<>(foundWord.getDefinitions());
        definitions.sort(Comparator.comparingInt(Definition::getYear));
        return definitions;
    }

    /* creating a json file with all the words from a dictionary (sorted) */
    void exportDictionary(String language) {
        HashMap<Integer, Word> wordMap = getWordMap(language);
        if (wordMap == null)
            return;

        /* create a list with all the words */
        List<Word> words = new ArrayList<>(wordMap.values());

        /* sort the list */
        words.sort(Comparator.comparing(Word::getWord));

        Gson gson = new Gson();

        /* create a token with the list type */
        Type type = new TypeToken<List<Word>>() {
        }.getType();
        String jsonString = gson.toJson(words, type); // make a json string from the list

        /* create a folder for exports */
        File dir = new File("./exports");
        String pathName = null;

        boolean isDir = dir.exists();
        if (!isDir)
            isDir = dir.mkdirs();

        if (isDir) {
            language += "_export.json";
            pathName = "./exports/" + language;
        }

        /* write the json string to a file */
        try {
            assert pathName != null;
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(pathName), StandardCharsets.UTF_8))) {
                writer.write(jsonString);
            }
        } catch (IOException e) {
            System.out.println("Can't create " + language + " file.");
            e.printStackTrace();
        }
    }
}
