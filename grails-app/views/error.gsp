<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Something went wrong</title>
    <meta name="layout" content="main"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/public/speciesLists,Species lists"/>
    <meta name="breadcrumb" content="Error"/>
</head>
<body>
<header id="page-header">
    <div class="inner">
        <hgroup>
            <h1>Something went wrong!</h1>
        </hgroup>
    </div>
</header>
<div class="inner">
    <div><pre>${raw(message)}</pre></div>
    <div>${exception}</div>
    <g:renderException exception="${raw(exception)}" />
</div>
</body>
</html>