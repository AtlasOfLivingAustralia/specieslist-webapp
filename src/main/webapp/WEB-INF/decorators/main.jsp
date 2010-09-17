<%--
    Document   : main.jsp (sitemesh decorator file)
    Created on : 18/09/2009, 13:57
    Author     : dos009
--%>
<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator" %><%@
taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page" %><%@
include file="/common/taglibs.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="en-US">
    <head profile="http://gmpg.org/xfn/11">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title><decorator:title default="Atlas of Living Australia" /></title>
		<link rel="stylesheet" href="http://test.ala.org.au/wp-content/themes/ala/style.css" type="text/css" media="screen" /> 
		<link rel="icon" type="image/x-icon" href="http://test.ala.org.au/wp-content/themes/ala/images/favicon.ico" /> 
		<link rel="shortcut icon" type="image/x-icon" href="http://test.ala.org.au/wp-content/themes/ala/images/favicon.ico" /> 
		
		<link rel="stylesheet" type="text/css" media="screen" href="http://test.ala.org.au/wp-content/themes/ala/css/sf.css" /> 
		<link rel="stylesheet" type="text/css" media="screen" href="http://test.ala.org.au/wp-content/themes/ala/css/superfish.css" /> 
		<link rel="stylesheet" type="text/css" media="screen" href="http://test.ala.org.au/wp-content/themes/ala/css/skin.css" />
		<link rel="stylesheet" type="text/css" media="screen" href="http://test.ala.org.au/wp-content/themes/ala/css/auth.css" /> 
		
		<script language="JavaScript" type="text/javascript" src="http://test.ala.org.au/wp-content/themes/ala/scripts/form.js"></script> 
		<script language="JavaScript" type="text/javascript" src="http://test.ala.org.au/wp-content/themes/ala/scripts/jquery-1.4.2.min.js"></script> 
		<script language="JavaScript" type="text/javascript" src="http://test.ala.org.au/wp-content/themes/ala/scripts/ui.core.js"></script> 
		<script language="JavaScript" type="text/javascript" src="http://test.ala.org.au/wp-content/themes/ala/scripts/ui.tabs.js"></script> 
		<script language="JavaScript" type="text/javascript" src="http://test.ala.org.au/wp-content/themes/ala/scripts/hoverintent-min.js"></script> 
		<script language="JavaScript" type="text/javascript" src="http://test.ala.org.au/wp-content/themes/ala/scripts/superfish/superfish.js"></script> 
		<script language="JavaScript" type="text/javascript" src="http://test.ala.org.au/wp-content/themes/ala/scripts/jquery.jcarousel.min.js"></script> 
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
		
				// initialise plugins
				jQuery(function(){
					jQuery('ul.sf').superfish( {
						delay:500,
						autoArrows:false,
						dropShadows:false
					});
				});
	            // highlight explore menu tab
	            jQuery(document).ready(function() {
	                jQuery("div#nav li.nav-explore").addClass("selected");
	            });
		
		</script> 
		<meta name="robots" content="noindex,nofollow"/> 
		<link rel="alternate" type="application/rss+xml" title="Atlas Living Australia NG &raquo; Feed" href="http://test.ala.org.au/feed/" /> 
		<link rel="alternate" type="application/rss+xml" title="Atlas Living Australia NG &raquo; Comments Feed" href="http://test.ala.org.au/comments/feed/" /> 
		<link rel='stylesheet' id='external-links-css'  href='http://test.ala.org.au/wp-content/plugins/sem-external-links/sem-external-links.css?ver=20090903' type='text/css' media='all' /> 
		<link rel="EditURI" type="application/rsd+xml" title="RSD" href="http://test.ala.org.au/xmlrpc.php?rsd" /> 
		<link rel="wlwmanifest" type="application/wlwmanifest+xml" href="http://test.ala.org.au/wp-includes/wlwmanifest.xml" /> 
		<link rel='index' title='Atlas Living Australia NG' href='http://test.ala.org.au/' /> 
		<link rel='prev' title='My Profile' href='http://test.ala.org.au/my-profile/' /> 
		<link rel='next' title='Search' href='http://test.ala.org.au/tools-services/search-tools/' /> 
		<meta name="generator" content="WordPress 3.0"/> 
		<link rel='canonical' href='http://test.ala.org.au/' /> 

        <decorator:head />
        
        <!-- WP Menubar 4.7: start CSS -->
        <!-- WP Menubar 4.7: end CSS -->

    </head>
    <body id="page-97" class="page page-id-97 page-parent page-template page-template-default two-column-right">
        <div id="wrapper">
            <div id="banner">
                <div id="logo">
                    <a href="http://test.ala.org.au" title="Atlas of Living Australia home"><img src="http://test.ala.org.au/wp-content/themes/ala/images/ala_logo.png" width="208" height="80" alt="Atlas of Living Ausralia logo" /></a>
                </div><!--close logo-->

                <c:set var="queryString" value="${pageContext.request.queryString}"/>
                <c:choose>
                    <c:when test="${empty queryString}">
                        <c:set var="requestUrl" value="${initParam.serverName}${pageContext.request.requestURI}"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="requestUrl" value="${initParam.serverName}${pageContext.request.requestURI}?${fn:replace(queryString, '+', '%2B')}"/>
                    </c:otherwise>
                </c:choose>

                <ala:bannerMenu returnUrlPath="${requestUrl}"/>

                <div id="wrapper_search">
                    <form id="search-form" action="${pageContext.request.contextPath}/search" method="get" name="search-form"> 
                        <label for="search">Search</label> 
                        <input type="text" class="filled" id="search" name="q" value="${not empty query ? query : 'Search the Atlas'}" /> 
                        <span class="search-button-wrapper"><input type="submit" class="search-button" id="search-button" alt="Search" value="Search" /></span> 
                    </form> 
                </div><!--close wrapper_search-->
            </div><!--close banner-->
            <div id="content">
                <c:if test="${!empty pageContext.request.remoteUser}">
                    <div id="loginId">You are logged in as: ${pageContext.request.remoteUser}</div>
                </c:if>
                <decorator:body />
            </div><!--close content-->
            <div id="footer">
                <ala:footerMenu returnUrlPath="${requestUrl}"/>
            </div><!--close footer-->
        </div><!--close wrapper-->
    </body>
</html>