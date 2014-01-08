<!doctype html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<title><g:layoutTitle default="Grails"/></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
		<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
		<g:layoutHead/>
		<r:require modules="jquery, application" />
        <r:layoutResources />
	</head>
	<body>
		
		<div id="header" role="banner">
			<g:link controller="home"><img src="${resource(dir: 'images', file: grailsApplication.config.daweb3.logo)}" alt="Grails"/></g:link>
			<h1>Web Konsole</h1>
			<div id="header-menu">
				<g:if test="${session.contractor}">
					<g:link controller="contractor" action="logout">Logout</a></g:link>
				</g:if>
			</div>
		</div>
		
		<div id="page-body">
			<g:layoutBody/>
		</div>
		<div align="center">
		<div class="footer" role="contentinfo"><g:meta name="app.name"/> <g:meta name="app.version"/>, @Historisch-Kulturwissenschaftliche Informationsverarbeitung 2011-2013</div>
		</div><div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
		
        <r:layoutResources />
        
	</body>
</html>
