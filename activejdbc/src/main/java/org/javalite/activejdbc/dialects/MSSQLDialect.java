package org.javalite.activejdbc.dialects;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.MetaModel;
import org.javalite.common.Util;

public class MSSQLDialect extends DefaultDialect {
    protected final Pattern selectPattern = Pattern.compile("^\\s*SELECT\\s*",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    /**
     * Generates adds limit, offset and order bys to a sub-query
     *
     * @param tableName name of table. If table name is null, then the subQuery parameter is considered to be a full query, and all that needs to be done is to
     * add limit, offset and order bys
     * @param subQuery sub-query or a full query
     * @param orderBys
     * @param limit
     * @param offset
     * @return query with
     */
    @Override
    public String formSelect(String tableName, String subQuery, List<String> orderBys, long limit, long offset) {
        boolean needLimit = limit != -1;
        boolean needOffset = offset != -1;

        if(needOffset && (orderBys == null || orderBys.isEmpty())) {
        	throw new DBException("MSSQL offset queries require an order by column.");
        }

        boolean keepSelect = false;
        StringBuilder fullQuery = new StringBuilder();
        if (needOffset) {
            fullQuery.append("SELECT sq.* FROM (SELECT ROW_NUMBER() OVER (ORDER BY ");
            Util.join(fullQuery, orderBys, ", ");
            fullQuery.append(") AS rownumber,");
        } else if (needLimit) {
            fullQuery.append("SELECT TOP ").append(limit);
        } else {
            keepSelect = true;
        }

		if (tableName == null) { //table is in the sub-query already
            if (keepSelect) {
                fullQuery.append(subQuery);
            } else {
                Matcher m = selectPattern.matcher(subQuery);
                if (m.find()) {
                    fullQuery.append(' ').append(subQuery.substring(m.end()));
                } else {
                    fullQuery.append(subQuery);
                }
            }
        } else {
            if (keepSelect) { fullQuery.append("SELECT"); }
            fullQuery.append(" * FROM ").append(tableName);
            appendSubQuery(fullQuery, subQuery);
        }

        if (needOffset) {
            // T-SQL offset starts with 1, not like MySQL with 0;
            if (needLimit) {
                fullQuery.append(") AS sq WHERE rownumber BETWEEN ").append(offset + 1)
                        .append(" AND ").append(limit + offset);
            } else {
                fullQuery.append(") AS sq WHERE rownumber >= ").append(offset + 1);
            }
        } else {
            appendOrderBy(fullQuery, orderBys);
        }

        return fullQuery.toString();
    }

    /**
     * TDS converts a number of important data types to String. This isn't what we want, nor helpful. Here, we change them back.
     */
    @Override
    public Object overrideDriverTypeConversion(MetaModel mm, String attributeName, Object value) {
        if (value instanceof String) {
            String typeName = mm.getColumnMetadata().get(attributeName).getTypeName();
            if ("date".equalsIgnoreCase(typeName)) {
                return java.sql.Date.valueOf((String) value);
            } else if ("datetime2".equalsIgnoreCase(typeName)) {
                return java.sql.Timestamp.valueOf((String) value);
            }
        }
        return value;
    }
}
