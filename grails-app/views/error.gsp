<!doctype html>
<html>
	<head>
		<title>Grails Runtime Exception</title>
		<meta name="layout" content="main">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css">
	</head>
	<body>
        <div class="inner">
            <h1>Error page</h1>
            <div>${message}</div>
            <div>${exception}</div>
            %{--<!-- class: ${exception.metaClass?.getTheClass()} -->--}%
            <g:renderException exception="${exception}" />
        </div>
	</body>
</html>