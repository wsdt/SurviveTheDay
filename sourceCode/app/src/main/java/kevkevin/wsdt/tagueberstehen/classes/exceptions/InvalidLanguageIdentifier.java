package kevkevin.wsdt.tagueberstehen.classes.exceptions;

/** Used in LanguageCode to determine whether languageCodeKuerzel/ID is correctly formatted. If not
 * this exception is thrown. */
public class InvalidLanguageIdentifier extends Exception {
    public InvalidLanguageIdentifier() {super();}
    public InvalidLanguageIdentifier(String message) {super(message);}
    public InvalidLanguageIdentifier(String message, Throwable cause) {super(message, cause);}
    public InvalidLanguageIdentifier(Throwable cause) {super(cause);}
}
