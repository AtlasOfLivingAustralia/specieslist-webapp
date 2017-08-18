<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="app.version" content="${g.meta(name:'app.version')}"/>
    <meta name="app.build" content="${g.meta(name:'app.build')}"/>
    <meta name="description" content="Atlas of Living Australia"/>
    <meta name="author" content="Atlas of Living Australia">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="${grailsApplication.config.headerAndFooter.baseURL}/css/bootstrap.min.css" rel="stylesheet" media="all" />
    <link href="${grailsApplication.config.headerAndFooter.baseURL}/css/ala-styles.css" rel="stylesheet" media="all" />
    <asset:stylesheet src="jquery.autocomplete.css" media="all" />
    <asset:stylesheet src="ala.css" media="all" />
    <asset:stylesheet src="application.css" media="all" />
    <link href="${grailsApplication.config.skin?.favicon?:'http://www.ala.org.au/wp-content/themes/ala2011/images/favicon.ico'}" rel="shortcut icon"  type="image/x-icon"/>

    <!--[if lt IE 9]>
    <asset:javascript src="html5.js" />
    <![endif]-->
    <asset:javascript src="head.js"/>
    <asset:javascript src="jquery-extensions.js" />

    <title><g:layoutTitle /></title>
    <g:layoutHead />
</head>
<body class="${pageProperty(name:'body.class')}" id="${pageProperty(name:'body.id')}" onload="${pageProperty(name:'body.onload')}">
<g:set var="fluidLayout" value="${pageProperty(name:'meta.fluidLayout')?:grailsApplication.config.skin?.fluidLayout}"/>
<g:set var="containerType" value="${fluidLayout?.toBoolean() ? 'container-fluid' : 'container'}"/>

<!-- Header -->
<hf:banner logoutUrl="${g.createLink(controller:"logout", action:"logout", absolute: true)}" />
<!-- End header -->
<!-- Breadcrumb -->
<g:if test="${pageProperty(name:'meta.breadcrumb')}">
    <section id="breadcrumb">
        <div class="container">
            <div class="row">
                <ul class="breadcrumb-list">
                    <li><a href="https://www.ala.org.au">Home</a></li>
                    <g:if test="${pageProperty(name:'meta.breadcrumbParent')}">
                        <g:set value="${pageProperty(name:'meta.breadcrumbParent').tokenize(',')}" var="parentArray"/>
                        <li><i class="glyphicon glyphicon-chevron-right"></i><a href="${parentArray[0]?:'/'}">${parentArray[1]}</a></li>
                    </g:if>
                    <li class="active"><i class="glyphicon glyphicon-chevron-right"></i>${pageProperty(name:'meta.breadcrumb')}</li>
                </ul>
            </div>
        </div>
    </section>
</g:if>
<!-- End Breadcrumb -->
<!-- Container -->
<div class="${containerType}" id="main">
    <plugin:isAvailable name="alaAdminPlugin">
        <ala:systemMessage/>
    </plugin:isAvailable>
    <g:layoutBody />
</div><!-- End container #main  -->

<!-- Footer -->
<hf:footer/>
<!-- End footer -->

<g:if test="${!grailsApplication.config.headerAndFooter.excludeApplicationJs}">
    <script type="text/javascript" src="${grailsApplication.config.headerAndFooter.baseURL}/js/application.js"></script>
</g:if>
<g:if test="${!grailsApplication.config.headerAndFooter.excludeBootstrapJs}">
    <script type="text/javascript" src="${grailsApplication.config.headerAndFooter.baseURL}/js/bootstrap.min.js"></script>
</g:if>
<asset:javascript src="ala.js" />
<asset:deferredScripts />

</body>
</html>
