<%--
    Document   : main.jsp (sitemesh decorator file)
    Created on : 18/09/2009, 13:57
    Author     : dos009
--%><%@
taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator" %><%@
taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page" %><%@
include file="/common/taglibs.jsp" %>
<!DOCTYPE html>
<html dir="ltr" lang="en-US">
    <head profile="http://gmpg.org/xfn/11">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title><decorator:title default="Atlas of Living Australia" /></title>
        <link rel="stylesheet" href="${initParam.centralServer}/wp-content/themes/ala/style.css" type="text/css" media="screen" />
        <link rel="icon" type="image/x-icon" href="${initParam.centralServer}/wp-content/themes/ala/images/favicon.ico" />
        <link rel="shortcut icon" type="image/x-icon" href="${initParam.centralServer}/wp-content/themes/ala/images/favicon.ico" />

        <link rel="stylesheet" type="text/css" media="screen" href="${initParam.centralServer}/wp-content/themes/ala/css/sf.css" />
        <link rel="stylesheet" type="text/css" media="screen" href="${initParam.centralServer}/wp-content/themes/ala/css/superfish.css" />
        <link rel="stylesheet" type="text/css" media="screen" href="${initParam.centralServer}/wp-content/themes/ala/css/skin.css" />
        <link rel="stylesheet" type="text/css" media="screen" href="${initParam.centralServer}/wp-content/themes/ala/css/jquery.autocomplete.css" />

        <script language="JavaScript" type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala/scripts/form.js"></script>
        <script language="JavaScript" type="text/javascript" src="${initParam.centralServer}/wp-content/themes/ala/scripts/jquery-1.4.2.min.js"></script>
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
                                    value: data[i].matchedNames[0],
                                    result: data[i].matchedNames[0]
                                };
                            }
                            return rows;
                        },
                        matchSubset: false,
                        formatItem: function(row, i, n) {
                            return row.matchedNames[0]; // + ' (' + row.rankString + ')';
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
        <link rel="alternate" type="application/rss+xml" title="Atlas Living Australia NG &raquo; Feed" href="${initParam.centralServer}/feed/" />
        <link rel="alternate" type="application/rss+xml" title="Atlas Living Australia NG &raquo; Comments Feed" href="${initParam.centralServer}/comments/feed/" />
        <link rel='stylesheet' id='external-links-css'  href='${initParam.centralServer}/wp-content/plugins/sem-external-links/sem-external-links.css?ver=20090903' type='text/css' media='all' />
        <link rel="EditURI" type="application/rsd+xml" title="RSD" href="${initParam.centralServer}/xmlrpc.php?rsd" />
        <link rel="wlwmanifest" type="application/wlwmanifest+xml" href="${initParam.centralServer}/wp-includes/wlwmanifest.xml" />
        <link rel='index' title='Atlas Living Australia NG' href='${initParam.centralServer}/' />
        <link rel='prev' title='My Profile' href='${initParam.centralServer}/my-profile/' />
        <link rel='next' title='Search' href='${initParam.centralServer}/tools-services/search-tools/' />
        <meta name="generator" content="BIE-WEBAPP"/>
        <link rel='canonical' href='${initParam.centralServer}/' />
        <decorator:head />
        <!-- WP Menubar 4.7: start CSS -->
        <!-- WP Menubar 4.7: end CSS -->
    </head>
    <body class="two-column-right">
     <div id="wrapper">
	 <c:set var="returnUrlPath" value="${initParam.serverName}${pageContext.request.requestURI}${not empty pageContext.request.queryString ? '?' : ''}${pageContext.request.queryString}"/>
         <ala:bannerMenu returnUrlPath="${returnUrlPath}" />
         <div id="content">
             <c:if test="${!empty pageContext.request.remoteUser}">
                 <div id="loginId">You are logged in as: ${pageContext.request.remoteUser}</div>
             </c:if>
             <decorator:body />
         </div><!--close content-->
         <div id="footer">
             <ala:footerMenu returnUrlPath="${returnUrlPath}"/>
         </div><!--close footer-->
     </div><!--close wrapper-->
    </body>
</html>