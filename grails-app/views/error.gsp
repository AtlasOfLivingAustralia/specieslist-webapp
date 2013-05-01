<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Grails Runtime Exception</title>
    <meta name="layout" content="main">

</head>
<body>
    <header id="page-header">
        <div class="inner row-fluid">
            <nav id="breadcrumb" class="span12">
                <ol class="breadcrumb">
                    <li><a href="${alaUrl}">Home</a> <span class=" icon icon-arrow-right"></span></li>
                    <li><a href="${alaUrl}/australias-species/">Australia&#39;s species</a> <span class=" icon icon-arrow-right"></span></li>
                    <li class="active">Biodiversity Information Explorer (BIE)</li>
                </ol>
            </nav>
            <hgroup>
                <h1>Error</h1>
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