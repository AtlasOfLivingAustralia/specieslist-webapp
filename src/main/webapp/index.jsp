<html>
<head>
<title>BIE - Profiler Index - Debug tool</title>
<script src="/bie-hbase/js/jquery.js" type="text/javascript"></script>
<script><!--

function query(){

	$("#queryString").text("Query string: "+$("#query").val())

	var query = $("#query").val();
	
	//do the ajax call

	//populate the table

	$.getJSON("query?q="+query,
	        function(data){
		        
		        for(var i=0; i< data.length; i++){

		        	var trow = $("<tr>");

		        	$("<td>").addClass("tableCell").text("dave").appendTo(trow);

		        	$("#resultsTable").append(trow);
		        }
	        });
	
}

function detail(guid){


}

--></script>
</head>
<body>
  <h2>Profile debug page</h2>
  
  <input type="text" value="" name="query" id="query"/>  
  <input type="submit" onclick="javascript:query();">
  
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
