package com.faa.utils.utilswt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
/**
 * XML工具类
 * Created by xt on 2017/11/2.
 */
public class XmlUtil {

    /**
     * xml 转 Map
     * @param xml
     * @return
     */
    public static Map<String,String> XmlToMap(String xml)
    {
        Map<String,String> map = new HashMap<>();
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(xml);
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(doc ==null)
            return map;
        Element root = doc.getRootElement();
        for (Iterator iterator = root.elementIterator(); iterator.hasNext();) {
            Element e = (Element) iterator.next();
            map.put(e.getName(), e.getText());
        }
        return map;
    }

    /**
     * Map 转 XML
     * @param map
     * @return
     */
    public static String mapToXML(Map map) {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><bizdata>");
        mapToXMLData(map, sb);
        sb.append("</bizdata>");
        try {
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void mapToXMLData(Map map, StringBuffer sb) {
        Set set = map.keySet();
        for (Iterator it = set.iterator(); it.hasNext();) {
            String key = (String) it.next();
            Object value = map.get(key);
            if (null == value)
                value = "";
            if (value.getClass().getName().equals("java.util.ArrayList")) {
                ArrayList list = (ArrayList) map.get(key);
                sb.append("<" + key + ">");
                for (int i = 0; i < list.size(); i++) {
                    HashMap hm = (HashMap) list.get(i);
                    mapToXMLData(hm, sb);
                }
                sb.append("</" + key + ">");

            } else {
                if (value instanceof HashMap) {
                    sb.append("<" + key + ">");
                    mapToXMLData((HashMap) value, sb);
                    sb.append("</" + key + ">");
                } else {
                    sb.append("<" + key + ">" + value + "</" + key + ">");
                }

            }

        }
    }
}
