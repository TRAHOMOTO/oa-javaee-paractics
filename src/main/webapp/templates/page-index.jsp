<%@ page import="static java.util.Objects.*" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="lang" value="${not empty param.lang ? param.lang : not empty lang ? lang : pageContext.request.locale}" scope="session" />

<%--<fmt:setLocale value="${lang}" scope="session"/>--%>
<fmt:setBundle basename="${bundleBaseName}"/>

<%
    if (isNull(request.getAttribute("token"))) {
        response.sendError(404, "Sorry, nothing to found!");
    }
%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="${lang}">

<head>
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet">
    <title><fmt:message key="page.title"/></title>
</head>

<body>
<nav class="navbar navbar-inverse navbar-fixed-top" role="navigation" style="margin-bottom: 100px">
    <div class="container">
        <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav navbar-right">
                <li <c:if test="${lang == 'ua'}">
                    class="active"</c:if>>
                    <a href="?lang=ua">UA</a>
                </li>
                <li <c:if test="${lang == 'ru'}">class="active"</c:if>>
                    <a href="?lang=ru">RU</a>
                </li>
                <li <c:if test="${lang == 'en'}">class="active"</c:if>>
                    <a href="?lang=en">EN</a>
                </li>
            </ul>
        </div><!--/.nav-collapse -->
    </div>
</nav>
<div class="container" style="margin-top: 40px">
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header"><fmt:message key="page.title"/></h1>
        </div>
        <!-- /.col-lg-12 -->
    </div>
    <!-- /.row -->
    <div class="row">
        <div class="col-lg-12">
            <div class="panel panel-primary">
                <!-- /.panel-heading -->
                <div class="panel-body">
                    <c:if test="${hasError == true}">
                        <div class="alert alert-danger">
                            <c:if test="${errMessages.size() > 0}">
                                <ul>
                                    <c:forEach var="entry" items="${errMessages}">
                                        <li><strong>${entry.key}</strong> ${entry.value}</li>
                                    </c:forEach>
                                </ul>
                            </c:if>
                            ${errMessage}
                        </div>
                    </c:if>
                    <c:if test="${hasSuccessMassages == true}">
                        <div class="alert alert-success">
                            <ul>
                                <c:forEach var="entry" items="${infoMessages}">
                                    <li><strong>${entry.key}</strong> : ${entry.value}</li>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:if>

                    <form role="form" action="<c:url value="/index"/>" method="post" enctype="multipart/form-data">
                        <div class="form-group">
                            <label for="photo"><fmt:message key="form.choose.file"/></label>
                            <input type="file" id="photo" name="${imgFieldName}" accept="image/jpeg, image/png" multiple>
                            <p class="help-block"><fmt:message key="form.images.allowed"/></p>
                        </div>
                        <button type="submit" class="btn btn-primary"><fmt:message key="form.submit"/></button>
                        <input type="hidden" name="form_token" value="${form_token}">
                    </form>
                </div>
                <!-- /.panel-body -->
            </div>
            <!-- /.panel -->
        </div>
        <div class="col-lg-12">
            <div class="panel panel-default">
                <!-- /.panel-heading -->
                <div class="panel-body">
                    <c:forEach var="image" items="${images}">
                        <a href="<c:url value="${image}"/>"><img height="200" src="<c:url value="${image}"/>"/></a>
                    </c:forEach>
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