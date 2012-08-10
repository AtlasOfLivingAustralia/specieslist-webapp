<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <gui:resources components="['dialog', 'autoComplete', 'richEditor']"/>
    <title>Dialog Demo</title>
</head>

<body class="yui-skin-sam">
<h1>Dialog Demo</h1>

<!--
        This javascript is an event handler for dialog1.  It is passed in by name
        as a button handler in the 'buttons' attribute below
    -->
<script>
    var yesHandler = function(o) {
        alert('You clicked "Yes"');
        this.cancel();
    }
</script>

<!--
        Example of a dialog that defines its own buttons, as well as the handlers for each button.
        If you define your own button, a default event handler will not be provided for you, you must
        provide one yourself, or else the button will do nothing.
    -->
<div class="yui-skin-sam">
    <gui:dialog
            id="dialog1"
            title="Text Dialog"
            triggers="[
                    show:[id:'show1', on:'click'],
                    hide:[id:'hide1', on:'click']
            ]"
            width="300px"
            buttons="[
                    [text:'Yes', handler: 'yesHandler', isDefault: true],
                    [text:'No', handler: 'function() {this.cancel();}', isDefault: false]
            ]">
        This is within a simple dialog with two buttons.  Each the yes button calls a javascript function
        called 'yesHandler', which was defined just before this tag was rendered.
    </gui:dialog>
</div>
<!--
        This modal dialog contains not just text, but HTML markup.  The width is default, and the buttons are
        default, which provides one 'OK' button that closes the dialog.
    -->
<gui:dialog
        id="dialog2"
        title="Fixed Modal Dialog"
        draggable="false"
        modal="true"
        triggers="[show:[id:'show2', on:'click']]">
    <img src="${createLinkTo(dir:'/images', file:'thumbsup.jpg')}" alt="thumbs up!"/>
</gui:dialog>

<!--
        This dialog contains not just markup, but a heavy GrailsUI component, the richEditor, within
        its body
    -->
<gui:dialog
        id="dialog3"
        title="Editor Dialog"
        triggers="[
                show:[id:'show3', on:'click'],
                hide:[id:'hide3', on:'click']
        ]">

    <gui:richEditor
            id='testEditor'
            value="Edit me!"/>

</gui:dialog>

<!--
        Contains an autoComplete tag, calls the server with selection, renders result back to div on page
    -->
<gui:dialog
        id="dialog4"
        title="AutoComplete Fruit Dialog"
        width="300px"
        triggers="[
                show:[id:'show4', on:'click'],
                hide:[id:'hide4', on:'click']
        ]"
        form="true"
        url="demo/ajaxDialogSelectionMade?id=7"
        update="replaceMe">
    <gui:autoComplete
            name="selection"
            id="fruit_ac"
            resultName="TestData"
            labelField="description"
            idField="id"
            controller="demo"
            action="testFruitDataAsJSON"
            forceSelection="true"/>
    <br/><br/>
</gui:dialog>

<!--
        Contains an autoComplete tag, calls the server with selection, renders result back to div on page
    -->
<gui:dialog
        id="beer_ac"
        title="AutoComplete Beer Dialog"
        width="300px"
        triggers="[show:[type:'button', text:'button trigger', on:'click']]"
        form="true"
        controller="demo"
        action="ajaxDialogSelectionMade"
        params="[id:7, a:'a', b:'b']"
        update="replaceMe">
    <!-- this autoComplete has no id... one is generated for it -->
    <gui:autoComplete
            name="selection"
            resultName="TestData"
            labelField="description"
            idField="id"
            controller="demo"
            action="testBeerDataAsJSON"
            forceSelection="true"/>
    <br/><br/>
</gui:dialog>


<!--
        This javascript is an event handler for dialog4.  It is passed in by name
        as a button handler in the 'buttons' attribute below
    -->
<script>
    var dlg4Handler = function(o) {
        alert('You clicked "Submit"');
        this.cancel();
    }
</script>

<!--
        This dialog wraps up a link that triggers it when clicked
    -->
<gui:dialog
        id="dialog6"
        title="Triggered by a link"
        triggers="[show:[type:'link', text:'Link trigger', on:'click']]">

    <img src="${createLinkTo(dir:'/images', file:'marshmallow.gif')}" alt="Happy Marshmallow!"/>
    <img src="${createLinkTo(dir:'/images', file:'marshmallow.gif')}" alt="Happy Marshmallow!"/>
    <img src="${createLinkTo(dir:'/images', file:'marshmallow.gif')}" alt="Happy Marshmallow!"/>
    <br/>
    <img src="${createLinkTo(dir:'/images', file:'marshmallow.gif')}" alt="Happy Marshmallow!"/>
    <img src="${createLinkTo(dir:'/images', file:'marshmallow.gif')}" alt="Happy Marshmallow!"/>
    <img src="${createLinkTo(dir:'/images', file:'marshmallow.gif')}" alt="Happy Marshmallow!"/>

</gui:dialog>
<br/>
<br/>

<!--
            This dialog wraps up a link that triggers it on mouseover
        -->
<gui:dialog
        id="dialog7"
        title="Triggered by a link"
        triggers="[show:[type:'link', text:'Mouseover trigger', on:'mouseover']]">

    <img src="${createLinkTo(dir:'/images', file:'marshmallow.gif')}" alt="Happy Marshmallow!"/>
    <img src="${createLinkTo(dir:'/images', file:'marshmallow.gif')}" alt="Happy Marshmallow!"/>
    <img src="${createLinkTo(dir:'/images', file:'marshmallow.gif')}" alt="Happy Marshmallow!"/>
    <br/>
    <img src="${createLinkTo(dir:'/images', file:'marshmallow.gif')}" alt="Happy Marshmallow!"/>
    <img src="${createLinkTo(dir:'/images', file:'marshmallow.gif')}" alt="Happy Marshmallow!"/>
    <img src="${createLinkTo(dir:'/images', file:'marshmallow.gif')}" alt="Happy Marshmallow!"/>

</gui:dialog>
<br/>
<br/>

<table>
    <!--
            The buttons that trigger dialog 1
        -->
    <tr>
        <td>
            <button id="show1">Show Text Dialog</button>
        </td>
        <td>
            <button id="hide1">Hide Text Dialog</button>
        </td>
    </tr>

    <!--
            The buttons that trigger dialog 2
        -->
    <tr>
        <td>
            <button id="show2">Show Modal Markup Dialog</button>
        </td>
        <td>
        </td>
    </tr>

    <!--
            The buttons that trigger dialog 3
        -->
    <tr>
        <td>
            <button id="show3">Show Rich Editor Dialog</button>
        </td>
        <td>
            <button id="hide3">Hide Rich Editor Dialog</button>
        </td>
    </tr>

    <!--
            The buttons that trigger dialog 4
        -->
    <tr>
        <td>
            <button id="show4">Show Fruit Autocomplete Dialog</button>
        </td>
        <td>
            <button id="hide4">Hide Fruit Autocomplete Dialog</button>
        </td>
    </tr>
</table>

<br/><br/>
<div id="replaceMe" style="padding:20px;width:300px;border:solid 1px red; background-color:#EEE">
    This content was here when the page loaded... it will be replaced by an autoCompete selection made in the AutoComplete Dialog
</div>

</body>

</html>
%{--<!doctype html>--}%
%{--<html>--}%
	%{--<head>--}%
		%{--<meta name="layout" content="ala2"/>--}%
		%{--<title>Grails template | Atlas of Living Australia</title>--}%
	%{--</head>--}%
	%{--<body>--}%
    %{--<div id="content">--}%
        %{--<header id="page-header">--}%
            %{--<div class="inner">--}%
                %{--<nav id="breadcrumb">--}%
                    %{--<ol>--}%
                        %{--<li><a href="http://www.ala.org.au">Home</a></li>--}%
                        %{--<li><a href="#">Species Lists</a></li>--}%
                        %{--<li class="last">ALA Grails Template</li>--}%
                    %{--</ol>--}%
                %{--</nav>--}%
                %{--<h1>ALA Grails Template - Hack me</h1>--}%
            %{--</div><!--inner-->--}%
        %{--</header>--}%
        %{--<div class="inner">--}%
            %{--<div id="section" class="col-wide">--}%
			    %{--<p>Congratulations, you have successfully started your first <b>ALA Grails application!</b> At the moment--}%
			   %{--this is the default page, and it comes from <b>PROJECT-ROOT/grails-app/views/index.gsp</b></p>--}%
            %{--</div>--}%
		%{--</div>--}%
    %{--</div> <!-- content div -->--}%
	%{--</body>--}%
%{--</html>--}%
