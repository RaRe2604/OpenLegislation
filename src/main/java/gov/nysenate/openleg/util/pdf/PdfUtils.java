package gov.nysenate.openleg.util.pdf;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.RegExUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;

public class PdfUtils {

    private static final Logger logger = LoggerFactory.getLogger(PdfUtils.class);

    public static final ImmutableSet<Character> forbiddenCharacters =
            ImmutableSet.<Character>builder()
                    .add('\f')      // Line feed
                    .add('\uFFFD')  // Unidentified/unused codes
                    .add('\uFFFE')
                    .add('\uFFFF')
                    .add('\u0082')  // Break permitted here
                    .add('\u007F')
                    .build();

    public static final Pattern forbiddenCharPattern = Pattern.compile(
            "[" + Joiner.on("").join(forbiddenCharacters) + "]"
    );

    public static final ImmutableMap<String, String> strReplaceMap = ImmutableMap.<String, String>builder()
            .put("\t", "    ")
            .put("\u008A", "e")  // An odd bracket like character used in place of an accented e.
            .put("\u0092", "'")
            .put("[\u0093\u0094]", "\"") // Fancy quotes
            .put("[\u001B\u00A0]", " ")
            .build();

    public static String sanitize(String text) {
        if (text == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : strReplaceMap.entrySet()) {
            text = text.replaceAll(entry.getKey(), entry.getValue());
        }

        return RegExUtils.replaceAll(text, forbiddenCharPattern, "");
    }
}
