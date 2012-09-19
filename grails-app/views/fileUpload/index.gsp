<%--
  Created by IntelliJ IDEA.
  User: raja
  Date: 9/19/12
  Time: 10:29 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Upload XLS File</title>
  <meta name="layout" content="main" />
</head>
<body>
    <div class="body">
        <div class="messages">
            <g:message code="${flash.message}" />
        </div>
        <g:if test="${processed}">
            <g:form action="processFields" method="POST">
                <g:hiddenField name="name" value="${name}" />
                <div>Please select the fields that you want stored in the database:</div>
                <div>
                    <g:select name="selectedFields" multiple="true" from="${fields}" />
                </div>
                <div>
                    <g:submitButton name="submit" value="Create Table" />
                </div>
            </g:form>
        </g:if>
        <g:else>
            <div class="floatLeft">
                <div>Please upload an Excel file.</div>
                <fileuploader:form 	upload="docs"
                                       successAction="index"
                                       successController="fileUpload"
                                       errorAction="index"
                                       errorController="fileUpload"
                />
            </div>

            <div class="models">
                Goto: <g:select name="model" from="${domains.entrySet()}"
                                optionKey="key"
                                optionValue="value"
                                noSelection="['-1':'Select One']"
                                />
            </div>
        </g:else>
    </div>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            jQuery('#selectedFields').find('option').attr('selected', 'selected');
            jQuery('#model').change(function() {
                if (jQuery(this).val() != -1) {
                    location.href="/DataLoader/ddc/list?dc=" + jQuery(this).val();
                }
            })
        });
    </script>
</body>
</html>