package com.phy.bcs.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.phy.bcs.common.mvc.domain.vo.BaseModelVo;
import com.phy.bcs.common.mvc.domain.vo.DictVo;
import com.phy.bcs.common.util.annotation.DictType;
import com.phy.bcs.common.util.annotation.JsonField;
import com.phy.bcs.common.util.base.Collections3;
import com.phy.bcs.common.util.base.Reflections;
import com.phy.bcs.common.util.exception.RuntimeMsgException;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.json.JsonEscape;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * json操作常用类
 */
public class JsonUtils {

    protected static Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    /*** 验证是否属于自定义类 */
    private static List<String> className = Lists.newArrayList(/*Reflections.classPackge.split(",")*/);
    //private static String dateFormart = PublicUtil.TIME_FORMAT;
    private static String dateFormart = PublicUtils.TIME_FORMAT_ISO_8601;
    /**
     * 缓存字典对象
     */
    private static JSONObject codeItemData = new JSONObject();
    private volatile static Map<String, Map<String, Map<String, String>>> localeIEnumMaps = Maps.newHashMap();
    private static ObjectMapper mapper = new ObjectMapper();
    private static boolean mapperInitialed = false;

    private static void initConfig() {
        dateFormart = PublicUtils.TIME_FORMAT;
        className = Lists.newArrayList(/*Reflections.classPackge.split(",")*/);
    }

    public synchronized static JsonUtils getInstance() {
        initConfig();
        initJacksonMapper();
        return SingletonHolder.instance;
    }

    /**
     * 指定数据列从数据字典中获取(注意顺序对应)
     *
     * @param kindIds kindId,kindId
     */
    public synchronized static JsonUtils getInstance(List<String> kindIds, String... keyCodeItems) {
        initConfig();
        initJacksonMapper();
        if (PublicUtils.isNotEmpty(kindIds)) {
            addDictItem(kindIds);
        }
        return SingletonHolder.instance;
    }

    private synchronized static void addDictItem(List<String> kindIds) {
        Map<String, Object> maps = new HashMap<String, Object>();
        Map<String, String> map;
        List<DictVo> itemList;
        /*for (String kid : kindIds) {
            if (codeItemData.get(kid) == null) {
                itemList = DictUtils.getAllDictList(kid);
                if (PublicUtils.isNotEmpty(itemList)) {
                    map = Maps.newHashMap();
                    for (DictVo dict : itemList) {
                        map.put(dict.getVal(), dict.getName());
                    }
                    maps.put(kid, map);
                }
            }
        }*/

        codeItemData.putAll(maps);
    }

    private static ObjectMapper initJacksonMapper() {
        if (!mapperInitialed) {
            SimpleModule module = new SimpleModule();
            module.addSerializer(BaseModelVo.class, new DataEntityVoSerializer());
            mapper.registerModule(module);
            mapperInitialed = true;
        }

        return mapper;
    }

    /**
     * 设置日期格式
     */
    public JsonUtils setDateFormat(String dateFormat) {
        if (PublicUtils.isNotEmpty(dateFormat)) {
            JsonUtils.dateFormart = dateFormat;
        }
        return this;
    }

    private boolean checkClassName(String name) {
        boolean flag = false;
        if (className != null && PublicUtils.isNotEmpty(name)) {
            for (String item : className) {
                if (name.contains(item)) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * 将对象json化
     */
    public JSON toJsonObject(Object obj) {
        return jacksonToJson(obj);
        // return toJsonObject(obj, JsonUtil.recurrenceStrList);
    }

    @Deprecated
    public JSON toJsonObject(Object obj, List<String> recurrenceStrList) {
        return jacksonToJson(obj);
        //return toJsonObject(obj, recurrenceStrList, JsonUtil.freeFilterList);
    }

    /**
     * @param recurrenceStrList 允许递归的属性名称集合
     * @param freeFilterList    过滤字段
     */
    public JSON toJsonObject(Object obj, List<String> recurrenceStrList, List<String> freeFilterList) {
        JSONObject jsonObj = new JSONObject();
        if (obj != null) {
            if (obj instanceof Map) {
                Map map = (Map) obj;
                if (PublicUtils.isNotEmpty(map)) {
                    Map mapItem = Maps.newHashMap();
                    for (Iterator<?> iterator = map.keySet().iterator(); iterator.hasNext(); ) {
                        Object key = (Object) iterator.next(), val = map.get(key);
                        if (val == null) {
                            continue;
                        }
                        if (val instanceof Collection) {
                            mapItem.put(key, toJsonArray((List<Object>) val));
                        } else if (checkClassName(val.getClass().getName())) {
                            mapItem.put(key, objToMap(val, recurrenceStrList, freeFilterList));
                        } else {
                            mapItem.put(key, getVal(obj, val, String.valueOf(key)));
                        }
                    }
                    jsonObj = new JSONObject(mapItem);
                }
            } else if (checkClassName(obj.getClass().getName())) {
                Map<String, Object> map = objToMap(obj, recurrenceStrList, freeFilterList);
                jsonObj = new JSONObject(map);
            } else if (obj instanceof Collection) {
                return toJsonArray((Collection) obj);
            } else {
                jsonObj.put(null, obj);
            }
        }
        return jsonObj;
    }

    public JSONArray toJsonArray(Collection col) {
        JSONArray jsonArray = new JSONArray();
        if (PublicUtils.isNotEmpty(col)) {
            Object object = col.toArray()[0];
            if (object != null && !checkClassName(object.getClass().getName())) {
                for (Object obj : col) {
                    jsonArray.add(toJsonObject(obj));
                }
            } else {
                List<Object> targert = Lists.newArrayList();
                for (Iterator<?> iterator = col.iterator(); iterator.hasNext(); ) {
                    object = (Object) iterator.next();
                    Map<String, Object> map = objToMap(object);
                    targert.add(map);
                }
                if (PublicUtils.isNotEmpty(targert)) {
                    jsonArray.addAll(targert);
                }
            }
        }
        return jsonArray;
    }

    /**
     * 将数组转换为map
     */
    public Map<String, Object> arrayToMap(Object[] objs, String attributes) {
        Map<String, Object> maps = new HashMap<String, Object>();
        try {
            if (PublicUtils.isNotEmpty(objs)) {
                String attr = null;
                Object val = null;
                List<String> attList = StringUtils.parseStringTokenizer(attributes, "|");
                for (int i = 0; i < attList.size(); i++) {
                    attr = attList.get(i);
                    if (attr.contains(".")) {
                        attr = attr.replace(".", "_");
                    }

                    DictType type = Reflections.getAnnotation(objs, attr, DictType.class);
                    if (type != null) {
                        String kindId = type.name();
                        JSONObject jobj = getCodeItemData(kindId, attr);
                        if (jobj != null) {
                            val = jobj;
                        }
                    } else {
                        val = objs[i];
                    }
                    if (val instanceof Date) {
                        //val = PublicUtil.fmtDate((Date) val, dateFormart);
                        val = ((Date) val).toInstant().toString();
                    }
                    maps.put(attr, val);
                }
            }
        } catch (Exception e) {
            logger.error("在将数组转换成Map的arrayToMap中出现异常对象-->", e);
        }

        return maps;
    }

    /**
     * 将obj转换为map
     *
     * @param clsName 前缀 ，通常不传参
     */
    @Deprecated
    public Map<String, Object> objToMap(Object obj, String... clsName) {
        return objToMap(obj, clsName);
    }

    public Map<String, Object> objToMap(Object obj, List<String> recurrenceStrList, List<String> freeFilterList, String... clsName) {
        Map<String, Object> maps = new HashMap<String, Object>();
        try {
            if (obj != null) {
                if (PublicUtils.isEmpty(className)) {
                    throw new RuntimeMsgException("自定义class为空,无法正常解析对象");
                }
                // Get annotation field
                PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(obj);
                Object val = null, objTemp = null, objVal = null;
                String key = null;
                for (PropertyDescriptor p : ps) {
                    key = p.getName();
                    if (freeFilterList != null && freeFilterList.contains(key)) {
                        continue;
                    }
                    JSONField jf = null;
                    try {
                        jf = Reflections.getAnnotation(obj, key, JSONField.class);
                        if (jf != null && !jf.serialize()) {
                            JsonField tempjf = Reflections.getAnnotation(obj, key, JsonField.class);
                            if (tempjf == null) {
                                continue;
                            }
                        }
                    } catch (Exception e) {
                    }
                    try {
                        PropertyUtils.getProperty(obj, key);
                    } catch (Exception e) {
                        continue;
                    }
                    val = PropertyUtils.getProperty(obj, key);
                    //处理枚举类字典
                    if (val instanceof IEnum) {
                        /*String enumLocaleVal = getIEnumLocaleVal((IEnum) val);
                        mapPutValue(maps, getKey(key, jf), enumLocaleVal);
                        continue;*/
                    }

                    if (PublicUtils.isNotEmpty(clsName)) {
                        List<String> argList = Lists.newArrayList(clsName);
                        key = PublicUtils
                                .toAppendStr(Collections3.convertToString(argList, "_"), "_", key);
                    }
                    if (PublicUtils.isEmpty(val) && jf != null && jf.serialzeFeatures() != null) {
                        mapPutValue(maps, getKey(key, jf), val);
                    }
                    if (val instanceof Collection<?>) {
                        Iterator<?> iter = ((Collection<?>) val).iterator();
                        List<Object> list = Lists.newArrayList();
                        while (iter.hasNext()) {
                            objTemp = iter.next();
                            if (checkClassName(objTemp.getClass().getName())) {
                                list.add(objToMap(objTemp, recurrenceStrList, freeFilterList));
                            } else {
                                list.add(toJsonObject(objTemp));
                            }
                        }
                        mapPutValue(maps, getKey(key, jf), list);
                    } else {
                        boolean isRecurrenceObject = false;
                        try {
                            isRecurrenceObject = checkClassName(p.getPropertyType().toString());
                        } catch (Exception e) {
                            val = null;
                            continue;
                        }
                        if (isRecurrenceObject) {
                            List<String> tempReKey = Lists.newArrayList();
                            if (PublicUtils.isNotEmpty(recurrenceStrList) && !key.contains("_")) {
                                for (String reKey : recurrenceStrList) {
                                    if (reKey.startsWith(PublicUtils.toAppendStr(key, "_"))
                                            && reKey.indexOf("_") != -1) {
                                        tempReKey.add(reKey);
                                    }
                                }
                            }
                            if (PublicUtils.isNotEmpty(tempReKey)) {
                                for (String tempKey : tempReKey) {
                                    String[] pros = tempKey.split("_");
                                    objVal = val;
                                    for (int i = 1; i < pros.length; i++) {
                                        if (objVal != null) {
                                            objVal = getFieldDictValue(objVal, pros[i]);
                                        }
                                    }
                                    mapPutValue(maps, getKey(tempKey, jf), getVal(obj, objVal, tempKey));
                                }
                            }
                            if (recurrenceStrList != null && recurrenceStrList.contains(key)) {
                                recurrenceStrList.remove(p.getName());
                                maps.putAll(objToMap(val, recurrenceStrList, freeFilterList, p.getName()));
                                recurrenceStrList.add(p.getName());
                            }
                            continue;
                        }
                        mapPutValue(maps, getKey(key, jf), getVal(obj, val, key));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("在将对象转换成Map的ObjToMap中出现异常对象-->", e);
        }
        return maps;
    }

    public String getIEnumLocaleVal(IEnum obj) {
        String currentLocale = LocaleMessageUtils.getCurrentLocale().toString();
        Map<String, Map<String, String>> currentLocaleMap = localeIEnumMaps.get(currentLocale);
        if (!localeIEnumMaps.containsKey(currentLocale)) {
            synchronized (JsonUtils.class) {
                if (!localeIEnumMaps.containsKey(currentLocale)) {
                    //目前只有uac启动时初始化了枚举类，所以使用rpc
                    //若各中心自己都初始化，则可直接使用 EnumUtils.getIEnumMap()
                    /*currentLocaleMap = UacFeignApiUtil.getUacDictFeignApi().getEnumMap();
                    if (currentLocaleMap != null) {
                        localeIEnumMaps.put(currentLocale, currentLocaleMap);
                    }*/
                }
            }
        }

        /*String enumsName = StringUtils.firstToLowerCase(obj.getClass().getSimpleName());
        if (currentLocaleMap != null) {
            Map<String, String> objEnumsMap = currentLocaleMap.get(enumsName);
            if (objEnumsMap != null && objEnumsMap.size() > 0) {
                return objEnumsMap.get(obj.getValue().toString());
            }
        }*/
        return obj.toString();
    }

    private Object getFieldDictValue(final Object obj, final String fieldName) {
        Field field = Reflections.getAccessibleField(obj, fieldName);

        if (field == null) {
            return null;
        }

        Object result = null;
        try {
            result = field.get(obj);
            DictType type = Reflections.getAnnotation(obj, fieldName, DictType.class);
            if (type != null) {
                String kindId = type.name();
//                if (PublicUtil.isEmpty(keyCodeItems) || !keyCodeItems.contains(fieldName)) {
                if (codeItemData.getJSONObject(kindId) == null) {
                    addDictItem(Lists.newArrayList(kindId));
                }
                result = getDictVal(result, kindId, fieldName);
            }
        } catch (IllegalAccessException e) {
            logger.error("不可能抛出的异常{}", e.getMessage());
        }
        return result;
    }

    private String getKey(String key, JSONField jf) {
        try {
            if (jf != null && PublicUtils.isNotEmpty(jf.name())) {
                key = jf.name();
            }
        } catch (Exception e) {
        }
        return StringUtils.toCamelCase(key);
    }

    private JSONObject getCodeItemData(String kindId, String key) {
        JSONObject jobj = codeItemData.getJSONObject(kindId);
        if (jobj == null) {
            addDictItem(Lists.newArrayList(kindId));
        }
        jobj = codeItemData.getJSONObject(kindId);
        return jobj;
    }

    private Object getDictVal(Object val, String kindId, String key) {
        JSONObject jobj = getCodeItemData(kindId, key);
        if (jobj != null) {
            String valStr = val + "";
            if (valStr.contains(",")) {
                StringBuffer temp = new StringBuffer();
                String[] vals = valStr.split(",");
                for (String item : vals) {
                    if (PublicUtils.isNotEmpty(item)) {
                        temp.append(jobj.get(item)).append(",");
                    }
                }
                if (temp.length() > 0) {
                    temp.deleteCharAt(temp.length() - 1);
                }
                val = temp.toString();
            } else {
                val = jobj.get(valStr);
            }
        } else {
            logger.warn("无法查询到kindId {} val {}  的字典对象", kindId, val);
        }
        return val;
    }

    private void mapPutValue(Map<String, Object> maps, String key, Object val) {
        boolean containValAndNotEmpty = (maps.containsKey(key) && PublicUtils.isNotEmpty(val));
        if (!maps.containsKey(key) || containValAndNotEmpty) {
            maps.put(key, val);
        }
    }

    public Object getVal(Object obj, Object val, String key) {
        Object temp = getFieldDictValue(obj, key);
        if (temp != null) {
            val = temp;
        }

        if (val instanceof Date) {
            //val = PublicUtil.fmtDate((Date) val, dateFormart);
            val = ((Date) val).toInstant().toString();
        }
        if (val instanceof ZonedDateTime) {
            //val = PublicUtil.fmtDate((ZonedDateTime) val, dateFormart);
            val = ((ZonedDateTime) val).toInstant().toString();
        }
        return val;
    }

    private JSON jacksonToJson(Object object) {
        String resultStr = null;
        try {
            resultStr = mapper.writeValueAsString(object);
            resultStr = JsonEscape.unescapeJson(resultStr);
            resultStr = resultStr.replaceAll("\"\\[", "\\[").replaceAll("]\"", "]").replaceAll("\"\\{", "{").replaceAll("}\"", "}");
        } catch (JsonProcessingException e) {
            logger.error("", e);
        }
        JSON result = (JSON) JSON.parse(resultStr);
        return result;
    }

    public String jacksonToString(Object object) {
        String resultStr = null;
        try {
            resultStr = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("", e);
        }
        return resultStr;
    }

    private static class SingletonHolder {
        private static final JsonUtils instance = new JsonUtils();
    }

}
