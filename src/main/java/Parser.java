import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class Parser {

    /* creating an Array with all words from the json */
    private Dictionary createDictionary(File jsonFile) {
        Path path = jsonFile.toPath();

        /* the json file name format is LANGUAGE_dict.json */
        String[] tokens = ((path.getFileName()).toString().split("_"));
        String fileName = tokens[0];

        Dictionary dictionary = new Dictionary(fileName);

        String jsonBody = null;
        try {
            jsonBody = Files.readString(path);

        } catch (IOException e) {
            System.out.println("IOException: " + path);
            e.printStackTrace();
        }

        Gson gson = new Gson();

        /* creating a token with the type of the list */
        Type type = new TypeToken<List<Word>>() {
        }.getType();

        /* adding all words to a list */
        List<Word> wordList = gson.fromJson(jsonBody, type);

        /* moving them into a hashmap */
        assert wordList != null;
        dictionary.fromList(wordList);

        return dictionary;
    }

    /* returns a HashMap with the info from all json files */
    public void allJsons(HashMap<String, Dictionary>
                                 dictionaryMap) {

        /* getting all files with .json extension */
        File dir = new File(".");
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".json"));

        /* for each json file, add words to hashmap */
        assert files != null;
        for (File jsonFile : files) {
            Dictionary dictionary = createDictionary(jsonFile);
            dictionaryMap.put(dictionary.getLanguage(), dictionary);
        }
    }
}
