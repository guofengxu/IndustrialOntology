package org.industrial.ontology.domain.lang;



import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.lang.LanguageCodeParser}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 10/04/16
 */
public class LanguageCodeParser {

    private static final Pattern pattern = Pattern.compile("\"(.+)\",\"([^\"]+)\"");


    public List<LanguageCode> parse(String codes) {
        String [] lines = codes.split("\n");
        List<LanguageCode> result = new ArrayList<>();
        for(String line : lines) {
            String trimmedLine = line.trim();
            Matcher matchResult = pattern.matcher(trimmedLine);
            if (matchResult.find()) {
                String lang = matchResult.group(1);
                String name = matchResult.group(2);
                result.add(new LanguageCode(lang, name));
            }
        }
        return result;
    }
}
