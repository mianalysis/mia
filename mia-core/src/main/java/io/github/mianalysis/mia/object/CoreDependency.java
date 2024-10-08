package io.github.mianalysis.mia.object;

import org.scijava.plugin.SciJavaPlugin;
import org.scijava.util.VersionUtils;

import io.github.mianalysis.mia.MIA;

public abstract class CoreDependency implements SciJavaPlugin {
    public enum Relationship {
        LESS_THAN, LESS_THAN_OR_EQUAL_TO, EQUAL_TO, GREATER_THAN_OR_EQUAL_TO, GREATER_THAN, NOT_EQUAL_TO;
    }

    public abstract String getMessage();
    public abstract String getClassName();
    public abstract String getVersionThreshold();
    public abstract Relationship getRelationship();

    public boolean test() {
        Class<?> dependencyClass = null;
        try {
            dependencyClass = Class.forName(getClassName());
        } catch (ClassNotFoundException e) {
            return false;
        }
        
        int comparison = VersionUtils.compare(dependencyClass.getPackage().getImplementationVersion(),
                this.getVersionThreshold());

        switch (getRelationship()) {
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
        String shortName = getClassName().substring(getClassName().lastIndexOf(".")+1);
        return shortName + " " + getSymbol(getRelationship()) + " " + getVersionThreshold();

    }
}
