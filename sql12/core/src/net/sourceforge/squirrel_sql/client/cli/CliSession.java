package net.sourceforge.squirrel_sql.client.cli;

import net.sourceforge.squirrel_sql.client.Main;
import net.sourceforge.squirrel_sql.client.session.ISession;
import net.sourceforge.squirrel_sql.client.session.properties.SessionProperties;
import net.sourceforge.squirrel_sql.client.session.schemainfo.SchemaInfo;
import net.sourceforge.squirrel_sql.fw.id.IIdentifier;
import net.sourceforge.squirrel_sql.fw.sql.IQueryTokenizer;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.ISQLConnection;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDatabaseMetaData;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDriver;
import net.sourceforge.squirrel_sql.fw.sql.QueryTokenizer;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import net.sourceforge.squirrel_sql.fw.sql.SQLDriverManager;
import net.sourceforge.squirrel_sql.fw.sql.SQLDriverPropertyCollection;

public class CliSession extends CliSessionAdapter
{
   private final SQLConnection _sqlConnection;
   private final QueryTokenizer _tokenizer;
   private final SessionProperties _sessionProperties;

   public CliSession(ISQLAlias aliasToConnectTo)
   {
      try
      {
         IIdentifier driverID = aliasToConnectTo.getDriverIdentifier();
         ISQLDriver sqlDriver = Main.getApplication().getDataCache().getDriver(driverID);


         SQLDriverManager sqlDriverManager = Main.getApplication().getSQLDriverManager();


         SQLDriverPropertyCollection props = aliasToConnectTo.getDriverPropertiesClone();

         if (!aliasToConnectTo.getUseDriverProperties())
         {
            props.clear();
         }


         _sqlConnection = sqlDriverManager.getConnection(sqlDriver, aliasToConnectTo, aliasToConnectTo.getUserName(), aliasToConnectTo.getPassword(), props);


         _sessionProperties = Main.getApplication().getSquirrelPreferences().getSessionProperties();
         _tokenizer = new QueryTokenizer(_sessionProperties.getSQLStatementSeparator(), _sessionProperties.getStartOfLineComment(), _sessionProperties.getRemoveMultiLineComment());

      }
      catch (Exception e)
      {
         throw CliExceptionUtil.wrapRunntime(e);
      }
   }

   @Override
   public ISQLConnection getSQLConnection()
   {
      return _sqlConnection;
   }

   @Override
   public ISQLDatabaseMetaData getMetaData()
   {
      return _sqlConnection.getSQLMetaData();
   }

   @Override
   public IQueryTokenizer getQueryTokenizer()
   {
      return _tokenizer;
   }

   @Override
   public SessionProperties getProperties()
   {
      return _sessionProperties;
   }
}
