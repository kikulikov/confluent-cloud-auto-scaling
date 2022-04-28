package io.confluent.autoscaling.connect;

class VersionUtil {

    public static String getVersion() {
        try {
            return VersionUtil.class.getPackage().getImplementationVersion();
        } catch (Exception ex) {
            return "UNDEFINED";
        }
    }
}
