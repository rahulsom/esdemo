<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Patient</title>
        <meta name="layout" content="main"/>
    </head>

    <body>
        <ul class="nav nav-tabs">
            <li role="presentation" class="active"><a href="#values">Values</a></li>
            <li role="presentation"><a href="#audit">Audit</a></li>
            <li role="presentation"><a href="#actions">Actions</a></li>
        </ul>

        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="values">
                <table class="table">
                    <g:each in="${patientSnapshot.properties.toSorted { a, b -> a.key <=> b.key }}" var="entry">
                        <tr>
                            <td>${entry.key}</td>
                            <td>
                                <code><%
                                    if (entry.value instanceof Number || entry.value instanceof String) {
                                        out.println entry.value
                                    } else {
                                        out.println(entry.value as grails.converters.JSON)
                                    }
                                %></code>
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>

            <div role="tabpanel" class="tab-pane" id="audit">
                <table class="table">
                    <thead>
                        <tr>
                            <td>Id</td>
                            <td>Time</td>
                            <td>Person</td>
                            <td>Type</td>
                            <td>Data</td>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${events}" var="event">
                            <tr>
                                <td>${event.id}</td>
                                <td>${event.dateCreated}</td>
                                <td>${event.createdBy}</td>
                                <td>${event.class.simpleName}</td>
                                <td>${event.audit}</td>
                            </tr>
                        </g:each>

                    </tbody>
                </table>
            </div>

            <div role="tabpanel" class="tab-pane" id="actions">
                <g:form action="changeName" class="form-inline">
                    <g:hiddenField name="authority" value="${params.authority}"/>
                    <g:hiddenField name="identifier" value="${params.identifier}"/>
                    <div class="form-group">
                        <label class="sr-only" for="name">Name</label>
                        <input type="text" class="form-control" id="name" placeholder="Name" name="name">
                    </div>
                    <g:submitButton name="Change Name" class="btn btn-default"/>
                </g:form>
            </div>
        </div>

    </body>
</html>