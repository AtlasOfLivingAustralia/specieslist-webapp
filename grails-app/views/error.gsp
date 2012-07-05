<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Grails Runtime Exception</title>
    <meta name="layout" content="main">

</head>
<body>
    <header id="page-header">
        <div class="inner">
            <nav id="breadcrumb">
                <ol>
                    <li><a href="${alaUrl}">Home</a></li>
                    <li><a href="${alaUrl}/australias-species/">Australia&#39;s species</a></li>
                    <li class="last">Error Page</li>
                </ol>
            </nav>
            <hgroup>
                %{--<h1>Error page</h1>--}%
            </hgroup>
        </div>
    </header>
    <div class="inner">
        <div>${message}</div>
        %{--<div>${exception}</div>--}%
        %{--<!-- class: ${exception.metaClass?.getTheClass()} -->--}%
        <g:renderException exception="${exception}" />
    </div>
</body>
</html>