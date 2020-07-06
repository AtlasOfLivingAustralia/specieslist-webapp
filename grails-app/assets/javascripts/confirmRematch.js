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

                // var url = "${createLink(controller:'speciesList', action:'rematchMyLists')}";
                $.post(url, function (data) {
                    alert(msgComplete);
                    window.location.reload()
                }).error(function (jqXHR, textStatus, error) {
                    alert("An error occurred: " + error + " - unable to rematch your lists.");
                }).complete(function () {
                    $("img#spinner").hide();
                    $("#fancyConfirm_ok").removeAttr("disabled");
                    jQuery.fancybox.close();
                });
            })
        }
    })
}