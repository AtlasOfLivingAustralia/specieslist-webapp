<html>
<head>
<title>BIE - Profiler Index - Debug tool</title>
<link rel="stylesheet" href="/bie-hbase/css/default.css" />
<script src="/bie-hbase/js/jquery.js" type="text/javascript"></script>
<script><!--

function query(){

	var query = $("#query").val();
	
	$.getJSON("query?q="+query,
   function(data){
    $("#resultsTable").empty();
    if(data.length==0){
    	$("#resultsTable").append("<tr><td>No results</td></tr>");
    }
    
    for(var i=0; i< data.length; i++){
    	var trow = $("<tr>");
    	$("<td>").addClass("tableCell").text(data[i].id).appendTo(trow);
    	var link = $("<a>").attr("href","taxon?guid="+data[i].id).text(data[i].text);
    	$("<td>").addClass("tableCell").append(link).appendTo(trow);
    	$("#resultsTable").append(trow);
    }


    
   });
}

--></script>
</head>
<body>
  <h1>Profile debug page</h1>
  
  <form action="javascript:query();" >
  
  <input type="text" value="" name="query" id="query" size="50"/>  
  <input type="submit" onclick="javascript:query();">
  </form>
  
  <div id="results">
    <h3 id="queryString"></h3>
    <table id="resultsTable">
    </table>
  </div>
  
  <div id="detail">
    <h3 id="detailString"></h3>
  
  </div>
  
</body>
</html>
