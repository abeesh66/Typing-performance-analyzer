import org.example.model.DifficultyLevel;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class TextGenerator {
    private static final String[] COMMON_WORDS = {
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "I"
    };
    
    private static final String[] MEDIUM_WORDS = {
        "quick", "brown", "fox", "jumps", "over", "lazy", "dog", "pack", "my", "box"
    };
    
    private static final String[] HARD_WORDS = {
        "quintessential", "juxtaposition", "xylophone", "quagmire", "kaleidoscope",
        "mnemonic", "pneumonia", "rhythm", "synecdoche", "zephyr"
    };
    
    private static final Random random = new Random();
    
    public static String generateText(DifficultyLevel level) {
        List<String> wordPool = new ArrayList<>();
        
        switch (level) {
            case EASY:
                addWords(wordPool, COMMON_WORDS, 100);
                break;
            case MEDIUM:
                addWords(wordPool, COMMON_WORDS, 50);
                addWords(wordPool, MEDIUM_WORDS, 50);
                break;
            case HARD:
                addWords(wordPool, MEDIUM_WORDS, 40);
                addWords(wordPool, HARD_WORDS, 60);
                break;
        }
        
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < level.getWordCount(); i++) {
            if (i > 0) text.append(" ");
            text.append(wordPool.get(random.nextInt(wordPool.size())));
        }
        
        return text.toString();
    }
    
    private static void addWords(List<String> wordPool, String[] words, int count) {
        for (int i = 0; i < count; i++) {
            wordPool.add(words[i % words.length]);
        }
    }
}
