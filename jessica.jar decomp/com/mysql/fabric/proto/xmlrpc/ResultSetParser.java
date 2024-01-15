/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.proto.xmlrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ResultSetParser {
    public List<Map<String, ?>> parse(Map<String, ?> info, List<List<Object>> rows) {
        List fieldNames = (List)info.get("names");
        HashMap fieldNameIndexes = new HashMap();
        for (int i = 0; i < fieldNames.size(); ++i) {
            fieldNameIndexes.put(fieldNames.get(i), i);
        }
        ArrayList result = new ArrayList(rows.size());
        for (List<Object> r : rows) {
            HashMap resultRow = new HashMap();
            for (Map.Entry f : fieldNameIndexes.entrySet()) {
                resultRow.put(f.getKey(), r.get((Integer)f.getValue()));
            }
            result.add(resultRow);
        }
        return result;
    }
}

