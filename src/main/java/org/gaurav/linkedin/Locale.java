package org.gaurav.linkedin;

public record Locale(String country, String language) {
    @Override
    public String toString() {
        return """
                [
                        country='%s',
                        language='%s'
                    ]""".formatted(country, language);
    }
}
