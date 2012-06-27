<!doctype html>
<html>
<head>
    <title>Grails Runtime Exception</title>
    <meta name="layout" content="main">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css">
</head>
<body>
    <header id="page-header">
        <div class="inner">
            <nav id="breadcrumb">
                <ol>
                    <li><a href="${alaUrl}">Home</a></li>
                    <li><a href="${alaUrl}/australias-species/">Australia&#39;s species</a></li>
                    <li class="last">Image browser for ${msg}</li>
                </ol>
            </nav>
            <hgroup>
                <h1>Images of <b id="totalImageCount">...</b> species from ${params.taxonRank}:
                    <a href="${grailsApplication.config.grails.serverURL}/species/${params.scientificName}" title="More information on this ${params.taxonRank}">${params.scientificName}</a></h1>
            </hgroup>
        </div>
    </header>
    <div class="inner">
        <h1>Error page</h1>
        <div>${message}</div>
        <div>${exception}</div>
        %{--<!-- class: ${exception.metaClass?.getTheClass()} -->--}%
        <g:renderException exception="${exception}" />
    </div>
</body>
</html>