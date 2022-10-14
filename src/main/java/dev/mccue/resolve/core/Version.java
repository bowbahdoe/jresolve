package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Incomplete;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

@Incomplete
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Version.scala")
public record Version(List<Item> items) implements Comparable<Version> {
    public static final Version ZERO = new Version("0");
    public Version(String repr) {
        this(parseRepr(repr));
    }

    private static List<Item> parseRepr(String repr) {
        return List.of();
    }

    @Override
    public int compareTo(Version o) {
        return 0;
    }

    sealed interface Item extends Comparable<Item> {
        int order();

        default int compareToEmpty() {
            return 1;
        }

        default boolean isEmpty() {
            return compareToEmpty() == 0;
        }

        @Override
        default int compareTo(Item o) {
            if (this instanceof Number a && o instanceof Number b) {
                return Integer.compare(a.value, b.value);
            }
            else if (this instanceof BigNumber a && o instanceof BigNumber b) {
                return a.value.compareTo(b.value);
            }
            else if (this instanceof Number a && o instanceof BigNumber b) {
                return BigInteger.valueOf(a.value).compareTo(b.value);
            }

            else if (this instanceof BigNumber a && o instanceof Number b) {
                return a.value.compareTo(BigInteger.valueOf(b.value));
            }
            else if (this instanceof Tag a && o instanceof Tag b) {
                return a.compareTag(b);
            }
            else {
                var rel0 = this.compareToEmpty();
                var rel1 = o.compareToEmpty();

                if (rel0 == rel1) {
                    return Integer.compare(order(), o.order());
                }
                else {
                    return Integer.compare(rel0, rel1);
                }
            }
        }
    }

    sealed interface Numeric extends Item {
        String repr();
        Numeric next();
    }

    record Number(int value) implements Numeric {
        @Override
        public int order() {
            return 0;
        }

        @Override
        public String repr() {
            return Integer.toString(value);
        }

        @Override
        public Numeric next() {
            return new Number(value + 1);
        }
    }

    record BigNumber(BigInteger value) implements Numeric {
        public BigNumber {
            Objects.requireNonNull(value, "value must not be null");
        }
        @Override
        public int order() {
            return 0;
        }

        @Override
        public String repr() {
            return value.toString();
        }

        @Override
        public BigNumber next() {
            return new BigNumber(value.add(BigInteger.ONE));
        }
    }

    record Tag(String value) implements Item {
        private static final int OTHER_LEVEL = -5;

        public int level() {
            return switch (value) {
                case "ga", "final", "" -> 0; // 1.0.0 equivalent
                case "snapshot" -> -1;
                case "rc", "cr" -> -2;
                case "beta", "b" -> -3;
                case "alpha", "a" -> -4;
                case "dev" -> -6;
                case "sp", "bin" -> 1;
                default -> OTHER_LEVEL;
            };
        }

        public boolean isPreRelease() {
            return level() < 0;
        }

        public int compareTag(Tag other) {
            int thisLevel = level();
            int otherLevel = other.level();
            int levelComp = Integer.compare(
                    thisLevel,
                    otherLevel
            );
            if (levelComp == 0 && thisLevel == otherLevel) {
                return value.compareToIgnoreCase(other.value);
            }
            else {
                return levelComp;
            }
        }

        @Override
        public int order() {
            return -1;
        }
    }

    record BuildMetadata(String value) implements Item {

        @Override
        public int order() {
            return 1;
        }

        @Override
        public int compareToEmpty() {
            return 0;
        }
    }

    record Min() implements Item {
        @Override
        public int order() {
            return -8;
        }

        @Override
        public int compareToEmpty() {
            return -1;
        }
    }

    record Max() implements Item {

        @Override
        public int order() {
            return 8;
        }
    }

}
