/*
 * Copyright (C) 2002 Christian Sell
 * csell@users.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * created by cse, 27.09.2002 20:15:39
 */
package net.sourceforge.jcomplete.completions;

import java.util.List;
import java.util.Collections;

import net.sourceforge.jcomplete.SQLCompletion;
import net.sourceforge.jcomplete.SQLSchema;

/**
 * this class represents a table completion, as it appears within the FROM
 * clause.<br>
 * <em>Note: do not confuse with SQLSchema.Table</em>
 */
public class SQLTable extends SQLCompletion
{
    public void setCatalog(String catalog, int pos)
    {
        this.catalog = catalog;
        setEndPosition(pos+catalog.length());
    }

    public void setSchema(String schema, int pos)
    {
        this.schema = schema;
        setEndPosition(pos+schema.length());
    }

    public void setName(String name, int pos)
    {
        this.name = name;
        setEndPosition(pos+name.length());
    }

    public void setAlias(String alias, int pos)
    {
        this.alias = alias;
        setEndPosition(pos+alias.length());
    }

   public String catalog;
    public String schema;
    public String name;
    public String alias;

    private SQLStatement statement;

    public SQLTable(SQLStatement statement, int start)
    {
        super(start);
        System.out.println("SQLTable: "+start);
        this.statement = statement;
    }

    public SQLTable(SQLStatement statement, int start, int end)
    {
        super(start);
        System.out.println("SQLTable: "+start+" "+end);
        this.statement = statement;
        setEndPosition(end);
    }

    public SQLStatement getStatement()
    {
        return statement;
    }

    public SQLSchema.Table[] getCompletions(int position)
    {
        String tb = (name != null && position > startPosition) ?
              name.substring(0, position - startPosition) : null;

        List tables = getStatement().getTables(catalog, schema, tb);
        Collections.sort(tables);
        return (SQLSchema.Table[])tables.toArray(new SQLSchema.Table[tables.size()]);
    }

    /**
     * tables are safe to repeat, as they only appear in the from clause
     * @return <em>true</em>
     */
    public boolean isRepeatable()
    {
        return true;
    }

    public boolean mustReplace(int position)
    {
        return name != null && position >= startPosition && position <= endPosition;
    }

    public String getText(int position, String option)
    {
        return option;
    }
}
