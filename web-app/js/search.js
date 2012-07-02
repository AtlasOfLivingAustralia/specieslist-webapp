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
 *//**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 29/06/12
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
$(document).ready(function() {
    // set the search input to the current q param value
    var query = SEARCH_CONF.query;
    if (query) {
        $(":input#search-2011").val(query);
    }

    // listeners for sort widgets
    $("select#sort").change(function() {
        var val = $("option:selected", this).val();
        reloadWithParam('sort',val);
    });
    $("select#dir").change(function() {
        var val = $("option:selected", this).val();
        reloadWithParam('dir',val);
    });
    $("select#per-page").change(function() {
        var val = $("option:selected", this).val();
        reloadWithParam('pageSize',val);
    });

    // AJAX search results
    injectBhlResults();
    injectBiocacheResults();
});

/**
 * Build URL params to remove selected fq
 *
 * @param facet
 */
function removeFacet(facet) {
    var q = $.getQueryParam('q') ? $.getQueryParam('q') : SEARCH_CONF.query ; //$.query.get('q')[0];
    var fqList = $.getQueryParam('fq'); //$.query.get('fq');
    var paramList = [];

    //is this a init search?
    if(fqList == null || fqList == 'undefined'){
        if('australian_s:recorded' == facet){
            fqList = ['australian_s:recorded'];
        }
        else{
            fqList = [''];
        }
    }
    if (q != null) {
        paramList.push("q=" + q);
    }

    //alert("this.facet = "+facet+"; fqList = "+fqList.join('|'));

    if (fqList instanceof Array && fqList.length > 1) {
        //alert("fqList is an array");
        for (var i in fqList) {
            //alert("i == "+i+" | fq = "+ decodeURIComponent(fqList[i]));
            var decodedFq = decodeURIComponent(fqList[i]);
            if (decodedFq == facet) {
                //alert("removing fq: "+fqList[i]);
                fqList.splice(fqList.indexOf(fqList[i]),1);

            }
        }
    } else {
        var decodedFq = decodeURIComponent(fqList);
        //alert("fqList is NOT an array: '" + decodedFq + "' vs '" + facet + "'");
        if (decodedFq == facet) {
            //alert("match");
            fqList = null;
        }
    }
    //alert("(post) fqList = "+fqList.join('|'));
    if (fqList != null && fqList.length > 0) {
        paramList.push("fq=" + fqList.join("&fq="));
        //alert("pushing fq back on: "+fqList);
    } else {
        // empty fq so redirect doesn't happen
        paramList.push("fq=");
    }
    //alert("new URL: " + window.location.pathname + '?' + paramList.join('&'));
    window.location.href = window.location.pathname + '?' + paramList.join('&');
}

/**
 * Catch sort drop-down and build GET URL manually
 */
function reloadWithParam(paramName, paramValue) {
    var paramList = [];
    var q = $.getQueryParam('q') ? $.getQueryParam('q') : SEARCH_CONF.query ;
    var fqList = $.getQueryParam('fq'); //$.query.get('fq');
    var sort = $.getQueryParam('sort');
    var dir = $.getQueryParam('dir');
    // add query param
    if (q != null) {
        paramList.push("q=" + q);
    }
    // add filter query param
    if (fqList != null) {
        paramList.push("fq=" + fqList.join("&fq="));
    }
    // add sort param if already set
    if (paramName != 'sort' && sort != null) {
        paramList.push('sort' + "=" + sort);
    }

    if (paramName != null && paramValue != null) {
        paramList.push(paramName + "=" +paramValue);
    }

    //alert("params = "+paramList.join("&"));
    //alert("url = "+window.location.pathname);
    window.location.href = window.location.pathname + '?' + paramList.join('&');
}

// jQuery getQueryParam Plugin 1.0.0 (20100429)
// By John Terenzio | http://plugins.jquery.com/project/getqueryparam | MIT License
// Adapted by Nick dos Remedios to handle multiple params with same name - return a list
(function ($) {
    // jQuery method, this will work like PHP's $_GET[]
    $.getQueryParam = function (param) {
        // get the pairs of params fist
        var pairs = location.search.substring(1).split('&');
        var values = [];
        // now iterate each pair
        for (var i = 0; i < pairs.length; i++) {
            var params = pairs[i].split('=');
            if (params[0] == param) {
                // if the param doesn't have a value, like ?photos&videos, then return an empty srting
                //return params[1] || '';
                values.push(params[1]);
            }
        }

        if (values.length > 0) {
            return values;
        } else {
            //otherwise return undefined to signify that the param does not exist
            return undefined;
        }

    };
})(jQuery);

/**
 * Taken from http://stackoverflow.com/a/8764051/249327
 * @param name
 * @return {String}
 */
function getURLParameter(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search)||[,""])[1].replace(/\+/g, '%20'))||null;
}

function numberWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function injectBhlResults() {
    var url = SEARCH_CONF.bhlUrl + "/select?q={!lucene q.op=AND}" + SEARCH_CONF.query + "&start=0&rows=0" +
        "&wt=json&fl=name%2CpageId%2CitemId%2Cscore&hl=on&hl.fl=text&hl.fragsize=200&" +
        "group=true&group.field=itemId&group.limit=7&group.ngroups=true&taxa=false";

    $.ajax({
        url: url,
        dataType: 'jsonp',
        jsonp: "json.wrf",
        success:  function(data) {
            var maxItems = parseInt(data.grouped.itemId.ngroups, 10);
            var url = SEARCH_CONF.serverName + "/bhl-search?q=" + SEARCH_CONF.query;
            var html = "<li data-count=\"" + maxItems + "\"><a href=\"" + url + "\" id=\"bhlSearchLink\">BHL Literature</a> [" + numberWithCommas(maxItems) + "]</li>";
            insertSearchLinks(html);
        }
    });
}

function injectBiocacheResults() {
    var url = SEARCH_CONF.biocacheUrl + "/ws/occurrences/search.json?q=" + SEARCH_CONF.query + "&start=0&pageSize=0&facet=off";

    $.ajax({
        url: url,
        dataType: 'jsonp',
        success:  function(data) {
            var maxItems = parseInt(data.totalRecords, 10);
            var url = SEARCH_CONF.biocacheUrl + "/occurrences/search?q=" + SEARCH_CONF.query;
            var html = "<li data-count=\"" + maxItems + "\"><a href=\"" + url + "\" id=\"biocacheSearchLink\">Occurrence Records</a> [" + numberWithCommas(maxItems) + "]</li>";
            insertSearchLinks(html);
        }
    });
}

function insertSearchLinks(html) {
    // check if the "related searches" facet section exists
    if (!$("div#facet-extSearch").length) {
        // if not, create it
        var h2 = "<h3>Related Searches</h3>";
        var div = "<div class='subnavlist' id='facet-extSearch'><ul></ul></div>";

        if ($("#currentFilters").length) {
            $("#currentFilters").next("h3").before(h2 + div);
        } else {
            $("#accordion").prepend(h2 + div);
        }


    }
    // add content
    $("#facet-extSearch ul").append(html);
    // sort by count
    $('#facet-extSearch ul li').sortElements(function(a, b){
        return $(a).data("count") < $(b).data("count") ? 1 : -1;
    });
}