<%@ page import="static java.util.Objects.*" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="${bundleBaseName}"/>

<c:if test="${param['lang'] != null}">
    <fmt:setLocale value="${param['lang']}" scope="session"/>
</c:if>

<%
    if (isNull(request.getAttribute("token"))) {
        response.sendError(404, "Sorry, nothing to found!");
    }
%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ua">

<head>
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet">
    <title><fmt:message key="page.title"/></title>
</head>

<body>
<div class="container">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Tables</h1>
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
        <div class="row">
            <div class="col-lg-12">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <fmt:message key="page.title"/>
                    </div>
                    <!-- /.panel-heading -->
                    <div class="panel-body">
                        <fmt:message key="page.title"/>
                    </div>
                    <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
            </div>
            <!-- /.col-lg-12 -->
        </div>
</div>
</body>
</html>