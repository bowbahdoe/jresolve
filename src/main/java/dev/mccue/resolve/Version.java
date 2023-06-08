package dev.mccue.resolve;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Maven;

import java.math.BigInteger;
import java.util.*;

@Coursier("https://github.com/coursier/coursier/blob/8882fb4/modules/core/shared/src/main/scala/coursier/core/Version.scala")
@Maven("https://github.com/apache/maven-resolver/blob/97dfd1c/maven-resolver-util/src/main/java/org/eclipse/aether/util/version/GenericVersion.java")
public final class Version implements Comparable<Version> {

    private final String version;

    private final List<Item> items;

    private final int hash;

    public Version(String version) {
        this.version = Objects.requireNonNull(version, "version cannot be null");
        items = parse(version);
        hash = items.hashCode();
    }

    private static List<Item> parse(String version) {
        List<Item> items = new ArrayList<>();

        var tokenizer = new Tokenizer(version);
        while (tokenizer.next()) {
            Item item = tokenizer.toItem();
            items.add(item);
        }

        trimPadding(items);

        return Collections.unmodifiableList(items);
    }

    private static void trimPadding(List<Item> items) {
        Boolean number = null;
        int end = items.size() - 1;
        for (int i = end; i > 0; i--) {
            Item item = items.get(i);
            if (!Boolean.valueOf(item.isNumber()).equals(number)) {
                end = i;
                number = item.isNumber();
            }
            if (end == i
                    && (i == items.size() - 1 || items.get(i - 1).isNumber() == item.isNumber())
                    && item.compareTo(null) == 0) {
                items.remove(i);
                end--;
            }
        }
    }

    private static int comparePadding(List<Item> items, int index, Boolean number) {
        int rel = 0;
        for (int i = index; i < items.size(); i++) {
            Item item = items.get(i);
            if (number != null && number != item.isNumber()) {
                // do not stop here, but continue, skipping non-number members
                continue;
            }
            rel = item.compareTo(null);
            if (rel != 0) {
                break;
            }
        }
        return rel;
    }

    @Override
    public int compareTo(Version obj) {
        final List<Item> these = items;
        final List<Item> those = obj.items;

        boolean number = true;

        for (int index = 0; ; index++) {
            if (index >= these.size() && index >= those.size()) {
                return 0;
            } else if (index >= these.size()) {
                return -comparePadding(those, index, null);
            } else if (index >= those.size()) {
                return comparePadding(these, index, null);
            }

            Item thisItem = these.get(index);
            Item thatItem = those.get(index);

            if (thisItem.isNumber() != thatItem.isNumber()) {
                if (number == thisItem.isNumber()) {
                    return comparePadding(these, index, number);
                } else {
                    return -comparePadding(those, index, number);
                }
            } else {
                int rel = thisItem.compareTo(thatItem);
                if (rel != 0) {
                    return rel;
                }
                number = thisItem.isNumber();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Version other) && compareTo(other) == 0;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return version;
    }

    private static final class Tokenizer {

        private static final Integer QUALIFIER_ALPHA = -5;

        private static final Integer QUALIFIER_BETA = -4;

        private static final Integer QUALIFIER_MILESTONE = -3;

        private static final Map<String, Integer> QUALIFIERS;

        static {
            QUALIFIERS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            QUALIFIERS.put("alpha", QUALIFIER_ALPHA);
            QUALIFIERS.put("beta", QUALIFIER_BETA);
            QUALIFIERS.put("milestone", QUALIFIER_MILESTONE);
            QUALIFIERS.put("cr", -2);
            QUALIFIERS.put("rc", -2);
            QUALIFIERS.put("snapshot", -1);
            QUALIFIERS.put("ga", 0);
            QUALIFIERS.put("final", 0);
            QUALIFIERS.put("release", 0);
            QUALIFIERS.put("", 0);
            QUALIFIERS.put("sp", 1);
        }

        private final String version;

        private final int versionLength;

        private int index;

        private String token;

        private boolean number;

        private boolean terminatedByNumber;

        Tokenizer(String version) {
            this.version = (version.length() > 0) ? version : "0";
            this.versionLength = this.version.length();
        }

        public boolean next() {
            if (index >= versionLength) {
                return false;
            }

            int state = -2;

            int start = index;
            int end = versionLength;
            terminatedByNumber = false;

            for (; index < versionLength; index++) {
                char c = version.charAt(index);

                if (c == '.' || c == '-' || c == '_') {
                    end = index;
                    index++;
                    break;
                } else {
                    int digit = Character.digit(c, 10);
                    if (digit >= 0) {
                        if (state == -1) {
                            end = index;
                            terminatedByNumber = true;
                            break;
                        }
                        if (state == 0) {
                            // normalize numbers and strip leading zeros (prereq for Integer/BigInteger handling)
                            start++;
                        }
                        state = (state > 0 || digit > 0) ? 1 : 0;
                    } else {
                        if (state >= 0) {
                            end = index;
                            break;
                        }
                        state = -1;
                    }
                }
            }

            if (end - start > 0) {
                token = version.substring(start, end);
                number = state >= 0;
            } else {
                token = "0";
                number = true;
            }

            return true;
        }

        @Override
        public String toString() {
            return String.valueOf(token);
        }

        public Item toItem() {
            if (number) {
                try {
                    if (token.length() < 10) {
                        return new Item(Item.Kind.INT, Integer.parseInt(token));
                    } else {
                        return new Item(Item.Kind.BIGINT, new BigInteger(token));
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                if (index >= version.length()) {
                    if ("min".equalsIgnoreCase(token)) {
                        return Item.MIN;
                    } else if ("max".equalsIgnoreCase(token)) {
                        return Item.MAX;
                    }
                }
                if (terminatedByNumber && token.length() == 1) {
                    switch (token.charAt(0)) {
                        case 'a':
                        case 'A':
                            return new Item(Item.Kind.QUALIFIER, QUALIFIER_ALPHA);
                        case 'b':
                        case 'B':
                            return new Item(Item.Kind.QUALIFIER, QUALIFIER_BETA);
                        case 'm':
                        case 'M':
                            return new Item(Item.Kind.QUALIFIER, QUALIFIER_MILESTONE);
                        default:
                    }
                }
                Integer qualifier = QUALIFIERS.get(token);
                if (qualifier != null) {
                    return new Item(Item.Kind.QUALIFIER, qualifier);
                } else {
                    return new Item(Item.Kind.STRING, token.toLowerCase(Locale.ENGLISH));
                }
            }
        }
    }

    private record Item(Kind kind, Object value) {

        enum Kind {
            MAX(8),
            BIGINT(5),
            INT(4),
            STRING(3),
            QUALIFIER(2),
            MIN(0);

            final int value;

            Kind(int value) {
                this.value = value;
            }
        }

        static final Item MAX = new Item(Kind.MAX, "max");

        static final Item MIN = new Item(Kind.MIN, "min");

        public boolean isNumber() {
            return (kind.value & Kind.QUALIFIER.value) == 0; // i.e. kind != string/qualifier
        }

        public int compareTo(Item that) {
            int rel;
            if (that == null) {
                // null in this context denotes the pad item (0 or "ga")
                rel = switch (kind) {
                    case MIN -> -1;
                    case MAX, BIGINT, STRING -> 1;
                    case INT, QUALIFIER -> (Integer) value;
                };
            } else {
                rel = kind.value - that.kind.value;
                if (rel == 0) {
                    switch (kind) {
                        case BIGINT ->
                                rel = ((BigInteger) value).compareTo((BigInteger) that.value);
                        case INT, QUALIFIER ->
                                rel = ((Integer) value).compareTo((Integer) that.value);
                        case STRING ->
                                rel = ((String) value).compareToIgnoreCase((String) that.value);
                    }
                }
            }
            return rel;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Item item) && compareTo(item) == 0;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
