/*
 * Copyright (C) 2011 Atlas of Living Australia
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

/*------------------------------------------------------------------------------------------*
 *---------------- FACET BREAKDOWN CHARTS USING THE MODULE PATTERN -------------------------*
 *------------------------------------------------------------------------------------------*/
// define a base object with the major defaults that we can inherit from (prototypically)
var baseFacetChart = {
    collectionsUrl: "http://collections.ala.org.au",
    biocacheServicesUrl: "http://biocache.ala.org.au/ws",
    biocacheWebappUrl: "http://biocache.ala.org.au",
    chartsDiv: null,  // the container for the chart
    chart: null,    // the google chart object
    width: 480,
    height: 350,
    chartArea: {left:0, top:30, width:"90%", height: "70%"},
    is3D: true,
    titleTextStyle: {color: "#555", fontName: 'Arial', fontSize: 15},
    sliceVisibilityThreshold: 0,
    legend: {position: "right"},
    chartType: "pie",
    column1DataType: 'string',
    datasets: 1,
    // defaults for individual facet charts
    individualChartOptions: {
        state_conservation: {chartArea: {left:60, height: "58%"}, title: 'By state conservation status'},
        occurrence_year: {chartArea: {left:60, height: "55%"}, requestFacetName: 'decade'},
        decade: {chartArea: {left:60, height: "55%"}, responseFacetName: 'occurrence_year'},
        year: {width: 600},
        month: {width: 600},
        institution_uid: {chartArea: {left: 0, width: "100%"}},
        collection_uid: {chartArea: {left: 0, width: "100%"}},
        species_group: {title: 'By higher-level group', ignore: ['Animals'], chartType: 'column',
            width: 450, chartArea: {left:60, height:"58%"},
            vAxis: {minValue: 0, textPosition:'in', gridlines:{color: '#ddd', count: 4}},
            colors: ['#108628'], reverseCategories:true, hAxis:{slantedTextAngle:60}},
        state: {ignore: ['Unknown1']},
        type_status: {title: 'By type status (as % of all type specimens)', ignore: ['notatype']},
        el895: {hAxis: {title:'Moisture Index'}},
        el882: {hAxis: {title:'mm'}},
        el889: {hAxis: {title:'mm'}},
        el887: {hAxis: {title:'MJ/m2/day'}},
        el865: {hAxis: {title:'Moisture Index'}},
        el894: {hAxis: {title:'MJ/m2/day'}},
        radiation: {hAxis: {title:'MJ/m2/day'}, chartArea: {width: "65%"}, facets: ['el887','el894'],
            facetLabels: ['seasonality (Bio23)','warmest quarter (Bio26)']},
        precipitation: {hAxis: {title:'mm'}, chartArea: {width: "65%"}, facets: ['el882','el889'],
            facetLabels: ['seasonality (Bio15)','driest quarter (Bio17)']},
        moisture: {hAxis: {title:'Moisture Index'}, chartArea: {width: "65%"}, facets: ['el895','el865'],
            facetLabels: ['lowest period (Bio30)','highest quarter mean (Bio32)']}
    },
    getChartTypeOptions: function (name) {
        if (this.individualChartOptions[name] !== undefined) {
            return this.individualChartOptions[name];
        } else {
            return {};
        }
    },
    // these override the facet names in chart titles
    chartLabels: {
        institution_uid: 'institution',
        data_resource_uid: 'data set',
        assertions: 'data assertion',
        biogeographic_region: 'biogeographic region',
        occurrence_year: 'decade',
        el895: 'Moisture Index - lowest period (Bio30)',
        el882: 'Precipitation - seasonality (Bio15)',
        el889: 'Precipitation - driest quarter (Bio17)',
        el887: 'Radiation - seasonality (Bio23)',
        el865: 'Moisture Index - highest quarter mean (Bio32)',
        el894: 'Radiation - warmest quarter (Bio26)',
        radiation: 'Radiation',
        precipitation: 'Precipitation',
        moisture: 'Moisture'
    },
    // select the properties that need to be passed to the chart library
    googleChartOptions: function() {
        var gChartOptions = {},
            that = this;
        $.each(['width','height','chartArea','is3D','titleTextStyle','sliceVisibilityThreshold','legend',
            'hAxis','vAxis','title','colors','reverseCategories','fontSize','backgroundColor','axisTitlesPosition',
            'fontName','focusTarget','titlePosition','tooltip'], function (i,prop) {
            if (that[prop] != undefined) {
                gChartOptions[prop] = that[prop];
            }
        });
        return gChartOptions;
    },
    name: '',
    chartLabel: function () { return this.chartLabels[this.name] ? this.chartLabels[this.name] : this.name;},
    title: function () { return "By " + this.chartLabel();},
    transformData: function (data) {
        if (this.syncTransforms[this.name]) {
            return this.syncTransforms[this.name].apply(null, [data]);
        }
        return data;
    },
    syncTransforms: {
        occurrence_year: function (data) {
            var firstDecade;
            var transformedData = [];
            $.each(data, function(i,obj) {
                if (obj.label === 'before') {
                    transformedData.splice(0,0,{label: "before " + firstDecade, count: obj.count});
                }
                else {
                    var decade = obj.label.substr(0,4);
                    if (i === 0) { firstDecade = decade; }
                    transformedData.push({label: decade + "s", count: obj.count});
                }
            });
            /*// sort
             transformedData.sort(function (a,b) {
             return b.label - a.label;
             });*/
            return transformedData;
        },
        decade: function (data) {
            var firstDecade;
            var transformedData = [];
            $.each(data, function(i,obj) {
                if (obj.label === 'before') {
                    transformedData.splice(0,0,{label: "before " + firstDecade, count: obj.count});
                }
                else {
                    var decade = obj.label.substr(0,4);
                    if (i === 0) { firstDecade = decade; }
                    transformedData.push({label: decade + "s", count: obj.count});
                }
            });
            /*// sort
             transformedData.sort(function (a,b) {
             return b.label - a.label;
             });*/
            return transformedData;
        },
        year: function (data) {
            var year, firstYear = 3000, lastYear = 0, dataMap = {}, transformedData = [];
            $.each(data, function(i,obj) {
                year = obj.label;
                if (Number(year) < firstYear) { firstYear = Number(year); }
                if (Number(year) > lastYear) { lastYear = Number(year); }
                dataMap[year] = obj.count;
            });
            if (firstYear > lastYear) { return [] }  // no data
            for (y = firstYear; y <= lastYear; y++) {
                transformedData.push({label: "" + y, count: dataMap["" + y] || 0});
            }
            return transformedData;
        }
    },
    formatLabel: function (data) {
        if (this.labelFormatters[this.name]) {
            return this.labelFormatters[this.name].apply(null, [data]);
        }
        return data;
    },
    labelFormatters: {
        month: function (data) {
            var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
                monthIdx;
            $.each(data, function(i,obj) {
                monthIdx = obj.label;
                obj.formattedLabel = months[monthIdx - 1];
            });
            return data;
        }
    },
    transformDataAfter: function (dataTable, opts) {
        var ts = this.asyncTransforms[this.name];
        if (ts) {
            ts.method.apply(null, [this.chart, dataTable, opts, ts.param]);
        }
    },
    asyncTransforms: {
        collection_uid: {method: lookupEntityNameFromUids, param: 'collection'},
        institution_uid: {method: lookupEntityNameFromUids, param: 'institution'},
        data_resource_uid: {method: lookupEntityNameFromUids, param: 'dataResource'}
    },
    setType: function (type) {
        this.chartType = type;
    },
    // create the chart
    init: function (data, options) {
        var name = this.name,
            specificOptions = options[name];

        // add the default title
        this.title = "By " + this.chartLabel();

        // apply chart-specific and user-defined options and user-defined individual chart options
        var opts = $.extend(true, {}, this.getChartTypeOptions(name), options, options[name] || {});
        for (var prop in opts) {
            if (opts.hasOwnProperty(prop)) {
                //alert(typeof opts[prop]);
                if (typeof opts[prop] === 'object' && !$.isArray(opts[prop])) {
                    // deep copy first nested level preserving existing values that are not explicitly overridden
                    for (var p in opts[prop]) {
                        if (opts[prop].hasOwnProperty(p)) {
                            // create the object if it doesn't exist
                            if (this[prop] === undefined) {
                                this[prop] = {};
                            }
                            this[prop][p] = opts[prop][p];
                        }
                    }
                } else {
                    this[prop] = opts[prop];
                }
            }
        }
        this.chartsDiv = $('#' + (this.chartsDiv || 'charts'));

        // preserve context for callback
        var that = this;

        // build the table
        var dataTable = this.buildDataTable(name, data);

        // reject the chart if there is only one facet value (after filtering)
        if (dataTable.getNumberOfRows() < 2) {
            return;
        }

        // create the container
        var $container = $('#' + name);
        if ($container.length == 0) {
            $container = $("<div id='" + name + "'></div>");
            this.chartsDiv.append($container);
        }

        // specify the type (for css tweaking)
        $container.addClass('chart-' + this.chartType);

        // create the chart
        switch (this.chartType) {
            case 'column': this.chart = new google.visualization.ColumnChart($container[0]); break;
            case 'bar': this.chart = new google.visualization.BarChart($container[0]); break;
            case 'line': this.chart = new google.visualization.LineChart($container[0]); break;
            case 'scatter': this.chart = new google.visualization.ScatterChart($container[0]); break;
            default: this.chart = new google.visualization.PieChart($container[0]); break;
        }

        // show the chart state
        this.chart.draw(dataTable, this.googleChartOptions());

        // kick off post-draw asynch actions (clone the opts as these objects are not as independent as they are meant to be)
        this.transformDataAfter(dataTable, $.extend(true, {}, this.googleChartOptions()));

        // setup a click handler - if requested
        if (this.clickThru != false) {  // defaults to true
            google.visualization.events.addListener(this.chart, 'select', function() {

                // default facet value is the name selected
                var id = dataTable.getValue(that.chart.getSelection()[0].row,0);

                // build the facet query
                var facetQuery = name + ":" + id;

                // the facet query can be overridden for date ranges
                if (name == 'occurrence_year' || name == 'decade') {
                    if (id.match("^before") == 'before') { // startWith
                        facetQuery = "occurrence_year:[*%20TO%20" + "1849-12-31T23:59:59Z]";
                    }
                    else {
                        var decade = id.substr(0,4);
                        var dateTo = parseInt(decade) + 9;
                        facetQuery = "occurrence_year:[" + decade + "-01-01T00:00:00Z%20TO%20" +
                                dateTo + "-12-31T23:59:59Z]";
                    }
                }

                // show the records
                document.location = urlConcat(biocacheWebappUrl,"/occurrences/search?q=") + that.query +
                        "&fq=" + facetQuery;
            });
        }
    },
    buildDataTable: function (name, data) {
        this.column1DataType = this.column1 || 'string';

        // preserve context for callback
        var that = this;

        // deduce the fieldName used in the data
        var fieldName = this.getChartTypeOptions(name).responseFacetName || name;

        // optionally transform the data
        var xformedData = this.transformData(data[fieldName]);

        // optionally format the labels
        xformedData = this.formatLabel(xformedData);

        // create the data table
        var dataTable = new google.visualization.DataTable(), dataOptions = this.getChartTypeOptions(name);
        if (dataOptions && dataOptions.facets) {
            // add a y-value column for each facet
            dataTable.addColumn(this.column1DataType, this.chartLabel());
            for (var i = 0; i < dataOptions.facets.length; i++) {
                dataTable.addColumn('number', dataOptions.facetLabels ? dataOptions.facetLabels[i] : this.chartLabel());
            }
        }
        else if (this.column1DataType == 'number') {
            dataTable.addColumn('number', this.chartLabel());
            dataTable.addColumn('number','records');
        } else {
            dataTable.addColumn('string', this.chartLabel());
            dataTable.addColumn('number','records');
        }

        if (dataOptions && dataOptions.facets && dataOptions.facets.length > 1) {
            // munge data from 2 facets TODO: only handles 2 facets here
            var facet1 = data[dataOptions.facets[0]],
                    facet2 = data[dataOptions.facets[1]],
                    xValue,
                    rowIndexes;

            $.each(facet1, function (i,obj) {
                dataTable.addRow([parseFloat(obj.label), obj.count, null]);
            });
            $.each(facet2, function (i,obj) {
                xValue = parseFloat(obj.label);
                // see if this x value already has a row in the table
                rowIndexes = dataTable.getFilteredRows([{column: 1, value: xValue}]);
                if (rowIndexes.length > 0) {
                    // if so just set the second value
                    dataTable.setCell(rowIndexes[0], 3, obj.count);
                } else {
                    // create a new row with null as the first value
                    dataTable.addRow([parseFloat(obj.label), null, obj.count]);
                }
            });

        } else {
            // handle each data item
            $.each(xformedData, function(i,obj) {
                var labelObj, label, formattedLabel, value;
                // filter any crap
                if (that.ignore == undefined || $.inArray(obj.label, that.ignore) == -1) {
                    // set std values
                    label = obj.label;
                    formattedLabel = obj.formattedLabel;
                    value = obj.count;

                    // handle camelCase labels
                    if (detectCamelCase(label)) {
                        formattedLabel = capitalise(expandCamelCase(label));
                    }

                    // handle numeric labels
                    if (that.column1DataType === 'number') {
                        label = parseFloat(label);
                    }

                    // inject formatted labels if available
                    if (formattedLabel !== undefined) {
                        labelObj = {v: label, f: formattedLabel};
                    } else {
                        labelObj = label;
                    }

                    // add the row
                    dataTable.addRow([labelObj, value]);
                }
            });
        }

        return dataTable;
    },
    newChart: function(options) {
        if(typeof Object.create !== 'function'){
            Object.create = function(o){
                var F = function(){};
                F.prototype = o;
                return new F();
            };
        }
        var that = Object.create(this);
        for (var prop in options) {
            if (options.hasOwnProperty(prop)) {
                that[prop] = options[prop];
            }
        }
        return that;
    }

};
// create charts specific for a chart type - by differential inheritance
// NOTE a new object is created and returned each time these are referenced
var facetChartTypes = {
    pie: function () { return baseFacetChart.newChart({chartType: 'pie'}); },
    bar: function () {
        var chartArea = baseFacetChart.chartArea;
        chartArea.left = 170;
        return baseFacetChart.newChart({chartType: 'bar', chartArea: chartArea});
    },
    column: function () {
        var chartArea = baseFacetChart.chartArea;
        chartArea.left = 60;
        chartArea.height = "58%";
        return baseFacetChart.newChart({chartType: 'column', width: 450, chartArea: chartArea,
            hAxis: {slantedText: true}, legend: 'none'});
    },
    scatter: function () {
        var chartArea = baseFacetChart.chartArea;
        chartArea.left = 60;
        chartArea.height = "65%";
        return baseFacetChart.newChart({chartType: 'scatter', vAxis: {title:'count'}, chartArea: chartArea,
            column1: 'number'})
    }
}

var defaultChartTypes = {
    state: 'pie',
    institution: 'pie',
    data_resource_uid: 'pie',
    type_status: 'pie',
    species_group: 'pie',
    assertions: 'bar',
    occurrence_year: 'column',
    decade: 'column',
    year: 'column',
    month: 'column',
    biogeographic_region: 'pie',
    state_conservation: 'column',
    el895: 'scatter',
    el882: 'scatter',
    el887: 'scatter',
    el889: 'scatter',
    el865: 'scatter',
    el894: 'scatter',
    radiation: 'scatter',
    precipitation: 'scatter',
    moisture: 'scatter'
}

// object to handle multiple facets charts from the same data request
var facetChartGroup = {
    // define a constructor for facet charts
    createChart: function (name, options, data) {
        // choose a chart type to instantiate
        var baseChart = (options[name] && options[name].chartType) ?
                options[name].chartType :
                (defaultChartTypes[name] || 'pie');

        var that = facetChartTypes[baseChart]();

        that.name = name;

        that.init(data, options);

        // return the new chart
        return that;
    },
    // this handles substitution of facet names where they are different to the chart name
    // and where charts require more than one facet
    buildFacetsFromChartName: function (name) {
        var facetNames = [name];

        // handle multiple and transformed facets
        var dataOptions = baseFacetChart.individualChartOptions[name];
        if (dataOptions && dataOptions.facets) {
            facetNames = dataOptions.facets;
        }

        return '&facets=' + facetNames.join('&facets=');
    },
    // create a set of facets charts - requesting the data
    loadAndDrawFacetCharts: function (options) {
        // the base url for getting the facet data
        var url = (options.biocacheServicesUrl == undefined) ? baseFacetChart.biocacheServicesUrl : options.biocacheServicesUrl,

                facets = "",

            // calc the target div
                chartsDiv = $('#' + (options.chartsDiv ? options.chartsDiv : baseFacetChart.chartsDiv)),

            // reference to this that we can use in callbacks
                that = this;

        // build facets list
        $.each(options.charts, function(i,name) {
            facets += that.buildFacetsFromChartName(name)
        });

        // show a message while requesting data
        chartsDiv.append($("<span>Loading charts...</span>"));

        // make request
        $.ajax({
            url: urlConcat(url, "/occurrences/search.json?pageSize=0&flimit=200&q=") + options.query + facets,
            dataType: 'jsonp',
            error: function() {
                cleanUp(); // TODO:
            },
            success: function(data) {

                // clear loading message
                chartsDiv.find('span').remove();

                // draw all charts
                that.drawFacetCharts(data, options);

            }
        });
    },
    // create a set of facets charts - given the data
    drawFacetCharts: function (data, options) {
        // check that we have results
        if (data.length == 0 || data.totalRecords == undefined || data.totalRecords == 0) {
            return;
        }

        // update total if requested
        if (options.totalRecordsSelector) {
            $(options.totalRecordsSelector).html(addCommas(data.totalRecords));
        }

        // transform facet results list into map keyed on field name (the facet name in the data)
        var facetMap = {};
        $.each(data.facetResults, function(idx, obj) {
            facetMap[obj.fieldName] = obj.fieldResult;
        });

        // draw the charts
        var chartsDiv = $('#' + (options.targetDivId ? options.targetDivId : 'charts')),
                query = options.query,
                that = this;
        $.each(options.charts, function(index, name) {
            that.createChart(name, options, facetMap);
        });
    }
};

// create a set of facets charts - requesting the data
var loadAndDrawFacetCharts = function (options) {
    // the base url for getting the facet data
    var url = (options.biocacheServicesUrl == undefined) ? baseFacetChart.biocacheServicesUrl : options.biocacheServicesUrl,

        // build the required facet set
            facets = options.charts.join('&facets='),

        // calc the target div
            chartsDiv = $('#' + (options.chartsDiv ? options.chartsDiv : baseFacetChart.chartsDiv));

    // show a message while requesting data
    chartsDiv.append($("<span>Loading charts...</span>"));

    // make request
    $.ajax({
        url: urlConcat(url, "/occurrences/search.json?pageSize=0&q=") + options.query + "&facets=" + facets,
        dataType: 'jsonp',
        error: function() {
            cleanUp(); // TODO:
        },
        success: function(data) {

            // clear loading message
            chartsDiv.find('span').remove();

            // draw all charts
            drawFacetCharts(data, options);

        }
    });
};

//function
/*------------------------- RECORD BREAKDOWN CHARTS ------------------------------*/

/***** external services & links *****/
// an instance of the collections app - used for name lookup services
var collectionsUrl = "http://collections.ala.org.au";  // should be overridden from config by the calling page
// an instance of the biocache web services app - used for facet and taxonomic breakdowns
var biocacheServicesUrl = "http://biocache.ala.org.au/ws";  // should be overridden from config by the calling page
// an instance of a web app - used to display search results
var biocacheWebappUrl = "http://biocache.ala.org.au";  // should be overridden from config by the calling page

// defaults for taxa chart
var taxonomyPieChartOptions = {
    width: 480,
    height: 350,
    chartArea: {left:0, top:30, width:"100%", height: "70%"},
    is3D: true,
    titleTextStyle: {color: "#555", fontName: 'Arial', fontSize: 15},
    sliceVisibilityThreshold: 0,
    legend: "right"
};

// defaults for facet charts
var genericChartOptions = {
    width: 480,
    height: 350,
    chartArea: {left:0, top:30, width:"100%", height: "70%"},
    is3D: true,
    titleTextStyle: {color: "#555", fontName: 'Arial', fontSize: 15},
    sliceVisibilityThreshold: 0,
    legend: "right",
    chartType: "pie"
};

// defaults for individual facet charts
var individualChartOptions = {
    state_conservation: {chartType: 'column', width: 450, chartArea: {left:60, height: "58%"},
        title: 'By state conservation status', hAxis: {slantedText: true}},
    occurrence_year: {chartType: 'column', width: 450, chartArea: {left:60, height: "65%"}, hAxis: {slantedText: true}},
    species_group: {title: 'By higher-level group', ignore: ['Animals']},
    state: {ignore: ['Unknown1']},
    type_status: {title: 'By type status (as % of all type specimens)', ignore: ['notatype']},
    assertions: {chartType: 'bar', chartArea: {left:170}}
};

/*----------------- FACET-BASED CHARTS USING DIRECT CALLS TO BIO-CACHE SERVICES ---------------------*/
// these override the facet names in chart titles
var chartLabels = {
    institution_uid: 'institution',
    data_resource_uid: 'data set',
    assertions: 'data assertion',
    biogeographic_region: 'biogeographic region',
    occurrence_year: 'decade'
}
// asynchronous transforms are applied after the chart is drawn, ie the chart is drawn with the original values
// then redrawn when the ajax call for transform data returns
var asyncTransforms = {
    institution_uid: {method: 'lookupEntityName', param: 'institution'},
    data_resource_uid: {method: 'lookupEntityName', param: 'dataResource'}
}
// synchronous transforms are applied to the json data before the data table is built
var syncTransforms = {
    occurrence_year: {method: 'transformDecadeData'}/*,
     assertions: {method: 'expandCamelCase'}*/
}

/********************************************************************************\
 * Ajax request for charts based on the facets available in the biocache breakdown.
 \********************************************************************************/
function loadFacetCharts(chartOptions) {
    if (chartOptions.collectionsUrl != undefined) { collectionsUrl = chartOptions.collectionsUrl; }
    if (chartOptions.biocacheServicesUrl != undefined) { biocacheServicesUrl = chartOptions.biocacheServicesUrl; }
    if (chartOptions.displayRecordsUrl != undefined) { biocacheWebappUrl = chartOptions.displayRecordsUrl; }

    var chartsDiv = $('#' + (chartOptions.targetDivId ? chartOptions.targetDivId : 'charts'));
    chartsDiv.append($("<span>Loading charts...</span>"));
    var query = chartOptions.query ? chartOptions.query : buildQueryString(chartOptions.instanceUid);
    $.ajax({
        url: urlConcat(biocacheServicesUrl, "/occurrences/search.json?pageSize=0&q=") + query,
        dataType: 'jsonp',
        error: function() {
            cleanUp();
        },
        success: function(data) {

            // clear loading message
            chartsDiv.find('span').remove();

            // draw all charts
            drawFacetCharts(data, chartOptions);

        }
    });
}
function cleanUp(chartOptions) {
    $('img.loading').remove();
    if (chartOptions != undefined && chartOptions.error) {
        window[chartOptions.error]();
    }
}
/*********************************************************************\
 * Loads charts based on the facets declared in the config object.
 * - does not require any markup other than div#charts element
 \*********************************************************************/
function drawFacetCharts(data, chartOptions) {
    // check that we have results
    if (data.length == 0 || data.totalRecords == undefined || data.totalRecords == 0) {
        return;
    }

    // update total if requested
    if (chartOptions.totalRecordsSelector) {
        $(chartOptions.totalRecordsSelector).html(addCommas(data.totalRecords));
    }

    // transform facet results into map
    var facetMap = {};
    $.each(data.facetResults, function(idx, obj) {
        facetMap[obj.fieldName] = obj.fieldResult;
    });

    // draw the charts
    var chartsDiv = $('#' + (chartOptions.targetDivId ? chartOptions.targetDivId : 'charts'));
    var query = chartOptions.query ? chartOptions.query : buildQueryString(chartOptions.instanceUid);
    $.each(chartOptions.charts, function(index, name) {
        if (facetMap[name] != undefined) {
            buildGenericFacetChart(name, facetMap[name], query, chartsDiv, chartOptions);
        }
    });
}
/************************************************************\
 * Create and show a generic facet chart
 \************************************************************/
function buildGenericFacetChart(name, data, query, chartsDiv, chartOptions) {

    // resolve chart label
    var chartLabel = chartLabels[name] ? chartLabels[name] : name;

    // resolve the chart options
    var opts = $.extend({}, genericChartOptions);
    opts.title = "By " + chartLabel;  // default title
    var individualOptions = individualChartOptions[name] ? individualChartOptions[name] : {};
    // merge generic, individual and user options
    opts = $.extend(true, {}, opts, individualOptions, chartOptions[name]);
    //Dumper.alert(opts);

    // optionally transform the data
    var xformedData = data;
    if (syncTransforms[name]) {
        xformedData = window[syncTransforms[name].method](data);
    }

    // create the data table
    var dataTable = new google.visualization.DataTable();
    dataTable.addColumn('string', chartLabel);
    dataTable.addColumn('number','records');
    $.each(xformedData, function(i,obj) {
        // filter any crap
        if (opts == undefined || opts.ignore == undefined || $.inArray(obj.label, opts.ignore) == -1) {
            if (detectCamelCase(obj.label)) {
                dataTable.addRow([{v: obj.label, f: capitalise(expandCamelCase(obj.label))}, obj.count]);
            }
            else {
                dataTable.addRow([obj.label, obj.count]);
            }
        }
    });

    // reject the chart if there is only one facet value (after filtering)
    if (dataTable.getNumberOfRows() < 2) {
        return;
    }

    // create the container
    var $container = $('#' + name);
    if ($container.length == 0) {
        $container = $("<div id='" + name + "'></div>");
        chartsDiv.append($container);
    }

    // specify the type (for css tweaking)
    $container.addClass('chart-' + opts.chartType);

    // create the chart
    var chart;
    switch (opts.chartType) {
        case 'column': chart = new google.visualization.ColumnChart(document.getElementById(name)); break;
        case 'bar': chart = new google.visualization.BarChart(document.getElementById(name)); break;
        default: chart = new google.visualization.PieChart(document.getElementById(name)); break;
    }

    chart.draw(dataTable, opts);

    // kick off post-draw asynch actions
    if (asyncTransforms[name]) {
        window[asyncTransforms[name].method](chart, dataTable, opts, asyncTransforms[name].param);
    }

    // setup a click handler - if requested
    if (chartOptions.clickThru != false) {  // defaults to true
        google.visualization.events.addListener(chart, 'select', function() {

            // default facet value is the name selected
            var id = dataTable.getValue(chart.getSelection()[0].row,0);

            // build the facet query
            var facetQuery = name + ":" + id;

            // the facet query can be overridden for date ranges
            if (name == 'occurrence_year') {
                if (id.match("^before") == 'before') { // startWith
                    facetQuery = "occurrence_year:[*%20TO%20" + "1850" + "-01-01T12:00:00Z]";
                }
                else {
                    var decade = id.substr(0,4);
                    var dateTo = parseInt(decade) + 10;
                    facetQuery = "occurrence_year:[" + decade + "-01-01T12:00:00Z%20TO%20" + dateTo + "-01-01T12:00:00Z]";
                }
            }

            // show the records
            document.location = urlConcat(biocacheWebappUrl,"/occurrences/search?q=") + query +
                    "&fq=" + facetQuery;
        });
    }
}

/*---------------------- DATA TRANSFORMATION METHODS ----------------------*/
function transformDecadeData(data) {
    var firstDecade;
    var transformedData = [];
    $.each(data, function(i,obj) {
        if (obj.label == 'before') {
            transformedData.splice(0,0,{label: "before " + firstDecade, count: obj.count});
        }
        else {
            var decade = obj.label.substr(0,4);
            if (i == 0) { firstDecade = decade; }
            transformedData.push({label: decade + "s", count: obj.count});
        }
    });
    return transformedData;
}
/*--------------------- LABEL TRANSFORMATION METHODS ----------------------*/
function detectCamelCase(name) {
    return /[a-z][A-Z]/.test(name);
}
function expandCamelCase(name) {
    return name.replace(/([a-z])([A-Z])/g, function(s, str1, str2){return str1 + " " + str2.toLowerCase();});
}
/* capitalises the first letter of the passed string */
function capitalise(item) {
    return item.replace(/^./, function(str){ return str.toUpperCase(); })
}
function lookupEntityName(chart, table, opts, entity) {
    var uidList = [];
    for (var i = 0; j = table.getNumberOfRows(), i < j; i++) {
        uidList.push(table.getValue(i,0));
    }
    $.jsonp({
        url: collectionsUrl + "/ws/resolveNames/" + uidList.join(',') + "?callback=?",
        cache: true,
        success: function(data) {
            for (var i = 0;j + table.getNumberOfRows(), i < j; i++) {
                var uid = table.getValue(i,0);
                table.setCell(i, 0, uid, data[uid]);
            }
            chart.draw(table, opts);
        },
        error: function(d,msg) {
            alert(msg);
        }
    });
}
// does name lookup from uids and transforms data table
function lookupEntityNameFromUids (chart, table, opts) {
    var uidList = [];
    for (var i = 0; j = table.getNumberOfRows(), i < j; i++) {
        uidList.push(table.getValue(i,0));
    }
    $.jsonp({
        url: collectionsUrl + "/ws/resolveNames/" + uidList.join(',') + "?callback=?",
        cache: true,
        success: function(data) {
            for (var i = 0;j + table.getNumberOfRows(), i < j; i++) {
                var uid = table.getValue(i,0);
                table.setCell(i, 0, uid, data[uid]);
            }
            chart.draw(table, opts);
        },
        error: function(d,msg) {
            alert(msg);
        }
    });
}


/*------------------------------------------------------------------------------------------*
 *----------- TAXONOMY BREAKDOWN CHARTS USING DIRECT CALLS TO BIO-CACHE SERVICES -----------*
 *------------------------------------------------------------------------------------------*/
// works for uid-based queries or q/fq general queries

var taxonomyChart = {
    // the base query that defines the full set of records being analysed
    baseQuery: "",
    // the active query - base plus any non-taxonomic restrictions such as date range
    query: "",
    // the rank of the current subset being displayed
    rank: undefined,
    // the name of the current subset being displayed
    name: undefined,
    // threshold - used when no rank+name given
    threshold: undefined,
    // chart configuration
    chartOptions: {},
    // history of chart state
    historyState: [],
    hasState: function () { return this.historyState.length > 0; },
    pushState: function () {
        this.historyState.push({rank:this.rank, name:this.name});
    },
    popState: function () {
        return this.hasState() ? this.historyState.pop() : {};
    },
    cleanUp: function () {
        $('img.loading').remove();
        if (this.chartOptions != undefined && this.chartOptions.error) {
            window[this.chartOptions.error]();
        }
    },
    // loads a new chart with the passed configuration
    load: function (chartOptions) {
        var thisChart = this;

        if (chartOptions) {
            this.chartOptions = chartOptions;

            if (chartOptions.collectionsUrl != undefined) { collectionsUrl = chartOptions.collectionsUrl; }
            if (chartOptions.biocacheServicesUrl != undefined) { biocacheServicesUrl = chartOptions.biocacheServicesUrl; }
            if (chartOptions.displayRecordsUrl != undefined) { biocacheWebappUrl = chartOptions.displayRecordsUrl; }

            this.baseQuery = chartOptions.query ? chartOptions.query : buildQueryString(chartOptions.instanceUid);
            this.query = this.baseQuery + (chartOptions.subquery ? chartOptions.subquery : '');

            this.rank = chartOptions.rank;
            this.name = chartOptions.name;
            this.threshold = chartOptions.threshold;
        }

        var url = urlConcat(biocacheServicesUrl, "/breakdown.json?q=") + this.query;

        // add url params to set state
        if (this.rank) {
            url += "&rank=" + this.rank + (this.name ? "&name=" + this.name: "");
        }
        else {
            url += "&max=" + (this.threshold ? this.threshold : '55');
        }

        $.ajax({
            url: url,
            dataType: 'jsonp',
            timeout: 30000,
            complete: function(jqXHR, textStatus) {
                if (textStatus == 'timeout') {
                    alert('Sorry - the request was taking too long so it has been cancelled.');
                }
                if (textStatus == 'error') {
                    alert('Sorry - the chart cannot be redrawn due to an error.');
                }
                if (textStatus != 'success') {
                    thisChart.cleanUp();
                }
            },
            success: function(data) {
                // check for errors
                if (data != undefined && data.taxa.length > 0) {
                    // draw the chart
                    thisChart.draw(data);
                }
            }
        });
    },
    draw: function (data) {
        var thisChart = this;

        // create the data table
        var dataTable = new google.visualization.DataTable();
        dataTable.addColumn('string', chartLabels[name] ? chartLabels[name] : name);
        dataTable.addColumn('number','records');
        $.each(data.taxa, function(i,obj) {
            dataTable.addRow([obj.label, obj.count]);
        });

        // resolve the chart options
        var opts = $.extend({}, taxonomyPieChartOptions);
        opts = $.extend(true, opts, this.chartOptions);
        opts.title = opts.name ? opts.name + " records by " + data.rank : "By " + data.rank;

        // create the outer div that will contain the chart and the additional links
        var $outerContainer = $('#taxa');
        if ($outerContainer.length == 0) {
            $outerContainer = $('<div id="taxa"></div>'); // create it
            $outerContainer.css('margin-bottom','-50px');
            var chartsDiv = $('div#' + (this.chartOptions.targetDivId ? this.chartOptions.targetDivId : 'charts'));
            // append it
            chartsDiv.prepend($outerContainer);
        }

        // create the chart container if not already there
        var $container = $('#taxaChart');
        if ($container.length == 0) {
            $container = $("<div id='taxaChart' class='chart-pie'></div>");
            $outerContainer.append($container);
        }

        // create the chart
        var chart = new google.visualization.PieChart(document.getElementById('taxaChart'));

        // notify any listeners
        if (this.chartOptions.notifyChange) {
            window[this.chartOptions.notifyChange](this.rank, this.name);
        }

        // draw the chart
        chart.draw(dataTable, opts);

        // draw the back button / instructions
        var $backLink = $('#backLink');
        if ($backLink.length == 0) {
            $backLink = $('<div class="link" id="backLink">&laquo; Previous rank</div>').appendTo($outerContainer);  // create it
            $backLink.css('position','relative').css('top','-75px');
            $backLink.click(function() {
                // only act if link was real
                if (!$backLink.hasClass('link')) return;

                // show spinner while loading
                $container.append($('<img class="loading" style="position:absolute;left:130px;top:220px;z-index:2000" ' +
                        'alt="loading..." src="' + collectionsUrl + '/images/ala/ajax-loader.gif"/>'));

                // get state from history
                var previous = thisChart.popState();

                // set new chart state
                taxonomyChart.rank = previous.rank;
                taxonomyChart.name = previous.name;

                // redraw chart
                thisChart.load();
            });
        }
        if (this.hasState()) {
            // show the prev link
            $backLink.html("&laquo; Previous rank").addClass('link');
        }
        else {
            // show the instruction
            $backLink.html("Click a slice to drill into the next taxonomic level.").removeClass('link');
        }

        // draw records link
        var $recordsLink = $('#recordsLink');
        if ($recordsLink.length == 0) {
            $recordsLink = $('<div class="link under" id="recordsLink">View records</div>').appendTo($outerContainer);  // create it
            $recordsLink.css('position','relative').css('top','-75px');
            $recordsLink.click(function () {
                thisChart.showRecords();  // called explicitly so we have the correct 'this' context
            });
        }

        // set link text
        if (this.hasState()) {
            $recordsLink.html('View records for ' + this.rank + ' ' + this.name);
        }
        else {
            $recordsLink.html('View all records');
        }

        // setup a click handler - if requested
        var clickThru = this.chartOptions.clickThru == undefined ? true : this.chartOptions.clickThru;  // default to true
        var drillDown = this.chartOptions.drillDown == undefined ? true : this.chartOptions.drillDown;  // default to true
        if (clickThru || drillDown) {
            google.visualization.events.addListener(chart, 'select', function() {

                // find out what they clicked
                var name = dataTable.getValue(chart.getSelection()[0].row,0);

                /* DRILL DOWN */
                if (drillDown && data.rank != "species") {
                    // show spinner while loading
                    $container.append($('<img class="loading" style="position:absolute;left:130px;top:220px;z-index:2000" ' +
                            'alt="loading..." src="' + collectionsUrl + '/images/ala/ajax-loader.gif"/>'));

                    // save current state as history - for back-tracking
                    thisChart.pushState();

                    // set new chart state
                    thisChart.rank = data.rank;
                    thisChart.name = name;

                    // redraw chart
                    thisChart.load();
                }

                /* SHOW RECORDS */
                else if (clickThru) {
                    // show occurrence records
                    document.location = urlConcat(biocacheWebappUrl, "/occurrences/search?q=") + query +
                            "&fq=" + data.rank + ":" + name;
                }
            });
        }
    },
    showRecords: function () {
        // show occurrence records
        var fq = "";
        if (this.rank != undefined && this.name != undefined) {
            fq = "&fq=" + this.rank + ":" + this.name;
        }
        document.location = urlConcat(biocacheWebappUrl, "/occurrences/search?q=") +
                this.query + fq;
    },
    reset: function () {
        if (this.hasState()) {
            var firstState = this.historyState[0];

            // this is a bit rubbish - the common code should be pulled out
            // set new chart state
            this.rank = firstState.rank;
            this.name = firstState.name;
        }

        // remove any non-taxonomic restrictions
        this.query = this.baseQuery;

        // redraw chart
        this.load();
    },
    updateQuery: function (query) {
        this.query = query;
        this.load();
    }
};

/*------------------------- TAXON TREE -----------------------------*/
function initTaxonTree(treeOptions) {
    var query = treeOptions.query ? treeOptions.query : buildQueryString(treeOptions.instanceUid);

    var targetDivId = treeOptions.targetDivId ? treeOptions.targetDivId : 'tree';
    var $container = $('#' + targetDivId);
    var title = treeOptions.title || 'Explore records by taxonomy';
    if (treeOptions.title !== "") {
        $container.append($('<h4>' + title + '</h4>'));
    }
    var $treeContainer = $('<div id="treeContainer"></div>').appendTo($container);
    $treeContainer.resizable({
        maxHeight: 900,
        minHeight: 100,
        maxWidth: 900,
        minWidth: 500
    });
    var $tree = $('<div id="taxaTree"></div>').appendTo($treeContainer);
    $tree
            .bind("after_open.jstree", function(event, data) {
        var children = $.jstree._reference(data.rslt.obj)._get_children(data.rslt.obj);
        // automatically open if only one child node
        if (children.length == 1) {
            $tree.jstree("open_node",children[0]);
        }
        // adjust container size
        var fullHeight = $tree[0].scrollHeight;
        if (fullHeight > $tree.height()) {
            fullHeight = Math.min(fullHeight, 700);
            $treeContainer.animate({height:fullHeight});
        }
    })
            .bind("select_node.jstree", function (event, data) {
                // click will show the context menu
                $tree.jstree("show_contextmenu", data.rslt.obj);
            })
            .bind("loaded.jstree", function (event, data) {
                // get rid of the anchor click handler because it hides the context menu (which we are 'binding' to click)
                //$tree.undelegate("a", "click.jstree");
                $tree.jstree("open_node","#top");
            })
            .jstree({
                json_data: {
                    data: {"data":"Kingdoms", "state":"closed", "attr":{"rank":"kingdoms", "id":"top"}},
                    ajax: {
                        url: function(node) {
                            var rank = $(node).attr("rank");
                            var u = urlConcat(biocacheServicesUrl, "/breakdown.json?q=") + query + "&rank=";
                            if (rank == 'kingdoms') {
                                u += 'kingdom';  // starting node
                            }
                            else {
                                u += rank + "&name=" + $(node).attr('id');
                            }
                            return u;
                        },
                        dataType: 'jsonp',
                        success: function(data) {
                            var nodes = [];
                            var rank = data.rank;
                            $.each(data.taxa, function(i, obj) {
                                var label = obj.label + " - " + obj.count;
                                if (rank == 'species') {
                                    nodes.push({"data":label, "attr":{"rank":rank, "id":obj.label}});
                                }
                                else {
                                    nodes.push({"data":label, "state":"closed", "attr":{"rank":rank, "id":obj.label}});
                                }
                            });
                            return nodes;
                        },
                        error: function(xhr, text_status) {
                            //alert(text_status);
                        }
                    }
                },
                core: { animation: 200, open_parents: true },
                themes:{
                    theme: treeOptions.theme || 'default',
                    icons: treeOptions.icons || false,
                    url: treeOptions.serverUrl + "/js/themes/" + (treeOptions.theme || 'default') + "/style.css"
                },
                checkbox: {override_ui:true},
                contextmenu: {select_node: false, show_at_node: false, items: {
                    records: {label: "Show records", action: function(obj) {showRecords(obj, query);}},
                    bie: {label: "Show information", action: function(obj) {showBie(obj);}},
                    create: false,
                    rename: false,
                    remove: false,
                    ccp: false }
                },
                plugins: ['json_data','themes','ui','contextmenu']
            });
}
/************************************************************\
 * Go to occurrence records for selected node
 \************************************************************/
function showRecords(node, query) {
    var rank = node.attr('rank');
    if (rank == 'kingdoms') return;
    var name = node.attr('id');
    // url for records list
    var recordsUrl = urlConcat(biocacheWebappUrl, "/occurrences/search?q=") + query +
            "&fq=" + rank + ":" + name;
    document.location.href = recordsUrl;
}
/************************************************************\
 * Go to 'species' page for selected node
 \************************************************************/
function showBie(node) {
    var rank = node.attr('rank');
    if (rank == 'kingdoms') return;
    var name = node.attr('id');
    var sppUrl = "http://bie.ala.org.au/species/" + name;
    if (rank != 'species') { sppUrl += "_(" + rank + ")"; }
    document.location.href = sppUrl;
}

/*------------------------- UTILITIES ------------------------------*/
/************************************************************\
 * build records query handling multiple uids
 * uidSet can be a comma-separated string or an array
 \************************************************************/
function buildQueryString(uidSet) {
    var uids = (typeof uidSet == "string") ? uidSet.split(',') : uidSet;
    var str = "";
    $.each(uids, function(index, value) {
        str += solrFieldNameForUid(value) + ":" + value + " OR ";
    });
    return str.substring(0, str.length - 4);
}
/************************************************************\
 * returns the appropriate facet name for the uid - to build
 * biocache occurrence searches
 \************************************************************/
function solrFieldNameForUid(uid) {
    switch(uid.substring(0,2)) {
        case 'co': return "collection_uid";
        case 'in': return "institution_uid";
        case 'dp': return "data_provider_uid";
        case 'dr': return "data_resource_uid";
        case 'dh': return "data_hub_uid";
        default: return "";
    }
}
/************************************************************\
 * returns the appropriate context for the uid - to build
 * biocache webservice urls
 \************************************************************/
function wsEntityForBreakdown(uid) {
    switch (uid.substr(0,2)) {
        case 'co': return 'collections';
        case 'in': return 'institutions';
        case 'dr': return 'dataResources';
        case 'dp': return 'dataProviders';
        case 'dh': return 'dataHubs';
        default: return "";
    }
}
/************************************************************\
 * Concatenate url fragments handling stray slashes
 \************************************************************/
function urlConcat(base, context) {
    // remove any trailing slash from base
    base = base.replace(/\/$/, '');
    // remove any leading slash from context
    context = context.replace(/^\//, '');
    // join
    return base + "/" + context;
}

/************************************************************\
 * Add commas to number strings
 \************************************************************/
function addCommas(nStr)
{
    nStr += '';
    x = nStr.split('.');
    x1 = x[0];
    x2 = x.length > 1 ? '.' + x[1] : '';
    var rgx = /(\d+)(\d{3})/;
    while (rgx.test(x1)) {
        x1 = x1.replace(rgx, '$1' + ',' + '$2');
    }
    return x1 + x2;
}

