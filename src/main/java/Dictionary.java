import java.util.HashMap;
import java.util.List;

public class Dictionary {
    private final String language;
    private final HashMap<Integer, Word> wordMap;

    public Dictionary(String languageCode) {
        this.language = languageCode;
        wordMap = new HashMap<>();
    }

    public HashMap<Integer, Word> getWordMap() {
        return wordMap;
    }

    public String getLanguage() {
        return language;
    }

    public void fromList(List<Word> wordList) {
        for (Word word : wordList) {
            Integer hashCode = word.hashCode();

            if (wordMap.containsKey(hashCode)) {
                System.out.println("The word " + word.getWord() +
                        "already exists!");
                continue;
            }

            wordMap.put(hashCode, word);
        }
    }
}