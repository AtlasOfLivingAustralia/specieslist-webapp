package au.org.ala.specieslist

/*
 * Copyright (C) 2012 Atlas of Living Australia
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

public enum ListType {

    SPECIES_CHARACTERS("Species characters list"),
    CONSERVATION_LIST("Conservation list"),
    SENSITIVE_LIST("Sensitive list of species"),
    LOCAL_LIST("Local checklist"),
    COMMON_TRAIT("Common trait of species"),
    COMMON_HABITAT("Common habitat of species"),
    SPATIAL_PORTAL("Spatial portal defined list"),
    PROFILE("Profile list"),
    TEST("Test list"),
    OTHER("Other")


  String displayValue
  ListType(String displayValue){

     this.displayValue = displayValue
  }

}