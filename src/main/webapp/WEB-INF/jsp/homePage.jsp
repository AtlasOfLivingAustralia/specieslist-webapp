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
	        <li><a href="regions/">Explore the states</a></li>
	        <li><a href="contributors">Contributors list</a></li>
	        <li><a href="images">Search with images (Alpha)</a></li>
	    </ul>
        <h2>Free text occurrence search the BIE</h2>
		<div id="inpage_search">
			<form id="search-inpage" action="search" method="get" name="search-form">
			<label for="search">Search</label>
			<input type="text" class="filled ac_input" id="search" name="q" placeholder="Search the Atlas" autocomplete="off">
			<span class="search-button-wrapper"><input type="submit" class="search-button" alt="Search" value="Search"></span>
			</form>
		</div>
	   </div>
    </body>
</html>