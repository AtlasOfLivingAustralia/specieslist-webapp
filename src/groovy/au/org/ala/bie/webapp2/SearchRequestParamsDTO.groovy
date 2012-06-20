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

package au.org.ala.bie.webapp2

/**
 * DTO to pass search request params between classes
 * 
 * @author Nick dos Remedios (nick.dosremedios@csiro.au)
 */
class SearchRequestParamsDTO {
    def q
    def fq
    def start
    def pageSize
    def sort
    def dir

    SearchRequestParamsDTO(q, fq, start, pageSize, sort, dir) {
        this.q = q
        this.fq = fq
        this.start = start
        this.pageSize = pageSize
        this.sort = sort
        this.dir = dir
    }

    def getQueryString() {
        def queryStr = new StringBuilder()
        queryStr.append("q=" + q)
        def fqIsList = fq.getClass().metaClass.getMetaMethod("join", String)
        if (fq && fqIsList) {
            def newFq = fq.collect { it.replaceAll(/\s+/, "+") }
            queryStr.append("&fq=" + newFq?.join("&fq="))
        } else if (fq) {
            queryStr.append("&fq=" + fq.replaceAll(" ", "+"))
        }
        queryStr.append("&start=" + start)
        queryStr.append("&pageSize=" + pageSize)
        queryStr.append("&sort=" + sort)
        queryStr.append("&dir=" + dir)
        return queryStr.toString()
    }

    public String toString() {
        return getQueryString()
    }
}
