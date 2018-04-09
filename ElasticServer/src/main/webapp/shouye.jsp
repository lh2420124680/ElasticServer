<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>	
<%-- <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> --%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en" class="no-js">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<base href="<%=basePath%>">
<script type="text/javascript" src="js/jquery-1.9.1.js"></script>
<link rel="stylesheet" href="chrome-search://local-ntp/theme.css"></link>
  <link rel="stylesheet" href="chrome-search://local-ntp/local-ntp.css"></link>
  <script src="chrome-search://local-ntp/config.js"></script>
  <script src="chrome-search://local-ntp/local-ntp.js"></script>
<style type="text/css">

</style>
</head>

<body>
  <div id="ntp-contents">
    <div id="logo" title="Google"></div>
    <div id="fakebox">
      <div id="fakebox-text"></div>
      <input id="fakebox-input" autocomplete="off" tabIndex="-1" type="url"
          aria-hidden="true">
      <div id="cursor"></div>
    </div>
    <div id="most-visited">
      <!-- The container for the tiles. The MV iframe goes in here. -->
      <div id="mv-tiles"></div>
      <!-- Notification shown when a tile is blacklisted. -->
      <div id="mv-notice" class="mv-notice-hide">
        <span id="mv-msg"></span>
        <!-- Links in the notification. -->
        <span id="mv-notice-links">
          <span id="mv-undo" tabIndex="1"></span>
          <span id="mv-restore" tabIndex="1"></span>
          <div id="mv-notice-x" tabIndex="1"></div>
        </span>
      </div>
    </div>
    <div id="attribution"><div id="attribution-text"></div></div>
  </div>
</body>
</html>
