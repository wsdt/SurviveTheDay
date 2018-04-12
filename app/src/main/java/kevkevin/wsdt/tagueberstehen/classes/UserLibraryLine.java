package kevkevin.wsdt.tagueberstehen.classes;

public class UserLibraryLine {
    private static final String TAG = "UserLibraryLine";

    private int lineId;
    private String lineText; //e.g. quote itself or similar
    private UserLibrary userLibrary; //for relationship


    //GETTER/SETTER ----------------------------------
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
