package org.example.util;

public class TextSamples {
    // Easy: Three progressive levels of simple Java concepts
    public static final String[] EASY_LEVEL_1 = {
        "Java is a popular language. It is used to make apps. Code is in classes. Classes have methods. The main method starts programs.",
        "Variables store data. Use int for numbers. String holds text. boolean is true or false. Always declare variables first.",
        "Loops repeat code. The for loop counts. The while loop checks. Use break to stop early. Loops make code efficient."
    };

    public static final String[] EASY_LEVEL_2 = {
        "Methods group code. They take parameters. They return values. void means no return. Methods make code reusable.",
        "Arrays store many values. They have fixed size. First index is zero. Use length to check size. Arrays are fast to access.",
        "If statements make choices. Use else for alternatives. Compare with == or equals(). Logical operators combine conditions."
    };

    public static final String[] EASY_LEVEL_3 = {
        "Objects are instances of classes. They have state and behavior. Use new to create them. Constructors initialize objects. Objects interact through methods.",
        "String methods are useful. length() gets size. toUpperCase() changes case. substring() extracts parts. equals() compares content.",
        "ArrayLists are flexible. They grow automatically. Use add() and remove(). size() gets count. Contains objects, not primitives."
    };

    // Medium: Three progressive levels of intermediate Java concepts
    public static final String[] MEDIUM_LEVEL_1 = {
        "Object-oriented programming uses classes and objects. Encapsulation hides implementation. Inheritance creates hierarchies. Polymorphism allows many forms. Abstraction simplifies complexity.",
        "Exception handling prevents crashes. Try contains risky code. Catch handles exceptions. Finally always executes. Use specific exception types.",
        "Collections store groups of objects. Lists maintain order. Sets ensure uniqueness. Maps store key-value pairs. Choose the right collection type."
    };

    public static final String[] MEDIUM_LEVEL_2 = {
        "Interfaces define contracts. Classes implement interfaces. Multiple interfaces allowed. Default methods provide implementation. Useful for callbacks.",
        "Generics make code type-safe. Use angle brackets for type parameters. Prevents class cast exceptions. Works with collections. Wildcards add flexibility.",
        "File I/O handles data persistence. Use File for files and directories. Streams read/write bytes. Readers/Writers handle text. Always close resources."
    };

    public static final String[] MEDIUM_LEVEL_3 = {
        "Multithreading enables concurrency. Threads run independently. Synchronization prevents race conditions. volatile ensures visibility. Atomic classes provide thread-safe operations.",
        "Java 8 introduced lambdas. They represent behavior. Used with functional interfaces. Method references simplify code. Streams process collections functionally.",
        "JDBC connects to databases. Load the driver first. Create a connection. Execute SQL statements. Use prepared statements for security."
    };

    // Hard: Three progressive levels of advanced Java topics
    public static final String[] HARD_LEVEL_1 = {
        "The Java Memory Model defines thread interaction. Happens-before ensures ordering. volatile provides visibility guarantees. synchronized enforces mutual exclusion. final fields have special semantics.",
        "Garbage collection manages memory automatically. Generational hypothesis guides collection. Young generation uses copying. Old generation uses mark-sweep-compact. Tune for throughput or latency.",
        "Class loading follows delegation. Bootstrap loads core classes. Extension loads standard extensions. System loads application classes. Custom class loaders enable plugins."
    };

    public static final String[] HARD_LEVEL_2 = {
        "JIT compilation optimizes performance. Interpreted code runs first. Hot methods get compiled. Inlining reduces call overhead. Escape analysis eliminates allocations.",
        "Concurrent collections are thread-safe. ConcurrentHashMap scales well. CopyOnWriteArrayList is good for rare modifications. BlockingQueue enables producer-consumer. ConcurrentNavigableMap supports concurrent navigation.",
        "NIO provides non-blocking I/O. Buffers hold data. Channels represent connections. Selectors manage multiple channels. Memory-mapped files improve performance."
    };

    public static final String[] HARD_LEVEL_3 = {
        "Reactive programming handles async data streams. Backpressure prevents overflow. Project Reactor implements Reactive Streams. Mono handles 0-1 results. Flux handles 0-N results with backpressure.",
        "Java modules enforce strong encapsulation. module-info.java declares dependencies. Services enable loose coupling. JLink creates custom runtimes. Javadoc documents modules.",
        "Low-level concurrency uses Unsafe. Compare-and-swap enables lock-free algorithms. VarHandles provide safe access. Fences control memory ordering. Off-heap memory bypasses GC."
    };

    /**
     * Gets a random text sample for the specified difficulty and level
     * @param difficulty "easy", "medium", or "hard"
     * @param level 1, 2, or 3 (1 being the easiest within the difficulty)
     * @return A string containing the text to type
     */
    public static String getTextByLevel(String difficulty, int level) {
        String[] selectedArray;
        
        switch (difficulty.toLowerCase()) {
            case "easy" -> {
                switch (level) {
                    case 1 -> selectedArray = EASY_LEVEL_1;
                    case 2 -> selectedArray = EASY_LEVEL_2;
                    case 3 -> selectedArray = EASY_LEVEL_3;
                    default -> throw new IllegalArgumentException("Invalid level for easy difficulty: " + level);
                }
            }
            case "medium" -> {
                switch (level) {
                    case 1 -> selectedArray = MEDIUM_LEVEL_1;
                    case 2 -> selectedArray = MEDIUM_LEVEL_2;
                    case 3 -> selectedArray = MEDIUM_LEVEL_3;
                    default -> throw new IllegalArgumentException("Invalid level for medium difficulty: " + level);
                }
            }
            case "hard" -> {
                switch (level) {
                    case 1 -> selectedArray = HARD_LEVEL_1;
                    case 2 -> selectedArray = HARD_LEVEL_2;
                    case 3 -> selectedArray = HARD_LEVEL_3;
                    default -> throw new IllegalArgumentException("Invalid level for hard difficulty: " + level);
                }
            }
            default -> throw new IllegalArgumentException("Invalid difficulty: " + difficulty);
        }
        
        // Return a random sentence from the selected level
        return selectedArray[(int) (Math.random() * selectedArray.length)];
    }
    
    /**
     * Gets a random text sample for the specified difficulty (for backward compatibility)
     */
    public static String getTextByDifficulty(String difficulty) {
        // Default to level 2 for backward compatibility
        return getTextByLevel(difficulty, 2);
    }
    
    // For testing or specific level access
    public static String getTextForTypingTest(String difficulty, int level) {
        return getTextByLevel(difficulty, level);
    }
}
