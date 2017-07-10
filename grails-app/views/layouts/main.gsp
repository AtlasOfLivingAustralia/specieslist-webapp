<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="app.version" content="${g.meta(name:'app.version')}"/>
    <meta name="app.build" content="${g.meta(name:'app.build')}"/>
    <meta name="description" content="Atlas of Living Australia"/>
    <meta name="author" content="Atlas of Living Australia">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="${grailsApplication.config.skin?.favicon?:'http://www.ala.org.au/wp-content/themes/ala2011/images/favicon.ico'}" rel="shortcut icon"  type="image/x-icon"/>

    <title><g:layoutTitle /></title>

    <%-- Do not include JS & CSS files here - add them to your app's "application" module (in "Configuration/ApplicationResources.groovy") --%>
    <r:require modules="bootstrap, ala"/>

    <r:layoutResources/>
    <g:layoutHead />
</head>
<body class="${pageProperty(name:'body.class')}" id="${pageProperty(name:'body.id')}" onload="${pageProperty(name:'body.onload')}">
<g:set var="fluidLayout" value="${pageProperty(name:'meta.fluidLayout')?:grailsApplication.config.skin?.fluidLayout}"/>
<g:set var="containerType" value="${fluidLayout ? 'container-fluid' : 'container'}"/>

<!-- Header -->
<hf:banner logoutUrl="${g.createLink(controller:"logout", action:"logout", absolute: true)}" />
<!-- End header -->
<!-- Breadcrumb -->
<g:if test="${pageProperty(name:'meta.breadcrumb')}">
    <section id="breadcrumb">
        <div class="container">
            <div class="row">
                <ul class="breadcrumb-list">
                    <li><a href="/">Home</a></li>
                    <g:if test="${pageProperty(name:'meta.breadcrumbParent')}">
                        <g:set value="${pageProperty(name:'meta.breadcrumbParent').tokenize(',')}" var="parentArray"/>
                        <li><i class="icon icon-chevron-right"></i><a href="${parentArray[0]}">${parentArray[1]}</a></li>
                    </g:if>
                    <li class="active"><i class="icon icon-chevron-right"></i>${pageProperty(name:'meta.breadcrumb')}</li>
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

<!-- JS resources-->
<r:layoutResources/>

</body>
</html>
