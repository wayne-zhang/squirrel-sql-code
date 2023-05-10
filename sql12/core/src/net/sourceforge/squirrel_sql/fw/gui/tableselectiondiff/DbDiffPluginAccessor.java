package net.sourceforge.squirrel_sql.fw.gui.tableselectiondiff;

import java.nio.file.Path;

import net.sourceforge.squirrel_sql.client.Main;
import net.sourceforge.squirrel_sql.fw.util.StringManager;
import net.sourceforge.squirrel_sql.fw.util.StringManagerFactory;

public class DbDiffPluginAccessor
{
   private static final StringManager s_stringMgr = StringManagerFactory.getStringManager(DbDiffPluginAccessor.class);

   private static void writeDbDiffPluginMissingMessage()
   {
      String msg = s_stringMgr.getString("DbDiffPluginAccessor.dbDiffPluginNeeded");
      Main.getApplication().getMessageHandler().showErrorMessage(msg);
   }

   public static void showDiff(Path leftMarkdown, Path rightMarkdown, String diffDialogTitle)
   {
      DBDiffPluginInterface dbDiffIf = (DBDiffPluginInterface) Main.getApplication().getPluginManager().bindExternalPluginService("dbdiff", DBDiffPluginInterface.class);

      if (null == dbDiffIf)
      {
         writeDbDiffPluginMissingMessage();
      }

      dbDiffIf.showDiff(leftMarkdown, rightMarkdown, diffDialogTitle);
   }
}