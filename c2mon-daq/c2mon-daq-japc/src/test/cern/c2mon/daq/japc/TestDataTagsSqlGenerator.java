/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.japc;

/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */

import static java.lang.String.format;
import static java.lang.System.out;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class TestDataTagsSqlGenerator {

    protected static final String outputFileNameTemplate = "gen-conf-sql/%s-insert-dtags.sql";

    protected static final String dataTagInsertTemplate = "INSERT INTO DATATAG (TAGID, TAGNAME, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAG_EQID, TAGRULEIDS, TAGADDRESS) VALUES "
            + "(%d, '%s', 0, '%s', 0, 1, 1, 'UNINITIALISED',%d,'%s','%s');\n\n";

    protected static final String simpleRuleTagInsertTemplate = "INSERT INTO DATATAG (TAGID, TAGNAME, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAGRULE, TAGRULEIDS) VALUES "
            + "(%d, '%s', 0, '%s', 0, 1, 1, 'UNINITIALISED','%s','%s');\n\n";

    protected abstract List<String> getInsertSqlStatemets();

    protected abstract String getOutputFileName();

    public final void generateSQL() {

        BufferedOutputStream os = null;
        try {
            File f = new File(getOutputFileName());
            os = new BufferedOutputStream(new FileOutputStream(f));

            out.println(format("Generating file: %s", f.getAbsolutePath()));

            if (f.exists()) {
                for (String sql : getInsertSqlStatemets())
                    os.write(sql.getBytes());
            }// if f exists

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (os != null)
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    // not much to do here
                }
        }
    }

}
