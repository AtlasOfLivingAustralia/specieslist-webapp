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
    <asset:script type="text/javascript">
        function rematchConfirm(msgConfirm, url, msgComplete) {
            jQuery.fancybox.open("<div style=\"padding:20px;width:400px;text-align:center;\">" + msgConfirm + "<div style=\"text-align:center;margin-top:10px;\"><input id=\"fancyConfirm_cancel\" type=\"button\" value=\"No\" class=\"actionButton btn btn-default btn-sm\">&nbsp;<input id=\"fancyConfirm_ok\" type=\"button\" value=\"Yes\" class=\"actionButton btn btn-default btn-sm\"><img src='${asset.assetPath(src:'spinner.gif')}' id='spinner'/></div></div>", {
                'padding': 0,
                'margin': 0,
                'width': 'auto',
                'height': 'auto',
                afterShow: function () {
                    jQuery("#fancyConfirm_cancel").click(function () {
                        ret = false;
                        jQuery.fancybox.close();
                    });
                    jQuery("#fancyConfirm_ok").click(function () {
                        ret = true;
                        $("img#spinner").show(); // show spinning gif
                        $("#fancyConfirm_ok").attr("disabled", "disabled"); // disable "Yes" button while processing
                        $.post(url, function (data) {
                        }).error(function (jqXHR, textStatus, error) {
                            alert("An error occurred: " + error + " - unable to rematch your lists.");
                        })
                        jQuery.fancybox.close();
                        window.location.reload()
                    })
                }
            })
        }

        function showRematchingProcess(url) {
            jQuery.fancybox.open("<div style='padding:20px;width:800px;text-align:center;'><div style='text-align:center;margin-top:10px;'><div id='logs'></div><input id='fancyConfirm_ok' type='button' value='Close' class='actionButton btn btn-default btn-sm'></div></div>", {
                'padding': 0,
                'margin': 0,
                'width': 'auto',
                'height': 'auto',
                afterShow: function () {
                    $.get(url, function(rematchingLogs) {
                        var message = "";
                        if (rematchingLogs.processing) {
                            var logs = rematchingLogs.history.filter(({status}) => status === "RUNNING");
                            message = "<table><tr><th>Who</th><th>Start time</th><th>Last update time</th><th>Remaining records</th><th>Total Records</th></tr>"
                            for(i in logs) {
                                var log = logs[i]
                                message +="<tr>"
                                message += "<td>"+log.byWhom+"</td>"
                                message += "<td>"+log.startTime+"</td>"
                                message += "<td>"+log.recentProcessTime+"</td>"
                                message += "<td>"+log.remaining+"</td>"
                                message += "<td>"+log.total+"</td>"
                                message +="</tr>"
                            }
                            message +="</table>";
                        } else {
                            message = "There is no active rematching process";
                        }

                        $("div#logs").html(message);
                    })
                    jQuery("#fancyConfirm_ok").click(function () {
                        jQuery.fancybox.close();
                    });
                }
            })
        }
    </asset:script>
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
                        <a class="btn btn-primary" title="${message(code:'upload.lists.header01', default:'Upload a list')}" href="${request.contextPath}/speciesList/upload">${message(code:'upload.lists.header01', default:'Upload a list')}</a>
                        <a class="btn btn-primary" title="${message(code:'generic.lists.button.mylist.label', default:'My Lists')}" href="${request.contextPath}/speciesList/list">${message(code:'generic.lists.button.mylist.label', default:'My Lists')}</a>
                        <g:if test="${rematchLogs.processing}">
                            <button class="btn btn-primary" onclick="showRematchingProcess('${request.contextPath}/ws/rematchStatus')">Rematching is on process. Click to check status</button>
                        </g:if>
                        <g:else>
                            <a href="#" title="${message(code:'admin.lists.page.button.rematch.tooltip', default:'Rematch')}"
                               onclick="rematchConfirm('${message(code:"admin.lists.actions.button.rematch.messages", default:"Are you sure that you would like to rematch?")}',
                                   '${request.contextPath}/speciesList/rematch',
                                   '${message(code:"admin.lists.page.button.rematch.messages", default:"Rematch complete")}');
                               return false;" class="btn btn-primary">${message(code:'admin.lists.page.button.rematch.label', default:'Rematch All')}</a>
                            <a href="${request.contextPath}/ws/rematchStatus">Check rematching history</a>
                        </g:else>
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