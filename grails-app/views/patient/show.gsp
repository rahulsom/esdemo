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
                <br>
                <g:if test="${params.version}">
                    <div class="alert alert-warning">
                        This is a snapshot in time. Click
                        <g:link controller="patient" action="show"
                                params="[identifier: params.identifier, authority: params.authority]">
                            here
                        </g:link>
                        to see latest.
                    </div>
                </g:if>
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
                <br>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Time</th>
                            <th>User</th>
                            <th>Type</th>
                            <th>Data</th>
                            <th></th>
                        </tr>
                    </thead>
                        <g:each in="${events}" var="event">
                            <tr class="${event.revertedBy ? 'reverted' : '' }">
                                <td>
                                    <g:link controller="patient" action="show"
                                            params="[identifier: params.identifier, authority: params.authority, version: event.id]">
                                        ${event.dateCreated}
                                    </g:link>
                                </td>
                                <td>${event.createdBy}</td>
                                <td>${event.class.simpleName}</td>
                                <td><code>${event.audit}</code></td>
                                <td>
                                    <g:if test="${event.class.simpleName != 'PatientCreated'}">
                                        <g:if test="${event.revertedBy}">
                                            Reverted by ${event.revertedBy}
                                        </g:if>
                                        <g:else>
                                            <g:link controller="patient" action="revertEvent" class="btn btn-danger"
                                                    params="[identifier: params.identifier, authority: params.authority, eventId: event.id]">
                                                Revert
                                            </g:link>
                                        </g:else>
                                    </g:if>
                                </td>
                            </tr>
                        </g:each>

                    </tbody>
                </table>
            </div>

            <div role="tabpanel" class="tab-pane" id="actions">
                <br>
                <g:form action="changeName" class="form-inline">
                    <g:hiddenField name="authority" value="${params.authority}"/>
                    <g:hiddenField name="identifier" value="${params.identifier}"/>
                    <div class="form-group">
                        <label class="sr-only" for="changeNameName">Name</label>
                        <input type="text" class="form-control" id="changeNameName" placeholder="Name" name="name">
                    </div>
                    <g:submitButton name="Change Name" class="btn btn-default"/>
                </g:form>
                <br>
                <g:form action="plan" class="form-inline">
                    <g:hiddenField name="authority" value="${params.authority}"/>
                    <g:hiddenField name="identifier" value="${params.identifier}"/>
                    <div class="form-group">
                        <label class="sr-only" for="planCode">Code</label>
                        <input type="text" class="form-control" id="planCode" placeholder="Code" name="code">
                    </div>
                    <g:submitButton name="Plan Procedure" class="btn btn-default"/>
                </g:form>
                <br>
                <g:form action="perform" class="form-inline">
                    <g:hiddenField name="authority" value="${params.authority}"/>
                    <g:hiddenField name="identifier" value="${params.identifier}"/>
                    <div class="form-group">
                        <label class="sr-only" for="performCode">Code</label>
                        <input type="text" class="form-control" id="performCode" placeholder="Code" name="code">
                    </div>
                    <g:submitButton name="Perform Procedure" class="btn btn-default"/>
                </g:form>
            </div>
        </div>

    </body>
</html>