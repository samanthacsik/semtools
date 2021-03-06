package org.ecoinformatics.sms.annotation.persistent.auto;

import org.apache.cayenne.CayenneDataObject;
import org.ecoinformatics.sms.annotation.persistent.DbMeasurement;

/**
 * Class _DbCharacteristic was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _DbCharacteristic extends CayenneDataObject {

    public static final String TYPE_PROPERTY = "type";
    public static final String MEASUREMENT_PROPERTY = "measurement";

    public static final String ID_PK_COLUMN = "id";

    public void setType(String type) {
        writeProperty("type", type);
    }
    public String getType() {
        return (String)readProperty("type");
    }

    public void setMeasurement(DbMeasurement measurement) {
        setToOneTarget("measurement", measurement, true);
    }

    public DbMeasurement getMeasurement() {
        return (DbMeasurement)readProperty("measurement");
    }


}
