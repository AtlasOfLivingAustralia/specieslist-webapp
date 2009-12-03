<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="header">
        <img src="<s:url value="/images/20050920_csiro50.gif"/>" height= "75" alt="CSIRO Logo"/>
        ALA Biodiversity Harvester
        <img src="<s:url value="/images/ALALogo.jpg"/>" height="75" alt="ALA Logo"/>
    </div>
    <div id="topmenu">
        <ul>
            <li><a href="${pageContext.request.contextPath}">Home</a></li>
            <li><a href="${pageContext.request.contextPath}/datastream">Index</a></li>
            <li><a href="${pageContext.request.contextPath}/harvest/setup">Harvest</a></li>
            <li><a href="${pageContext.request.contextPath}/search/solr">Search</a></li>
            <li><a href="${pageContext.request.contextPath}/glossary">Glossary</a></li>
            <!-- <li><a href="${pageContext.request.contextPath}">Contact us</a></li> -->
        </ul>
    </div>

    <div id="sidemenu">
        <ul>
            <li><a href="${pageContext.request.contextPath}/datastream">Show All</a></li>
            <li><a href="${pageContext.request.contextPath}/taxa">Taxon Concepts</a></li>
            <li><a href="${pageContext.request.contextPath}/name">Taxon Names</a></li>
            <li><a href="${pageContext.request.contextPath}/pub">Publications</a></li>
            <li><a href="${pageContext.request.contextPath}/image">Images</a></li>
            <li><a href="${pageContext.request.contextPath}/html">HTML Pages</a></li>
            <hr>
            <li><a href="${pageContext.request.contextPath}/search/RI">RI Search</a></li>
            <li><a href="${pageContext.request.contextPath}/search/solr">SOLR Search</a></li>
        </ul>
    </div>

