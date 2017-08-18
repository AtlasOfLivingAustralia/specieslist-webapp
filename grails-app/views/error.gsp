<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Grails Runtime Exception</title>
    <meta name="layout" content="main"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/public/speciesLists,Species lists"/>
    <meta name="breadcrumb" content="Error"/>
</head>
<body>
<header id="page-header">
    <div class="inner">
        <hgroup>
            <h1>Error page</h1>
        </hgroup>
    </div>
</header>
<div class="inner">
    <div><pre>${message}</pre></div>
    <div>${exception}</div>
    <g:renderException exception="${exception}" />
</div>
</body>
</html>