<%--
    Document   : main.jsp (sitemesh decorator file)
    Created on : 18/09/2009, 13:57
    Author     : dos009
--%><%@
taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator" %><%@
taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page" %><%@
include file="/common/taglibs.jsp" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><decorator:title default="Atlas of Living Australia" /></title>
        
<SCRIPT LANGUAGE="JavaScript">
    //calculate the time before calling the function in window.onload
    beforeload = (new Date()).getTime();
    function pageloadingtime() {
         //calculate the current time in afterload
        afterload = (new Date()).getTime();
         // now use the beforeload and afterload to calculate the seconds
        secondes = (afterload-beforeload)/1000;
         // If necessary update in window.status
        window.status='Page Load took  ' + secondes + ' second(s).';
        // Place the seconds in the innerHTML to show the results
        // document.getElementById("loadingtime").innerHTML = "Page Load took " + secondes + " seconds.";
    }
</SCRIPT>
    <%--<link rel="stylesheet" href="${initParam.centralServer}/wp-content/themes/ala2011/style.css" type="text/css" media="screen" />--%>
    <link rel="stylesheet" href="${initParam.centralServer}/wp-content/themes/ala2011/style2010.css" type="text/css" media="screen" />
    <link rel="stylesheet" href="${initParam.centralServer}/wp-content/themes/ala2011/style2011.css" type="text/css" media="screen" />
    <link rel="stylesheet" href="${initParam.centralServer}/wp-content/themes/ala2011/css/wp-styles.css" type="text/css" media="screen" />
    <link rel="stylesheet" href="${initParam.centralServer}/wp-content/themes/ala2011/css/buttons.css" type="text/css" media="screen" />
    <link rel="icon" type="image/x-icon" href="${initParam.centralServer}/wp-content/themes/ala2011/images/favicon.ico" />
    <link rel="shortcut icon" type="image/x-icon" href="${initParam.centralServer}/wp-content/themes/ala2011/images/favicon.ico" />
    <link rel="stylesheet" type="text/css" media="screen" href="${initParam.centralServer}/wp-content/themes/ala2011/css/jquery.autocomplete.css" />
    <link rel="stylesheet" type="text/css" media="screen" href="${initParam.centralServer}/wp-content/themes/ala2011/css/search.css" />
    <link rel="stylesheet" type="text/css" media="screen" href="${initParam.centralServer}/wp-content/themes/ala2011/css/skin.css" />
    <link rel="stylesheet" type="text/css" media="screen" href="${initParam.centralServer}/wp-content/themes/ala2011/css/sf.css" />

    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>

    <decorator:head />

    <script type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala2011/scripts/html5.js"></script>
    <script language="JavaScript" type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala2011/scripts/superfish/superfish.js"></script>
    <script language="JavaScript" type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala2011/scripts/jquery.autocomplete.js"></script>
    <script language="JavaScript" type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala2011/scripts/uservoice.js"></script>
    <script type="text/javascript">

        // initialise plugins

        jQuery(function(){
            jQuery('ul.sf').superfish( {
                delay:500,
                autoArrows:false,
                dropShadows:false
            });

            jQuery("form#search-form-2011 input#search-2011").autocomplete('http://bie.ala.org.au/search/auto.jsonp', {
                extraParams: {limit: 100},
                dataType: 'jsonp',
                parse: function(data) {
                    var rows = new Array();
                    data = data.autoCompleteList;
                    for(var i=0; i<data.length; i++){
                        rows[i] = {
                            data:data[i],
                            value: data[i].matchedNames[0],
                            result: data[i].matchedNames[0]
                        };
                    }
                    return rows;
                },
                matchSubset: false,
                formatItem: function(row, i, n) {
                    return row.matchedNames[0];
                },
                cacheLength: 10,
                minChars: 3,
                scroll: false,
                max: 10,
                selectFirst: false
            });
        });
    </script>
    <style type="text/css">
        div.solrResults {
            width: 762px;
            float: left;
        }
    </style>
</head>
<body class="page species">
    <div id="wrapper">
        <c:set var="returnUrlPath" value="${initParam.serverName}${pageContext.request.requestURI}${not empty pageContext.request.queryString ? '?' : ''}${pageContext.request.queryString}"/>
        <ala:header returnUrlPath="${returnUrlPath}" />
        <div id="content">
            <ala:loggedInUserId />
            <decorator:body />
        </div>
        <ala:footer />
    </div>
</body>
</html>