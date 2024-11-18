package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Main {
    public static void main(String[] args) {
        // JDK Enhancement Proposal => JEP

        // JEP 431: sequenced collection
        jep431SequencedCollection();

        // JEP 430: String Templates (Preview)
        jep430StringTemplatesDemo();

        // JEP 439: Generational ZGC
        jep439GenerationalZGC();

        // JEP 440: Record Patterns
        jep440RecordPatterns();

        // JEP 441: Pattern Matching for switch
        jep441PatternMatchingSwitch();
    }

    private static void jep441PatternMatchingSwitch() {
        // preview in JDK 17 - available in 21
        Object obj = 1L;

        switch (obj) {
            case null -> System.out.println("null obj");
            case Long l -> System.out.println(Integer.MAX_VALUE + 1);
            case Integer i -> System.out.println(i + 1);
            default -> System.out.println("Default case");
        }

        // advanced matching cases
        String str1 = "str1";

        switch (str1) {
            case null -> System.out.println("null");
            case String str when str.equalsIgnoreCase("str1") || str.length() == 1 -> System.out.println("1");
            case String str when str.equalsIgnoreCase("str2") || str.length() == 2 -> System.out.println("2");
            default -> System.out.println("default");
        }

        // domination (resolved sequentially)
        // supper types dominate subtype case labels
        // unguarded (e.g. type) case labels dominate guarded (e.g. conditional) case labels
        // pattern case labels may dominate constant case labels
        Integer object = 5;

        switch (object) {
            case 0 -> System.out.println("It's a 0 long");                      // constant
            case Integer it when it < 0 -> System.out.println("Negative long"); // guarded dominates constant
            case Integer it -> System.out.println();                            // unguarded dominates guarded
        }
    }

    private static void jep440RecordPatterns() {
        Object obj = new String("123");
        jep440RecordPatternsPreJava21(obj);

        // in java 21
        Object obj21 = new Payment("foo@gmail.com", "bar@gmail.com", new Money("EUR", 100.0));
        jep440RecordPatternsPostJava21(obj21);

    }

    private static void jep440RecordPatternsPreJava21(Object object) {
        // pre-java 16
        if (object instanceof String) {
            String objString = (String) object;
            System.out.println(objString.isEmpty());
        }

        // post-java 16
        if (object instanceof String str) {
            System.out.println(str.isEmpty());
        }
    }

    private static void jep440RecordPatternsPostJava21(Object object) {
        if (object instanceof Payment payment) {
            System.out.println(STR."Paying \{payment.amount} to \{payment.to}");
        }

        if (object instanceof Payment(String fromEmail, String toEmail, Money money)) {
            System.out.println(STR."Paying \{money} from \{fromEmail} to \{toEmail}");
        }
    }

    private static void jep439GenerationalZGC() {
        // motivation
        // Improve application performance by extending the Z Garbage Collector (ZGC) to maintain separate generations
        // for young and old objects.
        // This will allow ZGC to collect young objects — which tend to die young — more frequently.
    }

    private static void jep431SequencedCollection() {
        // Purpose
        // - lacks a collection type that represents a sequence of elements with a defined encounter order
        // - lacks a uniform set of operations that apply across such collections

        List<Long> objects1 = List.of(1L);
        Deque<Long> objects2 = new ArrayDeque<>(List.of(1L));

        System.out.println(objects1.get(0));
        System.out.println(objects2.getFirst());

        // SequencedCollection - shared interface
        // - reversed
        // - addFirst
        // - addLast
        // - getFirst
        // - getLast
        // - removeFirst
        // - removeLast
        SequencedCollection<Long> sequencedCollection = objects1; //
        System.out.println(sequencedCollection.getFirst());
        System.out.println(objects2.getFirst());

        // others: SequencedSet, SequencedMap
        SequencedMap<String, Long> map1 = new LinkedHashMap<>();
        SequencedMap<String, Long> map2 = new TreeMap<>();

        Map.Entry<String, Long> stringLongEntry = map1.firstEntry();
        System.out.println(stringLongEntry);

        // how is this useful (?) => more flexible generic methods
        BiFunction<SequencedCollection<Long>, SequencedCollection<Long>, SequencedCollection<Long>> merge = (col1, col2) -> {
            List<Long> result = new LinkedList<>();

            while (!col1.isEmpty() && !col2.isEmpty()) {
                Long l1 = col1.getFirst();
                Long l2 = col2.getFirst();

                if (l1 < l2) {
                    result.add(col1.removeFirst());
                } else {
                    result.add(col2.removeFirst());
                }
            }

            result.addAll(col1);
            result.addAll(col2);

            return result;
        };

        System.out.println(merge.apply(
                new LinkedHashSet<>(List.of(2l, 4l, 6l, 6L, 7l)),
                new ArrayList<>(List.of(1l, 2l, 9L, 11L, 11L, 13L)))
        );
    }

    // preview
    private static void jep430StringTemplatesDemo() {
        Long resourceId = 1L;
        String user = "admin@admin.com";

        // pre-jdk-21 string concatenation (hard to read)
        String logLine1 = "Received request to update resource with id " + resourceId + " from user " + user;
        System.out.println(logLine1);

        // pre-jdk-21 string builder (verbose)
        String logLine2 = new StringBuilder()
                .append("Received request to update resource with id ")
                .append(resourceId)
                .append(" from user ")
                .append(user)
                .toString();
        System.out.println(logLine2);

        // pre-jdk-21 string format / MessageFormat
        System.out.println(String.format("Received request to update resource with id %d from user %s", resourceId, user));

        // new introduced preview feature (experimental)
        // strings
        String logLine3 = STR."Received request to update resource with id \{resourceId} from user \{user}";
        System.out.println(logLine3);

        // arithmetic
        Long x = 1L;
        Long y = 2L;
        String arithmeticLogLine1 = STR."\{x} + \{y} = \{x + y}";
        System.out.println(arithmeticLogLine1);

        // method calls
        BiFunction<Long, Long, Long> add = (Long it1, Long it2) -> it1 + it2;
        String arithmeticLogLine2 = STR."\{x} + \{y} = \{add.apply(x, y)}";
        System.out.println(arithmeticLogLine2);

        // embedded code blocks, multi-line, non-escaped characters
        Consumer<String> log = (str) -> {
            System.out.println(STR."[\{
                        LocalDateTime.now()
                                     .format(DateTimeFormatter.ofPattern("dd-MM-yyyy - HH:mm:ss"))
                    }] \{str}");
        };

        log.accept(STR."Received request to update resource with id '\{resourceId}' from user \"\{user}\"");

        // multi-line template expressions
        String title = "My Web Page";
        String text  = "Hello, world";

        String contents = STR."""
                    <html>
                      <head>
                        <title>\{title}</title>
                      </head>
                      <body>
                        <p>\{text}</p>
                      </body>
                    </html>
                """;
        System.out.println(contents);

        // see FMT => additional formatting options similar to String::format
        // see RAW => constructs string template without creating string
    }

    private record Money(String currency, Double amount) {}
    private record Payment(String from, String to, Money amount) {}

    private sealed interface HouseAnimal permits Dog, Cat {}
    private record Dog() implements HouseAnimal {}
    private record Cat() implements HouseAnimal {}

    private record HouseAnimalPair<T1 extends HouseAnimal, T2 extends HouseAnimal>(T1 t1, T2 t2) {}
}