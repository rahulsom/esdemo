<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Patients</title>
        <meta name="layout" content="main"/>
    </head>

    <body>
        <g:form action="create" class="form-inline">
            <div class="form-group">
                <label class="sr-only" for="authority">Authority</label>
                <input type="text" class="form-control" id="authority" placeholder="Authority" name="authority">
            </div>

            <div class="form-group">
                <label class="sr-only" for="identifier">Identifier</label>
                <input type="text" class="form-control" id="identifier" placeholder="Identifier" name="identifier">
            </div>

            <div class="form-group">
                <label class="sr-only" for="name">Name</label>
                <input type="text" class="form-control" id="name" placeholder="Name" name="name">
            </div>
            <g:submitButton name="Create" class="btn btn-default"/>
        </g:form>

        <table class="table">
            <thead>
                <tr>
                    <th>Authority</th>
                    <th>Identifier</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${patientAggregateList}">
                    <tr>
                        <td>${it.authority}</td>
                        <td><g:link action="show" params="[identifier: it.identifier, authority: it.authority]"
                            >${it.identifier}</g:link></td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </body>
</html>