package io.github.mianalysis.mia.module;

import org.scijava.util.VersionUtils;

public class Dependency {
    private final Class dependencyClass;
    private final String versionThreshold;
    private final Relationship relationship;

    enum Relationship {
        LESS_THAN, LESS_THAN_OR_EQUAL_TO, EQUAL_TO, GREATER_THAN_OR_EQUAL_TO, GREATER_THAN, NOT_EQUAL_TO;
    }

    public Dependency(Class dependencyClass, String versionThreshold, Relationship relationship) {
        this.dependencyClass = dependencyClass;
        this.versionThreshold = versionThreshold;
        this.relationship = relationship;

    }

    public boolean test() {
        int comparison = VersionUtils.compare(dependencyClass.getPackage().getImplementationVersion(),
                this.versionThreshold);

        switch (relationship) {
            case LESS_THAN:
                return comparison < 0;

            case LESS_THAN_OR_EQUAL_TO:
                return comparison <= 0;

            case EQUAL_TO:
                return comparison == 0;

            case GREATER_THAN_OR_EQUAL_TO:
                return comparison >= 0;

            case GREATER_THAN:
                return comparison > 0;

            case NOT_EQUAL_TO:
                return comparison != 0;

        }

        return false;

    }

    static String getSymbol(Relationship relationship) {
        switch (relationship) {
            default:
                return "";

            case LESS_THAN:
                return "<";

            case LESS_THAN_OR_EQUAL_TO:
                return "<=";

            case EQUAL_TO:
                return "==";

            case GREATER_THAN_OR_EQUAL_TO:
                return ">=";

            case GREATER_THAN:
                return ">";

            case NOT_EQUAL_TO:
                return "!=";

        }
    }

    @Override
    public String toString() {
        return dependencyClass.getSimpleName() + " " + getSymbol(relationship) + " " + versionThreshold;

    }
}
