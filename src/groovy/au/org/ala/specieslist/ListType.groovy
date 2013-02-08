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

    SPECIES_CHARACTERS("list of species with characters"),
    CONSERVATION_LIST("conservation list"),
    SENSITIVE_LIST("sensitive species list"),
    LOCAL_LIST("checklist for local"),
    COMMON_TRAIT("species with common trait"),
    COMMON_HABITAT("species with common habitat"),
    SPATIAL_PORTAL("A list that has been defined from the spatial portal"),
    TEST("test list"),
    OTHER("other")


  String displayValue
  ListType(String displayValue){

     this.displayValue = displayValue
     //println(this.values())
  }

}