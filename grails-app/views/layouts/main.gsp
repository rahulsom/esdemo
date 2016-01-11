<!doctype html>
<html lang="en" class="no-js">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title><g:layoutTitle default="Grails"/></title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        %{--<asset:stylesheet src="application.css"/>--}%
        %{--<asset:javascript src="application.js"/>--}%

        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
              integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
              crossorigin="anonymous">

        <!-- Optional theme -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css"
              integrity="sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r"
              crossorigin="anonymous">

        <style>
            .theme-showcase {
                margin-top: 5em;
            }
        </style>
        <g:layoutHead/>
    </head>

    <body>

        <!-- Fixed navbar -->
        <nav class="navbar navbar-inverse navbar-fixed-top">
            <div class="container">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar"
                            aria-expanded="false" aria-controls="navbar">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <g:link uri="/" class="navbar-brand">ES Demo</g:link>
                </div>

                <div id="navbar" class="navbar-collapse collapse">
                    <ul class="nav navbar-nav">
                        <li class="active"><g:link uri="/">Home</g:link></li>
                        <li><g:link controller="patient">Patient</g:link></li>
                    </ul>
                    <ul class="nav navbar-nav navbar-right">
                        <g:if test="${session.user}">
                            <li><g:link controller="auth" action="logout">Logout</g:link></li>
                        </g:if>
                        <g:else>
                            <g:form controller="auth" action="login" class="navbar-form navbar-left">
                                <div class="form-group">
                                    <input type="text" class="form-control" placeholder="username" name="user">
                                </div>
                                <g:submitButton name="Login" class="btn btn-default"/>
                            </g:form>
                        </g:else>

                    </ul>
                </div>
            </div>
        </nav>

        <div class="container theme-showcase" role="main">
            <g:layoutBody/>
            <div class="footer" role="contentinfo"></div>
        </div>

        <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt"
                                                                           default="Loading&hellip;"/></div>

        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <!-- Latest compiled and minified JavaScript -->
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
                integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS"
                crossorigin="anonymous"></script>
    </body>
</html>
