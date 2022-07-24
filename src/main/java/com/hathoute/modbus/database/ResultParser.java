package com.hathoute.modbus.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface ResultParser<T> {

    T parse(ResultSet resultSet) throws SQLException;

    static <R> ResultParser<List<R>> listParser(ResultParser<R> parser) {
        return resultSet -> {
            List<R> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(parser.parse(resultSet));
            }

            return list;
        };
    }
}
