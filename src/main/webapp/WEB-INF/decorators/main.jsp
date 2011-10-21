<%--
    Document   : main.jsp (sitemesh decorator file)
    Created on : 18/09/2009, 13:57
    Author     : dos009
--%><%@
taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator" %><%@
taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page" %><%@
include file="/common/taglibs.jsp" %><!DOCTYPE html>
<html dir="ltr" lang="en-US">
    <head profile="http://gmpg.org/xfn/11">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title><decorator:title default="Atlas of Living Australia" /></title>
        
<SCRIPT LANGUAGE="JavaScript">
//calculate the time before calling the function in window.onload
beforeload = (new Date()).getTime();
function pageloadingtime()
{

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
        
        <link rel="stylesheet" href="${initParam.centralServer}/wp-content/themes/ala/style.css" type="text/css" media="screen" />
        <link rel="icon" type="image/x-icon" href="${initParam.centralServer}/wp-content/themes/ala/images/favicon.ico" />
        <link rel="shortcut icon" type="image/x-icon" href="${initParam.centralServer}/wp-content/themes/ala/images/favicon.ico" />

        <link rel="stylesheet" type="text/css" media="screen" href="${initParam.centralServer}/wp-content/themes/ala/css/sf.css" />
        <link rel="stylesheet" type="text/css" media="screen" href="${initParam.centralServer}/wp-content/themes/ala/css/superfish.css" />
        <link rel="stylesheet" type="text/css" media="screen" href="${initParam.centralServer}/wp-content/themes/ala/css/skin.css" />
        <link rel="stylesheet" type="text/css" media="screen" href="${initParam.centralServer}/wp-content/themes/ala/css/jquery.autocomplete.css" />

        <script language="JavaScript" type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala/scripts/form.js"></script>
        <%--<script language="JavaScript" type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala/scripts/jquery-1.4.3.min.js"></script>--%>
        <script language="JavaScript" type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-1.5.2.min.js"></script>
        <script language="JavaScript" type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala/scripts/hoverintent-min.js"></script>
        <script language="JavaScript" type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala/scripts/superfish/superfish.js"></script>
        <script language="JavaScript" type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala/scripts/jquery.autocomplete.js"></script>
        <script language="JavaScript" type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala/scripts/uservoice.js"></script>
        <script type="text/javascript">
            //add the indexOf method for IE7
            if(!Array.indexOf){
                Array.prototype.indexOf = function(obj){
                    for(var i=0; i<this.length; i++){
                        if(this[i]===obj){
                            return i;
                        }
                    }
                    return -1;
                }
            }

                    function stripHTML(string) {
                        if (string) {
                            string = string.replace(/<(.|\n)*?>/g, '');
                        }
                        return string;
                    }

            // initialise plugins
            jQuery(function(){
                    jQuery('ul.sf').superfish( {
                            delay:500,
                            autoArrows:false,
                            dropShadows:false
                    });
            });

            jQuery(document).ready(function() {
                // highlight explore menu tab
                $("div#nav li.nav-explore").addClass("selected");
                // autocomplete for search input
                $("form#search-form input#search").autocomplete('${pageContext.request.contextPath}/search/auto.json', {
                        extraParams: {limit:100},
                        dataType: 'jsonp',
                        parse: function(data) {
                            var rows = new Array();
                            data = data.autoCompleteList;
                            for(var i=0; i<data.length; i++){
                                rows[i] = {
                                    data:data[i],
                                    value: (data[i].matchedNames[0] == 'undefined' || data[i].matchedNames[0] == null)?'':data[i].matchedNames[0],
                                    result:  (data[i].matchedNames[0] == 'undefined' || data[i].matchedNames[0] == null)?'':data[i].matchedNames[0]
                                };
                            }
                            return rows;
                        },
                        matchSubset: false,
                        formatItem: function(row, i, n) {
                        	if(row.matchedNames[0] != 'undefined' && row.matchedNames[0] != null){
                            	return row.matchedNames[0] ; // + ' (' + row.rankString + ')';
                        	}
                        	return false;
                        },
                        //overwrite autocomplete.js function
                        highlight: function(value, term) {
                            if(value == 'undefined' || value == null){
                            	return;
                            }else{
                            	return value.replace(new RegExp("(?![^&;]+;)(?!<[^<>]*)(" + term.replace(/([\^\$\(\)\[\]\{\}\*\.\+\?\|\\])/gi, "\\$1") + ")(?![^<>]*>)(?![^&;]+;)", "gi"), "<strong>$1</strong>");
                            }
                        },
                        cacheLength: 10,
                        minChars: 3,
                        scroll: false,
                        max: 10,
                        selectFirst: false
                });
            }); // End docuemnt ready

        </script>
        <meta name="robots" content="index,follow"/>
        <META name="y_key" content="d5130872f549aec9" />
        <decorator:head />
    </head>
    <body class="two-column-right">
     <div id="wrapper">
	 <c:set var="returnUrlPath" value="${initParam.serverName}${pageContext.request.requestURI}${not empty pageContext.request.queryString ? '?' : ''}${pageContext.request.queryString}"/>
         <ala:bannerMenu returnUrlPath="${returnUrlPath}" />
         <div id="content">
             <ala:loggedInUserId />
             <decorator:body />
         </div><!--close content-->
         <div id="footer">
             <ala:footerMenu returnUrlPath="${returnUrlPath}"/>                        
             <c:if test="${not empty jspTime && not empty executeTime}">
             	<p><font style="background-color:#606060;" color="#606060">jsp: ${jspTime} millisec, controller: ${executeTime} millisec</font></p>
             </c:if>
             <script type="text/javascript">window.onload = pageloadingtime;</script>  
             <div id="loadingtime"></div>
         </div><!--close footer-->
     </div><!--close wrapper-->
    </body>
</html>