<%--
  User: Natasha Carter
  Date: 14/03/13
  Time: 10:18 AM
  Provide access to all the editable information at a species list level
--%>

<!doctype html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Species lists | ${grailsApplication.config.skin.orgNameLong}</title>
    <asset:stylesheet src="fancybox.css"/>
    <meta name="breadcrumbParent" content="${request.contextPath}/public/speciesLists,Species lists"/>
    <meta name="breadcrumb" content="Admin"/>
</head>
<body class="">
<div id="content" class="row">
    <div class="col-md-12">
        <header id="page-header">
            <div class="row">
                <hgroup class="col-md-8">
                    <h1>${message(code:'admin.lists.header', default:'Species lists')}</h1>
                </hgroup>
                <div class="col-md-4">
                    <span class="pull-right">
                        <g:if test="${isAdmin}">
                            <a class="btn btn-primary" title="Admin" href="${request.contextPath}/admin">Admin</a>
                        </g:if>
                        <a class="btn btn-primary" title="${message(code:'upload.lists.header01', default:'Upload a list')}" href="${request.contextPath}/speciesList/upload">${message(code:'upload.lists.header01', default:'Upload a list')}</a>
                        <a class="btn btn-primary" title="${message(code:'generic.lists.button.mylist.label', default:'My Lists')}" href="${request.contextPath}/speciesList/list">${message(code:'generic.lists.button.mylist.label', default:'My Lists')}</a>
                        <a class="btn btn-primary" title="${message(code:'admin.lists.page.button.rematch.tooltip', default:'Rematch')}" href="${request.contextPath}/speciesList/rematch">${message(code:'admin.lists.page.button.rematch.label', default:'Rematch All')}</a>
                    </span>
                </div>
            </div><!--inner-->
        </header>
        <div class="inner">
            <g:if test="${flash.message}">
                <div class="message alert alert-info">${flash.message}</div>
            </g:if>

            <g:if test="${lists && total>0}">
                <p>
                    ${message(code:'admin.lists.text', default:'Below is a listing of all species lists that can be administered.')}

                </p>
                <a href="${g.createLink(action: 'updateListsWithUserIds')}" class="btn btn-primary margin-bottom-5px">${message(code:'admin.lists.button.label', default:'Update List user details (name & email address)')}</a>
                <g:render template="/speciesList"/>
            </g:if>
            <g:else>
                <p>${message(code:'public.lists.search.noresult', default:'There are no Species Lists available')}</p>
            </g:else>

        </div>
    </div>
</div>
<asset:javascript src="fancybox.js"/>
</body>
</html>