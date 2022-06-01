/*
 * Copyright (C) 2022 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

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
