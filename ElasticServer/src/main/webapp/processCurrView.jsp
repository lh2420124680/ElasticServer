<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>查看当前流程图</title>
</head>
<body>
<img style="position: absolute;top: 0px;left: 0px"  src="/mp/Expense/ProcessDefController/showView.do?deploymentId=${deploymentId}&diagramResourceName=${diagramResourceName}">

<div style="position: absolute;border: 3px solid #05A7D8;top:${y-6}px;left:${x-6}px;width:${width+6}px;height:${height+6}px"></div>
</body>
</html>