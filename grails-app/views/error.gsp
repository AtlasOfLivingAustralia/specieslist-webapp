<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Grails Runtime Exception</title>
    <meta name="layout" content="ala2"/>

</head>
<body>
<header id="page-header">
    <div class="inner">
        <nav id="breadcrumb">
            <ol>
                <li><a href="http://www.ala.org.au">Home</a></li>
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