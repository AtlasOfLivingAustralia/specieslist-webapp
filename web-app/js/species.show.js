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

/**
 *
 * Javascript for species show page.
 *
 * User: nick
 * Date: 12/06/12
 * Time: 2:15 PM
 */

$(document).ready(function() {
    //setup tabs
    var bhlInit = false;
    $("ul.tabs").tabs("div.tabs-panes-noborder > section", {
        history: true,
        effect: 'fade',
        onClick: function(event, index) {
            if (index == 5 && !bhlInit) {
                doSearch(0, 10, false);
                bhlInit = true;
            }
        }
    });
    // Gallery image popups using ColorBox
    $("a.thumbImage").colorbox({
        title: function() {
            var titleBits = this.title.split("|");
            return "<a href='"+titleBits[1]+"'>"+titleBits[0]+"</a>"; },
        opacity: 0.5,
        maxWidth: "80%",
        maxHeight: "80%",
        preloading: false,
        onComplete: function() {
            $("#cboxTitle").html(""); // Clear default title div
            var index = $(this).attr('id').replace("thumb",""); // get the imdex of this image
            var titleHtml = $("div#thumbDiv"+index).html(); // use index to load meta data
            //console.log("index", index, "titleHtml", titleHtml);
            $("<div id='titleText'>"+titleHtml+"</div>").insertAfter("img.cboxPhoto");
            $("div#titleText").css("padding-top","8px");
            var cbox = $.fn.colorbox;
            if ( cbox != undefined){
                cbox.resize();
            } else{
                console.log("cboxis undefined 0: " + cbox);
            }
        }
    });

    // LSID link to show popup with LSID info and links
    $("a#lsid").fancybox({
        closeClick : false,
        helpers:  {
            title:  null
        },
        width: '70%',
        height: '70%',
        maxWidth: 640,
        fitToView: false
    });

    // mapping for facet names to display labels
    var facetLabels = {
        state: "State &amp; Territory",
        data_resource: "Dataset",
        month: "Date (by month)",
        occurrence_year: "Date (by decade)"
    };
    var months = {
        "01": "January",
        "02": "February",
        "03": "March",
        "04": "April",
        "05": "May",
        "06": "June",
        "07": "July",
        "08": "August",
        "09": "September",
        "10": "October",
        "11": "November",
        "12": "December"
    };

    // load the collections that contain specimens
    var colSpecUrl = SHOW_CONF.biocacheUrl + "/ws/occurrences/taxon/source/" + SHOW_CONF.guid + ".json?fq=basis_of_record:PreservedSpecimen&callback=?";
    $.getJSON(colSpecUrl, function(data) {
        if (data != null &&data != null && data.length >0){
            var content = '<h4>Collections that hold specimens: </h4>';
            content = content +'<ul>';
            $.each(data, function(i, li) {
                if(li.uid.match("^co")=="co"){
                    var link1 = '<a href="'+ SHOW_CONF.collectoryUrl + '/public/show/' + li.uid +'">' + li.name + '</a>';
                    var link2 = '(<a href="' + SHOW_CONF.biocacheUrl + '/occurrences/taxa/' + SHOW_CONF.guid + '?fq=collection_uid:'
                    link2 = link2 + li.uid +'&fq=basis_of_record:PreservedSpecimen">' + li.count + ' records</a>)';
                    content = content+'<li>' + link1 + ' ' + link2+'</li>';

                }
            });
            content = content + '</ul>';
            $('#recordBreakdowns').append(content);
        }
    });

    // load occurrence breakdowns for states
    var biocachUrl = SHOW_CONF.biocacheUrl + "/ws/occurrences/taxon/" + SHOW_CONF.guid + ".json?callback=?";
    $.getJSON(biocachUrl, function(data) {
        if (data.totalRecords != null && data.totalRecords > 0) {
            //alert("hi "+data.totalRecords);
            var count = data.totalRecords + ""; // concat of emtyp string forces var to a String
            $('#occurenceCount').html(count.replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,")); // update link text at top with count (formatted)
            //console.log('facets: ', data.facetResults);
            var facets = data.facetResults;
            $.each(facets, function(index, facet) {
                //console.log(node.fieldName, node.fieldResult);
                //if (node.fieldName == 'state' || node.fieldName == 'state' ||node.fieldName == 'state') {
                if (facet.fieldName in facetLabels) {
                    // dataTable for chart
                    var data = new google.visualization.DataTable();
                    var chart;
                    data.addColumn('string', facetLabels[facet.fieldName]);
                    data.addColumn('number', 'Records');
                    // HTML content
                    var isoDateSuffix = '-01-01T00:00:00Z';
                    var content = '<h4 style="margin-top:10px;">By '+ facetLabels[facet.fieldName] +'</h4>';
                    content = content +'<ul>';
                    // intermediate arrays to store facet values in - needed to handle the
                    // irregular date facet "before 1850" which comes at end of facet list
                    var rows = [];
                    var listItems = [];
                    var totalCount = 0;
                    $.each(facet.fieldResult, function(i, li) {
                        if (li.count > 0) {
                            totalCount += li.count; // keep a tally of total counts
                            var label = li.label;
                            var toValue;
                            var displayCount = (li.count + "").replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,");
                            var link = '<a href="' + SHOW_CONF.biocacheUrl + '/occurrences/taxa/' + SHOW_CONF.guid + '?fq='

                            if (facet.fieldName == 'occurrence_year') {
                                if (label == 'before') { // label.indexOf(searchValue, fromIndex)
                                    label = label + ' 1850';
                                    toValue = '1850' + isoDateSuffix;
                                    link = link + facet.fieldName+':[* TO '+toValue+']">';
                                } else {
                                    label = label.replace(isoDateSuffix, '');
                                    toValue = parseInt(label) + 10;
                                    label = label + '-' + toValue;
                                    toValue = toValue + isoDateSuffix;
                                    link = link + facet.fieldName+':['+li.label+' TO '+toValue+']">';
                                }
                            } else if (facet.fieldName == 'month') {
                                link = link + facet.fieldName+':'+li.label+'">';
                                label = months[label]; // substitiute month name for int value
                            } else {
                                link = link + facet.fieldName+':'+li.label+'">';
                            }
                            //content = content +'<li>'+label+': ' + link + displayCount + ' records</a></li>';
                            // add values to chart
                            //data.addRow([label, li.count]);
                            if (label == 'before 1850') {
                                // add to start of array
                                rows.unshift([label, li.count]);
                                listItems.unshift('<li>'+label+': ' + link + displayCount + ' records</a></li>');
                            } else {
                                // add to end of array
                                rows.push([label, li.count]);
                                listItems.push('<li>'+label+': ' + link + displayCount + ' records</a></li>');
                            }
                        }
                    });

                    // some date facets are all empty and this causes a JS error message, so recordCount checks for this
                    if (totalCount > 0) {
                        $.each(rows, function(i, row) {
                            // add to Google data table
                            data.addRow([ row[0], row[1] ]);
                        });
                        $.each(listItems, function(i, li) {
                            // build content string
                            content = content + li;
                        });
                        content = content + '</ul><div id="'+facet.fieldName+'_chart_div" style="margin: -10px;"></div>';
                        $('#recordBreakdowns').append(content);

                        if (facet.fieldName == 'occurrence_date' || facet.fieldName == 'month') {
                            var dateLabel = (facet.fieldName == 'occurrence_date') ? 'Decade' : 'Month';
                            chart = new google.visualization.BarChart(document.getElementById(facet.fieldName+'_chart_div'));
                            chart.draw(data, {width: 630, height: 300, legend: 'none', vAxis: {title: dateLabel}, hAxis: {title: 'Count'}});
                        } else {
                            chart = new google.visualization.PieChart(document.getElementById(facet.fieldName+'_chart_div'));
                            chart.draw(data, {width: 630, height: 300, legend: 'left'});
                        }
                    }
                }
            });
        } else {
            // hide the occurrence record section if no data or biocache is offline
            $('#occurrenceRecords').html("No records found");
        }
    });

    // alerts button
    $("#alertsButton").click(function(e) {
        e.preventDefault();
        var query = "Species: " + SHOW_CONF.scientificName;
        var searchString = "?q=" + SHOW_CONF.guid;
        //console.log("fqueries",fqueries, query);
        var url = SHOW_CONF.alertsUrl + "createBiocacheNewRecordsAlert?";
        url += "queryDisplayName="+encodeURIComponent(query);
        url += "&baseUrlForWS=" + encodeURIComponent(SHOW_CONF.biocacheUrl);
        url += "&baseUrlForUI=" + encodeURIComponent(SHOW_CONF.serverName);
        url += "&webserviceQuery=%2Fws%2Foccurrences%2Fsearch" + encodeURIComponent(searchString);
        url += "&uiQuery=%2Foccurrences%2Fsearch%3Fq%3D*%3A*";
        url += "&resourceName=" + encodeURIComponent("Atlas of Living Australia");
        window.location.href = url;
    });

}); // end document.ready

/**
 * BHL search to populate literature tab
 *
 * @param start
 * @param rows
 * @param scroll
 */
function doSearch(start, rows, scroll) {
    if (!start) {
        start = 0;
    }
    if (!rows) {
        rows = 10;
    }
    // var url = "http://localhost:8080/bhl-ftindex-demo/search/ajaxSearch?q=" + $("#tbSearchTerm").val();
    var taxonName = SHOW_CONF.scientificName ;
    var synonyms = SHOW_CONF.synonymsQuery;
    var query = ""; // = taxonName.split(/\s+/).join(" AND ") + synonyms;
    if (taxonName) {
        var terms = taxonName.split(/\s+/).length;
        if (terms > 2) {
            query += taxonName.split(/\s+/).join(" AND ");
        } else if (terms == 2) {
            query += '"' + taxonName + '"';
        } else {
            query += taxonName;
        }
    }
    if (synonyms) {
        //synonyms = "  " + ((synonyms.indexOf("OR") != -1) ? "(" + synonyms + ")" : synonyms);
        query += (taxonName) ? ' OR ' + synonyms : synonyms;
    }

    if (!query) {
        return cancelSearch("No names were found to search BHL");
    }

    var url = "http://bhlidx.ala.org.au/select?q=" + query + '&start=' + start + "&rows=" + rows +
        "&wt=json&fl=name%2CpageId%2CitemId%2Cscore&hl=on&hl.fl=text&hl.fragsize=200&" +
        "group=true&group.field=itemId&group.limit=7&group.ngroups=true&taxa=false";
    var buf = "";
    $("#status-box").css("display", "block");
    $("#synonyms").html("").css("display", "none")
    $("#results").html("");

    $.ajax({
        url: url,
        dataType: 'jsonp',
        //data: null,
        jsonp: "json.wrf",
        success:  function(data) {
            var itemNumber = parseInt(data.responseHeader.params.start, 10) + 1;
            var maxItems = parseInt(data.grouped.itemId.ngroups, 10);
            if (maxItems == 0) {
                return cancelSearch("No references were found for <code>" + query + "</code>");
            }
            var startItem = parseInt(start, 10);
            var pageSize = parseInt(rows, 10);
            var showingFrom = startItem + 1;
            var showingTo = (startItem + pageSize <= maxItems) ? startItem + pageSize : maxItems ;
            //console.log(startItem, pageSize, showingTo);
            var pageSize = parseInt(rows, 10);
            buf += '<div class="results-summary">Showing ' + showingFrom + " to " + showingTo + " of " + maxItems +
                ' results for the query <code>' + query + '</code>.</div>'
            // grab highlight text and store in map/hash
            var highlights = {};
            $.each(data.highlighting, function(idx, hl) {
                highlights[idx] = hl.text[0];
                //console.log("highlighting", idx, hl);
            });
            //console.log("highlighting", highlights, itemNumber);
            $.each(data.grouped.itemId.groups, function(idx, obj) {
                buf += '<div class="result-box">';
                buf += '<b>' + itemNumber++;
                buf += '.</b> <a target="item" href="http://bhl.ala.org.au/item/' + obj.groupValue + '">' + obj.doclist.docs[0].name + '</a> ';
                var suffix = '';
                if (obj.doclist.numFound > 1) {
                    suffix = 's';
                }
                buf += '(' + obj.doclist.numFound + '</b> matching page' + suffix + ')<div class="thumbnail-container">';

                $.each(obj.doclist.docs, function(idx, page) {
                    var highlightText = $('<div>'+highlights[page.pageId]+'</div>').htmlClean({allowedTags: ["em"]}).html();
                    buf += '<div class="page-thumbnail"><a target="page image" href="http://bhl.ala.org.au/page/' +
                        page.pageId + '"><img src="http://bhl.ala.org.au/pagethumb/' + page.pageId +
                        '" alt="Page Id ' + page.pageId + '"  width="60px" height="100px"/><div class="highlight-context">' +
                        highlightText + '</div></a></div>';
                })
                buf += "</div><!--end .thumbnail-container -->";
                buf += "</div>";
            })

            var prevStart = start - rows;
            var nextStart = start + rows;
            //console.log("nav buttons", prevStart, nextStart);

            buf += '<div id="button-bar">';
            if (prevStart >= 0) buf += '<input type="button" value="Previous page" onclick="doSearch(' + prevStart + ',' + rows + ', true)">';
            buf += '&nbsp;&nbsp;&nbsp;';
            if (nextStart <= maxItems) buf += '<input type="button" value="Next page" onclick="doSearch(' + nextStart + ',' + rows + ', true)">';
            buf += '</div>';

            $("#solr-results").html(buf);
            if (data.synonyms) {
                buf = "<b>Synonyms used:</b>&nbsp;";
                buf += data.synonyms.join(", ");
                $("#synonyms").html(buf).css("display", "block");
            } else {
                $("#synonyms").html("").css("display", "none");
            }
            $("#status-box").css("display", "none");

            if (scroll) {
                $('html, body').animate({scrollTop: '300px'}, 300);
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            $("#status-box").css("display", "none");
            $("#solr-results").html('An error has occurred, probably due to invalid query syntax');
        }
    });
} // end doSearch

function cancelSearch(msg) {
    $("#status-box").css("display", "none");
    $("#solr-results").html(msg);
    return true;
}

/**
 * Taken from bie-webapp (by Wai)
 * TODO needs to be improved to handle failed json GET (controller needs to return status code if failed?)
 *
 * @param guid
 * @param uri
 * @param infosourceId
 * @param documentId
 * @param blackList
 * @param positive
 * @param name
 */
function rankThisImage(guid, uri, infosourceId, documentId, blackList, positive, name){
    var encodedUri = escape(uri);
    var controllerSuffix = (SHOW_CONF.remoteUser) ? "WithUser" : "";
    var url = SHOW_CONF.bieUrl + "/rankTaxonImage" + controllerSuffix + ".json?guid="+guid+"&uri="+encodedUri+"&infosourceId="+infosourceId+"&blackList="+blackList+"&positive="+positive+"&name="+name+"&callback=?";
    $('.imageRank-'+documentId).html('Sending your ranking....');
    $.getJSON(url, function(data){ })
    $('.imageRank-'+documentId).each(function(index) {
        $(this).html('Thanks for your help!');
    }).error(function(jqXHR, textStatus, errorThrown) {
        // catch ajax errors (requires JQuery 1.5+) - usually 500 error
        $(this).html('An error occurred: ' + errorThrown + " (" + jqXHR.status + ")");
    });
}

function editThisImage(guid, uri){
    var encodedUri = escape(uri);
    var url = SHOW_CONF.bieUrl + "/admin/edit?guid="+guid+"&uri="+encodedUri;
    window.open(url);
}

function rankThisCommonName(guid, documentId, blackList, positive, name) {
    var controllerSuffix = (SHOW_CONF.remoteUser) ? "WithUser" : "";
    var url = SHOW_CONF.bieUrl + "/rankTaxonCommonName" + controllerSuffix + ".json?guid="+guid+"&blackList="+blackList+"&positive="+positive+"&name="+name+"&callback=?";
    var linkId = 'cnRank-'+documentId;
    $('#cnRank-'+documentId).html('Sending your ranking....');
    var jqxhr = $.getJSON(url, function(data){
        $('#cnRank-'+documentId).each(function(index) {
            $(this).html('Thanks for your help!');
        });
    }).error(function(jqXHR, textStatus, errorThrown) {
        // catch ajax errors (requires JQuery 1.5+) - usually 500 error
        $('#cnRank-'+documentId).html('An error occurred: ' + errorThrown + " (" + jqXHR.status + ")");
    });
}