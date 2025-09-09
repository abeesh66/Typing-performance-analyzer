public enum DifficultyLevel {
    EASY("Easy", 60, 3, 5, 100, "The easiest level with common words and longer time limits."),
    MEDIUM("Medium", 120, 5, 3, 200, "Moderate difficulty with a mix of common and less common words."),
    HARD("Hard", 180, 7, 1, 300, "Challenging level with complex words and shorter time limits.");

    private final String displayName;
    private final int timeLimit;
    private final int minWordLength;
    private final int maxWordLength;
    private final int wordCount;
    private final String description;

    DifficultyLevel(String displayName, int timeLimit, int minWordLength, 
                   int maxWordLength, int wordCount, String description) {
        this.displayName = displayName;
        this.timeLimit = timeLimit;
        this.minWordLength = minWordLength;
        this.maxWordLength = maxWordLength;
        this.wordCount = wordCount;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public int getTimeLimit() { return timeLimit; }
    public int getMinWordLength() { return minWordLength; }
    public int getMaxWordLength() { return maxWordLength; }
    public int getWordCount() { return wordCount; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return displayName;
    }
}
