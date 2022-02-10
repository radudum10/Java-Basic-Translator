import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class JUnitTest {
    /* parsing in every test in order to not use static variables and make the
    tests dependent by each other (also map doesn't have a reset method so can't
    reset it after each
    */

    @Test
    @DisplayName("Parsing check")
    void parsingTest() {
        HashMap<String, Dictionary> dictionaryMap = new HashMap<>();
        Parser parser = new Parser();
        parser.allJsons(dictionaryMap);

        StringBuilder roWords = new StringBuilder();
        StringBuilder frWords = new StringBuilder();
        for (Map.Entry<String, Dictionary> entry : dictionaryMap.entrySet()) {
            HashMap<Integer, Word> wordMap = entry.getValue().getWordMap();
            ArrayList<Word> wordList = new ArrayList<>(wordMap.values());
            wordList.sort(Comparator.comparing(Word::getWord));

            for (Word word : wordList)
                if (entry.getKey().equals("ro"))
                    roWords.append(word.getWord());
                else
                    frWords.append(word.getWord());
        }

        Assertions.assertEquals("câinemergepisică", roWords.toString());
        Assertions.assertEquals("allerchatjeumanger", frWords.toString());
    }

    @Test
    @DisplayName("addWord check")
    void addWordTest() {
        System.out.println("\n/// ADD WORD TEST ///");
        HashMap<String, Dictionary> dictionaryMap = new HashMap<>();
        Parser parser = new Parser();
        parser.allJsons(dictionaryMap);
        Administration admin = new Administration(dictionaryMap);

        ArrayList<String> singular = new ArrayList<>();
        singular.add("tigru");

        ArrayList<String> plural = new ArrayList<>();
        plural.add("tigri");

        ArrayList<String> text = new ArrayList<>();
        text.add("Specie de mamifer carnivor din familia felidelor, " +
                "de talie mare, cu blana de culoare galbenă-roșcată cu dungi " +
                "negre");

        Definition definition = new Definition("dex.ro", "definitions",
                2022, text);

        ArrayList<Definition> definitions = new ArrayList<>();
        definitions.add(definition);

        Word word = new Word("tigru", "tiger", "noun", singular, plural,
                definitions);

        boolean added = admin.addWord(word, "ro"); // clean add
        Assertions.assertTrue(added);

        added = admin.addWord(word, "ro"); // if the word already exists, fail
        Assertions.assertFalse(added);

        added = admin.addWord(word, "es"); // trying to add in a dictionary that doesn't exist
        Assertions.assertFalse(added);
    }

    @Test
    @DisplayName("removeWord test")
    void removeWordTest() {
        System.out.println("\n/// REMOVE WORD TEST ///");
        HashMap<String, Dictionary> dictionaryMap = new HashMap<>();
        Parser parser = new Parser();
        parser.allJsons(dictionaryMap);
        Administration admin = new Administration(dictionaryMap);

        boolean removed = admin.removeWord("aller", "fr"); // clean remove
        Assertions.assertTrue(removed);

        removed = admin.removeWord("mangez", "fr"); // clean remove (using a form of the word)
        Assertions.assertTrue(removed);

        removed = admin.removeWord("aller", "fr"); // removing a word that doesn't exist
        Assertions.assertFalse(removed);

        removed = admin.removeWord("messi", "es"); // removing from a dictionary that doesn't exist
        Assertions.assertFalse(removed);
    }

    @Test
    @DisplayName("addDefinition test")
    void addDefinitionTest() {
        System.out.println("\n/// ADD DEFINITION TEST ///");
        HashMap<String, Dictionary> dictionaryMap = new HashMap<>();
        Parser parser = new Parser();
        parser.allJsons(dictionaryMap);
        Administration admin = new Administration(dictionaryMap);

        ArrayList<String> text = new ArrayList<>();
        text.add("Specie de mamifer carnivor din familia felidelor, " +
                "de talie mare, cu blana de culoare galbenă-roșcată cu dungi " +
                "negre");

        Definition definition = new Definition("dex.ro", "definitions",
                2022, text);

        /* clean add */
        boolean added = admin.addDefinitionForWord("chat", "fr", definition);
        Assertions.assertTrue(added);

        /* updating a definition if I have a newer version from that dictionary */
        ArrayList<String> updatedText = new ArrayList<>();
        updatedText.add("Asta e o definitie de 2023");
        Definition updatedDef = new Definition("dex.ro", "definitions", 2023,
                updatedText);
        added = admin.addDefinitionForWord("chat", "fr", updatedDef);
        Assertions.assertTrue(added);

        /* trying to add an outdated definition */
        Definition outdatedDef = new Definition("dex.ro", "definitions", 2000,
                text);
        added = admin.addDefinitionForWord("chat", "fr", outdatedDef);
        Assertions.assertFalse(added);

        /* trying to add an existent definition */
        added = admin.addDefinitionForWord("chat", "fr", updatedDef);
        Assertions.assertFalse(added);

        /* trying to add to a word that doesn't exist */
        added = admin.addDefinitionForWord("fiabilitate", "it", updatedDef);
        Assertions.assertFalse(added);
    }

    @Test
    @DisplayName("removeDefinition test")
    void removeDefinitionTest() {
        System.out.println("\n/// REMOVE DEFINITION TEST ///");
        HashMap<String, Dictionary> dictionaryMap = new HashMap<>();
        Parser parser = new Parser();
        parser.allJsons(dictionaryMap);
        Administration admin = new Administration(dictionaryMap);

        /* trying to remove a definition from a dictionary that doesn't exist */
        boolean removed = admin.removeDefinition("chat", "fr",
                "Larousse, 2040");
        Assertions.assertFalse(removed);

        /* clean remove */
        removed = admin.removeDefinition("chat", "fr",
                "Larousse, 2000");
        Assertions.assertTrue(removed);

        /* trying to modify a nonexistent word */
        removed = admin.removeDefinition("cuvant", "ro", "Larousse, 2020");
        Assertions.assertFalse(removed);
    }

    @Test
    @DisplayName("translateWord test")
    void translateWordTest() {
        System.out.println("\n/// TRANSLATE WORD TEST ///");
        HashMap<String, Dictionary> dictionaryMap = new HashMap<>();
        Parser parser = new Parser();
        parser.allJsons(dictionaryMap);
        Administration admin = new Administration(dictionaryMap);
        Translation translator = new Translation(admin);

        /* trying to translate into a unsupported language */
        String translated = translator.translateWord("caine", "ro", "es");
        Assertions.assertNull(translated);
        System.out.println("Unsupported language test passed");

        /* trying to translate a nonexistent word (should return the given word) */
        translated = translator.translateWord("tigru", "ro", "fr");
        Assertions.assertEquals("tigru", translated);
        System.out.println("Nonexistent word in source dictionary test passed");

        /* translating a word that is not in the destination dictionary (should return the given word) */
        translated = translator.translateWord("manger", "fr", "ro");
        Assertions.assertEquals("manger", translated);
        System.out.println("Nonexistent word in destination dictionary test passed");

        /* default-case translation (from dictionary form) */
        translated = translator.translateWord("aller", "fr", "ro");
        Assertions.assertEquals("merge", translated);
        System.out.println("Default-case passed");

        /* singular, 1st person form */
        translated = translator.translateWord("vais", "fr", "ro");
        Assertions.assertEquals("merg", translated);
        System.out.println("1st person singular form passed");

        /* singular, 2nd person form */
        translated = translator.translateWord("vas", "fr", "ro");
        Assertions.assertEquals("mergi", translated);
        System.out.println("2nd person singular form passed");

        /* singular, 3rd person form */
        translated = translator.translateWord("va", "fr", "ro");
        Assertions.assertEquals("merge", translated);
        System.out.println("3rd person singular form passed");

        /* plural, 1st person form */
        translated = translator.translateWord("allons", "fr", "ro");
        Assertions.assertEquals("mergem", translated);
        System.out.println("1st person plural form passed");

        /* plural, 2nd person form */
        translated = translator.translateWord("allez", "fr", "ro");
        Assertions.assertEquals("mergeți", translated);
        System.out.println("2nd person plural form passed");

        /* plural, 3RD person form */
        translated = translator.translateWord("vont", "fr", "ro");
        Assertions.assertEquals("merg", translated);
        System.out.println("3rd person plural form passed");
    }

    @Test
    @DisplayName("translateSentence test")
    void translateSentenceTest() {
        /* there is no need to verify sentences with singular or plural form because translateSentence is dependent
        on translateWord, so if translateWord works well this should too.
         */
        System.out.println("\n/// TRANSLATE SENTENCE TEST ///");
        HashMap<String, Dictionary> dictionaryMap = new HashMap<>();
        Parser parser = new Parser();
        parser.allJsons(dictionaryMap);
        Administration admin = new Administration(dictionaryMap);
        Translation translator = new Translation(admin);

        /* sentence without punctuation */
        String translated = translator.translateSentence
                ("chat va", "fr", "ro");
        Assertions.assertEquals("Pisică merge", translated);
        System.out.println("Sentence without punctuation test passed.");

        /* sentence with punctuation */
        translated = translator.translateSentence("Pisică merge.",
                "ro", "fr");
        Assertions.assertEquals("Chat aller.", translated);
        System.out.println("Sentence with punctuation test passed.");

        /* sentence without all words supported */
        translated = translator.translateSentence("tigre va", "fr", "ro");
        Assertions.assertEquals("Tigre merge", translated);
        System.out.println("Sentence without all words supported test passed.");
    }

    @Test
    @DisplayName("translateSentence test")
    void translateSentencesTest() {
        /* there is no need to verify sentences with singular or plural form because translateSentence is dependent
        on translateWord, so if translateWord works well this should too.
         */
        System.out.println("\n/// TRANSLATE SENTENCES TEST ///");
        HashMap<String, Dictionary> dictionaryMap = new HashMap<>();
        Parser parser = new Parser();
        parser.allJsons(dictionaryMap);
        Administration admin = new Administration(dictionaryMap);
        Translation translator = new Translation(admin);

        ArrayList<String> translated = translator.translateSentences
                ("Pisică merge", "ro", "fr");

        String firstForm = translated.get(0);
        Assertions.assertEquals("Chat aller", firstForm);
        String secondForm = translated.get(1);
        Assertions.assertEquals("Greffier aller", secondForm);
        String thirdForm = translated.get(2);
        Assertions.assertEquals("Mistigri aller", thirdForm);

        System.out.println("Basic case passed.");

        /* exception case, when a word is not supported */
        translated = translator.translateSentences
                ("Chat mange", "fr", "ro");
        firstForm = translated.get(0);
        Assertions.assertEquals("Pisică mange", firstForm);
        secondForm = translated.get(1);
        Assertions.assertEquals("Mâță mange", secondForm);
        thirdForm = translated.get(2);
        Assertions.assertEquals("Cotoroabă mange", thirdForm);

        System.out.println("Exception case passed.");
    }

    @Test
    @DisplayName("getDefinitionsForWord test")
    void getDefinitionsForWordTest() {
        /* there is no need to verify sentences with singular or plural form because translateSentence is dependent
        on translateWord, so if translateWord works well this should too.
         */
        System.out.println("\n/// TRANSLATE SENTENCES TEST ///");
        HashMap<String, Dictionary> dictionaryMap = new HashMap<>();
        Parser parser = new Parser();
        parser.allJsons(dictionaryMap);
        Administration admin = new Administration(dictionaryMap);

        ArrayList<Definition> definitions = admin.getDefinitionsForWord
                ("câine", "ro");

        Definition firstForm = definitions.get(0);
        Assertions.assertEquals("Dicționar universal al limbei române," +
                " ediția a VI-a)", firstForm.getDict()); // 1929
        Definition secondForm = definitions.get(1);
        Assertions.assertEquals("Dicționar de sinonime", secondForm.getDict()); // 1998
        Definition thirdForm = definitions.get(2);
        Assertions.assertEquals("Dicționarul explicativ al limbii române " +
                "(ediția a II-a revăzută și adăugită)", thirdForm.getDict()); // 2009

        System.out.println("Basic case passed.");

        definitions = admin.getDefinitionsForWord("dulau", "fr");
        Assertions.assertNull(definitions);
        System.out.println("Exception case pased");

    }

    @Test
    @DisplayName("ExportDictionary test")
    void exportDictionaryTest() {
        System.out.println("\n/// EXPORT DICTIONARY TEST ///");
        HashMap<String, Dictionary> dictionaryMap = new HashMap<>();
        Parser parser = new Parser();
        parser.allJsons(dictionaryMap);
        Administration admin = new Administration(dictionaryMap);

        admin.exportDictionary("ro");

        File expected = new File("./exports/ro_export_ref.json");
        File output = new File("./exports/ro_export.json");

        try {
            Assertions.assertEquals(FileUtils.readLines(expected),
                    FileUtils.readLines(output));
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean res = output.delete();
        if (!res)
            System.out.println("Something happened?");

        System.out.println("Basic case passed.");

        File file = new File("./exports");
        long beforeSize = file.length();
        admin.exportDictionary("spaniolaceva");
        long afterSize = file.length();
        Assertions.assertEquals(beforeSize, afterSize);

        System.out.println("Exception case passed.");
    }
}