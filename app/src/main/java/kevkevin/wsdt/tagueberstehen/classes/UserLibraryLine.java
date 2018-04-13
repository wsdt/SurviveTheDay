package kevkevin.wsdt.tagueberstehen.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated //Add all Lines as Strings into UserLibrary
public class UserLibraryLine {
    private static final String TAG = "UserLibraryLine";

    private int lineId; //arrayindex
    private String lineText; //e.g. quote itself or similar
    private UserLibrary userLibrary; //for relationship

    private static Map<String,UserLibraryLine> allLibraryLines = new HashMap<>();

    public UserLibraryLine(int lineId, String lineText, UserLibrary userLibrary) {
        this.setLineId(lineId);
        this.setLineText(lineText);
        this.setUserLibrary(userLibrary);
    }


    //GETTER/SETTER ----------------------------------
    public static Map<String,UserLibraryLine> getAllLibraryLines() {
        return allLibraryLines;
    }

    public static void setAllLibraryLines(Map<String,UserLibraryLine> allLibraryLines) {
        UserLibraryLine.allLibraryLines = allLibraryLines;
    }

    public String getLineText() {
        return lineText;
    }

    public void setLineText(String lineText) {
        this.lineText = lineText;
    }

    public UserLibrary getUserLibrary() {
        return userLibrary;
    }

    public void setUserLibrary(UserLibrary userLibrary) {
        this.userLibrary = userLibrary;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }
}
