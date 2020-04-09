package com.phy.bcs.common.util;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.Lists;
import com.phy.bcs.common.mvc.domain.BaseModel;
import com.phy.bcs.common.mvc.domain.vo.BaseModelVo;
import com.phy.bcs.common.util.base.Reflections;
import org.apache.commons.beanutils.PropertyUtils;
import org.unbescape.json.JsonEscape;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 自定义 DataEntityVo 类型的序列化
 *
 * @author lijie
 */
public class DataEntityVoSerializer extends StdSerializer<BaseModelVo> {

    /**
     * 忽略的属性
     */
    public static List<String> freeFilterList = Lists.newArrayList("class", "new", "persistentState", "pkName", "pk",
            "version", "password");

    protected DataEntityVoSerializer(Class<BaseModelVo> t) {
        super(t);
    }

    public DataEntityVoSerializer() {
        this(null);
    }

    @Override
    public void serialize(BaseModelVo obj, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        serialize(obj, gen);
        gen.writeEndObject();
    }

    private void serialize(Object obj, JsonGenerator gen) throws IOException {

        PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(obj);
        Object val = null;
        String key = null;
        for (PropertyDescriptor p : ps) {
            key = p.getName();

            if (freeFilterList.contains(key)) {
                continue;
            }

            JsonIgnore jsonIgnoreAnn = Reflections.getAnnotation(obj, key, JsonIgnore.class);
            if (jsonIgnoreAnn != null) {
                continue;
            }

            JsonProperty jsonPropertyAnn = Reflections.getAnnotation(obj, key, JsonProperty.class);
            if (jsonPropertyAnn != null) {
                if (jsonPropertyAnn.access().equals(JsonProperty.Access.WRITE_ONLY)) {
                    continue;
                }
            }

            try {
                val = PropertyUtils.getProperty(obj, key);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            gen.writeFieldName(key);
            if (val instanceof IEnum) {
                String enumLocaleVal = JsonUtils.getInstance().getIEnumLocaleVal((IEnum) val);
                gen.writeString(enumLocaleVal);
                continue;
            } else if (val instanceof BaseModelVo || val instanceof BaseModel) {
                String nestString = JsonUtils.getInstance().jacksonToString(val);
                if (nestString != null) {
                    nestString = JsonEscape.unescapeJson(nestString);
                }
                gen.writeString(nestString);
            } else if (val instanceof Collection<?>) {
                Object objTemp;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("[");
                Iterator<?> iter = ((Collection<?>) val).iterator();
                while (iter.hasNext()) {
                    objTemp = iter.next();
                    String nestString = JsonUtils.getInstance().jacksonToString(objTemp);
                    if (nestString != null) {
                        nestString = JsonEscape.unescapeJson(nestString);
                    }
                    stringBuilder.append(nestString);
                    if (iter.hasNext()) {
                        stringBuilder.append(",");
                    }
                }
                stringBuilder.append("]");
                gen.writeString(stringBuilder.toString());
            } else if (val instanceof String) {
                String resultVal = (String) val;
                gen.writeString(JsonEscape.escapeJson(resultVal));
            } else {
                Object resultVal = JsonUtils.getInstance().getVal(obj, val, key);
                gen.writeObject(resultVal);
            }
        }

    }
}
