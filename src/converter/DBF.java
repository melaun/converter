/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converter;

import converter.document.Row;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nl.knaw.dans.common.dbflib.CorruptedTableException;
import nl.knaw.dans.common.dbflib.Field;
import nl.knaw.dans.common.dbflib.IfNonExistent;
import nl.knaw.dans.common.dbflib.Record;
import nl.knaw.dans.common.dbflib.Table;

/**
 *
 * @author Podzimek Vojtěch
 */
public class DBF {

    private List<Field> fileds;

    public String filePath = null;
    public String colName = null;
    public String colEan = null;
    public String colCount = null;
    public String colNC = null;
    public String colDPH = null;
    public String colSpecial = null;
    public String colNumber = null;
    public String colDate = null;
    public String colDocNumber = null;
    public String colFilialka = null;

    public ArrayList<Row> getRows(String path, String charSet) {

        Table table = new Table(new File(path), charSet);
        ArrayList<Row> rows = new ArrayList();
        try {

            table.open(IfNonExistent.CREATE);
            fileds = table.getFields();
            Iterator<Record> iterator = table.recordIterator();

            while (iterator.hasNext()) {
                Record record = iterator.next();
                Row row = new Row();
                row.name = getValue(record, colName);
                row.code = getValue(record, colNumber);
                row.ean = getValue(record, colEan);
                row.count = getValue(record, colCount);
                row.nc = getValue(record, colNC);
                row.dph = getValue(record, colDPH);
                row.docNumber = getValue(record, colDocNumber);
                row.docDate = getValue(record, colDate);

                if (colFilialka != null) {
                    row.filialka = getValue(record, colFilialka);
                } else {
                    row.filialka = "000";
                }

                rows.add(row);
                //System.out.println(row.toString());
            }
            table.close();
        } catch (IOException | CorruptedTableException ex) {
            System.err.println(path + " - " + ex);
        }
        return rows;
    }

    private String getValue(Record r, String name) {
        String type = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd");
        for (Field field : fileds) {
            if (field.getName().equals(name)) {
                type = field.getType().toString();
                break;
            }
        }
        switch (type) {
            case "CHARACTER":
                return r.getStringValue(name);
            case "NUMBER":
                return String.valueOf(r.getNumberValue(name));
            case "DATE":
                return sdf.format(r.getDateValue(name));
            case "LOGICAL":
                return String.valueOf(r.getBooleanValue(name));
        }
        return null;
    }
}
