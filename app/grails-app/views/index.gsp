<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Welcome to Grails</title>
        <style type="text/css" media="screen">
            #status {
                background-color: #eee;
                border: .2em solid #fff;
                padding: 1em;
                -moz-box-shadow: 0 0 1.25em #ccc;
                -webkit-box-shadow: 0 0 1.25em #ccc;
                box-shadow: 0 0 1.25em #ccc;
                -moz-border-radius: 0.6em;
                -webkit-border-radius: 0.6em;
                border-radius: 0.6em;
            }

            a.skip {
                position: absolute;
                left: -9999px;
            }

        </style>
    </head>
    <body>

        <a href="#page-body" class="skip"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="row">

            <div id="status" role="complementary" class="col-md-3">
                <h4>Application Status</h4>
                <ul class="list-unstyled">
                    <li>Environment: ${grails.util.Environment.current.name}</li>
                    <li>App profile: ${grailsApplication.config.grails?.profile}</li>
                    <li>App version: <g:meta name="info.app.version"/></li>
                    <li>Grails version: <g:meta name="info.app.grailsVersion"/></li>
                    <li>Groovy version: ${GroovySystem.getVersion()}</li>
                    <li>JVM version: ${System.getProperty('java.version')}</li>
                    <li>Reloading active: ${grails.util.Environment.reloadingAgentEnabled}</li>
                </ul>
                <h4>Artefacts</h4>
                <ul class="list-unstyled">
                    <li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
                    <li>Domains: ${grailsApplication.domainClasses.size()}</li>
                    <li>Services: ${grailsApplication.serviceClasses.size()}</li>
                    <li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
                </ul>
                <h4>Installed Plugins</h4>
                <ul class="list-unstyled">
                    <g:each var="plugin" in="${applicationContext.getBean('pluginManager').allPlugins}">
                        <li>${plugin.name} - ${plugin.version}</li>
                    </g:each>
                </ul>
            </div>
            <div id="page-body" role="main" class="col-md-9">
                <h1>Welcome to Grails</h1>
                <p>Congratulations, you have successfully started your first Grails application! At the moment
                this is the default page, feel free to modify it to either redirect to a controller or display whatever
                content you may choose. Below is a list of controllers that are currently deployed in this application,
                click on each to execute its default action:</p>

                <div id="controller-list" role="navigation">
                    <h2>Available Controllers:</h2>
                    <ul class="list-unstyled">
                        <g:each var="c" in="${grailsApplication.controllerClasses.sort { it.fullName } }">
                            <li class="controller"><g:link controller="${c.logicalPropertyName}">${c.fullName}</g:link></li>
                        </g:each>
                    </ul>
                </div>
            </div>
        </div>
    </body>
</html>
