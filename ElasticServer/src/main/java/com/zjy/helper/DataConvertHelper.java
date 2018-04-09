/**
 *  数据转换公共帮助类
 * @author  罗浩
 * @Create 2016年12月7日
 * @Version V1.0
 */
package com.zjy.helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import sun.misc.BASE64Encoder;

public class DataConvertHelper {

	public DataConvertHelper() {

	}

	/**
	 * 获取前段数据通用方法
	 * 
	 * @param request
	 * @return
	 * @author 罗浩
	 */
	public static Map<String, Object> getRequestParams(HttpServletRequest request) {
		Enumeration<String> names = request.getParameterNames();
		HashMap<String, Object> params = new HashMap<String, Object>();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			params.put(name, request.getParameter(name));
		}
		// 获取用户信息
//		String token = request.getParameter("token");
//		UserEntity userEntity = (UserEntity) SessionHelper.get(token);
//		params.put("USERNAME", userEntity.getUserName());
//		params.put("USERID", userEntity.getUserID());
		//params.put("USERINFO", userEntity);
		return params;
	}
	
	/**
	 * HashMap中value值为NULL转换为空字符串
	 * 
	 * @param request
	 * @return
	 * @author 罗浩
	 */
	public static Map<String, Object> convertNullString(Map<String, Object> map) {
		if(!map.isEmpty()) {
			Iterator<Map.Entry<String, Object>> entries = map.entrySet().iterator();
			Map.Entry<String, Object> entry = null;
			while (entries.hasNext()) {
				entry = entries.next();
				if ("NULL".equals(entry.getValue())){
					map.put(entry.getKey(), "");
				}
			}
		}
		return map;
	}

//	/**
//	 * obj 转 float 方法
//	 * 
//	 * @param obj
//	 * @return
//	 * @author 罗浩
//	 */
//	public static Float ConvertToFloat(Object obj) {
//		Float f_num = new Float("0");
//		
//		if (!StringHelper.IsEmptyOrNull(obj)) {
//			f_num = Float.parseFloat(obj.toString());
//		}
//		return f_num;
//	}
//	
//	/**
//	 * obj 转 float 方法，根据类型转百分、千分
//	 * 
//	 * @param obj, type(1-转百分，2-转千分)
//	 * @return
//	 * @author 罗浩
//	 */
//	public static Float ConvertToFloat(Object obj, String type) {
//		Float f_num = new Float("0");
//		
//		if (!StringHelper.IsEmptyOrNull(obj)) {
//			if("1".equals(type)){
//				f_num = Float.parseFloat(obj.toString()) / 100;
//			}
//			if("2".equals(type)){
//				f_num = Float.parseFloat(obj.toString()) / 1000;
//			}
//		}
//		return f_num;
//	}
//	
//	/**
//	 * obj 转 Integer 方法
//	 * 
//	 * @param obj
//	 * @return
//	 * @author 罗浩
//	 */
//	public static Integer ConvertToInt(Object obj) {
//		Integer num = 0;
//		
//		if (!StringHelper.IsEmptyOrNull(obj)) {
//			num = Integer.parseInt(obj.toString());
//		}
//		return num;
//	}
	
	/**
	 * 检查导入的数据是否有重复的记录
	 * @param list   导入数据集
	 * @param checkColumnName  需要验证的列
	 * @return
	 * @author 罗浩
	 */
	public static String CheckColumn(List<Map<String, Object>> list, String[] checkColumnName) {
		StringBuilder sbReturn = new StringBuilder();
		if (checkColumnName != null && checkColumnName.length > 0) {
			List<String> cfL = new ArrayList<String>();
			StringBuilder sbKey = null;
			
			Map<String, Object> tmpMap = null;
			for (int k = 0; k < list.size(); k++) {
				tmpMap = list.get(k);
				sbKey = new StringBuilder();//获取关键字
                for (int i = 0; i < checkColumnName.length; i++) {
                    sbKey.append(tmpMap.get(checkColumnName[i]).toString().trim() + ",");
                }

                if (cfL.contains(sbKey.toString())) {
                    //如果已经有此记录,则直接进入错误数据中;
                    sbReturn.append("第" + (k + 2)  + "行数据重复:" + sbKey.toString());
                }
                else {
                	cfL.add(sbKey.toString());
                }
			}
        }
		return sbReturn.toString();
	}
	
	/**
	 * 检查时间格式是否正确
	 * @param dateTime
	 * @return
	 */
	public static boolean checkDate(String dateTime){
		boolean flag = true;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = format.parse(dateTime);
		} catch (ParseException e) {
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 图片转化成base64字符串
	 * @return
	 */
    public static String getImageBaseStr(String imgFile) {//将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        //读取图片字节数组
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);//返回Base64编码过的字节数组字符串
    }
	
}
