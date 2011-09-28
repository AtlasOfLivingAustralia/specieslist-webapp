<%@ page contentType="text/html" pageEncoding="UTF-8" %><%@ 
taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="pageName" content="home"/>
        <meta name="google-site-verification" content="MdnA79C1YfZ6Yx2qYOXWi_TYFfUvEJOQAmHNaeEWIts"/>
        <title>Biodiversity Information Explorer | Atlas of Living Australia</title>
    </head>
    <body>
       <div class="section">
       	<h1>Biodiversity Information Explorer (BIE)</h1>
        <p>Welcome to the Atlas of Living Australia <strong>Biodiversity Information Explorer</strong>.</p>
        <ul>
        
	    </ul>
        <h2>Higher Taxa</h2>
		<div class="section">
			<form id="taxaDownload" action="taxaDownload" method="get" name="taxaDownload">
			<input type="text" class="filled ac_input" id="download_txt" name="higherTaxon">
			<input type="submit" class="" alt="Download" value="download">
			</form>
		</div>
	   </div>
    </body>
</html>