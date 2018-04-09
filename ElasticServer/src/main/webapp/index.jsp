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
</head>

<body>
	<input type="text" id="sh" name="sh" />
	<input type="button" id="su" name="search" value="搜索" />
	学段:
	<select id="stage"> 
		<option value="">全部</option> 
		<option value="c5bd53cce390d59c0a28b54f6dd00d6a">小学</option> 
	</select> 
	学科:
	<select id="subject"> 
		<option value="">全部</option> 
		<option value="47af3bf9e73cc5c815b104b921c19236">语文</option> 
		<option value="f41cf96b069ca77102c13f22d375de92">数学</option> 
	</select> 
	版本:
	<select id="version"> 
		<option value="">全部</option> 
		<option value="7FA88535212344A1A7C97F9B10AB624D">人民教育出版社(新课程标准)</option> 
		<option value="3473E8084E72419F90EAAF1624EE8911">江苏教育出版社</option> 
	</select>
	教材:
	<select id="book"> 
		<option value="">全部</option> 
		<option value="119480">一年级上册(2016新版)</option> 
		<option value="7741">一年级上册</option> 
		<option value="7742">一年级下册</option> 
	</select>
	类型:
	<select id="type"> 
		<option value="">全部</option> 
		<option value="a9bbba51bc8d443a8bdedbf69c9f4942">随堂课件</option> 
		<option value="d3df29d2e62f93bf2551254a2589df56">教学设计</option> 
		<option value="8dab6bb961991cb76358ff4d72c1935f">素材</option> 
	</select> <br>
	<div style="border:3px solid green;width: 50%">
		学段gid:<font id="st1"></font> <br>
		学科gid:<font id="st2"></font> <br>
		版本gid:<font id="st3"></font> <br>
		教材gid:<font id="st4"></font> <br>
		类型gid:<font id="st5"></font> <br>
	</div>
	<div>一共搜索到<font id="sear" style="color:red;"></font>条数据!</div>
	<!-- <div><input type="button" id="pre" value="上一页" />&nbsp;&nbsp;<font id="currpage">1</font>/<font id="totalpage">10</font>&nbsp;&nbsp;<input type="button" id="next" value="下一页" /></div> -->
	<div id="ul">
		<!-- <div id="item">
			标题:<div id="title"></div>
			内容:<div id="wrap"></div>
			创建人:<font id="creator">罗浩</font></br>
			创建时间:<font id="creatTime">2017-07-03</font> 
			<p><HR style="FILTER: alpha(opacity=100,finishopacity=0,style=3)" width="100%" color=#987cb9 SIZE=3></p>
		</div> -->
	</div>
	
	<script type="text/javascript">
		$("#su").click(function(){
			var search = $("#sh").val();
			var stage_gid=$("#stage option:selected").val();
			var subject_value=$("#subject option:selected").val();
			var book_version=$("#version option:selected").val();
			var text_book_gid=$("#book option:selected").val();
			var type_value=$("#type option:selected").val();
			
			$("#st1").html(stage_gid);
			$("#st2").html(subject_value);
			$("#st3").html(book_version);
			$("#st4").html(text_book_gid);
			$("#st5").html(type_value);
			$.ajax({
				   type: "POST",
				   dataType:"json",
				   url: "${pageContext.request.contextPath}/es/search.ashx",
				   data: {"resourcename":search,"curr":1,"stage_gid":stage_gid,"subject_value":subject_value,"book_version":book_version,"text_book_gid":text_book_gid,"type_value":type_value},
				   success: function(msg){
				   	debugger
					  var data = msg.data;
					  var str = "";
					  for(var i = 0;i<data.length;i++){
					  var resourcename = data[i].resourcename;
					  var viewnum = data[i].viewnum;
					  var uploadername = data[i].uploadername;
					  var submittimestr = data[i].submittimestr;
					  
					  var favoriteNum = data[i].favoriteNum;
					  var downloadNum = data[i].downloadNum;
					  
					  var stage_gid = data[i].stage_gid;
					  var subject_value = data[i].subject_value;
					  var book_version = data[i].book_version;
					  var text_book_gid = data[i].text_book_gid;
					  var type_value = data[i].type_value;
					  str += '<div id="item">'+
							'资源名称:<div id="resourcename">'+resourcename+'</div>'+
							'浏览量:<div id="viewnum">'+viewnum+'</div>'+
							'上传人:<font id="uploadername">'+uploadername+'</font></br>'+
							'上传时间:<font id="submittimestr">'+submittimestr+'</font></br> '+
							'收藏量:<font id="favoriteNum">'+favoriteNum+'</font></br> '+
							'下载量:<font id="downloadNum">'+downloadNum+'</font></br> '+
							'学段gid:<font id="stage_gid">'+stage_gid+'</font></br> '+
							'学科gid:<font id="subject_value">'+subject_value+'</font></br> '+
							'版本gid:<font id="book_version">'+book_version+'</font></br> '+
							'教材gid:<font id="text_book_gid">'+text_book_gid+'</font></br> '+
							'类型gid:<font id="type_value">'+type_value+'</font></br> '+
							'<p><HR style="FILTER: alpha(opacity=100,finishopacity=0,style=3)" width="100%" color=#987cb9 SIZE=3></p>'+
						'</div>';
					  }
					  $("#ul").html(str);
					  var total = msg.total;
					  $("#sear").html(total);
					  $("#totalpage").html(msg.totalpage);
				   }
				})
		});
		
		$("pre").click(function () {
			var curr = $("#currpage").html();
			curr = curr - 1;
			$("#currpage").html(curr);
			var search = $("#sh").val();
			$.ajax({
				   type: "POST",
				   dataType:"json",
				   url: "${pageContext.request.contextPath}/es/search.ashx",
				   data: {"resource_name":search,"curr":curr},
				   success: function(msg){
				   	debugger
					  var data = msg.data;
					  var str = "";
					  for(var i = 0;i<data.length;i++){
					  var resource_name = data[i].resource_name;
					  var masterial = data[i].masterial;
					  var rel_name = data[i].rel_name;
					  var submit_time = data[i].submit_time;
					  str += '<div id="item">'+
							'资源名称:<div id="title">'+resource_name+'</div>'+
							'教材:<div id="wrap">'+masterial+'</div>'+
							'创建人:<font id="creator">'+rel_name+'</font></br>'+
							'创建时间:<font id="creatTime">'+submit_time+'</font> '+
							'<p><HR style="FILTER: alpha(opacity=100,finishopacity=0,style=3)" width="100%" color=#987cb9 SIZE=3></p>'+
							'</div>';
					  }
					  $("#ul").html(str);
					  var total = msg.total;
					  $("#sear").html(total);
				   }
				})
		});
		
		$("next").click(function () {debugger
			var curr = $("#currpage").html();
			curr = curr + 1;
			$("#currpage").html(curr);
			var search = $("#sh").val();
			$.ajax({
				   type: "POST",
				   dataType:"json",
				   url: "${pageContext.request.contextPath}/es/search.ashx",
				   data: {"resource_name":search,"curr":curr},
				   success: function(msg){
				   	debugger
					  var data = msg.data;
					  var str = "";
					  for(var i = 0;i<data.length;i++){
					  var resource_name = data[i].resource_name;
					  var masterial = data[i].masterial;
					  var rel_name = data[i].rel_name;
					  var submit_time = data[i].submit_time;
					  str += '<div id="item">'+
							'资源名称:<div id="title">'+resource_name+'</div>'+
							'教材:<div id="wrap">'+masterial+'</div>'+
							'创建人:<font id="creator">'+rel_name+'</font></br>'+
							'创建时间:<font id="creatTime">'+submit_time+'</font> '+
							'<p><HR style="FILTER: alpha(opacity=100,finishopacity=0,style=3)" width="100%" color=#987cb9 SIZE=3></p>'+
						'</div>';
					  }
					  $("#ul").html(str);
					  var total = msg.total;
					  $("#sear").html(total);
				   }
				})
		});
	</script>
</body>
</html>
