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
                <div id="nav">
                    <!-- WP Menubar 4.7: start menu nav-site, template Superfish, CSS  -->
                    <ul class="sf">
                        <li class="nav-home"><a href="http://test.ala.org.au/" ><span>Home</span></a></li>
                        <li class="nav-explore selected"><a href="http://test.ala.org.au/explore/" ><span>Explore</span></a>
                            <ul><li><a href="http://biocache.ala.org.au/explore/your-area" ><span>Your Area</span></a></li>
                                <li><a href="http://bie.ala.org.au/regions/" ><span>States & Territories</span></a></li>
                                <li><a href="http://test.ala.org.au/explore/species-maps/" ><span>Species Maps</span></a></li>
                                <li><a href="http://collections.ala.org.au/public/map" ><span>Natural History Collections</span></a></li>
                                <li><a href="http://test.ala.org.au/explore/themes/" ><span>Themes & Highlights </span></a></li>
                            </ul></li>
                        <li class="nav-tools"><a href="http://test.ala.org.au/tools-services/" ><span>Tools</span></a>
                            <ul><li><a href="http://test.ala.org.au/tools-services/creative-commons-licensing/" ><span>Creative Commons licensing</span></a></li>
                                <li><a href="http://test.ala.org.au/tools-services/community-science/" ><span>Citizen Science</span></a></li>
                                <li><a href="http://test.ala.org.au/tools-services/identification-tools/" ><span>Identification Tools</span></a></li>
                                <li><a href="http://test.ala.org.au/tools-services/for-developers/" ><span>For Developers</span></a></li>
                            </ul></li>
                        <li class="nav-contribute"><a href="http://test.ala.org.au/contribute/" title="Contribute - links, images, images, literature, your time"><span>Contribute</span></a>
                            <ul><li><a href="http://test.ala.org.au/contribute/data-management/" ><span>Data Management</span></a></li>
                                <li><a href="http://test.ala.org.au/contribute/sighting/" ><span>Record Sighting</span></a></li>
                                <li><a href="http://test.ala.org.au/contribute/share-links/" ><span>share links, ideas, information</span></a></li>
                                <li><a href="http://test.ala.org.au/contribute/share-images/" ><span>Share Photos</span></a></li>
                                <li><a href="http://test.ala.org.au/contribute/share-data/" ><span>Electronic Data Sets</span></a></li>
                                <li><a href="http://test.ala.org.au/contribute/paper-based-information/" ><span>Paper-based Information</span></a></li></ul></li>
                        <li class="nav-support"><a href="http://test.ala.org.au/support/" ><span>Support</span></a><ul><li><a href="http://test.ala.org.au/support/get-started/" ><span>Get Started</span></a></li>
                                <li><a href="http://test.ala.org.au/support/forum/" ><span>Forum</span></a></li>
                                <li><a href="http://test.ala.org.au/support/faq/" ><span>Frequently Asked Questions</span></a></li>
                                <li><a href="http://test.ala.org.au/support/how-to/" ><span>How To</span></a></li></ul></li>
                        <li class="nav-contact"><a href="http://test.ala.org.au/contact-us/" ><span>Contact Us</span></a></li>
                        <li class="nav-about"><a href="http://test.ala.org.au/about/" ><span>About the Atlas</span></a>
                            <ul><li><a href="http://test.ala.org.au/about/people/" ><span>Working Together</span></a></li>
                                <li><a href="http://test.ala.org.au/about/portfolio-of-projects/" ><span>Projects</span></a></li>
                                <li><a href="http://test.ala.org.au/about/governance/" ><span>Governance</span></a></li>
                                <li><a href="http://test.ala.org.au/about/media-centre/" ><span>Media Centre</span></a></li>
                                <li><a href="http://test.ala.org.au/about/newsevents/" ><span>News & Events</span></a></li>
                                <li><a href="http://test.ala.org.au/about/resources/" ><span>Resources</span></a></li></ul></li>
                        <c:choose>
                            <c:when test="${empty pageContext.request.remoteUser}">
                                <li class="nav-login nav-right"><ala:loginLogoutLink returnUrlPath="${pageContext.request.requestURL}"/></li>
                            </c:when>
                            <c:otherwise>
                                <li class="nav-logout nav-right"><ala:loginLogoutLink returnUrlPath="${pageContext.request.requestURL}"/></li>
                            </c:otherwise>
                        </c:choose>
                   </ul>
                    <!-- WP Menubar 4.7: end menu nav-site, template Superfish, CSS  -->
                </div><!--close nav-->
                <c:if test="${!empty pageContext.request.remoteUser}">
                    <div id="loginId">${pageContext.request.remoteUser}</div>
                </c:if>
                <div id="wrapper_search">
			<form id="search-form" action="${pageContext.request.contextPath}/search" method="get" name="search-form"> 
				<label for="search">Search</label> 
				<input type="text" class="filled" id="search" name="q" value="${not empty query ? query : 'Search the Atlas'}" /> 
				<span class="search-button-wrapper"><input type="submit" class="search-button" id="search-button" alt="Search" value="Search" /></span> 
			</form> 
		</div><!--close wrapper_search-->
            </div><!--close banner-->
            <div id="content">
                <decorator:body />
            </div><!--close content-->
            <div id="nav-footer">
                <ul>
                    <!--About the Atlas-->
                    <li>About the Atlas			<ul>
                            <li class="page_item page-item-434"><a  href="http://test.ala.org.au/?page_id=434" title="Photo credits">Photo credits</a></li>
                            <li class="page_item page-item-10"><a  href="http://test.ala.org.au/?page_id=10" title="What is the Atlas?">What is the Atlas?</a></li>
                            <li class="page_item page-item-12"><a  href="http://test.ala.org.au/?page_id=12" title="Project Time Line">Project Time Line</a></li>
                            <li class="page_item page-item-14"><a  href="http://test.ala.org.au/?page_id=14" title="Partners">Partners</a></li>
                            <li class="page_item page-item-16"><a  href="http://test.ala.org.au/?page_id=16" title="Natural History Collections">Natural History Collections</a></li>
                            <li class="page_item page-item-18"><a  href="http://test.ala.org.au/?page_id=18" title="Contributors">Contributors</a></li>
                            <li class="page_item page-item-22"><a  href="http://test.ala.org.au/?page_id=22" title="Atlas Team">Atlas Team</a></li>
                            <li class="page_item page-item-24"><a  href="http://test.ala.org.au/?page_id=24" title="Media Centre">Media Centre</a></li>
                            <li class="page_item page-item-26"><a  href="http://test.ala.org.au/?page_id=26" title="Atlas Documents">Atlas Documents</a></li>
                        </ul>
                    </li>
                </ul>
                <ul>
                    <!--Tools & Services-->
                    <li>Tools &#038; Services			<ul>
                            <li class="page_item page-item-60"><a  href="http://test.ala.org.au/?page_id=60" title="Themes/Highlights">Themes/Highlights</a></li>
                            <li class="page_item page-item-269"><a  href="http://test.ala.org.au/?page_id=269" title="Searching Tools">Searching Tools</a></li>
                            <li class="page_item page-item-37"><a  href="http://test.ala.org.au/?page_id=37" title="Community Science">Community Science</a></li>
                            <li class="page_item page-item-43"><a  href="http://test.ala.org.au/?page_id=43" title="Identification Tools">Identification Tools</a></li>
                            <li class="page_item page-item-51"><a  href="http://test.ala.org.au/?page_id=51" title="Maps">Maps</a></li>
                            <li class="page_item page-item-72"><a  href="http://test.ala.org.au/?page_id=72" title="For Developers">For Developers</a></li>
                        </ul>
                    </li>
                </ul>
                <ul>
                    <!--Contact Us-->
                    <li>Contact Us			<ul>
                            <li class="page_item page-item-75"><a  href="http://test.ala.org.au/?page_id=75" title="Contact Our Team">Contact Our Team</a></li>
                            <li class="page_item page-item-77"><a  href="http://test.ala.org.au/?page_id=77" title="Contribute Data &amp; Images">Contribute Data &amp; Images</a></li>
                        </ul>
                    </li>
                </ul>
                <ul>
                    <!--Support-->
                    <li>Support			<ul>
                            <li class="page_item page-item-83"><a  href="http://test.ala.org.au/?page_id=83" title="Get Started">Get Started</a></li>
                            <li class="page_item page-item-85"><a  href="http://test.ala.org.au/?page_id=85" title="Forum">Forum</a></li>
                            <li class="page_item page-item-87"><a  href="http://test.ala.org.au/?page_id=87" title="Frequently Asked Questions">Frequently Asked Questions</a></li>
                            <li class="page_item page-item-89"><a  href="http://test.ala.org.au/?page_id=89" title="Help">Help</a></li>
                        </ul>
                    </li>
                </ul>
                <ul class="no-parent">
                    <li class="page_item page-item-91"><a  href="http://test.ala.org.au/?page_id=91" title="Privacy Policy">Privacy Policy</a></li>
                    <li class="page_item page-item-92"><a  href="http://test.ala.org.au/?page_id=92" title="Terms of Use">Terms of Use</a></li>
                    <li class="page_item page-item-93"><a  href="http://test.ala.org.au/?page_id=93" title="Citing the Atlas">Citing the Atlas</a></li>
                    <li class="page_item page-item-94"><a  href="http://test.ala.org.au/?page_id=94" title="Disclaimer">Disclaimer</a></li>
                </ul>
            </div><!--close nav-footer-->
            <div id="footer">
                <div id="footer-nav">
                    <ul id="menu-footer-site">
                        <li id="menu-item-1046" class="menu-item menu-item-type-post_type"><a href="http://test.ala.org.au/">Home</a></li>
                        <li id="menu-item-1049" class="menu-item menu-item-type-post_type"><a href="http://test.ala.org.au/explore/maps/explore-stateterritory/">Explore</a></li>
                        <li id="menu-item-1051" class="menu-item menu-item-type-post_type"><a href="http://test.ala.org.au/tools-services/">Tools</a></li>
                        <li id="menu-item-1050" class="menu-item menu-item-type-post_type"><a href="http://test.ala.org.au/support/">Support</a></li>
                        <li id="menu-item-1048" class="menu-item menu-item-type-post_type"><a href="http://test.ala.org.au/contact-us/">Contact Us</a></li>
                        <li id="menu-item-1047" class="menu-item menu-item-type-post_type"><a href="http://test.ala.org.au/about/">About the Atlas</a></li>
                        <li id="menu-item-1052" class="last menu-item menu-item-type-custom"><ala:loginLogoutLink returnUrlPath="${pageContext.request.requestUrl}"/></li>
                    </ul>
                    <ul id="menu-footer-legal">
                        <li id="menu-item-3090" class="menu-item menu-item-type-post_type"><a href="http://test.ala.org.au/site-map/">Site Map</a></li>
                        <li id="menu-item-1042" class="menu-item menu-item-type-post_type"><a href="http://test.ala.org.au/about/media-centre/terms-of-use/citing-the-atlas/">Citing the Atlas</a></li>
                        <li id="menu-item-1043" class="menu-item menu-item-type-post_type"><a href="http://test.ala.org.au/about/media-centre/terms-of-use/disclaimer/">Disclaimer</a></li>
                        <li id="menu-item-1044" class="menu-item menu-item-type-post_type"><a href="http://test.ala.org.au/about/media-centre/terms-of-use/privacy-policy/">Privacy Policy</a></li>
                        <li id="menu-item-1045" class="last menu-item menu-item-type-post_type"><a href="http://test.ala.org.au/about/media-centre/terms-of-use/">Terms of Use</a></li>
                    </ul>
                </div>
		<div class="copyright"><p><a href="http://creativecommons.org/licenses/by/2.5/au/" title="External link to Creative Commons" class="left no-pipe"><img src="http://test.ala.org.au/wp-content/themes/ala/images/somerights20.png" width="88" height="31" alt="" /></a>This work is licensed under a <a href="http://creativecommons.org/licenses/by/2.5/au/" title="External link to Creative Commons">Creative Commons Attribution 2.5 Australia License</a></p></div>
            </div><!--close footer-->
        </div><!--close wrapper-->
    </body>
</html>