<%--
  User: Natasha Carter
  Date: 14/03/13
  Time: 10:18 AM
  Provide access to all the editable information at a species list level
--%>

<!doctype html>
<html>
<head>
    <r:require modules="fancybox"/>
    <meta name="layout" content="ala2"/>
    <title>Species lists | Atlas of Living Australia</title>
    <r:layoutResources/>
</head>
<body class="species">
<r:layoutResources/>
<div id="content">
    <header id="page-header">
        <div class="inner">
            <nav id="breadcrumb">
                <ol>
                    <li><a href="http://www.ala.org.au">Home</a></li>
                    <li class="last">Species lists</li>
                </ol>
            </nav>
            <hgroup class="leftfloat">
                <h1>Species lists</h1>
            </hgroup>
            <div class="rightfloat">
                <a class="button orange" title="Add Species List" href="${request.contextPath}/speciesList/upload">Upload a list</a>
                <a class="button orange" title="My Lists" href="${request.contextPath}/speciesList/list">My Lists</a>
                <a class="button orange" title="Rematch" href="${request.contextPath}/speciesList/rematch">Rematch All</a>
            </div>
   </div><!--inner-->

    </header>
    <div class="inner">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>

        <g:if test="${lists && total>0}">
            <p>
                Below is a listing of all species lists that can be administered.
            </p>
            <g:render template="/speciesList"/>%
        </g:if>
        <g:else>
            <p>There are no Species Lists available</p>
        </g:else>

    </div>
</div>
</body>
</html>