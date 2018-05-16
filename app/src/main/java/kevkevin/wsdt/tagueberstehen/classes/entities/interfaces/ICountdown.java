package kevkevin.wsdt.tagueberstehen.classes.entities.interfaces;

public interface ICountdown {
    //Escape methods
    interface ESCAPE {
        String[] escapeEnter_illegalCharacters = new String[]{"\n", "\r", System.getProperty("line.separator")}; //illegalCharacters (enter) get replaced with legalCharacter below (only called for title and description because they use CustomEdittext)
        String escapeEnter_legalCharacter = " "; //when enter found, place space
    }
}
