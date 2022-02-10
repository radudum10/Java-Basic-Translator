import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Translation {
    private final Administration admin;

    public Translation(Administration admin) {
        this.admin = admin;
    }

    /* searches the dictionary for the english form of a word */
    private Word getWordByEnglishForm
    (String englishForm, HashMap<Integer, Word> wordMap) {
        for (Map.Entry<Integer, Word> entry : wordMap.entrySet()) {
            Word currWord = entry.getValue();
            if (currWord.getWord_en().equals(englishForm))
                return currWord;
        }

        return null;
    }

    /* translates a word from a language to another
        the translation is not smart enough
        to choose between infinitive and a form sadly */
    String translateWord(String word, String fromLanguage, String toLanguage) {
        HashMap<Integer, Word> sourceWordMap = admin.getWordMap(fromLanguage);
        HashMap<Integer, Word> destWordMap = admin.getWordMap(toLanguage);

        if (destWordMap == null || sourceWordMap == null) {
            System.out.println("The translation option from " +
                    fromLanguage + " to " + toLanguage + " is not available.");
            return null;
        }

        String englishForm = null;
        Word translatedWord;

        /* search the "main" form, no index or singular / plural form info needed */
        int hashCode = word.hashCode();
        if (sourceWordMap.containsKey(hashCode)) {
            Word foundWord = sourceWordMap.get(hashCode);
            englishForm = foundWord.getWord_en();
            translatedWord = getWordByEnglishForm(englishForm,
                    destWordMap);

            /* if there is no translation for the word then return the original word */
            if (translatedWord == null) {
                System.out.println("The word " + word +
                        " has no translation into " + toLanguage);

                return word;
            }
            return translatedWord.getWord();
        }

        /* search the other forms, similar with searchWord in administration,
         but storing the form index */

        boolean singular = true;
        int personConj = -1; // 1st, 2nd or 3rd person

        for (Map.Entry<Integer, Word> entry : sourceWordMap.entrySet()) {
            Word currWord = entry.getValue();
            ArrayList<String> singularList = currWord.getSingular();

            personConj = singularList.indexOf(word);

            /* indexOf() returns -1 if the searched argument is not in the list */
            if (personConj != -1) {
                englishForm = currWord.getWord_en();
                break;
            }

            ArrayList<String> pluralList = currWord.getPlural();
            personConj = pluralList.indexOf(word);
            if (personConj != -1) {
                englishForm = currWord.getWord_en();
                singular = false;
                break;
            }
        }

        if (englishForm == null) {
            System.out.println("The word " + word +
                    " is not in the source dictionary.");
            return word;
        }

        translatedWord = getWordByEnglishForm(englishForm, destWordMap);

        if (translatedWord == null) {
            System.out.println("The word " + word +
                    " is not in the destination dictionary");
            return word;
        }

        /* deciding which form of the word to return */
        if (singular) {
            ArrayList<String> translatedSingulars = translatedWord.
                    getSingular();

            return translatedSingulars.get(personConj);
        }

        ArrayList<String> translatedPlurals = translatedWord.getPlural();
        return translatedPlurals.get(personConj);
    }

    String translateSentence(String sentence, String fromLanguage,
                             String toLanguage) {
        String punctuation = null;
        if (sentence.matches(".*[.?!;]$")) {
            punctuation = sentence.substring(sentence.length() - 1); // storing the punctuation
            sentence = sentence.substring(0, sentence.length() - 1); // removing the punctuation
        }

        String[] tokens = sentence.split(" "); // get each word of the sentence

        StringBuilder translated = new StringBuilder();
        for (String token : tokens) {
            token = token.toLowerCase(Locale.ROOT);
            String foundString = translateWord(token, fromLanguage,
                    toLanguage);

            if (foundString == null)
                return null;

            translated.append(foundString);
            translated.append(" ");
        }

        translated.deleteCharAt(translated.length() - 1); // deleting the additional space
        String ans = translated.toString();

        /* capitalizing the first letter and adding punctuation */
        ans = ans.substring(0, 1).toUpperCase(Locale.ROOT) + ans.substring(1).
                toLowerCase(Locale.ROOT);

        if (punctuation != null)
            ans += punctuation;

        return ans;
    }

    /* returns an array list with all the synonyms of a word */
    ArrayList<String> getSynonyms(Word word) {
        ArrayList<Definition> definitions = word.getDefinitions();
        /* if there are no definitions for the word */
        if (definitions.size() == 0)
            return null;

        ArrayList<String> synonyms = new ArrayList<>();

        /* search for a definition with synonyms as dict type */
        for (Definition definition : definitions)
            /* if found, then add all synonyms */
            if ((definition.getDictType().equals("synonyms")))
                synonyms.addAll(definition.getText());

        return synonyms;
    }

    void capitalizeAndPunctuation(ArrayList<String> sentences,
                                  String punctuation) {
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            sentence = sentence.substring(0, 1).toUpperCase(Locale.ROOT)
                    + sentence.substring(1);
            if (punctuation != null)
                sentence += punctuation;
            sentences.set(i, sentence);
        }
    }

    ArrayList<String> translateSentences(String sentence, String fromLanguage, String
            toLanguage) {
        final int NO_OF_TRANSLATIONS = 3;

        ArrayList<String> translations = new ArrayList<>();
        String firstForm = translateSentence(sentence, fromLanguage, toLanguage);
        if (firstForm == null)
            return null;

        firstForm = firstForm.toLowerCase(Locale.ROOT);
        translations.add(firstForm);

        String punctuation = null;
        if (sentence.matches(".*[.?!;,]$")) {
            punctuation = sentence.substring(sentence.length() - 1); // storing the punctuation
            firstForm = firstForm.substring(0, firstForm.length() - 1); // removing the punctuation
        }
        String[] tokens = firstForm.split(" ");

        /* if there was no map then firstForm would be null so there is no need to check if it exists */
        HashMap<Integer, Word> destWordMap = admin.getWordMap(toLanguage);

        Word currWord;
        int counter = 0; // for storing the index of the word that should be replaced

        /* for each word, find all synonyms and replace in the existing forms that word */
        for (String token : tokens) {
            if (translations.size() == NO_OF_TRANSLATIONS) { // this is needed because the last addition is not checked
                capitalizeAndPunctuation(translations, punctuation);
                return translations;
            }

            currWord = admin.searchWord(destWordMap, token);
            if (currWord == null) { // if the word was in the source dictionary but not in the destination
                counter++;
                continue;
            }

            ArrayList<String> synonyms = getSynonyms(currWord);

            /* make a form with each synonym */
            for (String synonym : synonyms) {
                int size = translations.size();
                for (int i = 0; i < size; i++) {
                    if (translations.size() == NO_OF_TRANSLATIONS) {
                        capitalizeAndPunctuation(translations, punctuation);
                        return translations;
                    }
                    String form = translations.get(i);
                    translations.add(form.replace(tokens[counter], synonym));
                }
            }
            counter++;
        }
        return translations;
    }
}