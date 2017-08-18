package au.org.ala.specieslist

/**
 * Simple bean to bind params and pass to service
 */
class DownloadDto {
    String file
    String email
    String reasonTypeId
    String type


    public String toString() {
        final java.lang.StringBuilder sb = new java.lang.StringBuilder("DownloadDto{");
        sb.append("file='").append(file).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", reasonTypeId='").append(reasonTypeId).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
