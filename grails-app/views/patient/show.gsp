<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Patient</title>
        <meta name="layout" content="main"/>
        <style>
        tr.snapshot {
            background-color: #006dba;
        }
        </style>
    </head>

    <body>
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
        <ul class="nav nav-tabs">
            <li role="presentation" class="active"><a href="#values">Values</a></li>
            <li role="presentation"><a href="#audit">Audit</a></li>
            <g:if test="${!params.version}">
                <li role="presentation"><a href="#actions">Actions</a></li>
            </g:if>
        </ul>

        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="values">
                <br>
                <table class="table">
                    <g:each in="${patientSnapshot.properties.toSorted { a, b -> a.key <=> b.key }}" var="entry">
                        <tr>
                            <td>${entry.key}</td>
                            <td>
                                <%
                                    if (entry.value instanceof Number || entry.value instanceof String || entry.value instanceof Boolean) {
                                %><code><%=entry.value%></code><%
                                } else if (entry.value instanceof List || entry.value instanceof Set) {
                            %><ul><%
                                    entry.value.each {
                            %><li><code><%=it as grails.converters.JSON%></code></li><%
                                    }
                            %></ul><%
                                } else if (entry.value instanceof List || entry.value instanceof Set) {
                            %><code><%=entry.value as grails.converters.JSON%></code><%
                                }
                            %>
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>

            <div role="tabpanel" class="tab-pane" id="audit">
                <br>
                <!-- ${snapshotted} -->
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
                    <tbody>
                        <g:each in="${events}" var="event">
                            <g:if test="${snapshotted.contains(event.id)}">
                                <tr class="snapshot">
                                    <td colspan="5">&nbsp;</td>
                                </tr>
                            </g:if>
                            <tr class="${event.revertedBy ? 'reverted' : ''}">
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
                                            <g:if test="${!params.version}">
                                                <g:link controller="patient" action="revertEvent" class="btn btn-danger"
                                                        params="[identifier: params.identifier,
                                                                 authority: params.authority, eventId   : event.id]">
                                                    Revert
                                                </g:link>
                                            </g:if>
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
                <br>
                <g:form action="delete" class="form-inline">
                    <g:hiddenField name="authority" value="${params.authority}"/>
                    <g:hiddenField name="identifier" value="${params.identifier}"/>
                    <div class="form-group">
                        <label class="sr-only" for="deleteReason">Reason</label>
                        <input type="text" class="form-control" id="deleteReason" placeholder="Reason" name="reason">
                    </div>
                    <g:submitButton name="Delete" class="btn btn-default"/>
                </g:form>
            </div>
        </div>

    </body>
</html>