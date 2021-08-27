package wbif.sjx.MIA.Process;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class ModuleSearcher {
    HashMap<String, String> moduleDescriptions = new HashMap<>();
    HashMap<String, String[]> parameterDescriptions = new HashMap<>();

    public static void main(String[] args) {
        String target = "measure \"skeleton\" length";

        ModuleSearcher searcher = new ModuleSearcher();
        ArrayList<SearchMatch> matches = searcher.getMatches(target, true, true);
        for (SearchMatch match : matches) {
            System.out.println(match.getModuleName());
            System.out.println("    Score: " + match.getScore());
            for (String names : match.getNameMatches())
                System.out.println("    Names: " + names);
            for (String descriptions : match.getDescriptionMatches())
                System.out.println("    Descriptions: " + descriptions);
            for (String parameters : match.getParameterMatches())
                System.out.println("    Parameters: " + parameters);
        }
    }

    public ModuleSearcher() {
        List<String> classNames = ClassHunter.getModules(false);

        // Converting the list of classes to a list of Modules
        for (String className : classNames) {
            try {
                Class<Module> clazz = (Class<Module>) Class.forName(className);

                // Skip any abstract Modules
                if (Modifier.isAbstract(clazz.getModifiers()))
                    continue;

                Constructor<Module> constructor = clazz.getDeclaredConstructor(ModuleCollection.class);
                Module module = (Module) constructor.newInstance(new ModuleCollection());

                moduleDescriptions.put(module.getName().toLowerCase(), module.getDescription().toLowerCase());

                ParameterCollection parameters = module.getAllParameters();
                String[] descriptions = new String[parameters.size()];
                int i = 0;
                for (Parameter parameter : parameters.values())
                    descriptions[i++] = parameter.getDescription().toLowerCase();

                parameterDescriptions.put(module.getName().toLowerCase(), descriptions);

            } catch (InvocationTargetException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    protected static HashMap<String, Boolean> splitTargets(String target) {
        HashMap<String, Boolean> targets = new HashMap<>();

        // Ensuring any adjacent quotation marks have a recognisable symbol between them
        target = target.replace("\"\"", "\"$#£\"");

        // Adding anything before the first quotation mark
        int firstIdx = target.indexOf("\"");
        if (firstIdx != 0 && firstIdx != -1) {
            String[] tempTargets = target.substring(0, firstIdx).trim().split("\s");
            for (String tempTarget : tempTargets)
                targets.put(tempTarget, false);
        }

        // Any section within quotation marks is a single target, otherwise treat all
        // words separately
        Pattern pattern = Pattern.compile("(?<=([\"']))(?:(?=(\\\\?))\\2.)*?(?=\\1)");
        Matcher matcher = pattern.matcher(target);

        boolean state = false;
        while (matcher.find()) {
            state = !state;
            String foundString = matcher.group(0).trim();

            if (foundString.length() == 0)
                continue;

            if (foundString.equals("$#£")) // This is our dummy string to separate adjacent groups
                continue;

            if (state) {
                targets.put(foundString, true);
            } else {
                String[] tempTargets = foundString.split("\s");
                for (String tempTarget : tempTargets)
                    targets.put(tempTarget, false);
            }

        }

        // Adding anything after the first quotation mark
        int lastIdx = target.lastIndexOf("\"");
        if (lastIdx != target.length() - 1) {
            String[] tempTargets = target.substring(lastIdx + 1).trim().split("\s");
            for (String tempTarget : tempTargets)
                targets.put(tempTarget, false);
        }

        return targets;

    }

    public ArrayList<SearchMatch> getMatches(String target, boolean includeModuleDescriptions,
            boolean includeParameterDescriptions) {
        // Getting individual target words and phrases
        HashMap<String, Boolean> targets = splitTargets(target);

        HashMap<String, SearchMatch> matches = new HashMap<>();

        addNameMatches(matches, targets);
        if (includeModuleDescriptions)
            addDescriptionMatches(matches, targets);
        if (includeParameterDescriptions)
            addParameterMatches(matches, targets);

        ArrayList<SearchMatch> sortedMatches = new ArrayList<>(matches.values());
        enforcingFixedTargets(sortedMatches, targets);
        sortedMatches.sort(new SearchMatchSorter());

        return sortedMatches;

    }

    protected void addNameMatches(HashMap<String, SearchMatch> matches, HashMap<String, Boolean> targets) {
        for (String moduleName : moduleDescriptions.keySet()) {
            for (String target : targets.keySet()) {
                if (moduleName.contains(target)) {
                    matches.putIfAbsent(moduleName, new SearchMatch(moduleName));
                    SearchMatch match = matches.get(moduleName);
                    match.addNameMatch(moduleName);
                }
            }

        }
    }

    protected void addDescriptionMatches(HashMap<String, SearchMatch> matches, HashMap<String, Boolean> targets) {
        ArrayList<Pattern> patterns = new ArrayList<>();
        for (String target : targets.keySet())
            patterns.add(Pattern.compile(target));

        for (String moduleName : moduleDescriptions.keySet()) {
            String moduleDescription = moduleDescriptions.get(moduleName);

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(moduleDescription);
                while (matcher.find()) {
                    // Extract region 20 characters either side
                    int start = matcher.start();
                    int end = matcher.end();

                    matches.putIfAbsent(moduleName, new SearchMatch(moduleName));
                    SearchMatch match = matches.get(moduleName);

                    String substring = getLocalString(moduleDescription, start, end);

                    match.addDescriptionMatch(substring);

                }
            }
        }
    }

    protected void addParameterMatches(HashMap<String, SearchMatch> matches, HashMap<String, Boolean> targets) {
        ArrayList<Pattern> patterns = new ArrayList<>();
        for (String target : targets.keySet())
            patterns.add(Pattern.compile(target));

        for (String moduleName : parameterDescriptions.keySet()) {
            String[] currentParameterDescriptions = parameterDescriptions.get(moduleName);

            for (String parameterDescription : currentParameterDescriptions) {
                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(parameterDescription);
                    while (matcher.find()) {
                        // Extract region 20 characters either side
                        int start = matcher.start();
                        int end = matcher.end();

                        matches.putIfAbsent(moduleName, new SearchMatch(moduleName));
                        SearchMatch match = matches.get(moduleName);

                        String substring = getLocalString(parameterDescription, start, end);

                        match.addParameterMatch(substring);

                    }
                }
            }
        }
    }

    protected static void enforcingFixedTargets(ArrayList<SearchMatch> matches, HashMap<String, Boolean> targets) {
        ArrayList<String> fixedTargets = new ArrayList<>();
        for (String target : targets.keySet())
            if (targets.get(target))
                fixedTargets.add(target);

        Iterator<SearchMatch> iterator = matches.iterator();
        while (iterator.hasNext()) {
            boolean success = false;
            SearchMatch match = iterator.next();
            for (String nameMatch : match.getNameMatches())
                for (String fixedTarget : fixedTargets)
                    if (nameMatch.contains(fixedTarget))
                        success = true;
            for (String descriptionMatch : match.getDescriptionMatches())
                for (String fixedTarget : fixedTargets)
                    if (descriptionMatch.contains(fixedTarget))
                        success = true;
            for (String parameterMatch : match.getParameterMatches())
                for (String fixedTarget : fixedTargets)
                    if (parameterMatch.contains(fixedTarget))
                        success = true;

            if (!success)
                iterator.remove();
        }
    }

    protected static String getLocalString(String string, int start, int end) {
        int range = 50;
        start = Math.max(0, start - range);
        end = Math.min(string.length() - 1, end + range);

        // Removing incomplete words at the beginning
        if (start != 0) {
            int firstIdx = string.indexOf(" ", start);
            if (firstIdx != -1)
                start = firstIdx;
        }

        // Removing incomplete words at the end
        if (end != string.length() - 1) {
            int lastIdx = string.substring(0, end).lastIndexOf(" ");
            if (lastIdx != -1)
                end = lastIdx;
        }

        return string.substring(start, end).trim();

    }

    protected class SearchMatchSorter implements Comparator<SearchMatch> {
        @Override
        public int compare(SearchMatch o1, SearchMatch o2) {
            return o2.getScore() - o1.getScore();
        }
    }

    public class SearchMatch {
        private final int NAME_SCORE = 3;
        private final int DESCRIPTION_SCORE = 2;
        private final int PARAMETER_SCORE = 1;

        private final String moduleName;
        private final ArrayList<String> nameMatches = new ArrayList<>();
        private final ArrayList<String> descriptionMatches = new ArrayList<>();
        private final ArrayList<String> parameterMatches = new ArrayList<>();

        public SearchMatch(String moduleName) {
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }

        public void addNameMatch(String text) {
            nameMatches.add(text);
        }

        public void addDescriptionMatch(String text) {
            descriptionMatches.add(text);
        }

        public void addParameterMatch(String text) {
            parameterMatches.add(text);
        }

        public int getScore() {
            int score = 0;

            score += nameMatches.size() * NAME_SCORE;
            score += descriptionMatches.size() * DESCRIPTION_SCORE;
            score += parameterMatches.size() * PARAMETER_SCORE;

            return score;

        }

        public ArrayList<String> getNameMatches() {
            return nameMatches;
        }

        public ArrayList<String> getDescriptionMatches() {
            return descriptionMatches;
        }

        public ArrayList<String> getParameterMatches() {
            return parameterMatches;
        }
    }
}