import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Word {
    @SerializedName("word")
    private String word;

    @SerializedName("word_en")
    private String word_en;

    @SerializedName("type")
    private String type;

    @SerializedName("singular")
    private ArrayList<String> singular;

    @SerializedName("plural")
    private ArrayList<String> plural;

    @SerializedName("definitions")
    private ArrayList<Definition> definitions;

    public Word(String word, String word_en, String type,
                ArrayList<String> singular, ArrayList<String> plural,
                ArrayList<Definition> definitions) {
        this.word = word;
        this.word_en = word_en;
        this.type = type;
        this.singular = singular;
        this.plural = plural;
        this.definitions = definitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word1 = (Word) o;
        return word.equals(word1.word);
    }

    @Override
    public int hashCode() {
        return word.hashCode();
    }

    public String getWord() {
        return word;
    }

    public String getWord_en() {
        return word_en;
    }

    public ArrayList<String> getSingular() {
        return singular;
    }

    public ArrayList<String> getPlural() {
        return plural;
    }

    public ArrayList<Definition> getDefinitions() {
        return definitions;
    }

    @Override
    public String toString() {
        return "Word{" +
                "word='" + word + '\'' +
                ", word_en='" + word_en + '\'' +
                ", type='" + type + '\'' +
                ", singular=" + singular +
                ", plural=" + plural +
                ", definitions=" + definitions.toString() +
                '}';
    }
}
