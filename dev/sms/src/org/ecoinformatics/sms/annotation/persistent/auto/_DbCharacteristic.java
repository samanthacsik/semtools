package org.ecoinformatics.sms.annotation.persistent.auto;

import org.apache.cayenne.CayenneDataObject;

/**
 * Class _DbCharacteristic was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _DbCharacteristic extends CayenneDataObject {

    public static final String TYPE_PROPERTY = "type";

    public static final String ID_PK_COLUMN = "id";

    public void setType(String type) {
        writeProperty("type", type);
    }
    public String getType() {
        return (String)readProperty("type");
    }

}