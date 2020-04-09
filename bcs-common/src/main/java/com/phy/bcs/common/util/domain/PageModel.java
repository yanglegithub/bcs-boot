package com.phy.bcs.common.util.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phy.bcs.common.util.PublicUtils;
import com.phy.bcs.common.util.StringUtils;
import com.phy.bcs.common.util.base.Encodes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@ApiModel(description = "分页")
@Data
public class PageModel<T> implements Pageable, Serializable {

    public static final String F_DATA = "data";
    private static final Logger log = LoggerFactory.getLogger(PageModel.class);
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Page number of the requested page", example = "1")
    private Integer page = 1;

    @ApiModelProperty(value = "Size of a page", example = "10")
    private Integer size = 10;

    @ApiModelProperty(value = "排序方式：如多排序条件,分隔符为英文半角逗号：name asc,date desc")
    @JsonIgnore
    private String sortName;

    @ApiModelProperty(hidden = true, example = "1")
    @JsonProperty("total")
    private Long recordsTotal;


    @ApiModelProperty(value = "当前页数据", hidden = true)
    private List<T> data;


    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Sort sort;

    /**
     * 查询条件json
     */
    @JsonIgnore
    @ApiModelProperty(value = "查询条件:" +
            "\n* [{\"fieldName\":\"name\",\"operate\":\"eq\",\"value\":\"g\"},{},...,{}]\r\n" +
            "<br></br>" +
            "\n* 等于:eq，不等于:ne" +
            "\n* 大于:gt，小于:lt，大于等于:ge，小于等于:le" +
            "\n* 类似:like，不类似:notLike" +
            "\n* 区间:between，包含:in，不包含:notIn" +
            "\n* 为Null:isNull，不为Null:isNotNull")
    private String queryConditionJson;
    private List<Order> orders = Lists.newArrayList();

    public PageModel() {

    }

    /**
     * Creates a new {@link PageRequest}. Pages are zero indexed, thus providing 0 for {@code page}
     * will return the first page.
     *
     * @param page zero-based page index.
     * @param size the size of the page to be returned.
     */
    public PageModel(int page, int size) {
        this(page, size, null);
    }

    public PageModel(int page, int size, List<T> dataList, long recordsTotal) {
        this(page, size, null);
        setData(dataList);
        setRecordsTotal(recordsTotal);
    }

    /**
     * Creates a new {@link PageRequest} with sort parameters applied.
     *
     * @param page       zero-based page index.
     * @param size       the size of the page to be returned.
     * @param direction  the direction of the {@link Sort} to be specified, can be {@literal null}.
     * @param properties the properties to sort by, must not be {@literal null} or empty.
     */
    public PageModel(int page, int size, Direction direction, String... properties) {
        this(page, size, Sort.by(direction, properties));
    }

    /**
     * Creates a new {@link PageRequest} with sort parameters applied.
     *
     * @param page zero-based page index.
     * @param size the size of the page to be returned.
     * @param sort can be {@literal null}.
     */
    public PageModel(int page, int size, Sort sort) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero!");
        }

        if (size < 1) {
            throw new IllegalArgumentException("Page size must not be less than one!");
        }

        this.page = page;
        this.size = size;
        this.setSort(sort);
    }

    public void setPageInstance(Page<T> page) {
        setData(page.getContent());
        setRecordsTotal(page.getTotalElements());
    }

    public void setQueryConditionJson(String queryConditionJson) {
        this.queryConditionJson = Encodes.unescapeHtml(queryConditionJson);
    }

    public List<QueryCondition> findQueryConditions() {
        List<QueryCondition> list = Lists.newArrayList();
        if (PublicUtils.isNotEmpty(queryConditionJson)) {
            try {
                list.addAll(JSONArray.parseArray(queryConditionJson, QueryCondition.class));
            } catch (Exception e) {
                log.warn(PublicUtils.toAppendStr("queryCondition[", queryConditionJson,
                        "] is not json or other error", e.getMessage()));
            }
        }
        return list;
    }

    public void setSortName(String sort) {
        orders = Lists.newArrayList();
        if (sort.contains(" ")) {
            List<Sort.Order> orders = Lists.newArrayList();
            List<String> orderStrList = Arrays.asList(sort.split(StringUtils.SPLIT_DEFAULT));
            for (String orderStr : orderStrList) {
                if (sort.contains(" ")) {
                    String[] sts = orderStr.split(" ");
                    this.orders.add(new Order(sts[0], Order.Direction.fromString(sts[1])));
                    orders.add(new Sort.Order(Direction.fromString(sts[1]), sts[0]));
                } else {
                    throw new IllegalArgumentException("sortName格式有误，请检查");
                }
            }
            this.sort = Sort.by(orders);
        } else {
            this.sort = Sort.by(Direction.ASC, sort);
            this.orders.add(new Order(sort, Order.Direction.asc));
        }
    }

    public void setSortDefaultName(Direction direction, String... sorts) {
        if (sort == null) {
            this.sort = Sort.by(direction, sorts);
        }
    }

    @Override
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    public int getPageSize() {
        return size;
    }

    @Override
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    public int getPageNumber() {
        return page;
    }

    @Override
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    public long getOffset() {
        return (page - 1) * size;
    }

    @Override
    @JsonIgnore
    public boolean isPaged() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isUnpaged() {
        return !isPaged();
    }

    @Override
    public boolean hasPrevious() {
        return page > 0;
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @ApiModelProperty(hidden = true, example = "1")
    public Integer getPerpage() {
        return size;
    }

    @Override
    public Pageable next() {
        return PageRequest.of(getPageNumber() + 1, getPageSize(), getSort());
    }

    public PageModel previous() {
        return getPageNumber() == 0 ? this
                : new PageModel(getPageNumber() - 1, getPageSize(), getSort());
    }

    @Override
    public Pageable first() {
        return PageRequest.of(0, getPageSize(), getSort());
    }

    public <S> PageModel<S> map(Converter<? super T, ? extends S> converter) {
        return new PageModel(this.getPage(), this.getSize(),
                this.getConvertedContent(converter), this.getRecordsTotal());
    }

    protected <S> List<S> getConvertedContent(Converter<? super T, ? extends S> converter) {
        Assert.notNull(converter, "Converter must not be null!");
        List<S> result = new ArrayList(this.data.size());
        Iterator var3 = this.data.iterator();

        while (var3.hasNext()) {
            T element = (T) var3.next();
            result.add(converter.convert(element));
        }

        return result;
    }

    /**
     * 将JSON还原为PageMode对象
     */
    public PageModel<T> parseJsonToPm(JSON json, Class<T> clazz) {
        PageModel<JSONObject> jsonpm = JSON.parseObject(json.toJSONString(), PageModel.class);
        List<T> entityList = Lists.newArrayList();
        for (JSONObject data : jsonpm.getData()) {
            entityList.add(JSON.parseObject(data.toJSONString(), clazz));
        }
        PageModel<T> entityPm = new PageModel();
        BeanUtils.copyProperties(jsonpm, entityPm, "data");
        entityPm.setData(entityList);
        return entityPm;
    }

    @ApiModelProperty(hidden = true)
    public int getFirstResult() {
        int firstResult = (getPage() - 1) * getPageSize();
        if (firstResult >= getRecordsTotal()) {
            firstResult = 0;
        }
        return firstResult;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;

        result = prime * result + page;
        result = prime * result + size;

        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        PageModel other = (PageModel) obj;
        return this.page.equals(other.page) && this.size.equals(other.size);
    }
}

