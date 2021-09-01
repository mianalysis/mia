package io.github.mianalysis.MIA.Process;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.Parameter;

public class ModuleSearcher { 
    private ModuleCollection modules;

    public ModuleSearcher(ModuleCollection modules) {
        this.modules = modules;

    }

    protected static HashMap<String, Boolean> splitTargets(String target) {
        HashMap<String, Boolean> targets = new HashMap<>();

        // Ensuring any adjacent quotation marks have a recognisable symbol between them
        target = target.replace("\"\"", "\"$#£\"");

        // Adding anything before the first quotation mark
        int firstIdx = target.indexOf("\"");
        if (firstIdx != 0 && firstIdx != -1) {
            String[] tempTargets = target.substring(0, firstIdx).trim().split("\\s");
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
                String[] tempTargets = foundString.split("\\s");
                for (String tempTarget : tempTargets)
                    targets.put(tempTarget, false);
            }
        }

        // Adding anything after the first quotation mark
        int lastIdx = target.lastIndexOf("\"");
        if (lastIdx != target.length() - 1) {
            String[] tempTargets = target.substring(lastIdx + 1).trim().split("\\s");
            for (String tempTarget : tempTargets)
                targets.put(tempTarget, false);
        }

        return targets;

    }

    public ArrayList<SearchMatch> getMatches(String target, boolean includeModuleDescriptions,
            boolean includeParameterDescriptions) {
        target = target.toLowerCase();

        // Getting individual target words and phrases
        HashMap<String, Boolean> targets = splitTargets(target);

        HashMap<Module, SearchMatch> matches = new HashMap<>();

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

    protected void addNameMatches(HashMap<Module, SearchMatch> matches, HashMap<String, Boolean> targets) {
        for (Module module : modules) {
            for (String target : targets.keySet()) {
                if (module.getName().toLowerCase().contains(target)) {
                    matches.putIfAbsent(module, new SearchMatch(module));
                    SearchMatch match = matches.get(module);
                    match.addNameMatch(module.getName());
                }
            }
        }
    }

    protected void addDescriptionMatches(HashMap<Module, SearchMatch> matches, HashMap<String, Boolean> targets) {
        ArrayList<Pattern> patterns = new ArrayList<>();
        for (String target : targets.keySet())
            patterns.add(Pattern.compile(target));

        for (Module module : modules) {
            String moduleDescription = module.getDescription();

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(moduleDescription);
                while (matcher.find()) {
                    // Extract region 20 characters either side
                    int start = matcher.start();
                    int end = matcher.end();

                    matches.putIfAbsent(module, new SearchMatch(module));
                    SearchMatch match = matches.get(module);

                    String substring = getLocalString(moduleDescription, start, end);

                    match.addDescriptionMatch(substring);

                }
            }
        }
    }

    protected void addParameterMatches(HashMap<Module, SearchMatch> matches, HashMap<String, Boolean> targets) {
        ArrayList<Pattern> patterns = new ArrayList<>();
        for (String target : targets.keySet())
            patterns.add(Pattern.compile(target));

        for (Module module : modules) {
            for (Parameter parameter : module.getAllParameters().values()) {
                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(parameter.getDescription());
                    while (matcher.find()) {
                        // Extract region 20 characters either side
                        int start = matcher.start();
                        int end = matcher.end();

                        matches.putIfAbsent(module, new SearchMatch(module));
                        SearchMatch match = matches.get(module);

                        String substring = getLocalString(parameter.getDescription(), start, end);

                        match.addParameterMatch(substring);

                    }
                }
            }
        }
    }

    protected static void enforcingFixedTargets(ArrayList<SearchMatch> matches, HashMap<String, Boolean> targets) {
        // Check if there are any queries being forced, if not all are fine
        boolean enforcerOn = false;
        for (boolean enforce : targets.values())
            if (enforce)
                enforcerOn = true;

        if (!enforcerOn)
            return;

        ArrayList<String> fixedTargets = new ArrayList<>();
        for (String target : targets.keySet())
            if (targets.get(target))
                fixedTargets.add(target);

        Iterator<SearchMatch> iterator = matches.iterator();
        while (iterator.hasNext()) {
            SearchMatch match = iterator.next();
            boolean removed = false;
            
            for (String fixedTarget : fixedTargets) {
                // If removed as part of a previous fixed target match, skip this test
                if (removed)
                    continue;

                boolean success = false;
                
                for (String nameMatch : match.getNameMatches())
                    if (nameMatch.contains(fixedTarget))
                        success = true;
                for (String descriptionMatch : match.getDescriptionMatches())
                    if (descriptionMatch.contains(fixedTarget))
                        success = true;
                for (String parameterMatch : match.getParameterMatches())
                    if (parameterMatch.contains(fixedTarget))
                        success = true;

                if (!success) {
                    iterator.remove();
                    removed = true;
                }
            }
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
        private final int NAME_SCORE = 10;
        private final int DESCRIPTION_SCORE = 3;
        private final int PARAMETER_SCORE = 1;

        private final Module module;
        private final ArrayList<String> nameMatches = new ArrayList<>();
        private final ArrayList<String> descriptionMatches = new ArrayList<>();
        private final ArrayList<String> parameterMatches = new ArrayList<>();

        public SearchMatch(Module module) {
            this.module = module;
        }

        public Module getModule() {
            return module;
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