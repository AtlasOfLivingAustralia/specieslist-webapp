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

enum ListType {
    SPECIES_CHARACTERS("Species characters list", "listtype.species.characters.list", "listtooltip.species.characters.list"),
    CONSERVATION_LIST("Conservation list", "listtype.conservation.list", "listtooltip.conservation.list"),
    SENSITIVE_LIST("Sensitive list of species", "listtype.sensitive.list", "listtooltip.sensitive.list"),
    LOCAL_LIST("Area checklist", "listtype.local.checklist", "listtooltip.local.checklist"),
    COMMON_TRAIT("Common trait of species", "listtype.trait.list", "listtooltip.trait.list"),
    COMMON_HABITAT("Common habitat of species", "listtype.habitat.list", "listtooltip.habitat.list"),
    SPATIAL_PORTAL("Spatial portal defined list", "listtype.spatial.portal.list", "listtooltip.spatial.portal.list"),
    PROFILE("Profile list", "listtype.profile.list", "listtooltip.profile.list"),
    TEST("Test list","listtype.test.list", "listtooltip.test.list"),
    OTHER("Other", "listtype.other.list", "listtooltip.other.list")

  String displayValue
  String i18nValue
  String toolTip

  ListType(String displayValue, String i18nValue, String toolTip ){
    this.i18nValue = i18nValue
    this.displayValue = displayValue
    this.toolTip = toolTip
  }
}