package com.cdc.traccar.sql;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

import com.cdc.traccar.parser.DebeziumParser;
import com.cdc.traccar.parser.ParserFactory;
import com.cdc.traccar.utils.CharUtils;
import com.cdc.traccar.utils.DebeziumRecordUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * @author Mac
 * @since 2019-04-22 15:15
 */
public abstract class AbstractDebeziumSqlProvider {


    private Map<String, Object> sqlParameterMap = null;

    protected List<String> preparedColumnList = null;

    protected List<String> preparedPrimaryKeyList = null;

    protected List<String> primaryKeyList = null;

    public String getSql(Struct key, Struct payload, String table) {
        sqlParameterMap = Maps.newHashMap();

        if (needParsePrimaryKey()) {
            parsePrimaryKey(key);
        }

        if (needParseColumn()) {
            Struct afterValue = DebeziumRecordUtils.getRecordStructValue(payload, "after");

            handleColumn(afterValue, getFieldPredicate(), getColumnNameFunction());
        }

        return generateSql(table);

    }

    protected Predicate<Field> getFieldPredicate() {
        return field -> true;
    }

    protected Function<String, String> getColumnNameFunction() {
        return columnName -> columnName;
    }

    protected abstract boolean needParseColumn();

    protected abstract boolean needParsePrimaryKey();

    protected abstract String generateSql(String table);

    protected void parsePrimaryKey(Struct key) {
        primaryKeyList = Lists.newArrayList();
        preparedPrimaryKeyList = Lists.newArrayList();

        key.schema().fields().stream().forEach(field -> {
            String primaryKey = field.name();
            preparedPrimaryKeyList.add(primaryKey + "= :" + primaryKey);
            primaryKeyList.add(primaryKey);

            sqlParameterMap.put(primaryKey, parseColumnValue(field, key.get(field)));
        });
    }

    protected void handleColumn(Struct afterValue, Predicate<Field> predicate,
                                Function<String, String> function) {
        preparedColumnList = Lists.newArrayList();

        afterValue.schema().fields().stream()
                .filter(predicate)
                .forEach(field -> {
                    String columnName = field.name();
                    preparedColumnList.add(function.apply(columnName));

                    sqlParameterMap.put(columnName, parseColumnValue(field, afterValue.get(field)));
                });
    }

    protected Object parseColumnValue(Field field, Object value) {
        if (Objects.isNull(value)) {
            return null;
        }

        if (value instanceof ByteBuffer) {
           return CharUtils.getByte((ByteBuffer) value);
        }

        Schema schema = field.schema();
        DebeziumParser parser = ParserFactory.getParser(schema.name());
        if (Objects.nonNull(parser)) {
            return parser.parse(schema, value);
        }
        return value;
    }

    public Map<String, Object> getSqlParameterMap() {
        return sqlParameterMap;
    }
}