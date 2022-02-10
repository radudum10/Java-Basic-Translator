import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Objects;

public class Definition {
    @SerializedName("dict")
    private String dict;

    @SerializedName("dictType")
    private String dictType;

    @SerializedName("year")
    private int year;

    @SerializedName("text")
    private ArrayList<String> text;

    public Definition(String dict, String dictType, int year, ArrayList<String> text) {
        this.dict = dict;
        this.dictType = dictType;
        this.year = year;
        this.text = text;
    }

    public String getDict() {
        return dict;
    }

    public String getDictType() {
        return dictType;
    }

    public int getYear() {
        return year;
    }

    public ArrayList<String> getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Definition that = (Definition) o;
        return year == that.year && Objects.equals(dict, that.dict) &&
                Objects.equals(dictType, that.dictType) &&
                Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dict, dictType, year, text);
    }

    @Override
    public String toString() {
        return "Definition{" +
                "dict='" + dict + '\'' +
                ", dictType='" + dictType + '\'' +
                ", year=" + year +
                ", text=" + text +
                '}';
    }
}
