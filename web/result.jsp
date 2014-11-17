<%@ page import="sql.Config" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.DriverManager" %>
<%@ page import="java.sql.ResultSet" %>
<%--
  Created by IntelliJ IDEA.
  User: dangchienbn
  Date: 14/11/2014
  Time: 21:12
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<sql:setDataSource driver="com.mysql.jdbc.Driver"
                   url="jdbc:mysql://localhost/amobi.vn; databaseName=amobi.vn;" user="root"
                   password="" var="conn" />
<html>
<head>
    <title></title>
</head>
<body>
<sql:setDataSource driver="com.microsoft.sqlserver.jdbc.SQLServerDriver"
                   url="jdbc:sqlserver://localhost\\hai:1443;databaseName=test;" user="sa"
                   password="****" var="conn" />
<sql:query var="users" dataSource="${conn}">
    SELECT * FROM [USER]
</sql:query>

<table border="1">
    <tr>
        <c:forEach var="colName" items="${users.columnNames}">
            <th>
                <c:out value="${colName}" />
            </th>
        </c:forEach>
    </tr>
    <c:forEach items="${users.rowsByIndex}" var="row">
        <tr>
            <c:forEach var="col" items="${row}">
                <td>
                    <c:out value="${col}" />
                </td>
            </c:forEach>
        </tr>
    </c:forEach>
</table>
</body>
</html>
