package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.greendao_orm;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.Arrays;
import java.util.List;

/** This class is used for converting primitive values (like String lists or similar) to save
 * and retrieve it from greendao orm easily.*/
public class GreenDaoConverter implements PropertyConverter<List<String>, String>{
    private final static String DB_SEPARATOR = "S#+,*"; //unique separator for avoiding problems (so we might have to escape this when user inputs new quotes etc.)

    @Override
    public List<String> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        } else {
            return Arrays.asList(databaseValue.split(DB_SEPARATOR));
        }
    }

    @Override
    public String convertToDatabaseValue(List<String> entityProperty) {
        if (entityProperty == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            for (String entityPropertyLine : entityProperty) {
                sb.append(entityPropertyLine);
                sb.append(DB_SEPARATOR);
            }
            return sb.toString();
        }
    }
}
