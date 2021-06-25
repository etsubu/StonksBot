package com.etsubu.stonksbot.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Configurations for filtering unwanted messages
 *
 * @author etsubu
 */
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FilterConfig {
    private List<String> patterns;
    private List<Pattern> regexPatterns;
    /**
     * Filtering actions taken are mentioned on this channel to notify admins
     */
    private String notifyChannel;

    /**
     * Compiles regex patterns
     */
    public void update() {
        // Compile regex patterns
        regexPatterns = Optional.ofNullable(patterns)
                .map(x -> x.stream()
                        .map(Pattern::compile)
                        .collect(Collectors.toList()))
                .orElseGet(LinkedList::new);
    }

    public List<Pattern> getRegexPatterns() {
        if (regexPatterns == null) {
            regexPatterns = new LinkedList<>();
        }
        return Collections.unmodifiableList(regexPatterns);
    }

    public List<String> getPatterns() {
        if (patterns == null) {
            patterns = new LinkedList<>();
        }
        return Collections.unmodifiableList(patterns);
    }

    public String getNotifyChannel() {
        return notifyChannel;
    }
}
