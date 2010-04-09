package org.ecoinformatics.sms.annotation.persistent.auto;

import org.apache.cayenne.CayenneDataObject;
import org.ecoinformatics.sms.annotation.persistent.DbObservation;

/**
 * Class _DbContext was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _DbContext extends CayenneDataObject {

    public static final String RELATIONSHIP_PROPERTY = "relationship";
    public static final String OBSERVATION_PROPERTY = "observation";
    public static final String OBSERVATION_B_PROPERTY = "observationB";

    public static final String ID_PK_COLUMN = "id";

    public void setRelationship(String relationship) {
        writeProperty("relationship", relationship);
    }
    public String getRelationship() {
        return (String)readProperty("relationship");
    }

    public void setObservation(DbObservation observation) {
        setToOneTarget("observation", observation, true);
    }

    public DbObservation getObservation() {
        return (DbObservation)readProperty("observation");
    }


    public void setObservationB(DbObservation observationB) {
        setToOneTarget("observationB", observationB, true);
    }

    public DbObservation getObservationB() {
        return (DbObservation)readProperty("observationB");
    }


}
