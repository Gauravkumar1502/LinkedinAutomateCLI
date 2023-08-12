package org.gaurav.linkedin;

public record UserInfo(String sub, String email_verified, String name, Locale locale, String given_name, String family_name, String email, String picture) {
    @Override
    public String toString() {
        return """
                UserInfo[
                    sub='%s',
                    email_verified='%s',
                    name='%s',
                    locale=%s,
                    given_name='%s',
                    family_name='%s',
                    email='%s',
                    picture=%s
                ]""".formatted(sub, email_verified, name, locale, given_name, family_name, email, picture);
    }
}
