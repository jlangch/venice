/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.impl.util.excel;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.IndexedColors;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.support.Person;
import com.github.jlangch.venice.util.excel.DataRecord;
import com.github.jlangch.venice.util.excel.ExcelWriter;


public class ExcelTest {

    @Test
    public void test_Raw() {
        final List<Person> persons = persons();

        final Excel excel = Excel.createXlsx();

        final ExcelSheet sheet = excel.createSheet("Persons");
        sheet.setString( 0, 0, "FirstName");
        sheet.setString( 0, 1, "LastName");
        sheet.setString( 0, 2, "Age");

        persons.stream()
               .collect(Collectors.toMap(persons::indexOf, a -> a))
               .forEach((idx, v) -> {
                    sheet.setString( idx+1, 0, v.getFirstName());
                    sheet.setString(idx+1, 1, v.getLastName());
                    sheet.setInteger(idx+1, 2, v.getAge());
             });

        sheet.autoSizeColumns();

        assertNotNull(excel.writeToBytes());
    }

    @Test
    public void test_Builder_binary() {
        final byte[] data = ExcelWriter
                                .createXlsx()
                                .withSheet("Persons", Person.class)
                                    .withColumn("FirstName", Person::getFirstName)
                                        .end()
                                    .withColumn("LastName", Person::getLastName)
                                        .end()
                                    .withColumn("Age", Person::getAge)
                                        .end()
                                    .renderItems(persons())
                                    .autoSizeColumns()
                                    .end()
                                .writeToBytes();

        assertNotNull(data);
    }

    @Test
    public void test_Builder_binary_2sheets() {
        final byte[] data = ExcelWriter
                                .createXlsx()
                                .withSheet("Persons 1", Person.class)
                                    .end()
                                .withSheet("Persons 2", Person.class)
                                    .end()
                                .writeToBytes();

        assertNotNull(data);
    }

    @Test
    public void test_Builder_excel() {
        final List<Person> persons = persons();

        final Excel excel = ExcelWriter
                                .createXlsx()
                                .withSheet("Persons", Person.class)
                                    .withColumn("FirstName", Person::getFirstName)
                                        .end()
                                    .withColumn("LastName", Person::getLastName)
                                        .end()
                                    .withColumn("Age", Person::getAge)
                                        .end()
                                    .renderItems(persons)
                                    .autoSizeColumns()
                                    .end()
                                .toExcel();

        // verify data
        final ExcelSheet sheet = excel.getSheet("Persons");
        for(int ii=0; ii<persons.size(); ii++) {
            assertEquals(persons.get(ii).getFirstName(), sheet.getString(ii+1, 0));
            assertEquals(persons.get(ii).getLastName(), sheet.getString(ii+1, 1));
            assertEquals(persons.get(ii).getAge().intValue(), sheet.getInteger(ii+1, 2).intValue());
        }
    }

    @Test
    public void test_Builder_with_GenericEntity() {
        final List<DataRecord> persons = personMap();

        final Excel excel = ExcelWriter
                                .createXlsx()
                                .withSheet("Persons", DataRecord.class)
                                    .withColumn("FirstName", "firstName")
                                        .end()
                                    .withColumn("LastName", "lastName")
                                        .end()
                                    .withColumn("Age", "age")
                                        .end()
                                    .renderItems(persons)
                                    .autoSizeColumns()
                                    .end()
                                .toExcel();

        // verify data
        final ExcelSheet sheet = excel.getSheet("Persons");
        for(int ii=0; ii<persons.size(); ii++) {
            assertEquals(persons.get(ii).get("firstName"), sheet.getString(ii+1, 0));
            assertEquals(persons.get(ii).get("lastName"), sheet.getString(ii+1, 1));
            assertEquals(persons.get(ii).get("age"), sheet.getInteger(ii+1, 2).intValue());
        }
    }

    @Test
    public void test_BuilderWithMin() {
        final List<Person> persons = persons();

        final Excel excel = ExcelWriter
                                .createXlsx()
                                .withSheet("Persons", Person.class)
                                    .withColumn("FirstName", Person::getFirstName)
                                        .end()
                                    .withColumn("LastName", Person::getLastName)
                                        .end()
                                    .withColumn("Age")
                                        .colMapper(Person::getAge)
                                        .footerMin()
                                        .end()
                                    .renderItems(persons)
                                    .autoSizeColumns()
                                    .end()
                                .toExcel();

        // verify min
        final long minAge = excel.getSheet("Persons").getInteger(persons.size()+1, 2);
        assertEquals(28, minAge);
    }

    @Test
    public void test_BuilderWithMax() {
        final List<Person> persons = persons();

        final Excel excel = ExcelWriter
                                .createXlsx()
                                .withSheet("Persons", Person.class)
                                    .withColumn("FirstName", Person::getFirstName)
                                        .end()
                                    .withColumn("LastName", Person::getLastName)
                                        .end()
                                    .withColumn("Age")
                                        .colMapper(Person::getAge)
                                        .footerMax()
                                        .end()
                                    .renderItems(persons)
                                    .autoSizeColumns()
                                    .end()
                                .toExcel();

        // verify max
        final long maxAge = excel.getSheet("Persons").getInteger(persons.size()+1, 2);
        assertEquals(40, maxAge);
    }

    @Test
    public void test_BuilderWithAvg() {
        final List<Person> persons = persons();

        final Excel excel = ExcelWriter
                                .createXlsx()
                                .withSheet("Persons", Person.class)
                                    .withColumn("FirstName", Person::getFirstName)
                                        .end()
                                    .withColumn("LastName", Person::getLastName)
                                        .end()
                                    .withColumn("Age")
                                        .colMapper(Person::getAge)
                                        .footerAverage()
                                        .end()
                                    .renderItems(persons)
                                    .autoSizeColumns()
                                    .end()
                                .toExcel();

        // verify avg
        final long avgAge = excel.getSheet("Persons").getInteger(persons.size()+1, 2);
        assertEquals(33, avgAge);
    }

    @Test
    public void test_BuilderWithSum() {
        final List<Person> persons = persons();

        final Excel excel = ExcelWriter
                                .createXlsx()
                                .withSheet("Persons", Person.class)
                                    .withColumn("FirstName", Person::getFirstName)
                                        .end()
                                    .withColumn("LastName", Person::getLastName)
                                        .end()
                                    .withColumn("Age")
                                        .colMapper(Person::getAge)
                                        .footerSum()
                                        .end()
                                    .renderItems(persons)
                                    .autoSizeColumns()
                                    .end()
                                .toExcel();

        // verify sum
        final long sumAge = excel.getSheet("Persons").getInteger(persons.size()+1, 2);
        assertEquals(sumAge, persons.stream().mapToLong(p -> p.getAge()).sum());
    }

    @Test
    public void test_BuilderWithSumExplicit() {
        final List<Person> persons = persons();

        final int footerRow1 = persons.size()+2;

        final Excel excel = ExcelWriter
                                .createXlsx()
                                .withSheet("Persons", Person.class)
                                    .withColumn("FirstName", Person::getFirstName)
                                        .end()
                                    .withColumn("LastName", Person::getLastName)
                                        .end()
                                    .withColumn("Age", Person::getAge)
                                        .end()
                                    .renderItems(persons)
                                    .value(footerRow1, 1, "SUM age")
                                    .withSum(footerRow1, 3)
                                        .cellFrom(2, 3)
                                        .cellTo(footerRow1-1, 3)
                                        .end()
                                    .autoSizeColumns()
                                    .end()
                                .toExcel();

        // verify sum
        final long sumAge = excel.getSheet("Persons").getInteger(persons.size()+1, 2);
        assertEquals(sumAge, persons.stream().mapToLong(p -> p.getAge()).sum());
    }

    @Test
    public void test_BuilderWithFont_HTMLColor() {
        final List<Person> persons = persons();

        final Excel excel = ExcelWriter
                                .createXlsx()
                                .withFont("bold")
                                    .bold()
                                    .end()
                                .withFont("bold-blue")
                                    .bold()
                                    .colorHtml("#00FF00")
                                    .end()
                                .withCellStyle("header")
                                    .font("bold")
                                    .bgColorHtml("#A0A0A0")
                                    .end()
                                .withSheet("Persons", Person.class)
                                    .defaultHeaderStyle("header")
                                    .withColumn("FirstName")
                                        .colMapper(Person::getFirstName)
                                        .footerTextValue("SUM age")
                                        .footerStyle("sum-header")
                                        .end()
                                    .withColumn("LastName")
                                        .colMapper(Person::getLastName)
                                        .end()
                                    .withColumn("Age")
                                        .colMapper(Person::getAge)
                                        .bodyStyle("age")
                                        .footerSum()
                                        .footerStyle("sum-age")
                                        .end()
                                    .renderItems(persons)
                                    .autoSizeColumns()
                                    .end()
                                .toExcel();

        // verify sum
        final long sumAge = excel.getSheet("Persons").getInteger(persons.size()+1, 2);
        assertEquals(sumAge, persons.stream().mapToLong(p -> p.getAge() == null ? 0L : p.getAge()).sum());

        //FileUtil.save(excel.writeToBytes(), new File("/Users/juerg/Desktop/sum.xlsx"), true);
    }

    @Test
    public void test_BuilderWithSumWithStyles1() {
        final List<Person> persons = persons();

        final Excel excel = ExcelWriter
                                .createXlsx()
                                .withFont("bold")
                                    .bold()
                                    .end()
                                .withFont("bold-blue")
                                    .bold()
                                    .color(IndexedColors.BLUE)
                                    .end()
                                .withCellStyle("header")
                                    .font("bold")
                                    .bgColor(IndexedColors.GREY_25_PERCENT)
                                    .end()
                                .withCellStyle("age")
                                    .format("#,##0")
                                    .end()
                                .withCellStyle("sum-header")
                                    .font("bold")
                                    .end()
                                .withCellStyle("sum-age")
                                    .font("bold-blue")
                                    .format("#,##0")
                                    .end()
                                .withSheet("Persons", Person.class)
                                    .defaultHeaderStyle("header")
                                    .withColumn("FirstName")
                                        .colMapper(Person::getFirstName)
                                        .footerTextValue("SUM age")
                                        .footerStyle("sum-header")
                                        .end()
                                    .withColumn("LastName")
                                        .colMapper(Person::getLastName)
                                        .end()
                                    .withColumn("Age")
                                        .colMapper(Person::getAge)
                                        .bodyStyle("age")
                                        .footerSum()
                                        .footerStyle("sum-age")
                                        .end()
                                    .renderItems(persons)
                                    .autoSizeColumns()
                                    .end()
                                .toExcel();

        // verify sum
        final long sumAge = excel.getSheet("Persons").getInteger(persons.size()+1, 2);
        assertEquals(sumAge, persons.stream().mapToLong(p -> p.getAge() == null ? 0L : p.getAge()).sum());

        //FileUtil.save(excel.writeToBytes(), new File("/Users/juerg/Desktop/sum.xlsx"), true);
    }

    @Test
    public void test_BuilderWithSumWithStyles2() {
        final List<Person> persons = persons();

        final int footerRow1 = persons.size()+2;

        final Excel excel = ExcelWriter
                                .createXlsx()
                                .withFont("bold")
                                    .bold()
                                    .end()
                                .withFont("sum-bold")
                                    .bold()
                                    .color(IndexedColors.BLUE)
                                    .end()
                                .withCellStyle("header")
                                    .font("bold")
                                    .bgColor(IndexedColors.GREY_25_PERCENT)
                                    .end()
                                .withCellStyle("sum-header")
                                    .font("bold")
                                    .end()
                                .withCellStyle("sum-result")
                                    .font("sum-bold")
                                    .format("#,##0")
                                    .end()
                                .withSheet("Persons", Person.class)
                                    .defaultHeaderStyle("header")
                                    .withColumn("FirstName", Person::getFirstName)
                                        .end()
                                    .withColumn("LastName", Person::getLastName)
                                        .end()
                                    .withColumn("Age", Person::getAge)
                                        .end()
                                    .renderItems(persons)
                                    .value(footerRow1, 1, "SUM age", "sum-header")
                                    .withSum(footerRow1, 3)
                                        .cellFrom(2, 3)
                                        .cellTo(footerRow1-1, 3)
                                        .style("sum-result").end()
                                    .autoSizeColumns()
                                    .end()
                                .toExcel();

        // verify sum
        final long sumAge = excel.getSheet("Persons").getInteger(persons.size()+1, 2);
        assertEquals(sumAge, persons.stream().mapToLong(p -> p.getAge()).sum());

        //FileUtil.save(excel.writeToBytes(), new File("/Users/juerg/Desktop/sum.xlsx"), true);
    }


    private List<Person> persons() {
        return Arrays.asList(
                new Person("John", "Doe",   28),
                new Person("John", "Smith", 30),
                new Person("John", "Ford",  40),
                new Person("Sue",  "Ford",  34));
    }

    private List<DataRecord> personMap() {
        return persons().stream().map(p -> p.toEntityRecord()).collect(Collectors.toList());
    }

}
