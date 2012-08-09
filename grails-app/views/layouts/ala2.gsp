<!DOCTYPE html>
<html>
<head>
  <meta name="app.version" content="${g.meta(name:'app.version')}"/>
  <meta name="app.build" content="${g.meta(name:'app.build')}"/>
  <meta name="description" content="Atlas of Living Australia"/>
  <title><g:layoutTitle /></title>
  <link rel="stylesheet" href="http://www.ala.org.au/wp-content/themes/ala2011/style.css" type="text/css" media="screen" />
  <link rel="stylesheet" href="http://www.ala.org.au/wp-content/themes/ala2011/css/wp-styles.css" type="text/css" media="screen" />
  <link rel="stylesheet" href="http://www.ala.org.au/wp-content/themes/ala2011/css/buttons.css" type="text/css" media="screen" />
  <link rel="icon" type="image/x-icon" href="http://www.ala.org.au/wp-content/themes/ala2011/images/favicon.ico" />
  <link rel="shortcut icon" type="image/x-icon" href="http://www.ala.org.au/wp-content/themes/ala2011/images/favicon.ico" />
  <link rel="stylesheet" type="text/css" media="screen" href="http://www.ala.org.au/wp-content/themes/ala2011/css/jquery.autocomplete.css" />
  <link rel="stylesheet" type="text/css" media="screen" href="http://www.ala.org.au/wp-content/themes/ala2011/css/search.css" />
  <link rel="stylesheet" type="text/css" media="screen" href="http://www.ala.org.au/wp-content/themes/ala2011/css/skin.css" />
  <link rel="stylesheet" type="text/css" media="screen" href="http://www.ala.org.au/wp-content/themes/ala2011/css/sf.css" />

  %{--<link rel="stylesheet" href="${resource(dir:'css/smoothness',file:'jquery-ui-1.8.14.custom.css')}" type="text/css" media="screen"/>--}%

  %{--<script language="JavaScript" type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>--}%

  <script language="JavaScript" type="text/javascript" src="${grailsApplication.config.ala.baseURL}/wp-content/themes/ala/scripts/iframe.js"></script>
  <script language="JavaScript" type="text/javascript" src="${grailsApplication.config.ala.baseURL}/wp-content/themes/ala/scripts/form.js"></script>
  <script language="JavaScript" type="text/javascript" src="${resource(dir:'js',file:'jquery.min.js')}"></script>
  <script language="JavaScript" type="text/javascript" src="${grailsApplication.config.ala.baseURL}/wp-content/themes/ala/scripts/ui.core.js"></script>
  <script language="JavaScript" type="text/javascript" src="${grailsApplication.config.ala.baseURL}/wp-content/themes/ala/scripts/ui.tabs.js"></script>
  <script language="JavaScript" type="text/javascript" src="${grailsApplication.config.ala.baseURL}/wp-content/themes/ala/scripts/hoverintent-min.js"></script>
  <script language="JavaScript" type="text/javascript" src="${grailsApplication.config.ala.baseURL}/wp-content/themes/ala2011/scripts/html5.js"></script>
  <script language="JavaScript" type="text/javascript" src="${grailsApplication.config.ala.baseURL}/wp-content/themes/ala/scripts/superfish/superfish.js"></script>
  <script language="JavaScript" type="text/javascript" src="${grailsApplication.config.ala.baseURL}/wp-content/themes/ala/scripts/jquery.autocomplete.js"></script>
  <script language="JavaScript" type="text/javascript" src="${grailsApplication.config.ala.baseURL}/wp-content/themes/ala/scripts/uservoice.js"></script>
  <g:layoutHead />
  <link rel="stylesheet" href="${resource(dir:'css',file:'public.css')}"/>
  <script type="text/javascript">
    // initialise plugins
    jQuery(function(){
      jQuery('ul.sf').superfish( {
        delay:500,
        autoArrows:false,
        dropShadows:false
      });

      jQuery("form#search-form input#search").autocomplete('http://bie.ala.org.au/search/auto.jsonp', {
        extraParams: {limit: 100},
        dataType: 'jsonp',
        parse: function(data) {
          var rows = new Array();
          data = data.autoCompleteList;
          for(var i=0; i<data.length; i++){
            rows[i] = {
              data:data[i],
              value: data[i].matchedNames[0],
              result: data[i].matchedNames[0]
            };
          }
          return rows;
        },
        matchSubset: false,
        formatItem: function(row, i, n) {
          return row.matchedNames[0];
        },
        cacheLength: 10,
        minChars: 3,
        scroll: false,
        max: 10,
        selectFirst: false
      });
      jQuery("form#search-inpage input#search").autocomplete('http://bie.ala.org.au/search/auto.jsonp', {
        extraParams: {limit: 100},
        dataType: 'jsonp',
        parse: function(data) {
          var rows = new Array();
          data = data.autoCompleteList;
          for(var i=0; i<data.length; i++){
            rows[i] = {
              data:data[i],
              value: data[i].matchedNames[0],
              result: data[i].matchedNames[0]
            };
          }
          return rows;
        },
        matchSubset: false,
        formatItem: function(row, i, n) {
          return row.matchedNames[0];
        },
        cacheLength: 10,
        minChars: 3,
        scroll: false,
        max: 10,
        selectFirst: false
      });
    });
  </script>
</head>
<body class="${pageProperty(name:'body.class')}">

<hf:banner logoutUrl="${grailsApplication.config.grails.serverURL}/proxy/logout"/>

<hf:menu/>

<g:layoutBody />

<hf:footer/>

<script type="text/javascript">
  var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
  document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
  var pageTracker = _gat._getTracker("UA-4355440-1");
  pageTracker._initData();
  pageTracker._trackPageview();
</script>
<script type="text/javascript">
  // show warning if using IE6
  if ($.browser.msie && $.browser.version.slice(0,1) == '6') {
    $('#header').prepend($('<div style="text-align:center;color:red;">WARNING: This page is not compatible with IE6.' +
            ' Many functions will still work but layout and image transparency will be disrupted.</div>'));
  }
</script>
</body>
</html>