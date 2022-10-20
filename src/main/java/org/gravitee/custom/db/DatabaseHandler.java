/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gravitee.custom.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHandler {

    static Connection connection;

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName(DatabaseConfiguration.DRIVER);
        return connection = DriverManager.getConnection(
                DatabaseConfiguration.URL,
                DatabaseConfiguration.USER,
                DatabaseConfiguration.PASSWORD);
    }

    public static String getById(int id) {
        ResultSet resultSet;
        String query = "SELECT * FROM test_gravitee WHERE id = " + id;

        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString("text");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
