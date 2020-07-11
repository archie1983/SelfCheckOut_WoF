package com.example.selfcheckout_wof.data;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A representation of a data row in the table that stores BT devices
 * that we've connected to before. This is so that we can easily re-
 * connect them if they're available upon next startup.
 */
@Entity
public class StoredBTDevices {

    public enum BTDeviceType {
        ZJ_PRINTER(1),
        PPH_CARD_READER(2),
        UNKNOWN(0);

        int deviceTypeCode = 0;

        private BTDeviceType(int deviceTypeCode) {
            this.deviceTypeCode = deviceTypeCode;
        }

        public int getDeviceTypeCode() {
            return deviceTypeCode;
        }

        /**
         * Returns enum member according to the passed code.
         * @param devCode
         * @return
         */
        public static BTDeviceType getDeviceTypeByCode(int devCode) {
            for (BTDeviceType bdt : BTDeviceType.values()) {
                if (bdt.deviceTypeCode == devCode) {
                    return bdt;
                }
            }
            return BTDeviceType.UNKNOWN;
        }
    }

    @PrimaryKey(autoGenerate = true)
    public int dev_id;

    @ColumnInfo(name = "device_addr")
    public String deviceAddr;

    @ColumnInfo(name = "device_name")
    public String deviceName;

    /**
     * ATM we care about two device types - printer or card reader, so while this is
     * an int to be decoded into an Enum, for now there will only be two device types-
     * BT printer and BT PPH card reader.
     */
    @ColumnInfo(name = "dev_type")
    public int devType;

    /**
     * A copy constructor - useful when updating a device record.
     *
     * @param device
     */
    public StoredBTDevices(StoredBTDevices device) {
        this.deviceName = device.deviceName;
        this.deviceAddr = device.deviceAddr;
        this.devType = device.devType;
        this.dev_id = device.dev_id;
    }

    /**
     * Ordinary constructor to discern it from the copy constructor
     */
    public StoredBTDevices() {
    }

    /**
     * A static method for creating a device record.
     *
     * @param deviceName
     * @param deviceAddr
     * @param devType
     * @return
     */
    public static StoredBTDevices createDevice(String deviceName,
                                               String deviceAddr,
                                               BTDeviceType devType
    ) {
        StoredBTDevices d = new StoredBTDevices();
        d.deviceName = deviceName;
        d.deviceAddr = deviceAddr;
        d.devType = devType.getDeviceTypeCode();
        return d;
    }

    /**
     * For the purposes of clarity we have getID() which is equivalent to hashCode()
     * @return
     */
    public int getID() {
        return dev_id;
    }

    public String getDeviceName() {
        return deviceName;
    }
    public String getDeviceAddr() {
        return deviceAddr;
    }

    public BTDeviceType getDeviceType() {
        return BTDeviceType.getDeviceTypeByCode(devType);
    }

    /**
     * The ID coming from DB is already guaranteed unique
     * @return
     */
    @Override
    public int hashCode() {
        return getID();
    }

    /**
     * When comparing two SalesItems objects, the only thing that
     * matters is their ID (at least at the time of writing)
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        SalesItems other = (SalesItems) obj;

        if (dev_id == other.si_id) {
            return true;
        } else {
            return false;
        }
    }
}
