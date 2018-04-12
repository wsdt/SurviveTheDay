package kevkevin.wsdt.tagueberstehen.classes;

import java.util.ArrayList;
import java.util.List;

public class UserLibraryLine {
    private static final String TAG = "UserLibraryLine";

    private int lineId;
    private String lineText; //e.g. quote itself or similar
    private UserLibrary userLibrary; //for relationship

    private static List<UserLibraryLine> allLibraryLines = new ArrayList<>();


    //GETTER/SETTER ----------------------------------
    public static List<UserLibraryLine> getAllLibraryLines() {
        return allLibraryLines;
    }

    public static void setAllLibraryLines(List<UserLibraryLine> allLibraryLines) {
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
