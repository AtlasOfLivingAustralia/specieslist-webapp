/* holds all the list items */
var allItems;

/* holds the current list items */
var listItems;

/* list of current filters - items are {name, value} */
var currentFilters = [];

/* the base url of the home server */
var baseUrl;

function loadItems(serverUrl){
    baseUrl = serverUrl;
    $.getJSON()
}
function encode(str) {
    var resString = "";
    for (i = 0; i < str.length; i++) {
        if (str.charAt(i) == " ") resString += "+";
        else resString += str.charAt(i);
    }
   return escape(resString)
}

function removeFacet(facet) {
    //var q = $.getQueryParam('q'); //$.query.get('q')[0];
    var fqList = $.getQueryParam('fq'); //$.query.get('fq');

    var paramList = [];
    console.log("REMOVING " ,facet)
    if (fqList instanceof Array) {
        //alert("fqList is an array");
        for (var i in fqList) {
            var thisFq = decodeURI(fqList[i]); //.replace(':[',':'); // for dates to work
            //alert("fq = "+thisFq + " || facet = "+decodeURI(facet) +" " + encode(facet));
            if (thisFq.indexOf(decodeURI(facet)) != -1 || thisFq.indexOf(encode(facet)) != -1) {  // if(str1.indexOf(str2) != -1){
                //alert("removing fq: "+fqList[i]);
                fqList.splice($.inArray(fqList[i], fqList), 1);
            }

        }
    } else {
        //alert("fqList is NOT an array");
        if (decodeURI(fqList) == facet) {
            fqList = null;
        }
    }
    //alert("(post) fqList = "+fqList.join('|'));
    if (fqList != null) {
        paramList.push("fq=" + fqList.join("&fq="));
    }

        window.location.href = window.location.pathname + '?' + paramList.join('&') + window.location.hash;

}